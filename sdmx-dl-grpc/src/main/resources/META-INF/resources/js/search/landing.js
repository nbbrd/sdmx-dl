// ========================================================================
// SDMX-DL SEARCH: LANDING SCREEN, RECENT SEARCHES, FAVORITES, SUGGESTIONS
// ========================================================================

/**
 * Go back to the landing screen with an empty search box
 * @param {boolean} push - Create a new history entry
 */
function goHome(push = true) {
    hideSuggestions();
    state.dataset = null;
    state.series = null;
    state.search.query = '';
    state.search.token++;
    byId('q').value = '';
    byId('scope').value = state.search.scope = '';
    updateScopeStyle();
    byId('crumb-dataset-text').textContent = 'Narrow down';
    byId('crumb-series-text').textContent = 'Inspect a series';
    byId('keyline').textContent = '';
    showView('landing');
    renderHome();
    updateUrl(push);
    byId('q').focus();
}

/**
 * Search for a predefined text
 * @param {string} text - Text to search
 */
function quickSearch(text) {
    byId('q').value = text;
    runSearch();
}

// ------------------------------------------------------------------------
// Recent searches and favorites
// ------------------------------------------------------------------------

function addRecent(query) {
    const recents = loadStored('recents', []).filter(item => item !== query);
    saveStored('recents', [query, ...recents].slice(0, RECENT_LIMIT));
}

function removeRecent(index) {
    const recents = loadStored('recents', []);
    recents.splice(index, 1);
    saveStored('recents', recents);
    renderHome();
}

function clearRecents() {
    saveStored('recents', []);
    renderHome();
}

function isFavorite(ref) {
    return loadStored('favorites', []).some(item => refId(item) === refId(ref));
}

/**
 * Add the current dataset to the favorites, or remove it
 */
function toggleFavorite() {
    const dataset = state.dataset;
    if (!dataset) return;
    const ref = { source: dataset.source, flow: dataset.flow, database: dataset.database };
    const favorites = loadStored('favorites', []);
    const index = favorites.findIndex(item => refId(item) === refId(ref));
    if (index >= 0) {
        favorites.splice(index, 1);
        notify('Removed from favorites');
    } else {
        favorites.unshift({ ...ref, name: dataset.name || flowIdOf(dataset.flow) });
        notify('Added to favorites');
    }
    saveStored('favorites', favorites);
    updateFavoriteButton();
    renderHome();
}

function removeFavorite(index) {
    const favorites = loadStored('favorites', []);
    favorites.splice(index, 1);
    saveStored('favorites', favorites);
    updateFavoriteButton();
    renderHome();
}

function updateFavoriteButton() {
    const on = !!state.dataset && isFavorite(state.dataset);
    byId('favorite-icon').className = 'bi ' + (on ? 'bi-star-fill' : 'bi-star');
    byId('favorite-btn').title = on ? 'Remove from favorites' : 'Add to favorites';
}

function openFavorite(index) {
    const favorite = loadStored('favorites', [])[index];
    if (favorite) openDataset(favorite);
}

/**
 * Render the recent searches and the favorite datasets of the landing screen
 */
function renderHome() {
    const recents = loadStored('recents', []);
    byId('recents').innerHTML = recents.map((query, index) => `
        <div class="item d-flex align-items-center px-2 py-1" onclick="quickSearch(${jsArg(query)})">
            ${icon('search', 'me-2 text-white-50')}<span class="text-truncate">${escapeHtml(query)}</span>
            <a href="#" class="ms-auto text-white-50" title="Remove" onclick="event.preventDefault(); event.stopPropagation(); removeRecent(${index})">${icon('x')}</a>
        </div>`).join('') || '<div class="small text-white-50">Your searches will show up here.</div>';

    const favorites = loadStored('favorites', []);
    byId('favorites').innerHTML = favorites.map((favorite, index) => `
        <div class="item d-flex align-items-center px-2 py-1" onclick="openFavorite(${index})">
            ${icon('database', 'me-2 text-white-50')}<span class="text-truncate">${escapeHtml(favorite.name)}</span>
            <code class="ms-auto ps-2 small text-white-50 text-nowrap">${escapeHtml(favorite.source + '/' + flowIdOf(favorite.flow))}</code>
            <a href="#" class="ms-2 text-white-50" title="Remove" onclick="event.preventDefault(); event.stopPropagation(); removeFavorite(${index})">${icon('x')}</a>
        </div>`).join('') || '<div class="small text-white-50">Star a dataset to pin it here.</div>';
}

// ------------------------------------------------------------------------
// Suggestions of the search box
// ------------------------------------------------------------------------

let suggestTimer = null;
let suggestToken = 0;
let suggestActions = [];

/**
 * Show the suggestions for the text of the search box: dataset references, keys and matching datasets
 */
function showSuggestions() {
    const query = byId('q').value.trim();
    clearTimeout(suggestTimer);
    if (!query) {
        hideSuggestions();
        return;
    }

    const quick = [];
    const ref = parseDatasetRef(query);
    if (ref) {
        quick.push({
            html: `${icon('box-arrow-in-right', 'text-primary')}<span>Open <code>${escapeHtml(ref.source + '/' + ref.flow)}</code>${ref.key ? ` with key <code>${escapeHtml(ref.key)}</code>` : ''}</span>`,
            run: () => openDataset({ source: ref.source, flow: ref.flow, database: '' }, { key: ref.key })
        });
    } else if (state.dataset && looksLikeKey(query)) {
        quick.push({
            html: `${icon('key', 'text-warning')}<span>Open <code>${escapeHtml(query)}</code> in <b>${escapeHtml(flowIdOf(state.dataset.flow))}</b></span>`,
            run: () => openDataset(state.dataset, { key: query })
        });
    }
    renderSuggestions(query, quick, null);

    if (ref) return;
    const token = ++suggestToken;
    suggestTimer = setTimeout(async () => {
        try {
            const result = await getJSON(apiUrl(['flows'], {
                sources: byId('scope').value || 'all',
                maxConfidentiality: byId('scope').value ? '' : 'PUBLIC',
                query,
                maxResults: SUGGEST_MAX_RESULTS,
                timeout: SUGGEST_TIMEOUT_MS,
                plainText: true
            }));
            if (token === suggestToken) renderSuggestions(query, quick, itemsOf(result));
        } catch (error) {
            if (token === suggestToken) renderSuggestions(query, quick, []);
        }
    }, 300);
}

function renderSuggestions(query, quick, datasets) {
    suggestActions = [];
    let html = '';
    const addAction = (content, run, extraClass = '') => {
        html += `<a class="list-group-item list-group-item-action d-flex align-items-center gap-2 ${extraClass}" role="option" data-index="${suggestActions.length}" onmousedown="event.preventDefault(); runSuggestion(${suggestActions.length})">${content}</a>`;
        suggestActions.push(run);
    };

    if (quick.length) {
        html += '<div class="list-group-item bg-body-tertiary small text-secondary py-1">Go to</div>';
        quick.forEach(item => addAction(item.html, item.run));
    }
    if (datasets === null) {
        html += `<div class="list-group-item small text-secondary">${icon('hourglass-split')} Looking for datasets…</div>`;
    } else if (datasets.length) {
        html += '<div class="list-group-item bg-body-tertiary small text-secondary py-1">Datasets</div>';
        datasets.forEach(entry => addAction(
            `${icon('database', 'text-primary')}<span class="text-truncate">${highlight(entry.flow.name || flowIdOf(entry.flow.ref), query)}</span><code class="ms-auto small text-nowrap">${escapeHtml(entry.source + '/' + flowIdOf(entry.flow.ref))}</code>`,
            () => openDataset({ source: entry.source, flow: entry.flow.ref, database: entry.database || '' })));
    }
    addAction(`${icon('search')} Search all datasets for “${escapeHtml(query)}”`, () => runSearch(), 'text-primary');

    const suggest = byId('suggest');
    suggest.innerHTML = html;
    suggest.classList.remove('d-none');
}

function runSuggestion(index) {
    const run = suggestActions[index];
    hideSuggestions();
    if (run) run();
}

function hideSuggestions() {
    clearTimeout(suggestTimer);
    suggestToken++;
    byId('suggest').classList.add('d-none');
}

/**
 * Handle the keyboard in the search box: arrows move in the suggestions, Enter runs the active one
 * @param {KeyboardEvent} event - Keyboard event
 */
function onSearchKeyDown(event) {
    const suggest = byId('suggest');
    const actions = [...suggest.querySelectorAll('.list-group-item-action')];
    const active = suggest.querySelector('.list-group-item-action.active');
    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
        if (suggest.classList.contains('d-none') || !actions.length) return;
        event.preventDefault();
        const index = actions.indexOf(active);
        const next = event.key === 'ArrowDown' ? (index + 1) % actions.length : (index <= 0 ? actions.length - 1 : index - 1);
        actions.forEach((action, i) => action.classList.toggle('active', i === next));
        actions[next].scrollIntoView({ block: 'nearest' });
    } else if (event.key === 'Enter' && active && !suggest.classList.contains('d-none')) {
        event.preventDefault();
        runSuggestion(Number(active.dataset.index));
    } else if (event.key === 'Escape') {
        hideSuggestions();
    }
}
