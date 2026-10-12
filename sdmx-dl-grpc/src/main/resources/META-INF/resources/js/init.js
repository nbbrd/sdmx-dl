// ========================================================================
// INITIALIZATION
// ========================================================================

/**
 * Initialize the application on page load
 */
window.addEventListener('DOMContentLoaded', () => {
    // Check for callback URL parameter
    const urlParams = new URLSearchParams(window.location.search);
    const callbackParam = urlParams.get('callback');
    if (callbackParam) {
        keyBuilderCallbackUrl = callbackParam;
    }

    // Load form values from URL parameters FIRST (before setting up listeners)
    // This prevents change events from firing during URL parameter loading
    loadFormFromUrl();

    // Initialize view control buttons
    initializeViewControls();

    // Now set up the rest of the application
    getAbout();
    loadSources();
    loadScriptTargets();
    setupAutocomplete();

    // Auto-load sources on page load (loads in background for all tabs)
    getSources();

    // Initialize compare tab
    renderComparisonSeriesList();

    // Initialize flow search tab - just check cache status
    updateIndexStatus();
    displayRecentSearches();

    // Setup URL synchronization with forms (after URL loading is complete)
    setupFormUrlSync();

    // Close view menu when clicking outside
    document.addEventListener('click', (e) => {
        const viewMenu = document.getElementById('view-menu');
        const viewMenuBtn = document.getElementById('view-menu-btn');

        if (viewMenu && viewMenuBtn) {
            if (!viewMenuBtn.contains(e.target) && !viewMenu.contains(e.target)) {
                viewMenu.style.display = 'none';
            }
        }
    });

    // Close help modal when clicking outside or pressing Escape
    const modal = document.getElementById('url-params-modal');
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeUrlParamsHelp();
            }
        });

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && modal.style.display === 'flex') {
                closeUrlParamsHelp();
            }
        });
    }
});

/**
 * Handle browser back/forward buttons
 */
window.addEventListener('popstate', (event) => {
    if (event.state?.tab) {
        switchTab(event.state.tab, true);
    } else {
        // Reload from URL if no state
        loadFormFromUrl();
    }
});
