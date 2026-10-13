// ========================================================================
// COPY MENUS
// ========================================================================

const COPY_ACTIONS = [
    { action: 'json', icon: 'braces', label: 'Copy JSON' },
    { action: 'bash', icon: 'terminal', label: 'Copy as cURL (bash)' },
    { action: 'cmd', icon: 'terminal', label: 'Copy as cURL (cmd)' },
    { action: 'powershell', icon: 'terminal', label: 'Copy as PowerShell' }
];

/**
 * Build the copy menus declared in the page as <div class="copy-dropdown" data-copy="kind">.
 * The response and the request URL of a kind are read from window[kind + 'ResponseData']
 * and window[kind + 'RequestUrl'].
 */
function initCopyMenus() {
    document.querySelectorAll('.copy-dropdown[data-copy]').forEach(dropdown => {
        const kind = dropdown.dataset.copy;
        let html = `<button class="btn btn-outline-primary dropdown-toggle dropdown-button" type="button" data-bs-toggle="dropdown" aria-expanded="false">${icon('copy')} Copy</button>`;
        html += `<div class="dropdown-menu dropdown-menu-end shadow" id="${kind}-copy-dropdown-content">`;
        COPY_ACTIONS.forEach(item => {
            html += `<a href="#" class="dropdown-item" data-copy-action="${item.action}">${icon(item.icon)} ${item.label}</a>`;
        });
        if (dropdown.dataset.copyScripts) {
            html += `<div class="dropdown-section" id="${kind}-script-items" style="display: none;"></div>`;
        }
        html += '</div>';
        dropdown.innerHTML = html;

        dropdown.addEventListener('click', event => {
            const item = event.target.closest('[data-copy-action]');
            if (!item) return;
            event.preventDefault();
            copyResponse(kind, item.dataset.copyAction);
        });
    });
}

/**
 * Copy the last response of a kind as JSON, or its request as a shell command
 * @param {string} kind - 'about', 'sources', 'databases', 'flows', 'meta' or 'data'
 * @param {string} action - 'json', 'bash', 'cmd' or 'powershell'
 */
function copyResponse(kind, action) {
    const dropdownId = `${kind}-copy-dropdown`;
    if (action !== 'json') {
        copyRequestCommand(dropdownId, window[`${kind}RequestUrl`], action);
        return;
    }

    closeDropdown(dropdownId);
    const data = window[`${kind}ResponseData`];
    if (!data) {
        notify('No data available to copy. Please send the request first.', 'warning');
        return;
    }

    navigator.clipboard.writeText(JSON.stringify(data, null, 2)).then(() => {
        showCopyFeedback(dropdownId, 'JSON copied!');
    }).catch(err => {
        notify('Failed to copy: ' + err, 'danger');
    });
}

/**
 * Report a successful copy
 * @param {string} dropdownId - ID of the copy dropdown
 * @param {string} message - Message to display
 */
function showCopyFeedback(dropdownId, message) {
    notify(message, 'success');
}
