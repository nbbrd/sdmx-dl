// ========================================================================
// DATABASES TAB FUNCTIONALITY
// ========================================================================

function getDatabases() {
    const source = document.getElementById('db-source').value;
    if (!source) {
        alert('Please enter a source ID');
        return;
    }

    const copyDropdown = document.getElementById('databases-copy-dropdown');
    const prettyView = document.getElementById('databases-pretty-view');

    // Show pretty view initially
    prettyView.style.display = 'block';
    copyDropdown.style.display = 'none';

    const url = apiUrl([source, 'databases']);

    getJSON(url)
    .then(data => {
        copyDropdown.style.display = 'inline-block';

        // Store response data and request URL for copying
        window.databasesResponseData = data;
        window.databasesRequestUrl = url;

        // Render pretty view
        renderPrettyDatabases(data);
    })
    .catch(error => {
        prettyView.style.display = 'none';
        alert(`Error fetching databases: ${error.message}`);
    });
}

function renderPrettyDatabases(databases) {
    const container = document.getElementById('databases-pretty-content');
    let html = '';

    if (!databases || databases.length === 0) {
        html += '<div class="no-results">';
        html += '<div class="no-results-icon">📁</div>';
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
        html += '<div class="source-icon" style="background: linear-gradient(135deg, #4caf50 0%, #2e7d32 100%); border-color: #1b5e20;">';
        html += '<span class="material-symbols-outlined" style="color: white;">folder</span>';
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
