// ========================================================================
// SDMX-DL SEARCH: COMPARE SERIES
// ========================================================================

const compareCache = new Map();

function compareItems() {
    return loadStored('compare', []);
}

function currentSeriesRef() {
    const dataset = state.dataset;
    const series = state.series;
    return dataset && series ? { source: dataset.source, flow: dataset.flow, database: dataset.database, key: series.key } : null;
}

/**
 * Add the series of the drawer to the comparison, or remove it
 */
function toggleCompare() {
    const ref = currentSeriesRef();
    if (!ref) return;
    const items = compareItems();
    const index = items.findIndex(item => refId(item) === refId(ref));
    if (index >= 0) {
        items.splice(index, 1);
        notify('Removed from comparison');
    } else if (items.length >= COMPARE_LIMIT) {
        notify(`You can compare up to ${COMPARE_LIMIT} series`, 'warning');
        return;
    } else {
        items.push({ ...ref, title: byId('series-title').textContent || ref.key });
        notify(`Added to comparison (${items.length}/${COMPARE_LIMIT})`);
    }
    saveStored('compare', items);
    updateCompareButton();
    updateCompareBadge();
}

function updateCompareButton() {
    const ref = currentSeriesRef();
    const on = !!ref && compareItems().some(item => refId(item) === refId(ref));
    byId('compare-btn').innerHTML = on ? `${icon('dash-circle')} Remove from compare` : `${icon('plus-circle')} Add to compare`;
    byId('compare-btn').classList.toggle('active', on);
}

function updateCompareBadge() {
    const count = compareItems().length;
    byId('compare-badge').textContent = count;
    byId('compare-badge').classList.toggle('d-none', count === 0);
}

/**
 * Show the comparison view
 * @param {boolean} push - Create a new history entry
 */
function showCompare(push = true) {
    hideSuggestions();
    showView('compare');
    updateUrl(push);
    renderCompare();
}

function leaveCompare() {
    const previous = state.previousView;
    if (previous === 'dataset' && state.dataset) {
        showView('dataset');
    } else if (previous === 'search' && (state.search.query || state.search.scope)) {
        showView('search');
    } else {
        goHome();
        return;
    }
    updateUrl(true);
}

async function renderCompare() {
    const items = compareItems();
    const chart = byId('compare-chart');
    updateCompareBadge();
    if (!items.length) {
        if (typeof Plotly !== 'undefined') Plotly.purge(chart);
        chart.innerHTML = placeholderMessage('Nothing to compare yet: open a series and click “Add to compare”.');
        byId('compare-list').innerHTML = '';
        return;
    }

    const colors = plotlyTheme().layout.colorway;
    const results = items.map(item => ({ item, series: compareCache.get(refId(item)), error: null }));
    const renderList = () => {
        byId('compare-list').innerHTML = results.map(({ item, series, error }, index) => `
            <div class="list-group-item d-flex align-items-center gap-2">
                <span class="compare-swatch flex-none" style="background:${colors[index % colors.length]}"></span>
                <div class="min-w-0">
                    <div class="fw-semibold text-truncate">${escapeHtml(item.title)}</div>
                    <div class="small text-secondary"><code>${escapeHtml(item.source + '/' + flowIdOf(item.flow) + '/' + item.key)}</code>
                        ${error ? ` · <span class="text-danger">${escapeHtml(error)}</span>` : series ? ` · ${formatCount(series.obs.length)} obs` : ' · loading…'}</div>
                </div>
                <button class="btn btn-sm btn-outline-primary ms-auto" type="button" onclick="openComparedSeries(${index})">Open</button>
                <button class="btn btn-sm btn-outline-secondary" type="button" onclick="removeCompare(${index})" title="Remove" aria-label="Remove">${icon('x-lg')}</button>
            </div>`).join('');
    };
    renderList();
    if (!chart.classList.contains('js-plotly-plot')) chart.innerHTML = loadingMessage('Loading series…');

    await Promise.all(results.map(async result => {
        if (result.series) return;
        try {
            result.series = await fetchSeries(result.item);
            if (!result.series) result.error = 'not found';
            else compareCache.set(refId(result.item), result.series);
        } catch (error) {
            result.error = error.message;
        }
    }));
    if (state.view !== 'compare') return;
    renderList();

    try {
        await loadPlotly();
    } catch (error) {
        chart.innerHTML = errorMessage('Cannot load the charting library');
        return;
    }
    const theme = plotlyTheme();
    const traces = results.map(({ item, series }) => {
        const obs = series ? sortedObs(series) : [];
        return {
            name: item.title.length > 40 ? item.title.slice(0, 39) + '…' : item.title,
            x: obs.map(entry => periodStartOf(entry.period)),
            y: obs.map(entry => Number(entry.value)),
            text: obs.map(entry => formatPeriod(entry.period)),
            type: 'scatter',
            mode: 'lines',
            hovertemplate: '%{text}: %{y}<extra>%{fullData.name}</extra>'
        };
    });
    chart.innerHTML = '';
    Plotly.newPlot(chart, traces, {
        ...theme.layout,
        margin: { t: 10, r: 10, b: 30, l: 50 },
        legend: { orientation: 'h', y: -0.15 },
        xaxis: { ...theme.layout.xaxis, type: 'date' }
    }, theme.config);
}

function removeCompare(index) {
    const items = compareItems();
    items.splice(index, 1);
    saveStored('compare', items);
    renderCompare();
}

function clearCompare() {
    saveStored('compare', []);
    renderCompare();
}

function openComparedSeries(index) {
    const item = compareItems()[index];
    if (item) openDataset(item, { key: item.key, series: item.key });
}
