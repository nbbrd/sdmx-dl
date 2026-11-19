// ========================================================================
// SDMX-DL SEARCH: NAVIGATION SHORTCUTS AND INITIALIZATION
// ========================================================================

function goSearch() {
    if (state.search.query || state.search.scope) {
        byId('q').value = state.search.query;
        byId('scope').value = state.search.scope;
        updateScopeStyle();
        showView('search');
        updateUrl(true);
    } else {
        goHome();
    }
}

function goDataset() {
    if (!state.dataset) return;
    showView('dataset');
    updateUrl(true);
}

async function loadSources() {
    try {
        state.sources = itemsOf(await getJSON(apiUrl(['sources'])));
    } catch (error) {
        state.sources = [];
        notify('Cannot load the sources: ' + error.message, 'danger');
    }
    renderSources();
}

/**
 * Fill the source selector, grouping the non-public sources that are searched on demand only
 */
function renderSources() {
    const scope = byId('scope');
    const selected = scope.value;
    const open = state.sources.filter(source => isPublicSource(source.id));
    const restricted = state.sources.filter(source => !isPublicSource(source.id));
    const nameOf = source => (source.names && (source.names.en || Object.values(source.names)[0])) || '';
    const option = source => isPublicSource(source.id)
        ? `<option value="${escapeHtml(source.id)}" title="${escapeHtml(nameOf(source))}">${escapeHtml(source.id)}</option>`
        : `<option value="${escapeHtml(source.id)}" class="restricted" title="${escapeHtml(nameOf(source) + ' · ' + restrictedTitleOf(source.id))}">🔒 ${escapeHtml(source.id)}</option>`;
    scope.innerHTML = restricted.length
        ? '<option value="" title="Restricted sources are searched only if selected">All public sources</option>'
            + `<optgroup label="Public">${open.map(option).join('')}</optgroup>`
            + `<optgroup label="Restricted · may ask for credentials">${restricted.map(option).join('')}</optgroup>`
        : '<option value="">All sources</option>' + open.map(option).join('');
    scope.value = selected;
    updateScopeStyle();
    const count = state.sources.length;
    byId('source-count').textContent = `${count} source${count === 1 ? '' : 's'}`;
    byId('hero-subtitle').textContent = restricted.length
        ? `One search across ${open.length} public source${open.length === 1 ? '' : 's'} · ${restricted.length} restricted on demand`
        : `One search across ${count} statistical source${count === 1 ? '' : 's'}`;
}

/**
 * Highlight the source selector when a restricted source is selected
 */
function updateScopeStyle() {
    const scope = byId('scope');
    const restricted = !!scope.value && !isPublicSource(scope.value);
    scope.classList.toggle('scope-restricted', restricted);
    scope.title = restricted ? restrictedTitleOf(scope.value) : '';
}

function initSearchPage() {
    const q = byId('q');
    q.addEventListener('input', showSuggestions);
    q.addEventListener('keydown', onSearchKeyDown);
    q.addEventListener('focus', () => { if (q.value.trim()) showSuggestions(); });
    q.addEventListener('blur', () => setTimeout(hideSuggestions, 150));

    byId('code-find').addEventListener('input', event => findCodes(event.target.value));
    byId('series-filter').addEventListener('input', event => onSeriesFilter(event.target.value));
    byId('range').addEventListener('click', event => {
        const button = event.target.closest('button[data-years]');
        if (button) setRange(Number(button.dataset.years));
    });
    byId('series-tabs').addEventListener('click', event => {
        const link = event.target.closest('[data-tab]');
        if (!link) return;
        event.preventDefault();
        showSeriesTab(link.dataset.tab);
    });

    document.addEventListener('keydown', event => {
        const typing = /^(INPUT|TEXTAREA|SELECT)$/.test(document.activeElement.tagName);
        if (event.key === '/' && !typing) {
            event.preventDefault();
            q.focus();
            q.select();
        } else if (event.key === 'Escape' && state.series) {
            closeSeries();
        } else if (state.series && !typing && (event.key === 'ArrowDown' || event.key === 'ArrowUp')) {
            event.preventDefault();
            stepSeries(event.key === 'ArrowDown' ? 1 : -1);
        }
    });
    window.addEventListener('popstate', routeFromUrl);
    window.addEventListener('storage', () => {
        updateCompareBadge();
        if (state.view === 'landing') renderHome();
    });

    updateCompareBadge();
    renderHome();
}

document.addEventListener('DOMContentLoaded', async () => {
    initSearchPage();
    await loadSources();
    routeFromUrl();
});
