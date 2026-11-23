// ========================================================================
// SDMX-DL SEARCH: STEP 2, NARROW DOWN A DATASET
// ========================================================================

/**
 * Get the key of the current dataset selection (e.g. 'M.CHF+USD.EUR..')
 * @returns {string} - Key, with empty parts for wildcards
 */
function datasetKey() {
    const dataset = state.dataset;
    return dataset && dataset.selection ? dataset.selection.map(codes => codes.join('+')).join('.') : '';
}

/**
 * Get the dataset reference shown to and copied by the user (e.g. 'ECB/EXR/M.USD.EUR.SP00.A')
 * @param {string} key - Key, or the current one if omitted
 * @returns {string} - Reference
 */
function datasetRefText(key = datasetKey()) {
    const dataset = state.dataset;
    return `${dataset.source}/${flowIdOf(dataset.flow)}${key.replace(/\./g, '') ? '/' + key : ''}`;
}

/**
 * Open a dataset, optionally with a key and a series
 * @param {Object} ref - { source, flow, database, name?, description? }
 * @param {Object} options - { key, series, push }
 */
async function openDataset(ref, options = {}) {
    hideSuggestions();
    const push = options.push !== false;
    const current = state.dataset;
    if (current && current.dimensions && refId(current) === refId(ref)) {
        state.series = null;
        if (options.key !== undefined) applyKey(options.key);
        showView('dataset');
        renderDataset();
        refreshDataset();
        updateUrl(push);
        if (options.series) openSeries(options.series);
        return;
    }

    state.series = null;
    lastSeriesKey = null;
    const dataset = state.dataset = {
        source: ref.source,
        flow: ref.flow,
        database: ref.database || '',
        name: ref.name || '',
        description: ref.description || '',
        dimensions: null,
        attributes: [],
        selection: null,
        available: null,
        availabilityError: null,
        hot: null,
        codeFilters: {},
        series: [],
        seriesTotal: 0,
        unfilteredTotal: null,
        filter: '',
        grid: false,
        token: 0
    };
    byId('series-filter').value = '';
    byId('code-find').value = '';
    byId('code-hits').innerHTML = '';
    renderDatasetHeader();
    byId('dimensions').innerHTML = loadingMessage('Loading structure…');
    byId('keybar').innerHTML = '';
    byId('series').innerHTML = '';
    byId('series-grid').innerHTML = '';
    byId('series-footer').textContent = '';
    byId('more-series-btn').classList.add('d-none');
    byId('match-count').textContent = '0';
    byId('total-count').textContent = '…';
    byId('crumb-dataset-text').textContent = flowIdOf(dataset.flow);
    byId('crumb-series-text').textContent = 'Inspect a series';
    showView('dataset');
    updateUrl(push);

    try {
        const meta = await getJSON(apiUrl([dataset.source, dataset.flow, 'meta'], { database: dataset.database }));
        if (state.dataset !== dataset) return;
        const structure = meta.structure || {};
        if (meta.flow) {
            dataset.flow = meta.flow.ref || dataset.flow;
            dataset.name = meta.flow.name || dataset.name;
            dataset.description = meta.flow.description || dataset.description;
        }
        dataset.dimensions = (structure.dimensions || [])
            .slice()
            .sort((a, b) => (a.index || 0) - (b.index || 0))
            .map(dimension => ({
                id: dimension.id,
                name: dimension.name || dimension.id,
                codes: { ...((dimension.codelist && dimension.codelist.codes) || {}) }
            }));
        dataset.attributes = structure.attributes || [];
        dataset.selection = dataset.dimensions.map(() => []);
        applyKey(options.key || '');
        renderDatasetHeader();
        renderDataset();
        refreshDataset();
        updateUrl(false);
        if (options.series) openSeries(options.series);
    } catch (error) {
        if (state.dataset !== dataset) return;
        byId('dimensions').innerHTML = `<div class="p-2">${errorMessage(error.message)}</div>`;
        byId('series').innerHTML = `<tr><td colspan="2">${errorMessage('Cannot open this dataset')}</td></tr>`;
    }
}

/**
 * Select the codes of a key in the current dataset
 * @param {string} key - Key such as 'M.CHF+USD..', where empty and '*' parts are wildcards
 */
function applyKey(key) {
    const dataset = state.dataset;
    const text = String(key || '').trim();
    dataset.selection = dataset.dimensions.map(() => []);
    if (!text || text === 'all') return;
    const parts = text.split('.');
    if (parts.length !== dataset.dimensions.length) {
        notify(`Key ${text} ignored: ${dataset.dimensions.length} dimensions expected`, 'warning');
        return;
    }
    dataset.selection = parts.map(part => part && part !== '*' ? [...new Set(part.split('+').filter(Boolean))] : []);
}

function renderDatasetHeader() {
    const dataset = state.dataset;
    const source = state.sources.find(item => item.id === dataset.source);
    const sourceName = source && source.names ? (source.names.en || Object.values(source.names)[0]) : '';
    byId('dataset-source').innerHTML = sourceIcon(dataset.source, 16, 'source-logo me-1 align-text-bottom')
        + escapeHtml(sourceName ? `${dataset.source} · ${sourceName}` : dataset.source);
    byId('dataset-name').textContent = dataset.name || flowIdOf(dataset.flow);
    byId('dataset-ref').textContent = dataset.flow + (dataset.database ? ` (${dataset.database})` : '');
    byId('dataset-description').textContent = dataset.description && dataset.description !== dataset.name ? dataset.description : '';
    updateFavoriteButton();
}

/**
 * Reload the availability of the codes and the matching series after a change of selection
 */
function refreshDataset() {
    const dataset = state.dataset;
    if (!dataset || !dataset.dimensions) return;
    const token = ++dataset.token;
    loadAvailability(dataset, token);
    loadSeries(dataset, token, false);
}

async function loadAvailability(dataset, token) {
    dataset.availabilityLoading = true;
    renderAvailabilityNote();
    try {
        const result = await getJSON(apiUrl([dataset.source, dataset.flow, 'availability'], {
            key: datasetKey(),
            dimension: '*',
            database: dataset.database
        }));
        if (state.dataset !== dataset || token !== dataset.token) return;
        dataset.available = new Map(itemsOf(result).map(item => [item.dimension, new Map((item.codes || []).map(code => [code.id, code.name || '']))]));
        dataset.available.forEach((codes, id) => {
            const dimension = dataset.dimensions.find(item => item.id === id);
            if (dimension) codes.forEach((name, code) => {
                if (!(code in dimension.codes)) dimension.codes[code] = name;
            });
        });
        dataset.availabilityError = null;
    } catch (error) {
        if (state.dataset !== dataset || token !== dataset.token) return;
        dataset.available = null;
        dataset.availabilityError = error.message;
    }
    dataset.availabilityLoading = false;
    renderDataset();
}

async function loadSeries(dataset, token, append) {
    const offset = append ? dataset.series.length : 0;
    if (!append) {
        byId('series-footer').innerHTML = `<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Loading series…`;
    }
    byId('more-series-btn').disabled = true;
    try {
        const result = await getJSON(apiUrl([dataset.source, dataset.flow, 'data'], {
            key: datasetKey(),
            database: dataset.database,
            detail: 'NO_DATA',
            seriesOffset: offset || undefined,
            seriesLimit: SERIES_PAGE_SIZE
        }));
        if (state.dataset !== dataset || token !== dataset.token) return;
        const series = (result.data || []).map(item => ({ key: item.key, meta: item.meta || {} }));
        dataset.series = append ? dataset.series.concat(series) : series;
        dataset.seriesTotal = result.total || 0;
        dataset.seriesError = null;
        if (dataset.selection.every(codes => codes.length === 0)) {
            dataset.unfilteredTotal = dataset.seriesTotal;
        } else if (dataset.unfilteredTotal === null) {
            loadUnfilteredTotal(dataset);
        }
    } catch (error) {
        if (state.dataset !== dataset || token !== dataset.token) return;
        if (!append) {
            dataset.series = [];
            dataset.seriesTotal = 0;
        }
        dataset.seriesError = error.message;
    }
    byId('more-series-btn').disabled = false;
    renderDataset();
}

async function loadUnfilteredTotal(dataset) {
    dataset.unfilteredTotal = undefined;
    try {
        const result = await getJSON(apiUrl([dataset.source, dataset.flow, 'data'], {
            key: dataset.dimensions.map(() => '').join('.'),
            database: dataset.database,
            detail: 'SERIES_KEYS_ONLY',
            seriesLimit: 1
        }));
        if (state.dataset !== dataset) return;
        dataset.unfilteredTotal = result.total || 0;
        renderDataset();
    } catch (error) {
        console.warn('Cannot count the series of the dataset', error);
    }
}

function loadMoreSeries() {
    const dataset = state.dataset;
    if (dataset) loadSeries(dataset, dataset.token, true);
}

// ------------------------------------------------------------------------
// Rendering
// ------------------------------------------------------------------------

/**
 * Render the dimensions, the key, the counters and the series of the current dataset
 */
function renderDataset() {
    const dataset = state.dataset;
    if (!dataset || !dataset.dimensions) return;
    byId('dimensions').innerHTML = dataset.dimensions.map((dimension, index) => renderDimension(dimension, index)).join('')
        || '<div class="list-group-item small text-secondary">No dimension</div>';
    renderKeyBar();
    renderAvailabilityNote();

    const total = dataset.seriesTotal;
    const unfiltered = dataset.unfilteredTotal;
    byId('match-count').textContent = formatCount(total);
    byId('total-count').textContent = typeof unfiltered === 'number' ? formatCount(unfiltered) : '…';
    byId('match-bar').style.width = (typeof unfiltered === 'number' && unfiltered > 0 ? Math.max(2, total / unfiltered * 100) : 100) + '%';
    byId('crumb-dataset-text').textContent = `${flowIdOf(dataset.flow)} · ${formatCount(total)} series`;
    byId('keyline').innerHTML = `<code>${escapeHtml(datasetRefText())}</code>`;
    renderSeriesList();
}

function renderDimension(dimension, index) {
    const dataset = state.dataset;
    const selected = dataset.selection[index];
    const open = dataset.hot === index;
    const available = dataset.available && dataset.available.get(dimension.id);
    const badge = selected.length
        ? `<span class="ms-auto badge text-bg-primary">${selected.length} selected</span>`
        : `<span class="ms-auto badge text-bg-light border">${available ? formatCount(available.size) + ' available' : 'all'}</span>`;
    const codeCount = Object.keys(dimension.codes).length;
    return `
        <div class="list-group-item p-0" data-dimension="${escapeHtml(dimension.id)}">
            <button class="btn w-100 text-start d-flex align-items-center gap-2 px-3 py-2 dim-toggle" type="button" aria-expanded="${open}" onclick="toggleDimension(${index})">
                ${icon(open ? 'chevron-down' : 'chevron-right', 'small')}<span class="text-truncate" title="${escapeHtml(dimension.id)}">${escapeHtml(dimension.name)}</span>${badge}
            </button>
            ${open ? `<div class="px-3 pb-2 fade-in">
                ${codeCount > 8 ? `<input class="form-control form-control-sm mb-1 code-filter" placeholder="Search ${escapeHtml(dimension.name.toLowerCase())}…" value="${escapeHtml(dataset.codeFilters[index] || '')}" oninput="filterCodes(${index}, this.value)" aria-label="Search codes">` : ''}
                ${codeCount === 0 ? `<input class="form-control form-control-sm mb-1" placeholder="Type a code and press Enter" onkeydown="if (event.key === 'Enter' && this.value.trim()) pickCode(${index}, this.value.trim(), true)" aria-label="Code">` : ''}
                <div class="code-list" id="code-list-${index}">${renderCodeRows(index)}</div>
            </div>` : ''}
        </div>`;
}

function renderCodeRows(index) {
    const dataset = state.dataset;
    const dimension = dataset.dimensions[index];
    const selected = dataset.selection[index];
    const available = dataset.available && dataset.available.get(dimension.id);
    const filter = (dataset.codeFilters[index] || '').toLowerCase();
    let codes = Object.entries(dimension.codes);
    selected.filter(code => !(code in dimension.codes)).forEach(code => codes.push([code, '']));
    if (filter) codes = codes.filter(([code, name]) => (code + ' ' + name).toLowerCase().includes(filter));
    if (available) codes.sort((a, b) => (available.has(a[0]) || selected.includes(a[0]) ? 0 : 1) - (available.has(b[0]) || selected.includes(b[0]) ? 0 : 1));

    const rows = codes.slice(0, CODE_RENDER_LIMIT).map(([code, name], position) => {
        const checked = selected.includes(code);
        const usable = checked || !available || available.has(code);
        const id = `code-${index}-${position}`;
        return `<div class="form-check d-flex">
            <input class="form-check-input me-2" type="checkbox" id="${id}" ${checked ? 'checked' : ''} ${usable ? '' : 'disabled'} onchange="pickCode(${index}, ${jsArg(code)}, this.checked)">
            <label class="form-check-label small flex-grow-1 ${usable ? '' : 'text-secondary'}" for="${id}" title="${usable ? '' : 'No data with the current selection'}"><code>${escapeHtml(code)}</code> ${escapeHtml(name)}</label>
        </div>`;
    }).join('');
    const more = codes.length > CODE_RENDER_LIMIT ? `<div class="small text-secondary">${formatCount(codes.length - CODE_RENDER_LIMIT)} more, refine the search</div>` : '';
    return rows + more || '<div class="small text-secondary">No code</div>';
}

function renderKeyBar() {
    const dataset = state.dataset;
    byId('keybar').innerHTML = dataset.dimensions.map((dimension, index) => {
        const codes = dataset.selection[index];
        const part = `<span class="key-part ${codes.length ? '' : 'wild'} ${dataset.hot === index ? 'hot' : ''}" title="${escapeHtml(dimension.name)}" onclick="toggleDimension(${index})">${escapeHtml(codes.length ? codes.join('+') : '*')}</span>`;
        return part + (index < dataset.dimensions.length - 1 ? '<span class="text-secondary">.</span>' : '');
    }).join('');
}

function renderAvailabilityNote() {
    const dataset = state.dataset;
    const note = byId('availability-note');
    if (dataset.availabilityLoading) {
        note.innerHTML = `<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Checking which codes have data…`;
    } else if (dataset.availabilityError) {
        note.innerHTML = `${icon('info-circle')} Availability unknown for this source, all codes are shown. <span class="text-body-tertiary">(${escapeHtml(dataset.availabilityError)})</span>`;
    } else {
        note.textContent = 'Each selection narrows the list — pick codes on the left, or click a part of the key. Codes without data are greyed out.';
    }
}

/**
 * Get the title and subtitle of a series, from its TITLE attribute and the labels of its codes
 * @param {Object} series - Series with key and meta
 * @returns {{title: string, subtitle: string}} - Labels
 */
function seriesLabels(series) {
    const dataset = state.dataset;
    const parts = String(series.key || '').split('.');
    const varying = dataset.dimensions
        .map((dimension, index) => ({ dimension, index }))
        .filter(({ index }) => dataset.selection[index].length !== 1)
        .map(({ dimension, index }) => dimension.codes[parts[index]] || parts[index])
        .filter(Boolean);
    const title = series.meta.TITLE || series.meta.TITLE_COMPL || '';
    return title
        ? { title, subtitle: varying.join(' · ') }
        : { title: varying.join(' — ') || series.key, subtitle: '' };
}

function filteredSeries() {
    const dataset = state.dataset;
    const filter = dataset.filter.toLowerCase();
    if (!filter) return dataset.series;
    return dataset.series.filter(series => {
        const labels = seriesLabels(series);
        return (series.key + ' ' + labels.title + ' ' + labels.subtitle).toLowerCase().includes(filter);
    });
}

function renderSeriesList() {
    const dataset = state.dataset;
    const list = filteredSeries();
    const selectedKey = state.series && state.series.key;
    const empty = dataset.seriesError
        ? errorMessage(dataset.seriesError)
        : `No series match this selection. <a href="#" onclick="event.preventDefault(); resetDimensions()">Reset</a>`;

    byId('series').innerHTML = list.map(series => {
        const labels = seriesLabels(series);
        return `<tr class="series-row ${series.key === selectedKey ? 'selected' : ''}" data-key="${escapeHtml(series.key)}" onclick="openSeries(${jsArg(series.key)})">
            <td><div class="fw-semibold">${highlight(labels.title, dataset.filter)}</div>${labels.subtitle ? `<div class="small text-secondary">${highlight(labels.subtitle, dataset.filter)}</div>` : ''}</td>
            <td class="d-none d-md-table-cell"><code class="small">${highlight(series.key, dataset.filter)}</code></td>
        </tr>`;
    }).join('') || `<tr><td colspan="2" class="text-center text-secondary py-4">${empty}</td></tr>`;

    byId('series-grid').innerHTML = list.map(series => {
        const labels = seriesLabels(series);
        return `<div class="col-sm-6 col-xl-4"><div class="card result h-100 ${series.key === selectedKey ? 'border-primary' : ''}" onclick="openSeries(${jsArg(series.key)})"><div class="card-body p-2">
            <div class="fw-semibold small">${highlight(labels.title, dataset.filter)}</div>
            ${labels.subtitle ? `<div class="small text-secondary">${highlight(labels.subtitle, dataset.filter)}</div>` : ''}
            <code class="small text-secondary">${highlight(series.key, dataset.filter)}</code>
        </div></div></div>`;
    }).join('') || `<div class="col-12 text-center text-secondary py-4">${empty}</div>`;

    byId('series-footer').textContent = dataset.filter
        ? `${list.length} of ${dataset.series.length} loaded series match “${dataset.filter}” (${formatCount(dataset.seriesTotal)} in total)`
        : `Showing ${formatCount(dataset.series.length)} of ${formatCount(dataset.seriesTotal)} series`;
    byId('more-series-btn').classList.toggle('d-none', dataset.series.length >= dataset.seriesTotal);
}

// ------------------------------------------------------------------------
// Interactions
// ------------------------------------------------------------------------

function toggleDimension(index) {
    const dataset = state.dataset;
    dataset.hot = dataset.hot === index ? null : index;
    renderDataset();
    if (dataset.hot !== null) {
        const input = document.querySelector(`[data-dimension="${CSS.escape(dataset.dimensions[index].id)}"] .code-filter`);
        if (input) input.focus();
    }
}

function filterCodes(index, text) {
    state.dataset.codeFilters[index] = text;
    byId('code-list-' + index).innerHTML = renderCodeRows(index);
}

/**
 * Select or unselect a code of a dimension, then refresh the dataset
 * @param {number} index - Index of the dimension
 * @param {string} code - Code
 * @param {boolean} on - Select the code if true, unselect it otherwise
 */
function pickCode(index, code, on) {
    const dataset = state.dataset;
    const codes = new Set(dataset.selection[index]);
    if (on) codes.add(code); else codes.delete(code);
    dataset.selection[index] = [...codes];
    renderDataset();
    refreshDataset();
    updateUrl(false);
}

function resetDimensions() {
    const dataset = state.dataset;
    if (!dataset || !dataset.dimensions) return;
    dataset.selection = dataset.dimensions.map(() => []);
    dataset.codeFilters = {};
    renderDataset();
    refreshDataset();
    updateUrl(false);
}

function setGridMode(grid) {
    state.dataset.grid = grid;
    byId('list-mode-btn').classList.toggle('active', !grid);
    byId('grid-mode-btn').classList.toggle('active', grid);
    byId('series-table').classList.toggle('d-none', grid);
    byId('series-grid').classList.toggle('d-none', !grid);
}

function copyDatasetKey() {
    if (state.dataset) copyToClipboard(datasetRefText(), 'Key copied to clipboard');
}

function onSeriesFilter(text) {
    state.dataset.filter = text.trim();
    renderSeriesList();
}

/**
 * Find codes in all the dimensions of the current dataset
 * @param {string} text - Text to find in code ids and labels
 */
function findCodes(text) {
    const dataset = state.dataset;
    const query = text.trim().toLowerCase();
    if (!query || !dataset || !dataset.dimensions) {
        byId('code-hits').innerHTML = '';
        return;
    }
    const hits = [];
    dataset.dimensions.forEach((dimension, index) => Object.entries(dimension.codes).forEach(([code, name]) => {
        if ((code + ' ' + name).toLowerCase().includes(query)) hits.push({ index, dimension, code, name });
    }));
    byId('code-hits').innerHTML = hits.slice(0, 8).map(hit => `
        <a href="#" class="list-group-item list-group-item-action px-1 py-1 small d-flex gap-2" onclick="event.preventDefault(); pickFoundCode(${hit.index}, ${jsArg(hit.code)})">
            <span class="text-truncate">${highlight(hit.name || hit.code, query)}</span>
            <span class="ms-auto text-secondary text-nowrap">${escapeHtml(hit.dimension.name)} · <code>${escapeHtml(hit.code)}</code></span>
        </a>`).join('') || '<div class="small text-secondary px-1">No code found</div>';
}

function pickFoundCode(index, code) {
    state.dataset.hot = index;
    byId('code-find').value = '';
    byId('code-hits').innerHTML = '';
    pickCode(index, code, true);
}
