// ========================================================================
// SUGGESTION DROPDOWN
// ========================================================================

const MAX_LIST_SUGGESTIONS = 100;

/**
 * Attach a suggestion dropdown to an input, navigable with the mouse and the keyboard.
 * @param {HTMLInputElement} input - Input to complete
 * @param {Object} options
 * @param {Function} options.fetch - Async function receiving the trigger ('input', 'focus' or 'open')
 *                                   and returning the items [{value, label}] to show (empty or null hides the dropdown)
 * @param {Function} options.apply - Updates the input with the selected item
 * @param {boolean} [options.reopen] - Whether to refresh the suggestions after a selection
 * @param {boolean} [options.showOnFocus] - Whether to show the suggestions when the input gets the focus
 * @param {string[]} [options.selectKeys] - Keys that select the highlighted item
 * @param {Function} [options.onClosedKeydown] - Handles keydown events while the dropdown is hidden
 * @returns {{refresh: Function, hide: Function}}
 */
function attachSuggestions(input, options) {
    const { fetch, apply, reopen = false, showOnFocus = false, selectKeys = ['Enter', 'Tab'], onClosedKeydown } = options;
    input.setAttribute('autocomplete', 'off');
    input.parentElement.classList.add('suggestions-host');

    let dropdown = input.nextElementSibling;
    if (!dropdown || !dropdown.classList.contains('autocomplete-dropdown')) {
        dropdown = document.createElement('div');
        dropdown.className = 'autocomplete-dropdown';
        input.after(dropdown);
    }

    let selectedIndex = -1;
    let generation = 0;
    let selecting = false;

    const isShown = () => dropdown.classList.contains('show');

    const hide = () => {
        generation++;
        dropdown.classList.remove('show');
        selectedIndex = -1;
    };

    const select = item => {
        apply(item);
        hide();
        selecting = !reopen;
        input.dispatchEvent(new Event('input', { bubbles: true }));
        input.dispatchEvent(new Event('change', { bubbles: true }));
        selecting = false;
        input.focus();
    };

    const highlight = elements => {
        elements.forEach((element, index) => {
            element.classList.toggle('selected', index === selectedIndex);
            if (index === selectedIndex) element.scrollIntoView({ block: 'nearest' });
        });
    };

    const refresh = async trigger => {
        if (selecting) return;
        const current = ++generation;
        const items = await fetch(trigger);
        if (current !== generation) return;
        if (!items || items.length === 0) {
            hide();
            return;
        }

        dropdown.innerHTML = '';
        items.forEach(item => {
            const element = document.createElement('div');
            element.className = 'autocomplete-item';
            element.dataset.code = item.value;
            element.innerHTML = `<span class="code">${escapeHtml(item.value)}</span>`
                + (item.label ? `<span class="description">${escapeHtml(item.label)}</span>` : '');
            element.addEventListener('mousedown', e => e.preventDefault());
            element.addEventListener('click', () => select(item));
            dropdown.appendChild(element);
        });
        dropdown.style.left = `${input.offsetLeft}px`;
        dropdown.style.width = `${input.offsetWidth}px`;
        selectedIndex = -1;
        dropdown.classList.add('show');
    };

    input.addEventListener('input', () => refresh('input'));
    input.addEventListener('focus', () => {
        if (showOnFocus) refresh('focus');
    });
    input.addEventListener('click', () => {
        if (!isShown()) refresh('open');
    });
    input.addEventListener('blur', hide);

    input.addEventListener('keydown', e => {
        if (!isShown()) {
            if (e.key === 'ArrowDown') {
                e.preventDefault();
                refresh('open');
            } else if (onClosedKeydown) {
                onClosedKeydown(e);
            }
            return;
        }

        const elements = dropdown.querySelectorAll('.autocomplete-item');
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            selectedIndex = Math.min(selectedIndex + 1, elements.length - 1);
            highlight(elements);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            selectedIndex = Math.max(selectedIndex - 1, -1);
            highlight(elements);
        } else if (e.key === 'Escape') {
            e.preventDefault();
            hide();
        } else if (selectKeys.includes(e.key)) {
            if (selectedIndex >= 0 && elements[selectedIndex]) {
                e.preventDefault();
                elements[selectedIndex].click();
            } else if (e.key === 'Enter' || e.key === 'Tab') {
                hide();
            }
        }
    });

    return { refresh, hide };
}

/**
 * Filter suggestions by a text: values starting with the text first, then values or labels containing it
 * @param {Object[]} items - Items [{value, label}]
 * @param {string} text - Text to look for (empty keeps all items)
 * @param {number} [limit] - Maximum number of items
 * @returns {Object[]} - Matching items
 */
function filterSuggestions(items, text, limit = MAX_LIST_SUGGESTIONS) {
    const query = text.trim().toLowerCase();
    if (!query) return items.slice(0, limit);
    const starting = [];
    const containing = [];
    items.forEach(item => {
        const value = item.value.toLowerCase();
        if (value.startsWith(query)) {
            starting.push(item);
        } else if (value.includes(query) || (item.label || '').toLowerCase().includes(query)) {
            containing.push(item);
        }
    });
    return starting.concat(containing).slice(0, limit);
}

/**
 * Replace the native datalist popup of an input with a suggestion dropdown backed by the same datalist
 * @param {HTMLInputElement} input - Input with a list attribute
 */
function attachListSuggestions(input) {
    const listId = input?.getAttribute('list');
    if (!listId || input.dataset.suggestions) return;
    input.dataset.suggestions = listId;
    input.removeAttribute('list');

    const getItems = () => [...(document.getElementById(listId)?.options || [])]
        .map(option => ({ value: option.value, label: option.label && option.label !== option.value ? option.label : '' }));

    const suggestions = attachSuggestions(input, {
        fetch: async trigger => {
            const items = getItems();
            if (trigger !== 'input') return filterSuggestions(items, '');
            const matches = filterSuggestions(items, input.value);
            return matches.length === 1 && matches[0].value === input.value ? [] : matches;
        },
        apply: item => {
            input.value = item.value;
        }
    });

    document.getElementById(listId)?.addEventListener('datalistupdate', () => {
        if (document.activeElement === input) suggestions.refresh('input');
    });
}

/**
 * Replace the native datalist popups of all inputs with suggestion dropdowns
 */
function setupListSuggestions() {
    document.querySelectorAll('input[list]').forEach(attachListSuggestions);
}
