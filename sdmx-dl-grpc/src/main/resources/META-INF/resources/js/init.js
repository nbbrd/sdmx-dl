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

    initCopyMenus();

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
    setupListSuggestions();
    setupKeyAutocomplete();
    setupDataChartToolbar();

    // Auto-load sources on page load (loads in background for all tabs)
    getSources();

    // Initialize compare tab
    initComparisonSeries();

    // Initialize flow search tab - just check cache status
    updateIndexStatus();
    displayRecentSearches();

    // Setup URL synchronization with forms (after URL loading is complete)
    setupFormUrlSync();
});

/**
 * Handle browser back/forward buttons
 */
window.addEventListener('popstate', (event) => {
    // Restore the tab and the form values from the URL
    if (!loadFormFromUrl()) {
        switchTab('about', true);
    }
});
