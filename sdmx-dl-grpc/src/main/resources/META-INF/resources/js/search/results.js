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
    if (search.controller) search.controller.abort();
    const controller = new AbortController();
    Object.assign(search, {
        query, scope, items: [], scores: [], failures: [], total: 0, page: 0, sourceFilter: new Set(),
        busy: true, started: performance.now(), pending: [], update: null, controller
    });
    const token = ++search.token;
    showView('search');
    updateUrl(push);
    byId('result-count').textContent = '';
    byId('result-time').textContent = '';
    byId('failures').innerHTML = '';
    byId('facet-source').innerHTML = '';
    byId('facet-where').innerHTML = '';
    byId('page-info').textContent = '';
    byId('results').innerHTML = '';
    startProgress();

    // first pass: show quickly what most sources answer, then wait for the slow ones in the background
    // confidential sources are only searched when picked, as they may ask for credentials
    const params = {
        sources: scope || 'all', maxConfidentiality: scope ? '' : 'PUBLIC',
        query, maxResults: SEARCH_MAX_RESULTS, plainText: true, truncate: 400
    };
    try {
        const result = await getJSON(apiUrl(['flows'], { ...params, timeout: QUICK_SEARCH_TIMEOUT_MS }), { signal: controller.signal });
        if (token !== search.token) return;
        applySearchResult(result);
        search.pending = slowSourcesOf(result);
        if (search.pending.length) {
            searchSlowSources(token, params);
        } else {
            stopProgress();
        }
        renderResults();
    } catch (error) {
        if (token !== search.token) return;
        stopProgress();
        byId('results').innerHTML = error.name === 'AbortError'
            ? placeholderMessage('Search stopped')
            : errorMessage(error.message);
    }
}

/**
 * Search again the same sources with the remaining time, so that the slow ones can answer; the fast ones answer
 * from the cache and the ranking stays consistent across all sources
 * @param {number} token - Token of the search
 * @param {Object} params - Parameters of the first pass, without timeout
 */
async function searchSlowSources(token, params) {
    const search = state.search;
    const remaining = Math.max(1000, Math.round((SEARCH_TIMEOUT_MS - (performance.now() - search.started)) / 1000) * 1000);
    try {
        const result = await getJSON(apiUrl(['flows'], { ...params, timeout: remaining }), { signal: search.controller.signal });
        if (token !== search.token) return;
        const answered = search.pending.filter(source => !slowSourcesOf(result).includes(source));
        const added = (result.total || 0) - search.total;
        search.pending = [];
        stopProgress();
        if (added > 0 && search.items.length) {
            // do not reorder what the user is looking at: offer to show the new results instead
            search.update = { result, added, answered, elapsed: Math.round(performance.now() - search.started) };
        } else if (added > 0) {
            applySearchResult(result);
        } else {
            search.failures = result.failures || [];
        }
        renderResults();
    } catch (error) {
        if (token !== search.token) return;
        search.pending = [];
        stopProgress();
        if (error.name !== 'AbortError') notify(error.message, 'danger');
        renderSearchStatus();
        renderFailures();
    }
}

/**
 * Stop waiting for the current search, keeping the results already shown
 */
function stopSearch() {
    const search = state.search;
    if (search.controller) search.controller.abort();
}

/**
 * Show the results of the second pass, including the datasets of the slow sources
 */
function applySearchUpdate() {
    const update = state.search.update;
    if (!update) return;
    applySearchResult(update.result, update.elapsed);
    state.search.update = null;
    state.search.page = 0;
    renderResults();
}

function applySearchResult(result, elapsed) {
    const search = state.search;
    search.items = itemsOf(result);
    search.scores = result.scores || [];
    search.failures = result.failures || [];
    search.total = result.total || 0;
    search.elapsed = elapsed !== undefined ? elapsed : Math.round(performance.now() - search.started);
}

/**
 * Get the sources that did not answer in time
 * @param {Object} result - Response of /flows
 * @returns {Array<string>} - Source ids
 */
function slowSourcesOf(result) {
    return [...new Set((result.failures || [])
        .filter(failure => failure.reason === 'UPSTREAM_TIMEOUT' && !failure.database)
        .map(failure => failure.source))];
}

function startProgress() {
    const search = state.search;
    clearInterval(search.ticker);
    search.ticker = setInterval(tickProgress, 250);
    renderSearchStatus();
}

function stopProgress() {
    const search = state.search;
    clearInterval(search.ticker);
    search.busy = false;
    search.controller = null;
}

/**
 * Move the progress bar and the countdown towards the search timeout, which bounds the waiting time
 */
function tickProgress() {
    const search = state.search;
    const bar = byId('search-progress-bar');
    const time = byId('search-progress-time');
    if (!bar || !time) return;
    const elapsed = performance.now() - search.started;
    bar.style.width = Math.min(100, elapsed / SEARCH_TIMEOUT_MS * 100).toFixed(1) + '%';
    time.textContent = `${Math.max(0, Math.ceil((SEARCH_TIMEOUT_MS - elapsed) / 1000))} s left`;
}

/**
 * Render the progress of the search, or the offer to show the results of the slow sources
 */
function renderSearchStatus() {
    const search = state.search;
    const plural = count => count === 1 ? '' : 's';
    let html = '';
    if (search.busy) {
        const pending = search.pending;
        const label = pending.length
            ? `${pending.length} slow source${plural(pending.length)} still searching, results will be added when ${pending.length === 1 ? 'it answers' : 'they answer'}`
            : (search.scope ? `Searching ${escapeHtml(search.scope)}…` : 'Searching all sources…');
        html = `
            <div class="search-progress card card-body py-2 mb-2 small">
                <div class="d-flex align-items-center gap-2">
                    <span class="spinner-border spinner-border-sm text-primary flex-none" aria-hidden="true"></span>
                    <span class="flex-grow-1">${label}</span>
                    <span id="search-progress-time" class="text-secondary text-nowrap"></span>
                    <button type="button" class="btn btn-sm btn-outline-secondary py-0" onclick="stopSearch()">${icon('stop-circle')} Stop</button>
                </div>
                <div class="progress mt-2" role="progressbar" aria-label="Search time"><div id="search-progress-bar" class="progress-bar"></div></div>
                ${pending.length ? `<div class="d-flex flex-wrap gap-1 mt-2">${pending.map(source =>
                    `<span class="badge rounded-pill text-bg-light border">${icon('hourglass-split')} ${escapeHtml(source)}</span>`).join('')}</div>` : ''}
            </div>`;
    } else if (search.update) {
        const update = search.update;
        html = `
            <div class="alert alert-info py-2 small d-flex align-items-center gap-2 fade-in">
                ${icon('plus-circle')}
                <span class="flex-grow-1">${formatCount(update.added)} more dataset${plural(update.added)} from ${update.answered.map(escapeHtml).join(', ') || 'slow sources'}</span>
                <button type="button" class="btn btn-sm btn-primary py-0" onclick="applySearchUpdate()">Show</button>
            </div>`;
    }
    byId('search-status').innerHTML = html;
    tickProgress();
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
        || (search.busy ? '' : `<div class="text-center text-secondary py-5">${icon('emoji-neutral', 'fs-1')}<p class="mt-2">No dataset matches. Try fewer words or another source.</p></div>`);
    renderSearchStatus();
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
    const pending = state.search.pending;
    const failures = state.search.failures.filter(failure => failure.database || !pending.includes(failure.source));
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
