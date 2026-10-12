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
            container.innerHTML = `<div style="text-align: center; padding: 40px; color: #cc0000;">Error: ${escapeHtml(error.message)}</div>`;
        }
    });
}


function renderPrettyAbout(data) {
    const container = document.getElementById('about-pretty-content');
    let html = '';

    html += '<div style="max-width: 800px; margin: 0 auto;">';


    // Main info grid
    html += '<div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 30px;">';

    // Version
    if (data.version) {
        html += '<div style="background: white; padding: 20px; border-radius: 8px; border: 2px solid #e0e0e0;">';
        html += '<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">';
        html += '<span class="material-symbols-outlined" style="color: #003D6D; font-size: 32px;">info</span>';
        html += '<h3 style="margin: 0; color: #003D6D;">Version</h3>';
        html += '</div>';
        html += `<p style="font-size: 24px; font-weight: bold; color: #333; margin: 0;">${escapeHtml(data.version)}</p>`;
        html += '</div>';
    }

    // Name
    if (data.name) {
        html += '<div style="background: white; padding: 20px; border-radius: 8px; border: 2px solid #e0e0e0;">';
        html += '<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">';
        html += '<span class="material-symbols-outlined" style="color: #003D6D; font-size: 32px;">sdk</span>';
        html += '<h3 style="margin: 0; color: #003D6D;">Library</h3>';
        html += '</div>';
        html += `<p style="font-size: 18px; color: #333; margin: 0;">${escapeHtml(data.name)}</p>`;
        html += '</div>';
    }

    html += '</div>';

    // Description
    if (data.description) {
        html += '<div style="background: white; padding: 25px; border-radius: 8px; border: 2px solid #e0e0e0; margin-bottom: 20px;">';
        html += '<h3 style="color: #003D6D; margin: 0 0 15px 0;">About</h3>';
        html += `<p style="color: #666; line-height: 1.6; margin: 0;">${escapeHtml(data.description)}</p>`;
        html += '</div>';
    }

    // Features/Capabilities
    html += '<div style="background: white; padding: 25px; border-radius: 8px; border: 2px solid #e0e0e0;">';
    html += '<h3 style="color: #003D6D; margin: 0 0 20px 0;">Features</h3>';
    html += '<div style="display: grid; gap: 15px;">';

    const features = [
        { icon: 'cloud_download', label: 'Download SDMX data from multiple sources' },
        { icon: 'api', label: 'RESTful API with gRPC support' },
        { icon: 'security', label: 'Secure authentication and authorization' },
        { icon: 'speed', label: 'High-performance data retrieval' },
        { icon: 'format_list_bulleted', label: 'Support for multiple SDMX formats' },
        { icon: 'database', label: 'Access to multiple statistical databases' }
    ];

    features.forEach(feature => {
        html += '<div style="display: flex; align-items: center; gap: 12px;">';
        html += `<span class="material-symbols-outlined" style="color: #4caf50; font-size: 24px;">${feature.icon}</span>`;
        html += `<span style="color: #333;">${feature.label}</span>`;
        html += '</div>';
    });

    html += '</div>';
    html += '</div>';

    html += '</div>';

    container.innerHTML = html;
}
