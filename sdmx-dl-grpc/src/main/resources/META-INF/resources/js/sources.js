// ========================================================================
// SOURCES TAB FUNCTIONALITY
// ========================================================================

function getSources() {
    const copyDropdown = document.getElementById('sources-copy-dropdown');
    const prettyView = document.getElementById('sources-pretty-view');

    // Show pretty view initially
    prettyView.style.display = 'block';
    copyDropdown.style.display = 'none';
    document.getElementById('sources-pretty-content').innerHTML = loadingMessage();

    const url = apiUrl(['sources']);

    getJSON(url)
    .then(data => {
        copyDropdown.style.display = 'inline-block';

        // Store response data and request URL for copying
        window.sourcesResponseData = data;
        window.sourcesRequestUrl = url;

        // Render pretty view
        renderPrettySources(itemsOf(data));

        // Setup filter functionality
        setupSourcesFilter();
    })
    .catch(error => {
        prettyView.style.display = 'none';
        notify(`Error fetching sources: ${error.message}`, 'danger');
    });
}

function renderPrettySources(sources) {
    const container = document.getElementById('sources-pretty-content');
    let html = '';


    // Advanced filter field
    html += '<div class="filter-box">';
    html += '<div class="filter-input-row">';
    html += '<div class="filter-input-container">';
    html += '<input type="text" class="form-control" id="sources-filter-input" placeholder="Filter sources by name, ID, endpoint, or driver..." />';
    html += '<div class="filter-inline-buttons">';
    html += '<button type="button" class="filter-clear-btn" id="sources-clear-filter" title="Clear Filter">✕</button>';
    html += '<button type="button" class="filter-inline-btn" id="sources-match-case" title="Match Case">Cc</button>';
    html += '<button type="button" class="filter-inline-btn" id="sources-whole-words" title="Whole Words">W</button>';
    html += '<button type="button" class="filter-inline-btn" id="sources-regex" title="Regex">.*</button>';
    html += '<button type="button" class="filter-inline-btn" id="sources-fuzzy" title="Fuzzy Search">≈</button>';
    html += '</div>';
    html += '</div>';
    html += '<div class="filter-actions">';
    html += '<span id="sources-filter-count" class="filter-count"></span>';
    html += '</div>';
    html += '</div>';
    html += '</div>';

    // Sources grid
    html += '<div class="sources-grid">';

    sources.forEach(source => {
        // Create filterable text for this source
        const filterText = [
            source.id,
            source.driver || '',
            source.endpoint || '',
            source.website || '',
            ...(source.names ? Object.values(source.names) : []),
            ...(source.aliases || [])
        ].join(' ').toLowerCase();

        html += `<div class="source-card" data-filter-text="${escapeHtml(filterText)}" onclick="toggleSourceActions(this)">`;

        // Header with icon and title
        html += '<div class="source-header">';

        // Extract domain from website and use DuckDuckGo favicon
        // Only show favicon if confidentiality is PUBLIC or null/undefined
        let faviconHtml = icon('globe'); // Default icon fallback
        const isPublicOrNull = !source.confidentiality || source.confidentiality === 'PUBLIC';
        if (source.website && isPublicOrNull) {
            try {
                const url = new URL(source.website);
                const domain = url.hostname;
                const faviconUrl = `https://icons.duckduckgo.com/ip3/${domain}.ico`;
                faviconHtml = `<img src="${faviconUrl}" alt="${escapeHtml(source.id)}" onerror="this.parentElement.innerHTML='<i class=\\'bi bi-globe\\'></i>';" width="32" height="32">`;
            } catch (e) {
                // Keep default icon if URL parsing fails
            }
        }

        html += `<div class="source-icon">${faviconHtml}</div>`;
        html += '<div class="source-title">';
        html += `<h4 class="source-id">${escapeHtml(source.id)}</h4>`;

        // Display the first name if available
        if (source.names && Object.keys(source.names).length > 0) {
            const firstName = Object.values(source.names)[0];
            html += `<p class="source-name">${escapeHtml(firstName)}</p>`;
        }

        html += '</div>';
        html += `<span class="health-badge" data-source="${escapeHtml(source.id)}" style="display: none;"></span>`;
        html += '</div>';

        // Source details
        html += '<div class="source-details">';

        // Driver
        if (source.driver) {
            html += '<div class="source-detail-item">';
            html += '<span class="source-detail-label">Driver:</span>';
            html += `<span class="source-detail-value"><code>${escapeHtml(source.driver)}</code></span>`;
            html += '</div>';
        }

        // Endpoint
        if (source.endpoint) {
            html += '<div class="source-detail-item">';
            html += '<span class="source-detail-label">Endpoint:</span>';
            html += `<span class="source-detail-value source-endpoint">${escapeHtml(source.endpoint)}</span>`;
            html += '</div>';
        }

        // Confidentiality
        if (source.confidentiality) {
            html += '<div class="source-detail-item">';
            html += '<span class="source-detail-label">Access:</span>';
            const confidentialityClass = source.confidentiality === 'PUBLIC' ? 'text-success' : 'text-warning-emphasis';
            html += `<span class="source-detail-value fw-semibold ${confidentialityClass}">${escapeHtml(source.confidentiality)}</span>`;
            html += '</div>';
        }

        // Website
        if (source.website) {
            html += '<div class="source-detail-item">';
            html += '<span class="source-detail-label">Website:</span>';
            html += `<span class="source-detail-value"><a href="${escapeHtml(source.website)}" target="_blank" rel="noopener">${icon('link-45deg')} Visit</a></span>`;
            html += '</div>';
        }

        // Monitor
        if (source.monitorWebsite) {
            html += '<div class="source-detail-item">';
            html += '<span class="source-detail-label">Monitor:</span>';
            html += `<span class="source-detail-value"><a href="${escapeHtml(source.monitorWebsite)}" target="_blank" rel="noopener">${icon('link-45deg')} Status</a></span>`;
            html += '</div>';
        }

        // Aliases
        if (source.aliases && source.aliases.length > 0) {
            html += '<div class="source-detail-item">';
            html += '<span class="source-detail-label">Aliases:</span>';
            html += `<span class="source-detail-value">${source.aliases.map(a => escapeHtml(a)).join(', ')}</span>`;
            html += '</div>';
        }

        // Languages
        if (source.names && Object.keys(source.names).length > 0) {
            html += '<div class="source-detail-item">';
            html += '<span class="source-detail-label">Languages:</span>';
            html += '<span class="source-detail-value">';
            const languages = Object.keys(source.names).map(lang => `<code class="lang-badge">${escapeHtml(lang)}</code>`).join(' ');
            html += languages;
            html += '</span>';
            html += '</div>';
        }

        html += '</div>'; // Close source-details

        // Properties section
        if (source.properties && Object.keys(source.properties).length > 0) {
            html += '<div class="source-properties">';
            html += '<div class="source-properties-title">Properties</div>';

            Object.entries(source.properties).forEach(([key, value]) => {
                html += '<div class="source-property">';
                html += `<span class="source-property-key">${escapeHtml(key)}</span>`;
                html += `<span class="source-property-value">${escapeHtml(value)}</span>`;
                html += '</div>';
            });

            html += '</div>';
        }

        // Click hint
        html += '<div class="source-click-hint">Click card to show actions</div>';

        // Action buttons section (hidden by default)
        html += '<div class="source-actions">';
        html += '<div class="source-actions-title">Use in Other Tabs</div>';
        html += '<div class="source-action-buttons">';
        html += `<button class="btn btn-sm btn-outline-primary source-action-btn" type="button" onclick="event.stopPropagation(); useSourceInTab(${jsArg(source.id)}, 'databases')" title="Get databases for this source"><i class="bi bi-folder"></i> Databases</button>`;
        html += `<button class="btn btn-sm btn-outline-primary source-action-btn" type="button" onclick="event.stopPropagation(); useSourceInTab(${jsArg(source.id)}, 'flows')" title="Get flows for this source"><i class="bi bi-graph-up"></i> Flows</button>`;
        html += `<button class="btn btn-sm btn-outline-primary source-action-btn" type="button" onclick="event.stopPropagation(); useSourceInTab(${jsArg(source.id)}, 'meta')" title="Get meta for this source"><i class="bi bi-info-circle"></i> Meta</button>`;
        html += `<button class="btn btn-sm btn-outline-primary source-action-btn" type="button" onclick="event.stopPropagation(); useSourceInTab(${jsArg(source.id)}, 'keybuilder')" title="Use in key builder"><i class="bi bi-key"></i> Key Builder</button>`;
        html += `<button class="btn btn-sm btn-outline-primary source-action-btn" type="button" onclick="event.stopPropagation(); useSourceInTab(${jsArg(source.id)}, 'data')" title="Use in data tab"><i class="bi bi-bar-chart-line"></i> Data</button>`;
        html += `<button class="btn btn-sm btn-outline-primary source-action-btn" type="button" onclick="event.stopPropagation(); useSourceInTab(${jsArg(source.id)}, 'compare')" title="Use in compare tab"><i class="bi bi-arrow-left-right"></i> Compare</button>`;
        html += `<button class="btn btn-sm btn-outline-primary source-action-btn" type="button" onclick="event.stopPropagation(); checkSourceHealth(${jsArg(source.id)}, this)" title="Check monitor status and live access"><i class="bi bi-heart-pulse"></i> Health</button>`;
        html += '</div>';
        html += '</div>';

        html += '</div>'; // Close source-card
    });

    html += '</div>'; // Close sources-grid

    container.innerHTML = html;
    healthReports.forEach(showHealthReport);
}

// ========================================================================
// SOURCE HEALTH
// ========================================================================

const healthReports = new Map(); // source id -> last health report, kept across re-renders

const HEALTH_CLASSES = {
    OK: 'health-ok',
    DEGRADED: 'health-warn',
    LOCAL_ISSUE: 'health-warn',
    REMOTE_OUTAGE: 'health-error',
    UNREACHABLE: 'health-error'
};

/**
 * Describe a health report as plain text, for tooltips
 * @param {Object} report - HealthReportDto
 * @returns {string} - Multi-line description
 */
function describeHealthReport(report) {
    const lines = [`Verdict: ${report.verdict || 'UNKNOWN'}`];
    if (report.monitor) {
        let line = `Monitor: ${report.monitor.status || 'UNKNOWN'}`;
        if (report.monitor.uptimeRatio !== undefined) {
            line += `, uptime ${(report.monitor.uptimeRatio * 100).toFixed(2)}%`;
        }
        if (report.monitor.averageResponseTime !== undefined) {
            line += `, avg ${report.monitor.averageResponseTime} ms`;
        }
        lines.push(line);
    }
    if (report.monitorError) {
        lines.push(`Monitor error: ${report.monitorError}`);
    }
    if (report.access) {
        let line = `Access: ${report.access.accessible ? 'accessible' : (report.access.reachable ? 'reachable but failing' : 'unreachable')}`;
        if (report.access.duration !== undefined) {
            line += ` in ${report.access.duration} ms`;
        }
        if (report.access.statusCode !== undefined) {
            line += `, HTTP ${report.access.statusCode}`;
        }
        lines.push(line);
        if (report.access.errorMessage) {
            lines.push(`Access error: ${report.access.errorMessage}`);
        }
    }
    return lines.join('\n');
}

/**
 * Update the health badge of a source card
 * @param {string} sourceId - Source ID
 * @param {string} text - Badge text
 * @param {string} cssClass - Badge CSS class
 * @param {string} tooltip - Badge tooltip
 */
function setHealthBadge(sourceId, text, cssClass, tooltip) {
    document.querySelectorAll(`.health-badge[data-source="${CSS.escape(sourceId)}"]`).forEach(badge => {
        badge.textContent = text;
        badge.className = `health-badge ${cssClass || ''}`;
        badge.title = tooltip || '';
        badge.style.display = 'inline-block';
    });
}

/**
 * Display a health report on its source card
 * @param {Object} report - HealthReportDto
 */
function showHealthReport(report) {
    healthReports.set(report.source, report);
    const verdict = report.verdict || 'UNKNOWN';
    setHealthBadge(report.source, verdict.replace('_', ' '), HEALTH_CLASSES[verdict], describeHealthReport(report));
}

/**
 * Check the health of all sources using their third-party monitor
 */
async function checkAllSourcesHealth() {
    const btn = document.getElementById('sources-health-btn');
    const originalHTML = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<i class="bi bi-hourglass-split"></i> Checking...';

    document.querySelectorAll('.health-badge').forEach(badge => {
        setHealthBadge(badge.dataset.source, '...', 'health-pending', 'Checking...');
    });

    try {
        const reports = await getJSON(apiUrl(['health'], { sources: 'all', checks: 'monitor' }));
        itemsOf(reports).forEach(showHealthReport);
    } catch (error) {
        notify('Failed to check health: ' + error.message, 'danger');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalHTML;
    }
}

/**
 * Check the health of a single source using its monitor and a live request
 * @param {string} sourceId - Source ID
 * @param {HTMLElement} btn - Button that triggered the check
 */
async function checkSourceHealth(sourceId, btn) {
    const originalHTML = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<i class="bi bi-hourglass-split"></i> Checking...';
    setHealthBadge(sourceId, '...', 'health-pending', 'Checking...');

    try {
        showHealthReport(await getJSON(apiUrl([sourceId, 'health'], { checks: 'monitor,access' })));
    } catch (error) {
        setHealthBadge(sourceId, 'ERROR', 'health-error', error.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalHTML;
    }
}

function toggleSourceActions(cardElement) {
    // Close all other expanded cards
    const allCards = document.querySelectorAll('.source-card');
    allCards.forEach(card => {
        if (card !== cardElement) {
            card.classList.remove('expanded');
        }
    });

    // Toggle the clicked card
    cardElement.classList.toggle('expanded');
}

function useSourceInTab(sourceId, tabName) {
    // Fill the source field in the target tab
    switch(tabName) {
        case 'databases':
            document.getElementById('db-source').value = sourceId;
            switchTab('databases');
            break;
        case 'flows':
            document.getElementById('flows-source').value = sourceId;
            switchTab('flows');
            break;
        case 'meta':
            document.getElementById('meta-source').value = sourceId;
            switchTab('meta');
            break;
        case 'keybuilder':
            document.getElementById('kb-source').value = sourceId;
            switchTab('keybuilder');
            break;
        case 'data':
            document.getElementById('data-source').value = sourceId;
            switchTab('data');
            break;
        case 'compare':
            document.getElementById('compare-source').value = sourceId;
            switchTab('compare');
            break;
    }

    flashTab(tabName);
}

function setupSourcesFilter() {
    const filterInput = document.getElementById('sources-filter-input');
    const filterCount = document.getElementById('sources-filter-count');
    const clearBtn = document.getElementById('sources-clear-filter');
    const matchCaseBtn = document.getElementById('sources-match-case');
    const wholeWordsBtn = document.getElementById('sources-whole-words');
    const regexBtn = document.getElementById('sources-regex');
    const fuzzyBtn = document.getElementById('sources-fuzzy');

    function performFilter() {
        const query = filterInput.value.trim();
        const cards = document.querySelectorAll('.source-card');

        if (!query) {
            cards.forEach(card => card.classList.remove('filtered-hidden'));
            const total = cards.length;
            filterCount.textContent = `${total} sources`;
            return;
        }

        let visibleCount = 0;
        const matchCase = matchCaseBtn.classList.contains('active');
        const wholeWords = wholeWordsBtn.classList.contains('active');
        const isRegex = regexBtn.classList.contains('active');
        const isFuzzy = fuzzyBtn.classList.contains('active');

        cards.forEach(card => {
            const filterText = card.getAttribute('data-filter-text');

            const matches = matchesFilter(filterText, query, {
                matchCase,
                wholeWords,
                isRegex,
                isFuzzy
            });

            if (matches) {
                card.classList.remove('filtered-hidden');
                visibleCount++;
            } else {
                card.classList.add('filtered-hidden');
            }
        });

        const total = cards.length;
        filterCount.textContent = `${visibleCount} of ${total}`;
    }

    filterInput.addEventListener('input', performFilter);

    matchCaseBtn.addEventListener('click', function() {
        this.classList.toggle('active');
        performFilter();
    });

    wholeWordsBtn.addEventListener('click', function() {
        this.classList.toggle('active');
        performFilter();
    });

    regexBtn.addEventListener('click', function() {
        this.classList.toggle('active');
        performFilter();
    });

    fuzzyBtn.addEventListener('click', function() {
        this.classList.toggle('active');
        performFilter();
    });

    clearBtn.addEventListener('click', function() {
        filterInput.value = '';
        matchCaseBtn.classList.remove('active');
        wholeWordsBtn.classList.remove('active');
        regexBtn.classList.remove('active');
        fuzzyBtn.classList.remove('active');
        performFilter();
    });

    filterInput.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            clearBtn.click();
        }
    });

    // Initialize the filter count display
    performFilter();
}
