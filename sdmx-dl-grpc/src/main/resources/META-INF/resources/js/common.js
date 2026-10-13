const API_BASE = '/sdmx-dl/v2';

// ========================================================================
// UTILITY FUNCTIONS
// ========================================================================

/**
 * Escape HTML special characters to prevent XSS, in both text and attribute values
 * @param {string} text - Text to escape
 * @returns {string} - Escaped text
 */
function escapeHtml(text) {
    if (text === undefined || text === null) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

/**
 * Encode a value as a JavaScript string literal usable in an inline event handler attribute,
 * e.g. onclick="doSomething(${jsArg(value)})"
 * @param {string} value - Value to encode
 * @returns {string} - Escaped string literal
 */
function jsArg(value) {
    return escapeHtml(JSON.stringify(value === undefined || value === null ? '' : String(value)));
}

// ========================================================================
// UI HELPERS
// ========================================================================

/**
 * Build the HTML of a Bootstrap icon
 * @param {string} name - Icon name without the 'bi-' prefix (e.g. 'search')
 * @param {string} extraClass - Optional additional CSS classes
 * @returns {string} - HTML of the icon
 */
function icon(name, extraClass = '') {
    return `<i class="bi bi-${escapeHtml(name)}${extraClass ? ' ' + escapeHtml(extraClass) : ''}" aria-hidden="true"></i>`;
}

/**
 * Build the HTML of an error message
 * @param {string} message - Error message (escaped)
 * @returns {string} - HTML of the message
 */
function errorMessage(message) {
    return `<div class="alert alert-danger my-3" role="alert">${icon('exclamation-circle')} Error: ${escapeHtml(message)}</div>`;
}

/**
 * Build the HTML of a neutral placeholder message (no data, loading, ...)
 * @param {string} message - Message (escaped)
 * @returns {string} - HTML of the message
 */
function placeholderMessage(message) {
    return `<div class="placeholder-message">${escapeHtml(message)}</div>`;
}

/**
 * Build the HTML of a loading indicator
 * @param {string} message - Message (escaped)
 * @returns {string} - HTML of the indicator
 */
function loadingMessage(message = 'Loading...') {
    return `<div class="placeholder-message"><span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span><span role="status">${escapeHtml(message)}</span></div>`;
}

/**
 * Show a short notification at the bottom of the page
 * @param {string} message - Message to display
 * @param {string} variant - 'success', 'danger', 'warning' or 'info'
 */
function notify(message, variant = 'success') {
    const container = document.getElementById('toast-container');
    if (!container || typeof bootstrap === 'undefined') {
        console.log(message);
        return;
    }
    const icons = { success: 'check-circle', danger: 'exclamation-circle', warning: 'exclamation-triangle', info: 'info-circle' };
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-bg-${variant} border-0`;
    toast.setAttribute('role', variant === 'danger' ? 'alert' : 'status');
    toast.setAttribute('aria-live', 'polite');
    toast.innerHTML = `<div class="d-flex"><div class="toast-body">${icon(icons[variant] || 'info-circle')} ${escapeHtml(message)}</div>`
        + `<button type="button" class="btn-close${variant === 'warning' ? '' : ' btn-close-white'} me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button></div>`;
    container.appendChild(toast);
    toast.addEventListener('hidden.bs.toast', () => toast.remove());
    bootstrap.Toast.getOrCreateInstance(toast, { delay: variant === 'danger' ? 6000 : 2500 }).show();
}

/**
 * Briefly highlight a tab button, e.g. after sending it some values
 * @param {string} tabName - Name of the tab
 */
function flashTab(tabName) {
    const tab = document.querySelector(`.tab[data-tab="${tabName}"]`);
    if (!tab) return;
    tab.classList.remove('tab-flash');
    void tab.offsetWidth;
    tab.classList.add('tab-flash');
    setTimeout(() => tab.classList.remove('tab-flash'), 1000);
}

/**
 * Close the Bootstrap dropdown containing the given element, if any
 * @param {Element|string} elementOrId - Element (or its id) inside or wrapping the dropdown
 */
function closeDropdown(elementOrId) {
    const element = typeof elementOrId === 'string' ? document.getElementById(elementOrId) : elementOrId;
    if (!element || typeof bootstrap === 'undefined') return;
    const toggle = element.querySelector('[data-bs-toggle="dropdown"]')
        || element.closest('.dropdown, .dropup')?.querySelector('[data-bs-toggle="dropdown"]');
    if (toggle) {
        bootstrap.Dropdown.getOrCreateInstance(toggle).hide();
    }
}

let plotlyPromise = null;

/**
 * Load the bundled Plotly library on first use
 * @returns {Promise<Object>} - The Plotly global object
 */
function loadPlotly() {
    if (typeof Plotly !== 'undefined') {
        return Promise.resolve(Plotly);
    }
    if (!plotlyPromise) {
        plotlyPromise = new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.src = '/webjars/plotly.js-dist-min/plotly.min.js';
            script.onload = () => resolve(Plotly);
            script.onerror = () => {
                plotlyPromise = null;
                reject(new Error('Failed to load the charting library'));
            };
            document.head.appendChild(script);
        });
    }
    return plotlyPromise;
}

/**
 * Plotly layout and config matching the current color theme
 * @returns {Object} - { layout, config } to merge into chart options
 */
function plotlyTheme() {
    const style = getComputedStyle(document.documentElement);
    const text = style.getPropertyValue('--bs-body-color').trim() || '#1a1a1a';
    const grid = style.getPropertyValue('--bs-border-color').trim() || '#dee2e6';
    const muted = style.getPropertyValue('--bs-secondary-color').trim() || '#6c757d';
    const primary = style.getPropertyValue('--bs-primary').trim() || '#004495';
    const dark = document.documentElement.getAttribute('data-bs-theme') === 'dark';
    const colorway = dark
        ? ['#5b9bd5', '#ec5f95', '#a3c76a', '#f2cd5c', '#62bdf3', '#a8c7e8', '#b39ddb', '#f4a26b']
        : ['#004495', '#db2269', '#89af44', '#ecbd29', '#29a3ee', '#002d5a', '#7b5ea7', '#e8743b'];
    return {
        layout: {
            colorway,
            paper_bgcolor: 'rgba(0,0,0,0)',
            plot_bgcolor: 'rgba(0,0,0,0)',
            font: { family: 'system-ui, -apple-system, "Segoe UI", Roboto, sans-serif', color: text },
            xaxis: { gridcolor: grid, linecolor: grid, zerolinecolor: grid },
            yaxis: { gridcolor: grid, linecolor: grid, zerolinecolor: grid },
            modebar: { bgcolor: 'rgba(0,0,0,0)', color: muted, activecolor: primary }
        },
        config: {
            responsive: true,
            displaylogo: false,
            showSendToCloud: false
        }
    };
}

/**
 * Apply the current color theme to all rendered Plotly charts
 */
function restylePlotlyCharts() {
    if (typeof Plotly === 'undefined') return;
    const { layout } = plotlyTheme();
    document.querySelectorAll('.js-plotly-plot').forEach(chart => {
        Plotly.relayout(chart, {
            colorway: layout.colorway,
            'font.color': layout.font.color,
            'xaxis.gridcolor': layout.xaxis.gridcolor,
            'xaxis.linecolor': layout.xaxis.linecolor,
            'xaxis.zerolinecolor': layout.xaxis.zerolinecolor,
            'yaxis.gridcolor': layout.yaxis.gridcolor,
            'yaxis.linecolor': layout.yaxis.linecolor,
            'yaxis.zerolinecolor': layout.yaxis.zerolinecolor,
            'modebar.color': layout.modebar.color,
            'modebar.activecolor': layout.modebar.activecolor
        });
    });
}

document.addEventListener('themechange', restylePlotlyCharts);

// ========================================================================
// VIEW CONTROL FUNCTIONS
// ========================================================================

/**
 * Show URL parameters help modal
 */
function showUrlParamsHelp() {
    const modal = document.getElementById('url-params-modal');
    if (modal && typeof bootstrap !== 'undefined') {
        closeDropdown('view-menu');
        bootstrap.Modal.getOrCreateInstance(modal).show();
    }
}

/**
 * Close URL parameters help modal
 */
function closeUrlParamsHelp() {
    const modal = document.getElementById('url-params-modal');
    if (modal && typeof bootstrap !== 'undefined') {
        bootstrap.Modal.getOrCreateInstance(modal).hide();
    }
}

/**
 * Toggle header visibility
 */
function toggleHeader() {
    const container = document.querySelector('.container');
    const button = document.getElementById('toggle-header-btn');
    const url = new URL(window.location);

    if (container.classList.contains('hide-header')) {
        // Show header
        container.classList.remove('hide-header');
        button.classList.remove('active');
        url.searchParams.delete('hideHeader');
    } else {
        // Hide header
        container.classList.add('hide-header');
        button.classList.add('active');
        url.searchParams.set('hideHeader', 'true');
    }

    // Update URL without page reload
    window.history.pushState({}, '', url);
}

/**
 * Toggle other tabs visibility
 */
function toggleOtherTabs() {
    const tabsContainer = document.querySelector('.tabs');
    const button = document.getElementById('toggle-tabs-btn');
    const url = new URL(window.location);

    if (tabsContainer.classList.contains('hide-others')) {
        // Show all tabs
        tabsContainer.classList.remove('hide-others');
        button.classList.remove('active');
        url.searchParams.delete('hideOtherTabs');
    } else {
        // Hide other tabs
        tabsContainer.classList.add('hide-others');
        button.classList.add('active');
        url.searchParams.set('hideOtherTabs', 'true');
    }

    // Update URL without page reload
    window.history.pushState({}, '', url);
}

/**
 * Initialize button states based on current URL parameters
 */
function initializeViewControls() {
    const urlParams = new URLSearchParams(window.location.search);
    const hideHeader = urlParams.get('hideHeader') === 'true';
    const hideOtherTabs = urlParams.get('hideOtherTabs') === 'true';

    const headerBtn = document.getElementById('toggle-header-btn');
    const tabsBtn = document.getElementById('toggle-tabs-btn');

    if (hideHeader && headerBtn) {
        headerBtn.classList.add('active');
    }

    if (hideOtherTabs && tabsBtn) {
        tabsBtn.classList.add('active');
    }
}

/**
 * Build an absolute REST v2 URL from path segments and query parameters
 * @param {Array<string>} segments - Path segments (encoded individually)
 * @param {Object} params - Query parameters; empty values are skipped, arrays are repeated
 * @param {string} suffix - Optional raw suffix appended to the last segment (e.g. ':script')
 * @returns {string} - Absolute URL
 */
function apiUrl(segments, params = {}, suffix = '') {
    const path = segments.map(segment => encodeURIComponent(segment)).join('/');
    const url = new URL(`${API_BASE}/${path}${suffix}`, window.location.origin);
    Object.entries(params).forEach(([name, value]) => {
        (Array.isArray(value) ? value : [value]).forEach(item => {
            if (item !== undefined && item !== null && String(item).trim() !== '') {
                url.searchParams.append(name, String(item).trim());
            }
        });
    });
    return url.href;
}

/**
 * GET a REST v2 URL and parse its JSON response, turning errors into exceptions
 * @param {string} url - Absolute URL built with apiUrl
 * @returns {Promise<Object>} - Response JSON
 */
async function getJSON(url) {
    const response = await fetch(url, { headers: { 'Accept': 'application/json' } });

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
    }

    return response.json();
}

/**
 * Copy a shell command that performs the given GET request
 * @param {string} dropdownId - ID of the copy dropdown (closed and used for feedback)
 * @param {string} url - Absolute URL of the request
 * @param {string} shell - 'bash', 'cmd' or 'powershell'
 */
function copyRequestCommand(dropdownId, url, shell) {
    closeDropdown(dropdownId);

    if (!url) {
        notify('No request data available.', 'warning');
        return;
    }

    let command;
    let message;
    switch (shell) {
        case 'bash':
            command = `curl '${url.replace(/'/g, "'\\''")}'`;
            message = 'cURL (bash) copied!';
            break;
        case 'cmd':
            command = `curl "${url}"`;
            message = 'cURL (cmd) copied!';
            break;
        default:
            command = `Invoke-RestMethod -Uri '${url.replace(/'/g, "''")}'`;
            message = 'PowerShell copied!';
    }

    navigator.clipboard.writeText(command).then(() => {
        showCopyFeedback(dropdownId, message);
    }).catch(err => {
        notify('Failed to copy: ' + err, 'danger');
    });
}
