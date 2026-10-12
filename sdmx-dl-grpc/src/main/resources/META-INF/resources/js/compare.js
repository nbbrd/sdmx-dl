// ========================================================================
// COMPARE TAB FUNCTIONALITY
// ========================================================================

// Store series to compare
let comparisonSeries = [];

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
        alert('Please enter source ID, flow ID, and key');
        return;
    }

    const series = {
        id: Date.now(), // Unique ID for this series
        source,
        flow,
        key,
        database: database || '',
        label: label || `${source}/${flow}/${key}`
    };

    comparisonSeries.push(series);
    renderComparisonSeriesList();

    // Clear the form
    document.getElementById('compare-source').value = '';
    document.getElementById('compare-flow').value = '';
    document.getElementById('compare-key').value = '';
    document.getElementById('compare-database').value = '';
    document.getElementById('compare-label').value = '';

    // Enable compare button
    document.getElementById('compare-btn').disabled = false;
}

/**
 * Render the list of series to compare
 */
function renderComparisonSeriesList() {
    const listEl = document.getElementById('compare-series-list');

    if (comparisonSeries.length === 0) {
        listEl.innerHTML = '<p style="color: #666; text-align: center; padding: 20px;">No series added yet. Add series using the form below.</p>';
        document.getElementById('compare-btn').disabled = true;
        return;
    }

    let html = '<div style="background: white; border: 2px solid #e0e0e0; border-radius: 6px; padding: 15px;">';
    html += '<h3 style="margin: 0 0 15px 0; color: #003D6D; font-size: 1.1em;">Series to Compare (' + comparisonSeries.length + ')</h3>';

    comparisonSeries.forEach((series, index) => {
        html += `
            <div style="background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 4px; padding: 12px; margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center;">
                <div style="flex: 1;">
                    <div style="font-weight: 600; color: #003D6D; margin-bottom: 4px;">${escapeHtml(series.label)}</div>
                    <div style="font-size: 0.85em; color: #666;">
                        <span class="material-symbols-outlined" style="font-size: 14px;">source</span> ${escapeHtml(series.source)} &nbsp;
                        <span class="material-symbols-outlined" style="font-size: 14px;">data_object</span> ${escapeHtml(series.flow)} &nbsp;
                        <span class="material-symbols-outlined" style="font-size: 14px;">key</span> ${escapeHtml(series.key)}
                        ${series.database ? ` &nbsp;<span class="material-symbols-outlined" style="font-size: 14px;">database</span> ${escapeHtml(series.database)}` : ''}
                    </div>
                </div>
                <button onclick="removeSeriesFromComparison(${series.id})" style="background: #dc3545; border-color: #dc3545; padding: 8px 16px; min-width: auto;">
                    <span class="material-symbols-outlined">delete</span>
                </button>
            </div>
        `;
    });

    html += '</div>';
    listEl.innerHTML = html;
}

/**
 * Remove a series from comparison
 */
function removeSeriesFromComparison(seriesId) {
    comparisonSeries = comparisonSeries.filter(s => s.id !== seriesId);
    renderComparisonSeriesList();
}

/**
 * Clear all series from comparison
 */
function clearAllSeries() {
    if (comparisonSeries.length > 0 && !confirm('Clear all series from comparison?')) {
        return;
    }
    comparisonSeries = [];
    renderComparisonSeriesList();
    document.getElementById('compare-chart-wrapper').style.display = 'none';
}

/**
 * Fetch and compare all series
 */
async function compareAllSeries() {
    if (comparisonSeries.length === 0) {
        alert('Please add at least one series to compare');
        return;
    }

    const compareBtn = document.getElementById('compare-btn');
    const originalBtnText = compareBtn.innerHTML;
    compareBtn.disabled = true;
    compareBtn.innerHTML = '<span class="material-symbols-outlined">hourglass_empty</span> Loading...';

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
            alert('No data available from any series');
            return;
        }

        // Render comparison chart
        renderComparisonChart(allTraces, getDateFormat(allIsoPeriods));
        chartWrapper.style.display = 'block';

    } catch (error) {
        alert('Error fetching data: ' + error.message);
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
function renderComparisonChart(traces, dateFormat) {
    const chartDiv = document.getElementById('compare-chart');

    if (typeof Plotly === 'undefined') {
        chartDiv.innerHTML = '<div style="color:#cc0000; text-align:center; padding-top: 40px;">Error: Plotly library failed to load. Please refresh the page.</div>';
        console.error('Plotly is not defined');
        return;
    }

    // Check if using date axis
    const allLabels = new Set();
    traces.forEach(trace => {
        trace.x.forEach(x => {
            if (x instanceof Date) {
                allLabels.add(x.toISOString());
            } else {
                allLabels.add(x);
            }
        });
    });

    const useDateAxis = traces[0]?.x[0] instanceof Date;

    const layout = {
        title: {
            text: 'Time Series Comparison',
            font: { size: 16, color: '#003D6D' }
        },
        xaxis: {
            title: {
                text: 'Time Period',
                font: { size: 14, color: '#003D6D' }
            },
            type: useDateAxis ? 'date' : 'category',
            tickangle: useDateAxis ? 0 : -45,
            tickfont: { size: 11 },
            showgrid: true,
            gridcolor: '#e0e0e0',
            gridwidth: 1,
            showline: true,
            linecolor: '#003D6D',
            linewidth: 2,
            rangeslider: {
                visible: true,
                bgcolor: '#f8f9fa',
                bordercolor: '#003D6D',
                borderwidth: 1,
                thickness: 0.1
            },
            automargin: true
        },
        yaxis: {
            title: {
                text: 'Value',
                font: { size: 14, color: '#003D6D' }
            },
            showgrid: true,
            gridcolor: '#e0e0e0',
            gridwidth: 1,
            showline: true,
            linecolor: '#003D6D',
            linewidth: 2,
            zeroline: true,
            zerolinecolor: '#999',
            zerolinewidth: 1
        },
        margin: { l: 60, r: 30, t: 60, b: useDateAxis ? 80 : 100 },
        legend: {
            orientation: 'v',
            x: 1.02,
            xanchor: 'left',
            y: 1,
            yanchor: 'top',
            bgcolor: 'rgba(255,255,255,0.9)',
            bordercolor: '#e0e0e0',
            borderwidth: 1
        },
        hovermode: 'x unified',
        plot_bgcolor: '#ffffff',
        paper_bgcolor: '#f8f9fa'
    };

    if (useDateAxis) {
        layout.xaxis.tickformat = dateFormat;
        layout.xaxis.hoverformat = dateFormat;
    }

    const config = { responsive: true, displayModeBar: true };

    try {
        Plotly.react(chartDiv, traces, layout, config);

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
        chartDiv.innerHTML = '<div style="color:#cc0000; text-align:center; padding-top: 40px;">Error rendering chart: ' + escapeHtml(error.message) + '</div>';
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
