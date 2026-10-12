// ========================================================================
// SOURCES COPY FUNCTIONS
// ========================================================================

function toggleSourcesCopyDropdown() {
    toggleDropdown('sources-copy-dropdown');
}

function copySourcesJSON() {
    const dropdown = document.getElementById('sources-copy-dropdown');
    dropdown.classList.remove('open');

    if (!window.sourcesResponseData) {
        alert('No data available to copy. Please fetch sources first.');
        return;
    }

    const jsonText = JSON.stringify(window.sourcesResponseData, null, 2);
    navigator.clipboard.writeText(jsonText).then(() => {
        showCopyFeedback('sources-copy-dropdown', 'JSON copied!');
    }).catch(err => {
        alert('Failed to copy: ' + err);
    });
}

function copySourcesCurlBash() {
    copyRequestCommand('sources-copy-dropdown', window.sourcesRequestUrl, 'bash');
}

function copySourcesCurlCmd() {
    copyRequestCommand('sources-copy-dropdown', window.sourcesRequestUrl, 'cmd');
}

function copySourcesCurlPowerShell() {
    copyRequestCommand('sources-copy-dropdown', window.sourcesRequestUrl, 'powershell');
}

function showCopyFeedback(dropdownId, message) {
    const dropdown = document.getElementById(dropdownId);
    const button = dropdown.querySelector('.dropdown-button');
    const originalHTML = button.innerHTML;

    button.innerHTML = `<span class="material-symbols-outlined">check</span> ${escapeHtml(message)}`;
    button.style.background = '#4caf50';
    button.style.borderColor = '#4caf50';

    setTimeout(() => {
        button.innerHTML = originalHTML;
        button.style.background = '';
        button.style.borderColor = '';
    }, 2000);
}

// ========================================================================
// ABOUT TAB COPY FUNCTIONS
// ========================================================================

function toggleAboutCopyDropdown() {
    toggleDropdown('about-copy-dropdown');
}

function copyAboutJSON() {
    const dropdown = document.getElementById('about-copy-dropdown');
    dropdown.classList.remove('open');

    if (!window.aboutResponseData) {
        alert('No data available to copy. Please fetch information first.');
        return;
    }

    const jsonText = JSON.stringify(window.aboutResponseData, null, 2);
    navigator.clipboard.writeText(jsonText).then(() => {
        showCopyFeedback('about-copy-dropdown', 'JSON copied!');
    }).catch(err => {
        alert('Failed to copy: ' + err);
    });
}

function copyAboutCurlBash() {
    copyRequestCommand('about-copy-dropdown', window.aboutRequestUrl, 'bash');
}

function copyAboutCurlCmd() {
    copyRequestCommand('about-copy-dropdown', window.aboutRequestUrl, 'cmd');
}

function copyAboutCurlPowerShell() {
    copyRequestCommand('about-copy-dropdown', window.aboutRequestUrl, 'powershell');
}

// ========================================================================
// DATABASES COPY FUNCTIONS
// ========================================================================

function toggleDatabasesCopyDropdown() {
    toggleDropdown('databases-copy-dropdown');
}

function copyDatabasesJSON() {
    const dropdown = document.getElementById('databases-copy-dropdown');
    dropdown.classList.remove('open');

    if (!window.databasesResponseData) {
        alert('No data available to copy. Please fetch databases first.');
        return;
    }

    const jsonText = JSON.stringify(window.databasesResponseData, null, 2);
    navigator.clipboard.writeText(jsonText).then(() => {
        showCopyFeedback('databases-copy-dropdown', 'JSON copied!');
    }).catch(err => {
        alert('Failed to copy: ' + err);
    });
}

function copyDatabasesCurlBash() {
    copyRequestCommand('databases-copy-dropdown', window.databasesRequestUrl, 'bash');
}

function copyDatabasesCurlCmd() {
    copyRequestCommand('databases-copy-dropdown', window.databasesRequestUrl, 'cmd');
}

function copyDatabasesCurlPowerShell() {
    copyRequestCommand('databases-copy-dropdown', window.databasesRequestUrl, 'powershell');
}

// ========================================================================
// FLOWS COPY FUNCTIONS
// ========================================================================

function toggleFlowsCopyDropdown() {
    toggleDropdown('flows-copy-dropdown');
}

function copyFlowsJSON() {
    const dropdown = document.getElementById('flows-copy-dropdown');
    dropdown.classList.remove('open');

    if (!window.flowsResponseData) {
        alert('No data available to copy. Please fetch flows first.');
        return;
    }

    const jsonText = JSON.stringify(window.flowsResponseData, null, 2);
    navigator.clipboard.writeText(jsonText).then(() => {
        showCopyFeedback('flows-copy-dropdown', 'JSON copied!');
    }).catch(err => {
        alert('Failed to copy: ' + err);
    });
}

function copyFlowsCurlBash() {
    copyRequestCommand('flows-copy-dropdown', window.flowsRequestUrl, 'bash');
}

function copyFlowsCurlCmd() {
    copyRequestCommand('flows-copy-dropdown', window.flowsRequestUrl, 'cmd');
}

function copyFlowsCurlPowerShell() {
    copyRequestCommand('flows-copy-dropdown', window.flowsRequestUrl, 'powershell');
}

// ========================================================================
// META COPY FUNCTIONS
// ========================================================================

function toggleMetaCopyDropdown() {
    toggleDropdown('meta-copy-dropdown');
}

function copyMetaJSON() {
    const dropdown = document.getElementById('meta-copy-dropdown');
    dropdown.classList.remove('open');

    if (!window.metaResponseData) {
        alert('No data available to copy. Please fetch meta first.');
        return;
    }

    const jsonText = JSON.stringify(window.metaResponseData, null, 2);
    navigator.clipboard.writeText(jsonText).then(() => {
        showCopyFeedback('meta-copy-dropdown', 'JSON copied!');
    }).catch(err => {
        alert('Failed to copy: ' + err);
    });
}

function copyMetaCurlBash() {
    copyRequestCommand('meta-copy-dropdown', window.metaRequestUrl, 'bash');
}

function copyMetaCurlCmd() {
    copyRequestCommand('meta-copy-dropdown', window.metaRequestUrl, 'cmd');
}

function copyMetaCurlPowerShell() {
    copyRequestCommand('meta-copy-dropdown', window.metaRequestUrl, 'powershell');
}

// ========================================================================
// DATA COPY FUNCTIONS
// ========================================================================

function toggleDataCopyDropdown() {
    toggleDropdown('data-copy-dropdown');
}

function copyDataJSON() {
    const dropdown = document.getElementById('data-copy-dropdown');
    dropdown.classList.remove('open');

    if (!window.dataResponseData) {
        alert('No data available to copy. Please fetch data first.');
        return;
    }

    const jsonText = JSON.stringify(window.dataResponseData, null, 2);
    navigator.clipboard.writeText(jsonText).then(() => {
        showCopyFeedback('data-copy-dropdown', 'JSON copied!');
    }).catch(err => {
        alert('Failed to copy: ' + err);
    });
}

function copyDataCurlBash() {
    copyRequestCommand('data-copy-dropdown', window.dataRequestUrl, 'bash');
}

function copyDataCurlCmd() {
    copyRequestCommand('data-copy-dropdown', window.dataRequestUrl, 'cmd');
}

function copyDataCurlPowerShell() {
    copyRequestCommand('data-copy-dropdown', window.dataRequestUrl, 'powershell');
}

// Close dropdown when clicking outside
document.addEventListener('click', function(e) {
    const aboutDropdown = document.getElementById('about-view-dropdown');
    if (aboutDropdown && !aboutDropdown.contains(e.target)) {
        aboutDropdown.classList.remove('open');
    }

    const sourcesDropdown = document.getElementById('sources-view-dropdown');
    if (sourcesDropdown && !sourcesDropdown.contains(e.target)) {
        sourcesDropdown.classList.remove('open');
    }

    const sourcesCopyDropdown = document.getElementById('sources-copy-dropdown');
    if (sourcesCopyDropdown && !sourcesCopyDropdown.contains(e.target)) {
        sourcesCopyDropdown.classList.remove('open');
    }

    const databasesDropdown = document.getElementById('databases-view-dropdown');
    if (databasesDropdown && !databasesDropdown.contains(e.target)) {
        databasesDropdown.classList.remove('open');
    }

    const databasesCopyDropdown = document.getElementById('databases-copy-dropdown');
    if (databasesCopyDropdown && !databasesCopyDropdown.contains(e.target)) {
        databasesCopyDropdown.classList.remove('open');
    }

    const flowsDropdown = document.getElementById('flows-view-dropdown');
    if (flowsDropdown && !flowsDropdown.contains(e.target)) {
        flowsDropdown.classList.remove('open');
    }

    const flowsCopyDropdown = document.getElementById('flows-copy-dropdown');
    if (flowsCopyDropdown && !flowsCopyDropdown.contains(e.target)) {
        flowsCopyDropdown.classList.remove('open');
    }

    const metaDropdown = document.getElementById('meta-view-dropdown');
    if (metaDropdown && !metaDropdown.contains(e.target)) {
        metaDropdown.classList.remove('open');
    }

    const metaCopyDropdown = document.getElementById('meta-copy-dropdown');
    if (metaCopyDropdown && !metaCopyDropdown.contains(e.target)) {
        metaCopyDropdown.classList.remove('open');
    }

    const dataDropdown = document.getElementById('data-view-dropdown');
    if (dataDropdown && !dataDropdown.contains(e.target)) {
        dataDropdown.classList.remove('open');
    }

    const dataCopyDropdown = document.getElementById('data-copy-dropdown');
    if (dataCopyDropdown && !dataCopyDropdown.contains(e.target)) {
        dataCopyDropdown.classList.remove('open');
    }
});
