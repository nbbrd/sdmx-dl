// ========================================================================
// DATABASES TAB FUNCTIONALITY
// ========================================================================

function getDatabases() {
    const source = document.getElementById('db-source').value;
    if (!source) {
        notify('Please enter a source ID', 'warning');
        return;
    }

    const copyDropdown = document.getElementById('databases-copy-dropdown');
    const prettyView = document.getElementById('databases-pretty-view');

    // Show pretty view initially
    prettyView.style.display = 'block';
    copyDropdown.style.display = 'none';
    document.getElementById('databases-pretty-content').innerHTML = loadingMessage();

    const url = apiUrl([source, 'databases']);

    getJSON(url)
    .then(data => {
        copyDropdown.style.display = 'inline-block';

        // Store response data and request URL for copying
        window.databasesResponseData = data;
        window.databasesRequestUrl = url;

        // Render pretty view
        renderPrettyDatabases(itemsOf(data));
    })
    .catch(error => {
        prettyView.style.display = 'none';
        notify(`Error fetching databases: ${error.message}`, 'danger');
    });
}

function renderPrettyDatabases(databases) {
    const container = document.getElementById('databases-pretty-content');
    let html = '';

    if (!databases || databases.length === 0) {
        html += '<div class="no-results">';
        html += `<div class="no-results-icon">${icon('folder')}</div>`;
        html += '<p>No databases available</p>';
        html += '</div>';
        container.innerHTML = html;
        return;
    }

    // Databases grid
    html += '<div class="sources-grid">';

    databases.forEach(db => {
        html += '<div class="source-card">';
        html += '<div class="source-header">';
        html += '<div class="source-icon database-icon">';
        html += icon('database');
        html += '</div>';
        html += '<div class="source-title">';
        html += `<div class="source-id">${escapeHtml(db.ref || db.id || 'N/A')}</div>`;
        html += `<div class="source-name">${escapeHtml(db.label || db.name || 'Unnamed Database')}</div>`;
        html += '</div>';
        html += '</div>';
        html += '</div>';
    });

    html += '</div>';
    container.innerHTML = html;
}
