// ========================================================================
// ABOUT TAB FUNCTIONALITY
// ========================================================================

function getAbout() {
    const prettyView = document.getElementById('about-pretty-view');
    const copyDropdown = document.getElementById('about-copy-dropdown');

    if (!prettyView) {
        console.error('About pretty view element not found');
        return;
    }

    // Show pretty view
    prettyView.style.display = 'block';
    copyDropdown.style.display = 'none';

    const url = apiUrl(['about']);

    getJSON(url)
    .then(data => {
        // Store response data and request URL for copying
        window.aboutResponseData = data;
        window.aboutRequestUrl = url;

        // Show copy dropdown
        copyDropdown.style.display = 'inline-block';

        // Render pretty view
        renderPrettyAbout(data);
    })
    .catch(error => {
        const container = document.getElementById('about-pretty-content');
        if (container) {
            container.innerHTML = errorMessage(error.message);
        }
    });
}


function renderPrettyAbout(data) {
    const container = document.getElementById('about-pretty-content');
    let html = '<div class="about-grid">';

    if (data.version) {
        html += '<div class="card about-card"><div class="card-body">';
        html += `<h3 class="about-card-title">${icon('info-circle')} Version</h3>`;
        html += `<p class="about-card-value fs-4 fw-bold">${escapeHtml(data.version)}</p>`;
        html += '</div></div>';
    }

    if (data.name) {
        html += '<div class="card about-card"><div class="card-body">';
        html += `<h3 class="about-card-title">${icon('box-seam')} Library</h3>`;
        html += `<p class="about-card-value fs-5">${escapeHtml(data.name)}</p>`;
        html += '</div></div>';
    }

    html += '</div>';

    if (data.description) {
        html += '<div class="card about-card mb-3"><div class="card-body">';
        html += '<h3 class="about-card-title">About</h3>';
        html += `<p class="text-body-secondary mb-0">${escapeHtml(data.description)}</p>`;
        html += '</div></div>';
    }

    const features = [
        { icon: 'cloud-download', label: 'Download SDMX data from multiple sources' },
        { icon: 'plug', label: 'RESTful API with gRPC support' },
        { icon: 'shield-lock', label: 'Secure authentication and authorization' },
        { icon: 'speedometer2', label: 'High-performance data retrieval' },
        { icon: 'list-ul', label: 'Support for multiple SDMX formats' },
        { icon: 'database', label: 'Access to multiple statistical databases' }
    ];

    html += '<div class="card about-card"><div class="card-body">';
    html += '<h3 class="about-card-title">Features</h3>';
    html += '<ul class="list-unstyled about-features mb-0">';
    features.forEach(feature => {
        html += `<li>${icon(feature.icon, 'text-success')} ${feature.label}</li>`;
    });
    html += '</ul></div></div>';

    container.innerHTML = html;
}