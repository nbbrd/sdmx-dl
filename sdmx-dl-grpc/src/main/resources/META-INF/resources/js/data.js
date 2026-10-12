// ========================================================================
// DATA TAB FUNCTIONALITY
// ========================================================================

/**
 * Read the optional data request parameters of the Data tab
 * @returns {Object} - REST v2 query parameters (start, end, firstN, lastN, detail)
 */
function getDataOptions() {
    const value = id => document.getElementById(id).value.trim();
    return {
        start: value('data-start'),
        end: value('data-end'),
        firstN: value('data-firstn'),
        lastN: value('data-lastn'),
        detail: value('data-detail')
    };
}

/**
 * Fetch and display data series
 */
function getData() {
    const source = document.getElementById('data-source').value;
    const flow = document.getElementById('data-flow').value;
    const key = document.getElementById('data-key').value;
    const database = document.getElementById('data-database').value;

    if (!source || !flow || !key) {
        alert('Please enter source ID, flow ID, and key');
        return;
    }

    const options = getDataOptions();

    // Show loading and reset views
    const prettyView = document.getElementById('data-pretty-view');
    const copyDropdown = document.getElementById('data-copy-dropdown');

    prettyView.style.display = 'block';
    copyDropdown.style.display = 'none';

    const url = apiUrl([source, flow, 'data'], { key, database, ...options });

    getJSON(url)
    .then(data => {
        copyDropdown.style.display = 'inline-block';

        // Store response data and request params for copying
        window.dataResponseData = data;
        window.dataRequestUrl = url;
        window.dataRequestParams = { source, flow, key, database, ...options };

        // Render chart and table
        renderDataChart(data);
        renderDataTable(data);
    })
    .catch(error => {
        prettyView.style.display = 'none';
        alert(`Error fetching data: ${error.message}`);
    });
}

// Variables for chart data
let fullDatasets = null;
let allPeriodLabels = null;

/**
 * Extract start component from ISO period format "start/duration"
 * Examples: "2023-01/P1M" -> "2023-01", "2023-Q1/P3M" -> "2023-Q1"
 */
function extractPeriodStart(period) {
    if (!period) return null;

    // If period contains '/', it's in ISO format "start/duration"
    if (typeof period === 'string' && period.includes('/')) {
        return period.split('/')[0].trim();
    }

    // Otherwise return as-is
    return period;
}

/**
 * Format an ISO period "start/duration" according to its frequency
 * Examples: "2023-01-01T00:00:00/P1Y" -> "2023", "2023-04-01T00:00:00/P3M" -> "2023-Q2",
 * "2023-07-01T00:00:00/P6M" -> "2023-S2", "2023-01-01T00:00:00/P1M" -> "2023-01", "2023-01-02T00:00:00/P1D" -> "2023-01-02"
 * @param {string} period - ISO period
 * @returns {string} - Human-readable period
 */
function formatPeriod(period) {
    if (!period || typeof period !== 'string') return period;
    const [start, duration] = period.split('/');
    const match = start.match(/^(\d{4})-(\d{2})-(\d{2})(?:T(\d{2}):(\d{2})(?::(\d{2}))?)?/);
    if (!match) return start;
    const [, year, month, day, hours = '00', minutes = '00', seconds = '00'] = match;
    if (hours !== '00' || minutes !== '00' || seconds !== '00') {
        return start.replace('T', ' ');
    }
    const monthIndex = parseInt(month, 10) - 1;
    if (day === '01') {
        if (duration === 'P1Y' && monthIndex === 0) return year;
        if (duration === 'P6M' && monthIndex % 6 === 0) return `${year}-S${monthIndex / 6 + 1}`;
        if (duration === 'P3M' && monthIndex % 3 === 0) return `${year}-Q${monthIndex / 3 + 1}`;
        if (duration === 'P1M') return `${year}-${month}`;
    }
    return `${year}-${month}-${day}`;
}

/**
 * Get the Plotly date format that matches the frequency of a set of ISO periods
 * @param {string[]} periods - ISO periods
 * @returns {string} - d3 date format
 */
function getDateFormat(periods) {
    const durations = new Set(periods.map(p => (typeof p === 'string' && p.includes('/')) ? p.split('/')[1] : ''));
    if (durations.size === 1 && durations.has('P1Y')) return '%Y';
    if ([...durations].every(d => d === 'P1M' || d === 'P3M' || d === 'P6M')) return '%Y-%m';
    return '%Y-%m-%d';
}

/**
 * Get a display name for each series: its title when available and unique, its key otherwise
 * @param {Object[]} seriesList - Series with key and meta
 * @returns {string[]} - Display names, in the same order
 */
function getSeriesDisplayNames(seriesList) {
    const titles = seriesList.map(series => (series.meta && series.meta.TITLE) || '');
    const counts = new Map();
    titles.forEach(title => counts.set(title, (counts.get(title) || 0) + 1));
    return seriesList.map((series, index) => {
        const key = series.key || `Series ${index + 1}`;
        const title = titles[index];
        return title && counts.get(title) === 1 ? title : key;
    });
}

/**
 * Shorten a text for chart legends
 * @param {string} text - Text to shorten
 * @param {number} maxLength - Maximum length
 * @returns {string} - Shortened text
 */
function shortenLabel(text, maxLength = 60) {
    return text.length > maxLength ? text.substring(0, maxLength - 1) + '…' : text;
}

/**
 * Escape a text for use in a Plotly hover template
 * @param {string} text - Text to escape
 * @returns {string} - Escaped text
 */
function escapePlotlyTemplate(text) {
    return escapeHtml(text).replace(/%\{/g, '%&#123;');
}

/**
 * Parse period string to JavaScript Date
 * Supports: YYYY, YYYY-MM, YYYY-MM-DD, YYYY-Qn, YYYY-Wnn
 */
function parseToDate(periodStr) {
    if (!periodStr) return null;

    periodStr = periodStr.trim();

    // Annual: YYYY
    if (/^\d{4}$/.test(periodStr)) {
        return new Date(parseInt(periodStr), 0, 1);
    }

    // Quarterly: YYYY-Q[1-4]
    const qMatch = periodStr.match(/^(\d{4})-Q([1-4])$/);
    if (qMatch) {
        const year = parseInt(qMatch[1]);
        const quarter = parseInt(qMatch[2]);
        return new Date(year, (quarter - 1) * 3, 1);
    }

    // Monthly: YYYY-MM
    const mMatch = periodStr.match(/^(\d{4})-(\d{2})$/);
    if (mMatch) {
        return new Date(parseInt(mMatch[1]), parseInt(mMatch[2]) - 1, 1);
    }

    // Weekly: YYYY-W[01-53]
    const wMatch = periodStr.match(/^(\d{4})-W(\d{2})$/);
    if (wMatch) {
        const year = parseInt(wMatch[1]);
        const week = parseInt(wMatch[2]);
        const date = new Date(year, 0, 1);
        date.setDate(date.getDate() + (week - 1) * 7);
        return date;
    }

    // Daily: YYYY-MM-DD
    const dMatch = periodStr.match(/^(\d{4})-(\d{2})-(\d{2})$/);
    if (dMatch) {
        return new Date(parseInt(dMatch[1]), parseInt(dMatch[2]) - 1, parseInt(dMatch[3]));
    }

    // Try standard Date parsing
    const date = new Date(periodStr);
    return isNaN(date.getTime()) ? null : date;
}

function renderDataChart(data) {
    const chartDiv = document.getElementById('data-chart');

    // Parse SDMX data structure into Plotly traces
    const traces = [];
    const allLabelsSet = new Set();
    const periodToDateMap = new Map();

    if (data.data && Array.isArray(data.data)) {
        const seriesDataMaps = [];
        const periodLabels = new Map();
        const allIsoPeriods = [];
        const displayNames = getSeriesDisplayNames(data.data);
        data.data.forEach((series, index) => {
            if (series.obs && Array.isArray(series.obs)) {
                const dataMap = new Map();
                series.obs.forEach(obs => {
                    // Extract start from ISO period format (start/duration)
                    let period = obs.period;
                    if (period) {
                        allIsoPeriods.push(period);
                        period = extractPeriodStart(period);
                        periodLabels.set(period, formatPeriod(obs.period));

                        // Parse to date and store mapping
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
                    seriesDataMaps.push({ key: series.key || `Series ${index + 1}`, name: displayNames[index], dataMap });
                }
            }
        });

        // Sort labels by date
        const sortedLabels = Array.from(allLabelsSet).sort((a, b) => {
            const dateA = periodToDateMap.get(a);
            const dateB = periodToDateMap.get(b);
            if (dateA && dateB) {
                return dateA - dateB;
            }
            return a.localeCompare(b);
        });

        // Check if all periods were successfully parsed to dates
        const allParsed = sortedLabels.every(label => periodToDateMap.has(label));

        seriesDataMaps.forEach((seriesInfo) => {
            // Use Date objects if all periods parsed, otherwise use strings
            const xValues = sortedLabels.map(label =>
                allParsed && periodToDateMap.has(label)
                    ? periodToDateMap.get(label)
                    : label
            );
            const yValues = sortedLabels.map(label => seriesInfo.dataMap.get(label) ?? null);

            traces.push({
                x: xValues,
                y: yValues,
                customdata: sortedLabels.map(label => periodLabels.get(label) || label),
                mode: 'lines+markers',
                name: escapeHtml(shortenLabel(seriesInfo.name)),
                connectgaps: true,
                hovertemplate: '%{customdata}: %{y}<extra>' + escapePlotlyTemplate(seriesInfo.key) + '</extra>'
            });
        });

        fullDatasets = traces;
        allPeriodLabels = Array.from(sortedLabels);

        // Store whether using dates
        chartDiv.dataset.useDateAxis = allParsed ? 'true' : 'false';
        chartDiv.dataset.dateFormat = getDateFormat(allIsoPeriods);
    }

    if (!fullDatasets || fullDatasets.length === 0 || !allPeriodLabels || allPeriodLabels.length === 0) {
        chartDiv.innerHTML = '<div style="color:#666; text-align:center; padding-top: 40px;">No data available to display</div>';
        return;
    }


    // Create Plotly chart with rangeslider
    createPlotlyChart(data, allPeriodLabels, fullDatasets);
}

function createPlotlyChart(data, labels, traces) {
    const chartDiv = document.getElementById('data-chart');

    // Check if Plotly is loaded
    if (typeof Plotly === 'undefined') {
        chartDiv.innerHTML = '<div style="color:#cc0000; text-align:center; padding-top: 40px;">Error: Plotly library failed to load. Please refresh the page.</div>';
        console.error('Plotly is not defined. Make sure the Plotly script is loaded before calling this function.');
        return;
    }

    // Check if using date axis
    const useDateAxis = chartDiv.dataset.useDateAxis === 'true';

    const layout = {
        title: {
            text: escapeHtml(`${data.ref || 'Data'} - ${(data.query && data.query.key) ? data.query.key : ''}`),
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
            orientation: 'h',
            x: 0.5,
            xanchor: 'center',
            y: -0.2,
            bgcolor: 'rgba(255,255,255,0.8)',
            bordercolor: '#e0e0e0',
            borderwidth: 1
        },
        hovermode: 'x unified',
        plot_bgcolor: '#ffffff',
        paper_bgcolor: '#f8f9fa'
    };

    // Add date-specific formatting
    if (useDateAxis) {
        const dateFormat = chartDiv.dataset.dateFormat || '%Y-%m-%d';
        layout.xaxis.tickformat = dateFormat;
        layout.xaxis.hoverformat = dateFormat;
    }

    const config = { responsive: true, displayModeBar: true };

    try {
        Plotly.react(chartDiv, traces, layout, config);

        // Force immediate resize to ensure correct initial size
        setTimeout(() => {
            try {
                Plotly.Plots.resize(chartDiv);
            } catch (e) {
                console.warn('Initial Plotly resize failed:', e);
            }
        }, 50);

        // Setup resize observer for future size changes
        setupChartResizeObserver();
    } catch (error) {
        chartDiv.innerHTML = '<div style="color:#cc0000; text-align:center; padding-top: 40px;">Error rendering chart: ' + escapeHtml(error.message) + '</div>';
        console.error('Plotly rendering error:', error);
    }
}

function setupChartResizeObserver() {
    const chartDiv = document.getElementById('data-chart');
    const container = chartDiv?.parentElement; // .chart-container
    if (!chartDiv || !container || typeof Plotly === 'undefined') return;

    // Avoid duplicate observers
    if (chartDiv._resizeObserver) {
        return;
    }

    // Debounce to avoid excessive resize calls
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

    // Observe container size
    try {
        const ro = new ResizeObserver(() => triggerResize());
        ro.observe(container);
        // Store observer for cleanup if needed
        chartDiv._resizeObserver = ro;
    } catch (e) {
        console.warn('ResizeObserver not available:', e);
    }

    // Also listen to window resize
    const handleWindowResize = () => triggerResize();
    window.addEventListener('resize', handleWindowResize, { passive: true });
    chartDiv._windowResizeHandler = handleWindowResize;
}



function renderDataTable(data) {
    const tableContent = document.getElementById('table-content');

    if (!data.data || !Array.isArray(data.data) || data.data.length === 0) {
        tableContent.innerHTML = '<p style="text-align: center; color: #666; padding: 20px;">No data available to display</p>';
        return;
    }

    // Collect all unique periods across all series, and index observations by period start
    const periodLabels = new Map();
    const obsByPeriod = data.data.map(series => {
        const map = new Map();
        if (series.obs && Array.isArray(series.obs)) {
            series.obs.forEach(obs => {
                if (obs.period) {
                    const period = extractPeriodStart(obs.period);
                    periodLabels.set(period, formatPeriod(obs.period));
                    map.set(period, obs);
                }
            });
        }
        return map;
    });

    const sortedPeriods = Array.from(periodLabels.keys()).sort();

    // Create table structure
    let tableHTML = '<table class="data-table"><thead><tr>';
    tableHTML += '<th>Period</th>';

    // Add column for each series
    data.data.forEach((series, index) => {
        const seriesKey = series.key || `Series ${index + 1}`;
        const seriesTitle = (series.meta && series.meta.TITLE) || '';
        tableHTML += `<th title="${escapeHtml(seriesTitle)}">${escapeHtml(seriesKey)}`;
        if (seriesTitle) {
            tableHTML += `<div class="series-title">${escapeHtml(seriesTitle)}</div>`;
        }
        tableHTML += '</th>';
    });

    tableHTML += '</tr></thead><tbody>';

    // Create rows for each period
    sortedPeriods.forEach(period => {
        tableHTML += `<tr><td title="${escapeHtml(period)}"><strong>${escapeHtml(periodLabels.get(period))}</strong></td>`;

        // Add value for each series
        obsByPeriod.forEach(seriesObs => {
            let value = '-';
            const obs = seriesObs.get(period);
            if (obs && obs.value !== undefined && obs.value !== null) {
                // Format number with appropriate decimals
                const numValue = parseFloat(obs.value);
                if (!isNaN(numValue)) {
                    value = numValue.toLocaleString(undefined, {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 6
                    });
                }
            }
            tableHTML += `<td>${value}</td>`;
        });

        tableHTML += '</tr>';
    });

    tableHTML += '</tbody></table>';

    // Add summary information
    const totalObs = data.data.reduce((sum, series) => {
        return sum + (series.obs ? series.obs.length : 0);
    }, 0);

    tableHTML += `<div class="table-summary">`;
    tableHTML += `<strong>Summary:</strong> `;
    tableHTML += `${data.data.length} series, `;
    tableHTML += `${sortedPeriods.length} periods, `;
    tableHTML += `${totalObs} total observations`;
    tableHTML += `</div>`;

    tableContent.innerHTML = tableHTML;
}
