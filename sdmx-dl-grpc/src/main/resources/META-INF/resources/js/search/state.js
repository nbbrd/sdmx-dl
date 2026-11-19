// ========================================================================
// SDMX-DL SEARCH: STATE, STORAGE, NAVIGATION AND HELPERS
// ========================================================================

const SEARCH_MAX_RESULTS = 200;
const SEARCH_PAGE_SIZE = 10;
const SEARCH_TIMEOUT_MS = 20000;
const QUICK_SEARCH_TIMEOUT_MS = 3000;
const SUGGEST_MAX_RESULTS = 4;
const SUGGEST_TIMEOUT_MS = 5000;
const SERIES_PAGE_SIZE = 12;
const CODE_RENDER_LIMIT = 300;
const COMPARE_LIMIT = 5;
const RECENT_LIMIT = 6;

const STORAGE_KEYS = {
    recents: 'sdmxdl-search-recents',
    favorites: 'sdmxdl-search-favorites',
    compare: 'sdmxdl-search-compare'
};

const state = {
    view: 'landing',
    previousView: 'search',
    sources: [],
    search: {
        query: '',
        scope: '',
        items: [],
        scores: [],
        failures: [],
        total: 0,
        page: 0,
        sort: 'relevance',
        sourceFilter: new Set(),
        elapsed: 0,
        token: 0,
        busy: false,
        started: 0,
        pending: [],
        update: null,
        controller: null,
        ticker: 0
    },
    dataset: null,
    series: null
};

// ------------------------------------------------------------------------
// Helpers
// ------------------------------------------------------------------------

const byId = id => document.getElementById(id);

/**
 * Get the id part of a flow reference (e.g. 'EXR' for 'ECB,EXR,1.0')
 * @param {string} ref - Flow reference
 * @returns {string} - Flow id
 */
function flowIdOf(ref) {
    const parts = String(ref || '').split(',');
    return parts.length === 3 ? parts[1] : String(ref || '');
}

/**
 * Split a search query into the words used for highlighting
 * @param {string} query - Search query
 * @returns {Array<string>} - Words of at least two characters
 */
function queryWords(query) {
    return String(query || '').split(/[\s.,;:/+*()]+/).filter(word => word.length > 1);
}

/**
 * Escape a text as HTML and highlight the words of a query
 * @param {string} text - Raw text
 * @param {string} query - Query whose words are highlighted
 * @returns {string} - Safe HTML
 */
function highlight(text, query) {
    const words = queryWords(query);
    if (!text || words.length === 0) return escapeHtml(text);
    const pattern = new RegExp('(' + words.map(word => word.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')).join('|') + ')', 'ig');
    return String(text).split(pattern).map((part, index) => index % 2 === 1 ? `<mark>${escapeHtml(part)}</mark>` : escapeHtml(part)).join('');
}

/**
 * Format a number with the locale of the browser
 * @param {number} value - Number
 * @returns {string} - Formatted number
 */
function formatCount(value) {
    return Number(value || 0).toLocaleString();
}

/**
 * Format an observation value without spurious decimals
 * @param {number|string} value - Observation value
 * @returns {string} - Formatted value
 */
function formatValue(value) {
    const number = Number(value);
    if (value === undefined || value === null || Number.isNaN(number)) return '–';
    return number.toLocaleString(undefined, { maximumFractionDigits: 6 });
}

/**
 * Get the start of a time interval (e.g. '2026-09-01T00:00:00' for '2026-09-01T00:00:00/P1M')
 * @param {string} period - Time interval
 * @returns {string} - ISO local date-time
 */
function periodStartOf(period) {
    return String(period || '').split('/')[0];
}

/**
 * Format a time interval as a human-readable period (e.g. '2026-09' for a month)
 * @param {string} period - Time interval
 * @returns {string} - Readable period
 */
function formatPeriod(period) {
    const [start, duration = ''] = String(period || '').split('/');
    const match = /^(\d{4})-(\d{2})-(\d{2})(?:T(\d{2}:\d{2}))?/.exec(start);
    if (!match) return String(period || '');
    const [, year, month, day, time] = match;
    const quarter = Math.floor((Number(month) - 1) / 3) + 1;
    switch (duration) {
        case 'P1Y':
            return year;
        case 'P6M':
            return `${year}-S${Number(month) <= 6 ? 1 : 2}`;
        case 'P3M':
            return `${year}-Q${quarter}`;
        case 'P1M':
            return `${year}-${month}`;
        case 'P7D':
            return `${year}-${month}-${day} (week)`;
        default:
            return time && time !== '00:00' ? `${year}-${month}-${day} ${time}` : `${year}-${month}-${day}`;
    }
}

/**
 * Copy a text to the clipboard and notify the user
 * @param {string} text - Text to copy
 * @param {string} message - Notification on success
 */
function copyToClipboard(text, message) {
    if (!navigator.clipboard) {
        notify('Clipboard not available in this context', 'warning');
        return;
    }
    navigator.clipboard.writeText(text)
        .then(() => notify(message))
        .catch(error => notify('Failed to copy: ' + error, 'danger'));
}

/**
 * Download a text as a file
 * @param {string} content - File content
 * @param {string} fileName - File name
 * @param {string} mediaType - Media type
 */
function downloadText(content, fileName, mediaType) {
    const link = document.createElement('a');
    link.href = URL.createObjectURL(new Blob([content], { type: mediaType }));
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    link.remove();
    setTimeout(() => URL.revokeObjectURL(link.href), 1000);
}

/**
 * Tell whether a source is public, so that it is searched by default and never asks for credentials
 * @param {string} id - Source id
 * @returns {boolean} - True if public or unknown
 */
function isPublicSource(id) {
    const source = state.sources.find(item => item.id === id);
    return !source || !source.confidentiality || source.confidentiality === 'PUBLIC';
}

/**
 * Describe the access of a non-public source
 * @param {string} id - Source id
 * @returns {string} - Tooltip text
 */
function restrictedTitleOf(id) {
    const source = state.sources.find(item => item.id === id);
    return `${source ? source.confidentiality : 'Restricted'} source: may ask for credentials`;
}

/**
 * Parse a dataset reference typed in the search box, such as 'ECB/EXR' or 'ECB/EXR/M.USD.EUR.SP00.A'
 * @param {string} text - Typed text
 * @returns {{source: string, flow: string, key: string}|null} - Reference, or null if the source is unknown
 */
function parseDatasetRef(text) {
    const match = /^([^\s/]+)\/([^\s/]+)(?:\/(\S*))?$/.exec(String(text || '').trim());
    if (!match) return null;
    const source = state.sources.find(item => item.id.toLowerCase() === match[1].toLowerCase());
    return source ? { source: source.id, flow: match[2], key: match[3] || '' } : null;
}

/**
 * Tell whether a text looks like a series key (e.g. 'M.USD.EUR.SP00.A' or 'M..EUR')
 * @param {string} text - Typed text
 * @returns {boolean} - True if the text looks like a key
 */
function looksLikeKey(text) {
    return /^[\w*+@$-]*(\.[\w*+@$-]*)+$/.test(String(text || '').trim());
}

// ------------------------------------------------------------------------
// Local storage
// ------------------------------------------------------------------------

function loadStored(name, fallback) {
    try {
        const value = JSON.parse(localStorage.getItem(STORAGE_KEYS[name]));
        return Array.isArray(value) ? value : fallback;
    } catch (error) {
        return fallback;
    }
}

function saveStored(name, value) {
    try {
        localStorage.setItem(STORAGE_KEYS[name], JSON.stringify(value));
    } catch (error) {
        console.warn('Cannot save ' + name, error);
    }
}

/**
 * Identify a dataset or a series across views and storage
 * @param {Object} ref - Object with source, flow, optional database and optional key
 * @returns {string} - Identifier
 */
function refId(ref) {
    return [ref.source, ref.database || '', ref.flow, ref.key || ''].join('|');
}

// ------------------------------------------------------------------------
// Navigation
// ------------------------------------------------------------------------

/**
 * Show a view and update the stepper
 * @param {string} view - 'landing', 'search', 'dataset' or 'compare'
 */
function showView(view) {
    if (view === 'dataset' && !state.dataset) return;
    if (view !== 'dataset') closeSeries(true);
    if (view === 'compare' && state.view !== 'compare') state.previousView = state.view;

    document.body.classList.toggle('landing', view === 'landing');
    document.querySelectorAll('.view').forEach(element => element.classList.remove('active'));
    byId('view-' + (view === 'landing' ? 'search' : view)).classList.add('active');
    state.view = view;

    byId('crumb-search').classList.toggle('active', view === 'search');
    byId('crumb-dataset').classList.toggle('active', view === 'dataset');
    byId('crumb-dataset').classList.toggle('disabled', !state.dataset);
    byId('crumb-series').classList.toggle('disabled', !state.series && !(state.dataset && lastSeriesKey));
    window.scrollTo({ top: 0 });
}

/**
 * Write the current view and selection into the URL, so that it can be shared or bookmarked
 * @param {boolean} push - Create a new history entry instead of replacing the current one
 */
function updateUrl(push = false) {
    const params = new URLSearchParams();
    if (state.view === 'search') {
        if (state.search.query) params.set('q', state.search.query);
        if (state.search.scope) params.set('scope', state.search.scope);
    } else if (state.view === 'dataset' && state.dataset) {
        params.set('source', state.dataset.source);
        params.set('flow', state.dataset.flow);
        if (state.dataset.database) params.set('database', state.dataset.database);
        const key = datasetKey();
        if (key.replace(/\./g, '')) params.set('key', key);
        if (state.series) params.set('series', state.series.key);
    } else if (state.view === 'compare') {
        params.set('view', 'compare');
    }
    const query = params.toString();
    const url = window.location.pathname + (query ? '?' + query : '');
    if (url === window.location.pathname + window.location.search) return;
    if (push) {
        window.history.pushState(null, '', url);
    } else {
        window.history.replaceState(null, '', url);
    }
}

/**
 * Restore the view and selection from the URL
 */
function routeFromUrl() {
    const params = new URLSearchParams(window.location.search);
    if (params.get('view') === 'compare') {
        showCompare(false);
    } else if (params.get('source') && params.get('flow')) {
        const ref = { source: params.get('source'), flow: params.get('flow'), database: params.get('database') || '' };
        openDataset(ref, { key: params.get('key') || '', series: params.get('series') || '', push: false });
    } else if (params.get('q') || params.get('scope')) {
        byId('q').value = params.get('q') || '';
        byId('scope').value = params.get('scope') || '';
        runSearch({ push: false });
    } else {
        goHome(false);
    }
}
