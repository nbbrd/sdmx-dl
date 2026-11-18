// ========================================================================
// SDMX-DL SEARCH: STEP 1, FIND A DATASET
// ========================================================================

/**
 * Search the datasets matching the text of the search box, or open the dataset it references
 * @param {Object} options - { push: create a new history entry (default true) }
 */
async function runSearch(options = {}) {
    const push = options.push !== false;
    const query = byId('q').value.trim();
    const scope = byId('scope').value;
    hideSuggestions();

    const ref = parseDatasetRef(query);
    if (ref) {
        openDataset({ source: ref.source, flow: ref.flow, database: '' }, { key: ref.key, push });
        return;
    }
    if (!query && !scope) {
        if (state.view !== 'landing') notify('Type a few words to search', 'info');
        byId('q').focus();
        return;
    }
    if (query) addRecent(query);

    const search = state.search;
    Object.assign(search, { query, scope, items: [], scores: [], failures: [], total: 0, page: 0, sourceFilter: new Set() });
    const token = ++search.token;
    showView('search');
    updateUrl(push);
    byId('result-count').textContent = '';
    byId('result-time').textContent = '';
    byId('failures').innerHTML = '';
    byId('facet-source').innerHTML = '';
    byId('facet-where').innerHTML = '';
    byId('page-info').textContent = '';
    byId('results').innerHTML = loadingMessage(scope ? `Searching ${scope}…` : 'Searching all sources…');

    const started = performance.now();
    try {
        const result = await getJSON(apiUrl(['flows'], {
            sources: scope || 'all',
            query,
            maxResults: SEARCH_MAX_RESULTS,
            timeout: SEARCH_TIMEOUT_MS,
            plainText: true,
            truncate: 400
        }));
        if (token !== search.token) return;
        search.items = itemsOf(result);
        search.scores = result.scores || [];
        search.failures = result.failures || [];
        search.total = result.total || 0;
        search.elapsed = Math.round(performance.now() - started);
        renderResults();
    } catch (error) {
        if (token !== search.token) return;
        byId('results').innerHTML = errorMessage(error.message);
    }
}

function onScopeChange() {
    if (byId('q').value.trim() || byId('scope').value || state.view === 'search') runSearch();
}

function onSortChange() {
    state.search.sort = byId('sort').value;
    state.search.page = 0;
    renderResults();
}

function changePage(delta) {
    state.search.page += delta;
    renderResults();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function toggleSourceFacet(source, checked) {
    const filter = state.search.sourceFilter;
    if (checked) filter.add(source); else filter.delete(source);
    state.search.page = 0;
    renderResults();
}

/**
 * Tell where the words of the query were found in a search result
 * @param {Object} entry - Search result with source and flow
 * @param {string} query - Search query
 * @returns {Array<string>} - 'Id', 'Name' and/or 'Description'
 */
function matchedFieldsOf(entry, query) {
    const words = queryWords(query).map(word => word.toLowerCase());
    if (!words.length) return [];
    const has = text => words.some(word => String(text || '').toLowerCase().includes(word));
    const result = [];
    if (has(entry.flow.ref)) result.push('Id');
    if (has(entry.flow.name)) result.push('Name');
    if (has(entry.flow.description)) result.push('Description');
    return result;
}

/**
 * Render the current page of results, the facets and the pager
 */
function renderResults() {
    const search = state.search;
    let entries = search.items.map((entry, index) => ({ entry, index, score: search.scores[index] }));
    if (search.sourceFilter.size) entries = entries.filter(item => search.sourceFilter.has(item.entry.source));
    if (search.sort === 'name') {
        entries.sort((a, b) => (a.entry.flow.name || '').localeCompare(b.entry.flow.name || ''));
    }

    const count = entries.length;
    const pages = Math.max(1, Math.ceil(count / SEARCH_PAGE_SIZE));
    search.page = Math.min(Math.max(0, search.page), pages - 1);
    const first = search.page * SEARCH_PAGE_SIZE;
    const page = entries.slice(first, first + SEARCH_PAGE_SIZE);

    byId('result-count').textContent = `${formatCount(search.total)} dataset${search.total === 1 ? '' : 's'}`;
    byId('result-time').textContent = (search.query ? `for “${search.query}”` : `in ${search.scope}`) + ` · ${formatCount(search.elapsed)} ms`;
    byId('prev-page').disabled = search.page === 0;
    byId('next-page').disabled = first + SEARCH_PAGE_SIZE >= count;
    byId('page-info').textContent = count
        ? `Showing ${first + 1}–${first + page.length} of ${formatCount(count)}${search.total > search.items.length ? ` (best ${search.items.length} of ${formatCount(search.total)})` : ''}`
        : '';

    byId('results').innerHTML = page.map(({ entry, index, score }) => renderResultCard(entry, index, score)).join('')
        || `<div class="text-center text-secondary py-5">${icon('emoji-neutral', 'fs-1')}<p class="mt-2">No dataset matches. Try fewer words or another source.</p></div>`;
    renderFailures();
    renderFacets();
}

function renderResultCard(entry, index, score) {
    const query = state.search.query;
    const flow = entry.flow;
    const matched = matchedFieldsOf(entry, query);
    return `
        <div class="card result fade-in" data-index="${index}" onclick="openResult(${index})">
            <div class="card-body py-3">
                <div class="d-flex align-items-start gap-3">
                    <div class="source-tile rounded bg-primary-subtle text-primary d-flex align-items-center justify-content-center fw-bold flex-none">${escapeHtml(entry.source)}</div>
                    <div class="flex-grow-1 min-w-0">
                        <div class="d-flex flex-wrap align-items-baseline gap-2">
                            <h6 class="mb-0">${highlight(flow.name || flowIdOf(flow.ref), query)}</h6>
                            <code class="small text-secondary">${highlight(flowIdOf(flow.ref), query)}</code>
                            ${entry.database ? `<span class="badge text-bg-light border">${escapeHtml(entry.database)}</span>` : ''}
                        </div>
                        ${flow.description && flow.description !== flow.name ? `<div class="small text-secondary mt-1">${highlight(flow.description, query)}</div>` : ''}
                        <div class="mt-2 d-flex flex-wrap align-items-center gap-2 small">
                            ${matched.map(field => `<span class="badge text-bg-light border" title="Matched in">${field}</span>`).join('')}
                            <a href="#" class="ms-1 peek-link" onclick="event.preventDefault(); event.stopPropagation(); peekStructure(${index})">${icon('eye')} Peek structure</a>
                        </div>
                        <div id="peek-${index}" class="mt-2 d-none" onclick="event.stopPropagation()"></div>
                    </div>
                    <div class="text-end d-none d-sm-block flex-none">
                        ${score !== undefined ? `<div class="score ms-auto" title="Relevance ${Math.round(score * 100)}%"><span style="width:${Math.round(score * 100)}%"></span></div><div class="small text-secondary mt-1">relevance</div>` : ''}
                        <span class="btn btn-sm btn-outline-primary mt-2">Explore ${icon('arrow-right')}</span>
                    </div>
                </div>
            </div>
        </div>`;
}

function renderFailures() {
    const failures = state.search.failures;
    byId('failures').innerHTML = failures.length ? `
        <details class="alert alert-warning py-2 small">
            <summary>${icon('exclamation-triangle')} ${failures.length} source${failures.length === 1 ? '' : 's'} could not be searched</summary>
            <ul class="mb-0 mt-2">${failures.map(failure => `<li><b>${escapeHtml(failure.source)}</b>${failure.database ? ' / ' + escapeHtml(failure.database) : ''}: ${escapeHtml(failure.message || failure.reason)}</li>`).join('')}</ul>
        </details>` : '';
}

function renderFacets() {
    const search = state.search;
    const sources = new Map();
    search.items.forEach(entry => sources.set(entry.source, (sources.get(entry.source) || 0) + 1));
    byId('facet-source').innerHTML = [...sources.entries()].map(([source, count], index) => `
        <div class="form-check">
            <input class="form-check-input" type="checkbox" id="facet-source-${index}" ${search.sourceFilter.has(source) ? 'checked' : ''} onchange="toggleSourceFacet(${jsArg(source)}, this.checked)">
            <label class="form-check-label d-flex" for="facet-source-${index}">${escapeHtml(source)}<span class="ms-auto text-secondary small">${count}</span></label>
        </div>`).join('') || '<div class="small text-secondary">No source</div>';

    const fields = new Map();
    search.items
        .filter(entry => !search.sourceFilter.size || search.sourceFilter.has(entry.source))
        .forEach(entry => matchedFieldsOf(entry, search.query).forEach(field => fields.set(field, (fields.get(field) || 0) + 1)));
    byId('facet-where').innerHTML = [...fields.entries()].map(([field, count]) =>
        `<div class="d-flex small"><span>${field}</span><span class="ms-auto text-secondary">${count}</span></div>`).join('')
        || '<div class="small text-secondary">–</div>';
}

function openResult(index) {
    const entry = state.search.items[index];
    if (!entry) return;
    openDataset({
        source: entry.source,
        flow: entry.flow.ref,
        database: entry.database || '',
        name: entry.flow.name,
        description: entry.flow.description
    });
}

/**
 * Show or hide the dimensions of a search result, loading them on first use
 * @param {number} index - Index of the result
 */
async function peekStructure(index) {
    const element = byId('peek-' + index);
    const entry = state.search.items[index];
    if (!element || !entry) return;
    if (element.dataset.loaded) {
        element.classList.toggle('d-none');
        return;
    }
    element.classList.remove('d-none');
    element.innerHTML = `<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> <span class="small text-secondary">Loading structure from ${escapeHtml(entry.source)}…</span>`;
    try {
        const dimensions = itemsOf(await getJSON(apiUrl([entry.source, entry.flow.ref, 'dimensions'], { database: entry.database })));
        element.dataset.loaded = 'true';
        element.innerHTML = `<div class="small text-secondary mb-1">${dimensions.length} dimension${dimensions.length === 1 ? '' : 's'} · structure <code>${escapeHtml(entry.flow.structureRef || '')}</code></div>`
            + dimensions.map(dimension => `<span class="badge text-bg-light border me-1" title="${escapeHtml(dimension.id)}">${escapeHtml(dimension.name || dimension.id)}</span>`).join('');
    } catch (error) {
        element.innerHTML = `<span class="small text-danger">${icon('exclamation-circle')} ${escapeHtml(error.message)}</span>`;
    }
}
