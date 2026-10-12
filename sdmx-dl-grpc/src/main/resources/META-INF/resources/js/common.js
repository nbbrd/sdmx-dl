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
// VIEW CONTROL FUNCTIONS
// ========================================================================

/**
 * Toggle view menu visibility
 */
function toggleViewMenu() {
    const menu = document.getElementById('view-menu');
    const isVisible = menu.style.display !== 'none';

    if (isVisible) {
        menu.style.display = 'none';
    } else {
        menu.style.display = 'block';
    }
}

/**
 * Show URL parameters help modal
 */
function showUrlParamsHelp() {
    const modal = document.getElementById('url-params-modal');
    if (modal) {
        modal.style.display = 'flex';
        // Close view menu if open
        const viewMenu = document.getElementById('view-menu');
        if (viewMenu) {
            viewMenu.style.display = 'none';
        }
    }
}

/**
 * Close URL parameters help modal
 */
function closeUrlParamsHelp() {
    const modal = document.getElementById('url-params-modal');
    if (modal) {
        modal.style.display = 'none';
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
 * Generic dropdown toggle utility
 * @param {string} dropdownId - ID of the dropdown element
 */
function toggleDropdown(dropdownId) {
    const dropdown = document.getElementById(dropdownId);
    if (dropdown) {
        dropdown.classList.toggle('open');
    }
}

/**
 * Generic view switcher utility
 * @param {string} prefix - Prefix for view IDs (e.g., 'sources', 'flows')
 * @param {string} view - View name (e.g., 'json', 'pretty', 'chart')
 * @param {Array<string>} viewNames - Array of all possible view names
 */
function switchView(prefix, view, viewNames) {
    // Hide all views
    viewNames.forEach(viewName => {
        const element = document.getElementById(`${prefix}-${viewName}`);
        if (element) {
            element.style.display = 'none';
        }
    });

    // Show selected view
    const selectedView = document.getElementById(`${prefix}-${view}`);
    if (selectedView) {
        selectedView.style.display = 'block';
    }

    // Close dropdown if exists
    const dropdown = document.getElementById(`${prefix}-view-dropdown`);
    if (dropdown) {
        dropdown.classList.remove('open');
    }
}

/**
 * Update dropdown button text and active state
 * @param {string} dropdownId - ID of the dropdown
 * @param {string} view - Current view name
 * @param {Object} viewConfig - Configuration object mapping view names to display text and icons
 */
function updateDropdownActive(dropdownId, view, viewConfig) {
    const dropdownButton = document.querySelector(`#${dropdownId} .dropdown-button`);
    // Handle both patterns: "xxx-view-dropdown" -> "xxx-dropdown-content" or direct ID mapping
    const contentId = dropdownId.replace('-view-dropdown', '-dropdown-content');
    const dropdownItems = document.querySelectorAll(`#${contentId} a`);

    if (dropdownButton && viewConfig[view]) {
        const config = viewConfig[view];
        dropdownButton.innerHTML = `<span class="material-symbols-outlined">${config.icon}</span> ${config.label} <span class="dropdown-arrow">▼</span>`;
    }

    // Update active states
    dropdownItems.forEach(item => {
        item.classList.remove('active');
        Object.keys(viewConfig).forEach(viewName => {
            if (item.onclick && item.onclick.toString().includes(`'${viewName}'`)) {
                if (viewName === view) {
                    item.classList.add('active');
                }
            }
        });
    });
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
    document.getElementById(dropdownId).classList.remove('open');

    if (!url) {
        alert('No request data available.');
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
        alert('Failed to copy: ' + err);
    });
}

// Button ripple micro-interaction
(function attachRipple(){
    const buttons = document.querySelectorAll('button, .source-action-btn, .flow-action-btn');
    buttons.forEach(btn => {
        btn.addEventListener('click', function(e){
            const rect = this.getBoundingClientRect();
            const ripple = document.createElement('span');
            ripple.className = 'ripple';
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size/2;
            const y = e.clientY - rect.top - size/2;
            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            this.appendChild(ripple);
            ripple.addEventListener('animationend', () => ripple.remove());
        });
    });
})();
