// ========================================================================
// SDMX-DL SEARCH: STEP 3, INSPECT A SERIES
// ========================================================================

const OBS_TABLE_LIMIT = 500;

let scriptTargets = null;
let scriptChoice = { language: 'python', transport: 'cli' };
let scriptContent = null;
let lastSeriesKey = null;

/**
 * Fetch the observations of a series
 * @param {Object} ref - { source, flow, database, key }
 * @returns {Promise<Object|null>} - Series with key, meta and obs, or null if not found
 */
async function fetchSeries(ref) {
    const result = await getJSON(apiUrl([ref.source, ref.flow, 'data'], {
        key: ref.key,
        database: ref.database,
        detail: 'FULL',
        seriesLimit: 1
    }));
    const series = (result.data || [])[0];
    return series ? { key: series.key, meta: series.meta || {}, obs: series.obs || [] } : null;
}

/**
 * Open the drawer of a series of the current dataset
 * @param {string} key - Series key
 */
async function openSeries(key) {
    const dataset = state.dataset;
    if (!dataset || !dataset.dimensions) return;
    const listed = dataset.series.find(item => item.key === key);
    const series = state.series = { key, meta: listed ? listed.meta : {}, obs: null };
    const labels = seriesLabels(series);

    byId('series-dataset').textContent = `${dataset.source} · ${dataset.name || flowIdOf(dataset.flow)}`;
    byId('series-title').textContent = labels.title;
    byId('series-key').textContent = key;
    byId('series-kpis').innerHTML = '';
    byId('obs-body').innerHTML = `<tr><td colspan="3">${loadingMessage('Loading observations…')}</td></tr>`;
    byId('series-chart').innerHTML = '';
    byId('crumb-series-text').textContent = key;
    byId('crumb-series').classList.remove('disabled');
    setRange(0, false);
    updateCompareButton();
    renderSeriesDimensions();
    renderSnippet();
    document.querySelectorAll('.series-row').forEach(row => row.classList.toggle('selected', row.dataset.key === key));
    byId('drawer').classList.add('open');
    byId('backdrop').classList.add('open');
    updateUrl(false);

    try {
        const loaded = await fetchSeries({ ...dataset, key });
        if (state.series !== series) return;
        if (!loaded) {
            byId('obs-body').innerHTML = `<tr><td colspan="3">${placeholderMessage('No such series in this dataset')}</td></tr>`;
            return;
        }
        Object.assign(series, { meta: { ...series.meta, ...loaded.meta }, obs: loaded.obs });
        byId('series-title').textContent = seriesLabels(series).title;
        renderSeriesKpis();
        renderObservations();
        renderSeriesDimensions();
        renderSeriesChart();
    } catch (error) {
        if (state.series !== series) return;
        byId('obs-body').innerHTML = `<tr><td colspan="3">${errorMessage(error.message)}</td></tr>`;
    }
}

/**
 * Close the drawer
 * @param {boolean} silent - Do not update the URL
 */
function closeSeries(silent = false) {
    const drawer = byId('drawer');
    if (!drawer) return;
    drawer.classList.remove('open');
    byId('backdrop').classList.remove('open');
    if (state.series) {
        lastSeriesKey = state.series.key;
        state.series = null;
        document.querySelectorAll('.series-row.selected').forEach(row => row.classList.remove('selected'));
        if (!silent) updateUrl(false);
    }
    byId('crumb-series').classList.toggle('disabled', !lastSeriesKey || !state.dataset);
}

function reopenSeries() {
    const key = (state.series && state.series.key) || lastSeriesKey;
    if (key && state.dataset) {
        if (state.view !== 'dataset') showView('dataset');
        openSeries(key);
    }
}

/**
 * Open the previous or next series of the list
 * @param {number} delta - -1 or 1
 */
function stepSeries(delta) {
    if (!state.series || !state.dataset) return;
    const list = filteredSeries();
    const index = list.findIndex(item => item.key === state.series.key);
    const next = list[index + delta];
    if (next) openSeries(next.key);
}

// ------------------------------------------------------------------------
// Rendering
// ------------------------------------------------------------------------

function sortedObs(series) {
    return (series.obs || [])
        .filter(obs => obs.value !== undefined && obs.value !== null && !Number.isNaN(Number(obs.value)))
        .slice()
        .sort((a, b) => periodStartOf(a.period).localeCompare(periodStartOf(b.period)));
}

function renderSeriesKpis() {
    const obs = sortedObs(state.series);
    const kpi = (label, value, detail) => `
        <div class="col-6 col-sm-3"><div class="border rounded p-2 h-100">
            <div class="small text-secondary">${label}</div>
            <div class="fs-5 fw-semibold">${value}</div>
            <div class="small text-secondary">${detail}</div>
        </div></div>`;
    if (!obs.length) {
        byId('series-kpis').innerHTML = kpi('Observations', '0', 'no value');
        return;
    }
    const last = obs[obs.length - 1];
    const previous = obs[obs.length - 2];
    const peak = obs.reduce((a, b) => Number(b.value) > Number(a.value) ? b : a);
    const low = obs.reduce((a, b) => Number(b.value) < Number(a.value) ? b : a);
    let change = formatPeriod(last.period);
    if (previous && Number(previous.value) !== 0) {
        const percent = (Number(last.value) - Number(previous.value)) / Math.abs(Number(previous.value)) * 100;
        const css = percent > 0 ? 'text-success' : percent < 0 ? 'text-danger' : '';
        change += ` · <span class="${css}">${percent > 0 ? '+' : ''}${percent.toFixed(2)}%</span>`;
    }
    byId('series-kpis').innerHTML = kpi('Latest', formatValue(last.value), change)
        + kpi('Peak', formatValue(peak.value), formatPeriod(peak.period))
        + kpi('Low', formatValue(low.value), formatPeriod(low.period))
        + kpi('Observations', formatCount(obs.length), `${formatPeriod(obs[0].period)} → ${formatPeriod(last.period)}`);
}

/**
 * Get the label of an observation status from the codelist of its attribute
 * @param {string} code - Status code
 * @returns {string} - Label, or the code itself
 */
function obsStatusLabel(code) {
    if (!code) return '';
    const attribute = (state.dataset.attributes || []).find(item => item.id === 'OBS_STATUS');
    const codes = attribute && attribute.codelist && attribute.codelist.codes;
    return codes && codes[code] ? codes[code] : code;
}

function renderObservations() {
    const obs = sortedObs(state.series).reverse();
    byId('obs-body').innerHTML = obs.slice(0, OBS_TABLE_LIMIT).map(item => {
        const status = (item.meta || {}).OBS_STATUS;
        return `<tr><td>${escapeHtml(formatPeriod(item.period))}</td><td class="text-end font-monospace">${formatValue(item.value)}</td><td class="small text-secondary" title="${escapeHtml(status || '')}">${escapeHtml(obsStatusLabel(status))}</td></tr>`;
    }).join('')
        + (obs.length > OBS_TABLE_LIMIT ? `<tr><td colspan="3" class="small text-secondary">${formatCount(obs.length - OBS_TABLE_LIMIT)} older observations, download the CSV to get them all</td></tr>` : '')
        || `<tr><td colspan="3">${placeholderMessage('No observation')}</td></tr>`;
}

function renderSeriesDimensions() {
    const dataset = state.dataset;
    const series = state.series;
    const parts = series.key.split('.');
    const dims = dataset.dimensions.map((dimension, index) => `
        <dt class="col-5 text-truncate" title="${escapeHtml(dimension.id)}">${escapeHtml(dimension.name)}</dt>
        <dd class="col-7"><code>${escapeHtml(parts[index] || '')}</code> ${escapeHtml(dimension.codes[parts[index]] || '')}</dd>`).join('');
    const attributes = Object.entries(series.meta || {}).map(([name, value]) => {
        const attribute = (dataset.attributes || []).find(item => item.id === name);
        const label = attribute && attribute.codelist && attribute.codelist.codes && attribute.codelist.codes[value];
        return `<dt class="col-5 text-truncate text-secondary" title="${escapeHtml(name)}">${escapeHtml(attribute && attribute.name ? attribute.name : name)}</dt>
            <dd class="col-7">${escapeHtml(value)}${label && label !== value ? ` <span class="text-secondary">${escapeHtml(label)}</span>` : ''}</dd>`;
    }).join('');
    byId('dimension-list').innerHTML = dims + (attributes ? `<dt class="col-12 mt-2 border-top pt-2">Attributes</dt><dd class="col-12 mb-0"></dd>${attributes}` : '');
}

async function renderSeriesChart() {
    const series = state.series;
    const obs = sortedObs(series);
    const chart = byId('series-chart');
    if (!obs.length) {
        chart.innerHTML = placeholderMessage('Nothing to plot');
        return;
    }
    try {
        await loadPlotly();
    } catch (error) {
        chart.innerHTML = errorMessage('Cannot load the charting library');
        return;
    }
    if (state.series !== series) return;
    const theme = plotlyTheme();
    chart.innerHTML = '';
    Plotly.newPlot(chart, [{
        x: obs.map(item => periodStartOf(item.period)),
        y: obs.map(item => Number(item.value)),
        text: obs.map(item => formatPeriod(item.period)),
        type: 'scatter',
        mode: obs.length > 60 ? 'lines' : 'lines+markers',
        hovertemplate: '%{text}: %{y}<extra></extra>',
        fill: 'tozeroy',
        fillcolor: 'rgba(0, 68, 149, 0.08)'
    }], {
        ...theme.layout,
        margin: { t: 10, r: 10, b: 30, l: 50 },
        showlegend: false,
        xaxis: { ...theme.layout.xaxis, type: 'date' },
        yaxis: { ...theme.layout.yaxis, rangemode: 'normal' }
    }, theme.config);
    applyRange();
}

let rangeYears = 0;

/**
 * Restrict the chart to the last years
 * @param {number} years - Number of years, or 0 for the whole series
 * @param {boolean} apply - Relayout the chart
 */
function setRange(years, apply = true) {
    rangeYears = years;
    byId('range').querySelectorAll('button').forEach(button => button.classList.toggle('active', Number(button.dataset.years) === years));
    if (apply) applyRange();
}

function applyRange() {
    const chart = byId('series-chart');
    const obs = state.series ? sortedObs(state.series) : [];
    if (typeof Plotly === 'undefined' || !chart.classList.contains('js-plotly-plot') || !obs.length) return;
    if (!rangeYears) {
        Plotly.relayout(chart, { 'xaxis.autorange': true, 'yaxis.autorange': true });
        return;
    }
    const end = new Date(periodStartOf(obs[obs.length - 1].period));
    const start = new Date(end);
    start.setFullYear(start.getFullYear() - rangeYears);
    const visible = obs.filter(item => new Date(periodStartOf(item.period)) >= start).map(item => Number(item.value));
    const min = Math.min(...visible);
    const max = Math.max(...visible);
    const pad = (max - min) * 0.05 || Math.abs(max) * 0.05 || 1;
    Plotly.relayout(chart, { 'xaxis.range': [start.toISOString(), end.toISOString()], 'yaxis.range': [min - pad, max + pad] });
}

function showSeriesTab(tab) {
    byId('series-tabs').querySelectorAll('.nav-link').forEach(link => link.classList.toggle('active', link.dataset.tab === tab));
    ['obs', 'dims', 'use'].forEach(name => byId('tab-' + name).classList.toggle('d-none', name !== tab));
    if (tab === 'use') renderSnippet();
}

// ------------------------------------------------------------------------
// Scripts and downloads
// ------------------------------------------------------------------------

async function loadScriptTargets() {
    if (scriptTargets) return scriptTargets;
    try {
        scriptTargets = itemsOf(await getJSON(apiUrl(['script', 'targets'])))
            .filter(target => !target.commands || target.commands.includes('data'));
    } catch (error) {
        scriptTargets = [];
    }
    return scriptTargets;
}

/**
 * Render the language and transport choices and the script fetching the current series
 */
async function renderSnippet() {
    if (byId('tab-use').classList.contains('d-none') || !state.series) return;
    const series = state.series;
    const targets = await loadScriptTargets();
    if (!targets.length) {
        byId('snippet').textContent = 'No script generator available';
        return;
    }
    const languages = [...new Set(targets.map(target => target.language))];
    if (!languages.includes(scriptChoice.language)) scriptChoice.language = languages[0];
    const transports = targets.filter(target => target.language === scriptChoice.language).map(target => target.transport);
    if (!transports.includes(scriptChoice.transport)) scriptChoice.transport = transports[0];

    byId('script-languages').innerHTML = languages.map(language =>
        `<button type="button" class="btn btn-outline-secondary ${language === scriptChoice.language ? 'active' : ''}" onclick="chooseScript(${jsArg(language)}, null)">${escapeHtml(language)}</button>`).join('');
    byId('script-transports').innerHTML = transports.map(transport =>
        `<button type="button" class="btn btn-outline-secondary ${transport === scriptChoice.transport ? 'active' : ''}" onclick="chooseScript(null, ${jsArg(transport)})">${escapeHtml(transport)}</button>`).join('');

    const dataset = state.dataset;
    const target = `${scriptChoice.language}/${scriptChoice.transport}`;
    byId('snippet').textContent = 'Generating…';
    try {
        const script = await getJSON(apiUrl([dataset.source, dataset.flow, 'data'], { key: series.key, database: dataset.database, target }, ':script'));
        if (state.series !== series) return;
        scriptContent = script;
        byId('snippet').textContent = script.content;
    } catch (error) {
        if (state.series !== series) return;
        scriptContent = null;
        byId('snippet').textContent = error.message;
    }
}

function chooseScript(language, transport) {
    if (language) scriptChoice.language = language;
    if (transport) scriptChoice.transport = transport;
    renderSnippet();
}

function copySnippet() {
    if (scriptContent) copyToClipboard(scriptContent.content, 'Script copied to clipboard');
}

function downloadSnippet() {
    if (!scriptContent || !state.series) return;
    const extension = scriptContent.fileExtension || 'txt';
    downloadText(scriptContent.content, `${state.dataset.source}_${flowIdOf(state.dataset.flow)}_${state.series.key}.${extension.replace(/^\./, '')}`, scriptContent.mediaType || 'text/plain');
}

function downloadSeriesCsv() {
    const series = state.series;
    if (!series || !series.obs) return;
    const quote = value => /[",\n]/.test(value) ? `"${value.replace(/"/g, '""')}"` : value;
    const rows = sortedObs(series).map(item => [formatPeriod(item.period), String(item.value), (item.meta || {}).OBS_STATUS || ''].map(quote).join(','));
    downloadText(['period,value,status', ...rows].join('\n') + '\n', `${state.dataset.source}_${flowIdOf(state.dataset.flow)}_${series.key}.csv`, 'text/csv');
}
