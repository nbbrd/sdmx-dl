// ========================================================================
// SCRIPT GENERATION
// ========================================================================

/**
 * Build the URL of a script generation request for the last Data or Flows request
 * @param {string} kind - 'data' or 'flows'
 * @param {string} target - Script target (e.g. 'python/cli')
 * @param {Object} extra - Additional query parameters (e.g. format)
 * @returns {string|null} - Absolute URL, or null if no request was made yet
 */
function scriptUrl(kind, target, extra = {}) {
    if (kind === 'data') {
        const params = window.dataRequestParams;
        if (!params) return null;
        const { source, flow, ...query } = params;
        return apiUrl([source, flow], { ...query, target, ...extra }, '/data:script');
    }
    const params = window.flowsRequestParams;
    if (!params) return null;
    return apiUrl([params.source], { database: params.database, target, ...extra }, '/flows:script');
}

/**
 * Generate a script for the last request and copy it to the clipboard
 * @param {string} kind - 'data' or 'flows'
 * @param {string} target - Script target
 */
async function copyScript(kind, target) {
    const dropdownId = `${kind}-copy-dropdown`;
    closeDropdown(dropdownId);

    const url = scriptUrl(kind, target);
    if (!url) {
        notify('No request data available.', 'warning');
        return;
    }

    try {
        const script = await getJSON(url);
        await navigator.clipboard.writeText(script.content);
        const warnings = script.warnings || [];
        warnings.forEach(warning => console.warn(`Script ${target}: ${warning}`));
        showCopyFeedback(dropdownId, warnings.length > 0
            ? `${target} copied (${warnings.length} warning${warnings.length > 1 ? 's' : ''}, see console)`
            : `${target} copied!`);
    } catch (error) {
        notify('Failed to generate script: ' + error.message, 'danger');
    }
}

/**
 * Generate a script for the last request and download it as a file
 * @param {string} kind - 'data' or 'flows'
 * @param {string} target - Script target
 */
function downloadScript(kind, target) {
    closeDropdown(`${kind}-copy-dropdown`);

    const url = scriptUrl(kind, target, { format: 'raw' });
    if (!url) {
        notify('No request data available.', 'warning');
        return;
    }

    const link = document.createElement('a');
    link.href = url;
    link.download = '';
    document.body.appendChild(link);
    link.click();
    link.remove();
}

/**
 * Load the available script targets and add them to the Data and Flows copy menus
 */
async function loadScriptTargets() {
    try {
        const targets = itemsOf(await getJSON(apiUrl(['script', 'targets'])));
        ['data', 'flows'].forEach(kind => {
            const container = document.getElementById(`${kind}-script-items`);
            const supported = targets.filter(t => (t.commands || []).includes(kind));
            if (!container || supported.length === 0) return;

            let html = '<div class="dropdown-divider"></div><h6 class="dropdown-header">Copy as script</h6>';
            supported.forEach(t => {
                const target = escapeHtml(t.target);
                html += `<a href="#" class="dropdown-item script-item" onclick="event.preventDefault(); copyScript(${jsArg(kind)}, ${jsArg(t.target)})" title="Copy a ${target} script">`;
                html += `<span class="script-label">${icon('file-earmark-code')} ${target}</span>`;
                html += `<span class="script-download" role="button" onclick="event.preventDefault(); event.stopPropagation(); downloadScript(${jsArg(kind)}, ${jsArg(t.target)})" title="Download as file">${icon('download')}</span>`;
                html += '</a>';
            });
            container.innerHTML = html;
            container.style.display = 'block';
        });
    } catch (error) {
        console.error('Failed to load script targets:', error);
    }
}
