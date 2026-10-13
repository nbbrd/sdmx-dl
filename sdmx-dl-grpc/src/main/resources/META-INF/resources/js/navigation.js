// ========================================================================
// TAB NAVIGATION
// ========================================================================

/**
 * Switch to a tab and update URL with current form values
 * @param {string} tabName - Name of the tab to switch to
 * @param {boolean} fromUrl - True if called from URL parameter loading
 */
function switchTab(tabName, fromUrl = false) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    // Show selected tab
    const tabContent = document.getElementById(tabName + '-tab');
    if (tabContent) {
        tabContent.classList.add('active');
    }

    // Activate the corresponding tab button
    document.querySelectorAll('.tab').forEach(btn => {
        const selected = btn.dataset.tab === tabName;
        btn.classList.toggle('active', selected);
        btn.setAttribute('aria-selected', String(selected));
    });
    const tabSelect = document.getElementById('tab-select');
    if (tabSelect) tabSelect.value = tabName;

    // Check if we should hide other tabs
    const urlParams = new URLSearchParams(window.location.search);
    const hideOtherTabs = urlParams.get('hideOtherTabs') === 'true';
    const tabsContainer = document.querySelector('.tabs');
    if (tabsContainer) {
        if (hideOtherTabs) {
            tabsContainer.classList.add('hide-others');
        } else {
            tabsContainer.classList.remove('hide-others');
        }
    }

    // Check if we should hide the header
    const hideHeader = urlParams.get('hideHeader') === 'true';
    const container = document.querySelector('.container');
    if (container) {
        if (hideHeader) {
            container.classList.add('hide-header');
        } else {
            container.classList.remove('hide-header');
        }
    }

    // Update URL if not loading from URL
    if (!fromUrl) {
        updateUrlFromForm(tabName);
    }
}

/**
 * Update URL query parameters based on current tab's form values
 * @param {string} tabName - Current active tab name
 * @param {boolean} replace - True to replace the current history entry instead of adding a new one
 */
function updateUrlFromForm(tabName, replace = false) {
    const url = new URL(window.location);

    // Preserve hideOtherTabs, hideHeader and callback parameters if they exist
    const hideOtherTabs = url.searchParams.get('hideOtherTabs');
    const hideHeader = url.searchParams.get('hideHeader');
    const callback = url.searchParams.get('callback');

    // Clear all existing parameters
    url.search = '';

    // Always add the tab parameter
    url.searchParams.set('tab', tabName);

    // Restore hideOtherTabs parameter if it was set
    if (hideOtherTabs === 'true') {
        url.searchParams.set('hideOtherTabs', 'true');
    }

    // Restore hideHeader parameter if it was set
    if (hideHeader === 'true') {
        url.searchParams.set('hideHeader', 'true');
    }

    // Restore callback parameter if it was set
    if (callback) {
        url.searchParams.set('callback', callback);
    }

    // Add form field values for the current tab with simplified param names
    const formFieldsMap = getFormFieldsForTab(tabName);
    formFieldsMap.forEach(mapping => {
        const element = document.getElementById(mapping.fieldId);
        if (element && element.value.trim()) {
            url.searchParams.set(mapping.paramName, element.value.trim());
        }
    });
    if (tabName === 'compare') {
        getComparisonSeriesParams().forEach(value => url.searchParams.append('series', value));
    }

    // Update URL without page reload, avoiding duplicate history entries
    if (replace || url.href === window.location.href) {
        window.history.replaceState({ tab: tabName }, '', url);
    } else {
        window.history.pushState({ tab: tabName }, '', url);
    }
}

/**
 * Get list of form field mappings for a specific tab
 * @param {string} tabName - Tab name
 * @returns {Array<{fieldId: string, paramName: string}>} - Array of field mappings
 */
function getFormFieldsForTab(tabName) {
    const fieldsByTab = {
        'databases': [
            { fieldId: 'db-source', paramName: 'source' }
        ],
        'flows': [
            { fieldId: 'flows-source', paramName: 'source' },
            { fieldId: 'flows-database', paramName: 'database' }
        ],
        'meta': [
            { fieldId: 'meta-source', paramName: 'source' },
            { fieldId: 'meta-flow', paramName: 'flow' },
            { fieldId: 'meta-database', paramName: 'database' }
        ],
        'keybuilder': [
            { fieldId: 'kb-source', paramName: 'source' },
            { fieldId: 'kb-flow', paramName: 'flow' },
            { fieldId: 'kb-database', paramName: 'database' }
        ],
        'data': [
            { fieldId: 'data-source', paramName: 'source' },
            { fieldId: 'data-flow', paramName: 'flow' },
            { fieldId: 'data-key', paramName: 'key' },
            { fieldId: 'data-database', paramName: 'database' },
            { fieldId: 'data-start', paramName: 'start' },
            { fieldId: 'data-end', paramName: 'end' },
            { fieldId: 'data-firstn', paramName: 'firstN' },
            { fieldId: 'data-lastn', paramName: 'lastN' },
            { fieldId: 'data-detail', paramName: 'detail' }
        ],
        'compare': [
            { fieldId: 'compare-source', paramName: 'source' },
            { fieldId: 'compare-flow', paramName: 'flow' },
            { fieldId: 'compare-key', paramName: 'key' },
            { fieldId: 'compare-database', paramName: 'database' },
            { fieldId: 'compare-label', paramName: 'label' }
        ]
    };

    return fieldsByTab[tabName] || [];
}

/**
 * Load form values from URL parameters
 * @returns {boolean} - True if a valid tab was selected from the URL
 */
function loadFormFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    const tab = urlParams.get('tab');
    const hideOtherTabs = urlParams.get('hideOtherTabs') === 'true';
    const hideHeader = urlParams.get('hideHeader') === 'true';

    // Apply hideOtherTabs setting even if no tab specified
    const tabsContainer = document.querySelector('.tabs');
    if (tabsContainer) {
        if (hideOtherTabs) {
            tabsContainer.classList.add('hide-others');
        } else {
            tabsContainer.classList.remove('hide-others');
        }
    }

    // Apply hideHeader setting
    const container = document.querySelector('.container');
    if (container) {
        if (hideHeader) {
            container.classList.add('hide-header');
        } else {
            container.classList.remove('hide-header');
        }
    }

    // If no tab parameter, don't do anything more (use default tab)
    if (!tab) {
        return false;
    }

    // Validate tab exists
    const tabContent = document.getElementById(tab + '-tab');
    if (!tabContent) {
        console.warn(`Tab '${tab}' not found, ignoring URL parameters`);
        return false;
    }

    // Get form field mappings for this tab
    const formFieldsMap = getFormFieldsForTab(tab);

    // Fill form fields from URL parameters
    formFieldsMap.forEach(mapping => {
        const value = urlParams.get(mapping.paramName);
        if (value) {
            const element = document.getElementById(mapping.fieldId);
            if (element) {
                element.value = value;
            }
        }
    });

    if (tab === 'compare') {
        loadComparisonSeriesFromUrl(urlParams);
    }

    // Switch to the tab (without updating URL to avoid circular update)
    switchTab(tab, true);
    return true;
}

/**
 * Add change listeners to form fields to update URL
 */
function setupFormUrlSync() {
    // Get all form input fields
    const allFields = [
        'db-source',
        'flows-source', 'flows-database',
        'meta-source', 'meta-flow', 'meta-database',
        'kb-source', 'kb-flow', 'kb-database',
        'data-source', 'data-flow', 'data-key', 'data-database',
        'data-start', 'data-end', 'data-firstn', 'data-lastn', 'data-detail',
        'compare-source', 'compare-flow', 'compare-key', 'compare-database', 'compare-label'
    ];

    allFields.forEach(fieldId => {
        const element = document.getElementById(fieldId);
        if (element) {
            element.addEventListener('change', () => {
                // Find which tab this field belongs to
                const activeTab = document.querySelector('.tab-content.active');
                if (activeTab) {
                    const tabName = activeTab.id.replace('-tab', '');
                    updateUrlFromForm(tabName, true);
                }
            });
        }
    });
}
