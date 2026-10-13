// ========================================================================
// COMPARE TAB FUNCTIONALITY
// ========================================================================

// Store series to compare
let comparisonSeries = [];

const COMPARE_STORAGE_KEY = 'sdmxdl-compare-series';
const COMPARE_SERIES_FIELDS = ['source', 'flow', 'key', 'database', 'label'];

/**
 * Get the default label of a series
 * @param {Object} series - Series with source, flow and key
 * @returns {string} - Default label
 */
function getDefaultSeriesLabel(series) {
    return `${series.source}/${series.flow}/${series.key}`;
}

/**
 * Replace the series to compare
 * @param {Object[]} list - Series with source, flow, key and optional database and label
 */
function setComparisonSeries(list) {
    const now = Date.now();
    comparisonSeries = list
        .filter(series => series && series.source && series.flow && series.key)
        .map((series, index) => ({
            id: now + index,
            source: series.source,
            flow: series.flow,
            key: series.key,
            database: series.database || '',
            label: series.label || getDefaultSeriesLabel(series)
        }));
}

/**
 * Encode a series as a 'series' URL parameter value: source/flow/key[/database[/label]]
 * @param {Object} series - Series to encode
 * @returns {string} - Parameter value
 */
function encodeComparisonSeries(series) {
    const parts = COMPARE_SERIES_FIELDS.map(field =>
        field === 'label' && series.label === getDefaultSeriesLabel(series) ? '' : (series[field] || ''));
    while (parts.length > 3 && !parts[parts.length - 1]) parts.pop();
    return parts.map(part => part.replace(/%/g, '%25').replace(/\//g, '%2F')).join('/');
}

/**
 * Decode a 'series' URL parameter value
 * @param {string} value - Parameter value
 * @returns {Object|null} - Series, or null if invalid
 */
function decodeComparisonSeries(value) {
    try {
        const parts = value.split('/').map(decodeURIComponent);
        return Object.fromEntries(COMPARE_SERIES_FIELDS.map((field, index) => [field, parts[index] || '']));
    } catch (error) {
        console.warn('Invalid series parameter:', value);
        return null;
    }
}

/**
 * Get the 'series' URL parameter values of the series to compare
 * @returns {string[]} - Parameter values
 */
function getComparisonSeriesParams() {
    return comparisonSeries.map(encodeComparisonSeries);
}

/**
 * Replace the series to compare by the ones of the 'series' URL parameters, if any
 * @param {URLSearchParams} urlParams - URL parameters
 */
function loadComparisonSeriesFromUrl(urlParams) {
    const values = urlParams.getAll('series');
    if (values.length === 0) return;
    setComparisonSeries(values.map(decodeComparisonSeries));
    saveComparisonSeries();
    renderComparisonSeriesList();
}

/**
 * Save the series to compare in the local storage
 */
function saveComparisonSeries() {
    try {
        localStorage.setItem(COMPARE_STORAGE_KEY, JSON.stringify(
            comparisonSeries.map(series => Object.fromEntries(COMPARE_SERIES_FIELDS.map(field => [field, series[field]])))));
    } catch (error) {
        console.warn('Could not save the series to compare:', error);
    }
}

/**
 * Restore the series to compare from the local storage, unless already set (e.g. from the URL), and render them
 */
function initComparisonSeries() {
    if (comparisonSeries.length === 0) {
        try {
            const stored = JSON.parse(localStorage.getItem(COMPARE_STORAGE_KEY) || '[]');
            if (Array.isArray(stored)) setComparisonSeries(stored);
        } catch (error) {
            console.warn('Could not restore the series to compare:', error);
        }
    }
    renderComparisonSeriesList();
}

/**
 * Save and render the series to compare after a change, and update the URL of the Compare tab
 */
function onComparisonSeriesChanged() {
    saveComparisonSeries();
    renderComparisonSeriesList();
    if (document.getElementById('compare-tab')?.classList.contains('active')) {
        updateUrlFromForm('compare', true);
    }
}

/**
 * Add a series to the comparison list
 */
function addSeriesToComparison() {
    const source = document.getElementById('compare-source').value.trim();
    const flow = document.getElementById('compare-flow').value.trim();
    const key = document.getElementById('compare-key').value.trim();
    const database = document.getElementById('compare-database').value.trim();
    const label = document.getElementById('compare-label').value.trim();

    if (!source || !flow || !key) {
        notify('Please enter source ID, flow ID, and key', 'warning');
        return;
    }

    const series = {
        id: Date.now(), // Unique ID for this series
        source,
        flow,
        key,
        database: database || '',
        label: label || getDefaultSeriesLabel({ source, flow, key })
    };

    comparisonSeries.push(series);

    // Clear the form
    document.getElementById('compare-source').value = '';
    document.getElementById('compare-flow').value = '';
    document.getElementById('compare-key').value = '';
    document.getElementById('compare-database').value = '';
    document.getElementById('compare-label').value = '';

    onComparisonSeriesChanged();

    // Enable compare button
    document.getElementById('compare-btn').disabled = false;
}

/**
 * Render the list of series to compare
 */
function renderComparisonSeriesList() {
    const listEl = document.getElementById('compare-series-list');

    if (comparisonSeries.length === 0) {
        listEl.innerHTML = placeholderMessage('No series added yet. Add series using the form below.');
        document.getElementById('compare-btn').disabled = true;
        return;
    }

    let html = '<div class="panel compare-series-panel">';
    html += `<h3>Series to Compare <span class="badge rounded-pill text-bg-primary">${comparisonSeries.length}</span></h3>`;
    html += '<ul class="list-group">';

    comparisonSeries.forEach(series => {
        html += `
            <li class="list-group-item d-flex justify-content-between align-items-center gap-2">
                <div class="flex-grow-1 text-break">
                    <div class="fw-semibold compare-series-label">${escapeHtml(series.label)}</div>
                    <div class="small text-body-secondary d-flex flex-wrap column-gap-3">
                        <span title="Source">${icon('hdd-stack')} ${escapeHtml(series.source)}</span>
                        <span title="Flow">${icon('collection')} ${escapeHtml(series.flow)}</span>
                        <span title="Key">${icon('key')} ${escapeHtml(series.key)}</span>
                        ${series.database ? `<span title="Database">${icon('database')} ${escapeHtml(series.database)}</span>` : ''}
                    </div>
                </div>
                <button class="btn btn-sm btn-outline-danger" type="button" onclick="removeSeriesFromComparison(${series.id})" title="Remove series" aria-label="Remove series">
                    ${icon('trash')}
                </button>
            </li>
        `;
    });

    html += '</ul></div>';
    listEl.innerHTML = html;
    document.getElementById('compare-btn').disabled = false;
}
/**
 * Remove a series from comparison
 */
function removeSeriesFromComparison(seriesId) {
    comparisonSeries = comparisonSeries.filter(s => s.id !== seriesId);
    onComparisonSeriesChanged();
}

/**
 * Clear all series from comparison
 */
function clearAllSeries() {
    if (comparisonSeries.length > 0 && !confirm('Clear all series from comparison?')) {
        return;
    }
    comparisonSeries = [];
    onComparisonSeriesChanged();
    document.getElementById('compare-chart-wrapper').style.display = 'none';
}

/**
 * Fetch and compare all series
 */
async function compareAllSeries() {
    if (comparisonSeries.length === 0) {
        notify('Please add at least one series to compare', 'warning');
        return;
    }

    const compareBtn = document.getElementById('compare-btn');
    const originalBtnText = compareBtn.innerHTML;
    compareBtn.disabled = true;
    compareBtn.innerHTML = '<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Loading...';

    const chartWrapper = document.getElementById('compare-chart-wrapper');
    chartWrapper.style.display = 'none';

    try {
        // Fetch all series data in parallel
        const promises = comparisonSeries.map(series =>
            fetchSeriesData(series.source, series.flow, series.key, series.database)
        );

        const results = await Promise.all(promises);

        // Combine all series into traces
        const allTraces = [];
        const allLabelsSet = new Set();
        const periodToDateMap = new Map();
        const periodLabels = new Map();
        const allIsoPeriods = [];

        results.forEach((data, index) => {
            const seriesLabel = comparisonSeries[index].label;

            if (data && data.data && Array.isArray(data.data)) {
                const displayNames = getSeriesDisplayNames(data.data);
                data.data.forEach((series, seriesIndex) => {
                    if (series.obs && Array.isArray(series.obs)) {
                        const dataMap = new Map();

                        series.obs.forEach(obs => {
                            // Extract start from ISO period format (start/duration)
                            let period = obs.period;
                            if (period) {
                                allIsoPeriods.push(period);
                                period = extractPeriodStart(period);
                                periodLabels.set(period, formatPeriod(obs.period));
                                const dateObj = parseToDate(period);
                                if (dateObj) {
                                    periodToDateMap.set(period, dateObj);
                                }
                            }
                            const value = parseFloat(obs.value);
                            if (!isNaN(value) && period) {
                                dataMap.set(period, value);
                                allLabelsSet.add(period);
                            }
                        });

                        if (dataMap.size > 0) {
                            // Use custom label plus series key
                            const traceName = data.data.length > 1
                                ? `${seriesLabel} - ${displayNames[seriesIndex]}`
                                : seriesLabel;

                            // Sort labels by date
                            const sortedLabels = Array.from(allLabelsSet).sort((a, b) => {
                                const dateA = periodToDateMap.get(a);
                                const dateB = periodToDateMap.get(b);
                                if (dateA && dateB) {
                                    return dateA - dateB;
                                }
                                return a.localeCompare(b);
                            });

                            const allParsed = sortedLabels.every(label => periodToDateMap.has(label));
                            const xValues = sortedLabels.map(label =>
                                allParsed && periodToDateMap.has(label)
                                    ? periodToDateMap.get(label)
                                    : label
                            );
                            const yValues = sortedLabels.map(label => dataMap.get(label) ?? null);

                            allTraces.push({
                                x: xValues,
                                y: yValues,
                                customdata: sortedLabels.map(label => periodLabels.get(label) || label),
                                mode: 'lines+markers',
                                name: escapeHtml(shortenLabel(traceName)),
                                connectgaps: true,
                                hovertemplate: '%{customdata}: %{y}<extra>' + escapePlotlyTemplate(series.key || traceName) + '</extra>'
                            });
                        }
                    }
                });
            }
        });

        if (allTraces.length === 0) {
            notify('No data available from any series', 'warning');
            return;
        }

        // Render comparison chart
        await renderComparisonChart(allTraces, getDateFormat(allIsoPeriods));
        chartWrapper.style.display = 'block';

    } catch (error) {
        notify('Error fetching data: ' + error.message, 'danger');
        console.error('Comparison error:', error);
    } finally {
        compareBtn.disabled = false;
        compareBtn.innerHTML = originalBtnText;
    }
}

/**
 * Fetch data for a single series
 */
async function fetchSeriesData(source, flow, key, database) {
    return getJSON(apiUrl([source, flow, 'data'], { key, database }));
}

/**
 * Render the comparison chart using Plotly
 */
async function renderComparisonChart(traces, dateFormat) {
    const chartDiv = document.getElementById('compare-chart');

    try {
        await loadPlotly();
    } catch (error) {
        chartDiv.innerHTML = errorMessage(error.message);
        console.error('Plotly could not be loaded:', error);
        return;
    }

    const useDateAxis = traces[0]?.x[0] instanceof Date;
    const theme = plotlyTheme();

    const layout = {
        ...theme.layout,
        title: {
            text: 'Time Series Comparison',
            font: { size: 16 }
        },
        xaxis: {
            ...theme.layout.xaxis,
            title: { text: 'Time Period', font: { size: 14 } },
            type: useDateAxis ? 'date' : 'category',
            tickangle: useDateAxis ? 0 : -45,
            tickfont: { size: 11 },
            showgrid: true,
            showline: true,
            rangeslider: {
                visible: true,
                bgcolor: 'rgba(128,128,128,0.08)',
                bordercolor: theme.layout.xaxis.linecolor,
                borderwidth: 1,
                thickness: 0.1
            },
            automargin: true
        },
        yaxis: {
            ...theme.layout.yaxis,
            title: { text: 'Value', font: { size: 14 } },
            showgrid: true,
            showline: true,
            zeroline: true
        },
        margin: { l: 60, r: 30, t: 60, b: useDateAxis ? 80 : 100 },
        legend: {
            orientation: 'v',
            x: 1.02,
            xanchor: 'left',
            y: 1,
            yanchor: 'top',
            bgcolor: 'rgba(0,0,0,0)'
        },
        hovermode: 'x unified'
    };

    if (useDateAxis) {
        layout.xaxis.tickformat = dateFormat;
        layout.xaxis.hoverformat = dateFormat;
    }

    try {
        Plotly.react(chartDiv, traces, layout, { ...theme.config, displayModeBar: true });

        // Force resize
        setTimeout(() => {
            try {
                Plotly.Plots.resize(chartDiv);
            } catch (e) {
                console.warn('Initial Plotly resize failed:', e);
            }
        }, 50);

        // Setup resize observer
        setupCompareChartResizeObserver();
    } catch (error) {
        chartDiv.innerHTML = errorMessage('Chart rendering failed: ' + error.message);
        console.error('Plotly rendering error:', error);
    }
}
/**
 * Setup resize observer for comparison chart
 */
function setupCompareChartResizeObserver() {
    const chartDiv = document.getElementById('compare-chart');
    const container = chartDiv?.parentElement;
    if (!chartDiv || !container || typeof Plotly === 'undefined') return;

    if (chartDiv._resizeObserver) {
        return;
    }

    let resizeTimeout = null;
    const triggerResize = () => {
        if (resizeTimeout) clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(() => {
            try {
                Plotly.Plots.resize(chartDiv);
            } catch (e) {
                console.warn('Plotly resize failed:', e);
            }
        }, 100);
    };

    try {
        const ro = new ResizeObserver(() => triggerResize());
        ro.observe(container);
        chartDiv._resizeObserver = ro;
    } catch (e) {
        console.warn('ResizeObserver not available:', e);
    }

    const handleWindowResize = () => triggerResize();
    window.addEventListener('resize', handleWindowResize, { passive: true });
    chartDiv._windowResizeHandler = handleWindowResize;
}
