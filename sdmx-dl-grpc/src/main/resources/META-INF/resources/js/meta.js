// ========================================================================
// METADATA TAB FUNCTIONALITY
// ========================================================================

/**
 * Fetch and display metadata for a flow
 */
function getMeta() {
    const source = document.getElementById('meta-source').value;
    const flow = document.getElementById('meta-flow').value;
    const database = document.getElementById('meta-database').value;

    if (!source || !flow) {
        alert('Please enter both source ID and flow ID');
        return;
    }

    // Show loading and reset views
    const prettyView = document.getElementById('meta-pretty-view');
    const copyDropdown = document.getElementById('meta-copy-dropdown');

    // Show pretty view initially
    prettyView.style.display = 'block';
    copyDropdown.style.display = 'none';

    const url = apiUrl([source, flow, 'meta'], { database });

    getJSON(url)
    .then(data => {
        copyDropdown.style.display = 'inline-block';

        // Store response data and request URL for copying
        window.metaResponseData = data;
        window.metaRequestUrl = url;

        // Render pretty view
        renderPrettyMeta(data);
    })
    .catch(error => {
        prettyView.style.display = 'none';
        alert(`Error fetching metadata: ${error.message}`);
    });
}

function renderPrettyMeta(data) {
    const container = document.getElementById('meta-pretty-content');
    let html = '';

    // Flow Information Section
    if (data.flow) {
        html += '<div class="meta-section">';
        html += '<h3>Flow Information</h3>';
        html += '<div class="meta-info-grid">';

        if (data.flow.ref) {
            html += '<div class="meta-info-item">';
            html += '<div class="meta-info-label">Flow Reference</div>';
            html += `<div class="meta-info-value"><code>${escapeHtml(data.flow.ref)}</code></div>`;
            html += '</div>';
        }

        if (data.flow.name) {
            html += '<div class="meta-info-item">';
            html += '<div class="meta-info-label">Name</div>';
            html += `<div class="meta-info-value">${escapeHtml(data.flow.name)}</div>`;
            html += '</div>';
        }

        if (data.flow.structureRef) {
            html += '<div class="meta-info-item">';
            html += '<div class="meta-info-label">Structure Reference</div>';
            html += `<div class="meta-info-value"><code>${escapeHtml(data.flow.structureRef)}</code></div>`;
            html += '</div>';
        }

        if (data.flow.description) {
            html += '<div class="meta-info-item" style="grid-column: 1 / -1;">';
            html += '<div class="meta-info-label">Description</div>';
            html += `<div class="meta-info-value">${escapeHtml(data.flow.description)}</div>`;
            html += '</div>';
        }

        html += '</div>';
        html += '</div>';
    }

    // Structure Information Section
    if (data.structure) {
        html += '<div class="meta-section">';
        html += '<h3>Data Structure</h3>';
        html += '<div class="meta-info-grid">';

        if (data.structure.ref) {
            html += '<div class="meta-info-item">';
            html += '<div class="meta-info-label">Structure Reference</div>';
            html += `<div class="meta-info-value"><code>${escapeHtml(data.structure.ref)}</code></div>`;
            html += '</div>';
        }

        if (data.structure.name) {
            html += '<div class="meta-info-item">';
            html += '<div class="meta-info-label">Structure Name</div>';
            html += `<div class="meta-info-value">${escapeHtml(data.structure.name)}</div>`;
            html += '</div>';
        }

        if (data.structure.timeDimensionId) {
            html += '<div class="meta-info-item">';
            html += '<div class="meta-info-label">Time Dimension ID</div>';
            html += `<div class="meta-info-value"><code>${escapeHtml(data.structure.timeDimensionId)}</code></div>`;
            html += '</div>';
        }

        if (data.structure.primaryMeasureId) {
            html += '<div class="meta-info-item">';
            html += '<div class="meta-info-label">Primary Measure ID</div>';
            html += `<div class="meta-info-value"><code>${escapeHtml(data.structure.primaryMeasureId)}</code></div>`;
            html += '</div>';
        }

        html += '</div>';

        // Dimensions Table
        if (data.structure.dimensions && data.structure.dimensions.length > 0) {
            html += '<h4 style="margin: 20px 0 10px 0; color: #333;">Dimensions</h4>';
            html += '<table class="dimensions-table">';
            html += '<thead><tr>';
            html += '<th style="width: 60px;">#</th>';
            html += '<th>ID</th>';
            html += '<th>Name</th>';
            html += '<th>Codelist</th>';
            html += '</tr></thead>';
            html += '<tbody>';

            data.structure.dimensions.forEach((dim, index) => {
                html += '<tr>';
                html += `<td class="dimension-position">${index}</td>`;
                html += `<td><code>${escapeHtml(dim.id || 'N/A')}</code></td>`;
                html += `<td>${escapeHtml(dim.name || 'N/A')}</td>`;
                html += '<td>';

                if (dim.codelist && dim.codelist.codes) {
                    const codesCount = Object.keys(dim.codelist.codes).length;
                    html += `<span class="codelist-badge">${codesCount} codes</span>`;

                    // Show preview of first few codes
                    const codes = Object.entries(dim.codelist.codes).slice(0, 5);
                    if (codes.length > 0) {
                        html += '<div class="codelist-preview">';
                        codes.forEach(([key, value]) => {
                            html += `<div class="code-item"><span class="code-key">${escapeHtml(key)}</span>: ${escapeHtml(value)}</div>`;
                        });
                        if (codesCount > 5) {
                            html += `<div class="code-item" style="color: #999; font-style: italic;">... and ${codesCount - 5} more</div>`;
                        }
                        html += '</div>';
                    }
                } else {
                    html += '<span style="color: #999;">No codelist</span>';
                }

                html += '</td>';
                html += '</tr>';
            });

            html += '</tbody></table>';
        }

        // Attributes Section
        if (data.structure.attributes && data.structure.attributes.length > 0) {
            html += '<h4 style="margin: 20px 0 10px 0; color: #333;">Attributes</h4>';
            html += '<div class="attributes-list">';

            data.structure.attributes.forEach(attr => {
                html += '<div class="attribute-item">';
                html += `<span class="attribute-name">${escapeHtml(attr.name || attr.id)}</span>`;
                html += `<span class="attribute-id">${escapeHtml(attr.id)}</span>`;
                html += '</div>';
            });

            html += '</div>';
        }

        html += '</div>';
    }

    // If no data
    if (!data.flow && !data.structure) {
        html += '<div class="no-results">';
        html += '<div class="no-results-icon">📋</div>';
        html += '<p>No metadata available</p>';
        html += '</div>';
    }

    container.innerHTML = html;
}
