// ========================================================================
// AUTOCOMPLETE FUNCTIONALITY
// ========================================================================

/**
 * Forms with source, database and flow fields, and the datalists that autocomplete them
 */
const AUTOCOMPLETE_FORMS = [
    { prefix: 'flows', databasesList: 'databases-list-flows' },
    { prefix: 'meta', databasesList: 'databases-list-meta', flowsList: 'flows-list-meta' },
    { prefix: 'kb', databasesList: 'databases-list-kb', flowsList: 'flows-list-kb' },
    { prefix: 'data', databasesList: 'databases-list-data', flowsList: 'flows-list-data' },
    { prefix: 'compare', databasesList: 'databases-list-compare', flowsList: 'flows-list-compare' }
];

async function loadSources() {
    try {
        const sources = await getJSON(apiUrl(['sources']));
        const sourceOptions = sources.map(s => createOption(s));
        updateDatalist('sources-list', sourceOptions);
    } catch (error) {
        console.error('Failed to load sources:', error);
    }
}

/**
 * Fill a datalist with the result of a request, unless it is already filled (or being filled) for the same query
 * @param {string} datalistId - Datalist ID
 * @param {string} query - Identifier of the request (e.g. source and database)
 * @param {Function} fetchOptions - Async function returning the options
 */
async function fillDatalistOnce(datalistId, query, fetchOptions) {
    const datalist = document.getElementById(datalistId);
    if (!datalist || datalist.dataset.query === query) return;

    datalist.dataset.query = query;
    try {
        const options = await fetchOptions();
        if (datalist.dataset.query === query) {
            updateDatalist(datalistId, options);
        }
    } catch (error) {
        if (datalist.dataset.query === query) {
            delete datalist.dataset.query;
        }
        throw error;
    }
}

async function loadDatabases(sourceId, datalistId) {
    if (!sourceId) return;

    try {
        await fillDatalistOnce(datalistId, sourceId, async () => {
            const databases = await getJSON(apiUrl([sourceId, 'databases']));
            return databases.map(d => createOption(d));
        });
    } catch (error) {
        console.error('Failed to load databases:', error);
    }
}

async function loadFlows(sourceId, datalistId, database = '') {
    if (!sourceId) return;

    try {
        await fillDatalistOnce(datalistId, `${sourceId}|${database}`, async () => {
            const flows = await getJSON(apiUrl([sourceId, 'flows'], { database }));
            return flows.map(f => createOption(f));
        });
    } catch (error) {
        console.error('Failed to load flows:', error);
    }
}

/**
 * Load the databases and flows of the current source of a form, if not already loaded
 * @param {Object} form - Entry of AUTOCOMPLETE_FORMS
 */
function loadFormAutocomplete(form) {
    const source = document.getElementById(`${form.prefix}-source`)?.value.trim();
    if (!source) return;

    loadDatabases(source, form.databasesList);
    if (form.flowsList) {
        const database = document.getElementById(`${form.prefix}-database`)?.value.trim() || '';
        loadFlows(source, form.flowsList, database);
    }
}

/**
 * Setup autocomplete listeners for source/database/flow fields
 */
function setupAutocomplete() {
    AUTOCOMPLETE_FORMS.forEach(form => {
        const load = () => loadFormAutocomplete(form);
        let debounceTimer = null;

        const sourceField = document.getElementById(`${form.prefix}-source`);
        if (sourceField) {
            // Debounced while typing, immediate when selecting from the datalist or leaving the field
            sourceField.addEventListener('input', () => {
                clearTimeout(debounceTimer);
                debounceTimer = setTimeout(load, 300);
            });
            sourceField.addEventListener('change', () => {
                clearTimeout(debounceTimer);
                load();
            });
        }

        // Lazy-load on focus, as fields may be filled without input events (URL parameters, card actions)
        document.getElementById(`${form.prefix}-database`)?.addEventListener('focus', load);
        document.getElementById(`${form.prefix}-database`)?.addEventListener('change', load);
        document.getElementById(`${form.prefix}-flow`)?.addEventListener('focus', load);

        // Preload when the source is already filled (e.g. from URL parameters)
        load();
    });
}

// Autocomplete helper functions
function updateDatalist(datalistId, options) {
    const datalist = document.getElementById(datalistId);
    datalist.innerHTML = '';
    options.forEach(option => {
        const optionElement = document.createElement('option');
        // Support both simple strings and {value, label} objects
        if (typeof option === 'string') {
            optionElement.value = option;
        } else {
            optionElement.value = option.value;
            if (option.label) {
                optionElement.label = option.label;
                // Set textContent for better search experience
                optionElement.textContent = option.label;
            }
        }
        datalist.appendChild(optionElement);
    });
    datalist.dispatchEvent(new Event('datalistupdate'));
}

// Helper to create searchable option with ID, name, and description
function createOption(item) {
    const id = item.id || item.ref || item.name || '';
    const name = item.name || '';
    const description = item.description || item.label || '';

    // Create a descriptive label
    let label = id;
    if (name && name !== id) {
        label = `${id} - ${name}`;
    }
    if (description && description !== name && description !== id) {
        // Truncate long descriptions
        const maxLength = 100;
        const cleanDesc = description.replace(/<[^>]*>/g, '').trim();
        const truncDesc = cleanDesc.length > maxLength
            ? cleanDesc.substring(0, maxLength) + '...'
            : cleanDesc;
        if (truncDesc) {
            label = `${label} (${truncDesc})`;
        }
    }

    return { value: id, label: label };
}
