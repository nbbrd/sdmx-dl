// ========================================================================
// FLOW SEARCH TAB FUNCTIONALITY
// ========================================================================

// IndexedDB setup
const DB_NAME = 'sdmx-dl-flows-cache';
const DB_VERSION = 1;
const STORE_NAME = 'flows';
let flowsDB = null;
let searchDebounceTimer = null;
let recentSearches = JSON.parse(localStorage.getItem('flowSearchHistory') || '[]');
let currentSearchResults = [];

/**
 * Initialize IndexedDB
 */
function initFlowsDB() {
    return new Promise((resolve, reject) => {
        if (flowsDB) {
            resolve(flowsDB);
            return;
        }

        const request = indexedDB.open(DB_NAME, DB_VERSION);

        request.onerror = () => reject(request.error);
        request.onsuccess = () => {
            flowsDB = request.result;
            resolve(flowsDB);
        };

        request.onupgradeneeded = (event) => {
            const db = event.target.result;

            // Create object store if it doesn't exist
            if (!db.objectStoreNames.contains(STORE_NAME)) {
                const store = db.createObjectStore(STORE_NAME, { keyPath: 'id' });
                store.createIndex('source', 'source', { unique: false });
                store.createIndex('ref', 'ref', { unique: false });
                store.createIndex('name', 'name', { unique: false });
                store.createIndex('searchText', 'searchText', { unique: false });
            }
        };
    });
}

/**
 * Save flows to IndexedDB
 */
async function saveFlowsToDB(flows) {
    const db = await initFlowsDB();
    const transaction = db.transaction([STORE_NAME], 'readwrite');
    const store = transaction.objectStore(STORE_NAME);

    flows.forEach(flow => {
        store.put(flow);
    });

    return new Promise((resolve, reject) => {
        transaction.oncomplete = () => resolve();
        transaction.onerror = () => reject(transaction.error);
    });
}

/**
 * Get all flows from IndexedDB
 */
async function getAllFlowsFromDB() {
    const db = await initFlowsDB();
    const transaction = db.transaction([STORE_NAME], 'readonly');
    const store = transaction.objectStore(STORE_NAME);
    const request = store.getAll();

    return new Promise((resolve, reject) => {
        request.onsuccess = () => resolve(request.result);
        request.onerror = () => reject(request.error);
    });
}

/**
 * Clear flows cache from IndexedDB
 */
async function clearFlowsFromDB() {
    const db = await initFlowsDB();
    const transaction = db.transaction([STORE_NAME], 'readwrite');
    const store = transaction.objectStore(STORE_NAME);
    const request = store.clear();

    return new Promise((resolve, reject) => {
        request.onsuccess = () => resolve();
        request.onerror = () => reject(request.error);
    });
}

/**
 * Get cache statistics
 */
async function getCacheStats() {
    try {
        const flows = await getAllFlowsFromDB();
        const sources = new Set(flows.map(f => f.source));
        return {
            totalFlows: flows.length,
            totalSources: sources.size,
            lastUpdated: flows.length > 0 ? flows[0].cachedAt : null
        };
    } catch (error) {
        return { totalFlows: 0, totalSources: 0, lastUpdated: null };
    }
}

/**
 * Update index status display
 */
async function updateIndexStatus() {
    const stats = await getCacheStats();
    const statusEl = document.getElementById('index-status-text');

    if (stats.totalFlows > 0) {
        const date = stats.lastUpdated ? new Date(stats.lastUpdated).toLocaleString() : 'Unknown';
        statusEl.innerHTML = `<span class="material-symbols-outlined" style="color: #28a745;">check_circle</span> ${stats.totalFlows} flows from ${stats.totalSources} sources (cached: ${date})`;
    } else {
        statusEl.innerHTML = `<span class="material-symbols-outlined" style="color: #ffc107;">info</span> No flows indexed yet`;
    }
}

/**
 * Index all flows from all sources
 */
async function indexAllFlows() {
    const btn = document.getElementById('index-flows-btn');
    const statusEl = document.getElementById('index-status-text');
    const progressContainer = document.getElementById('index-progress-container');
    const progressBar = document.getElementById('index-progress-bar');
    const progressText = document.getElementById('index-progress-text');
    const originalBtnText = btn.innerHTML;

    btn.disabled = true;
    btn.innerHTML = '<span class="material-symbols-outlined">hourglass_empty</span> Indexing...';
    statusEl.innerHTML = '<span class="material-symbols-outlined">sync</span> Starting indexing process...';
    progressContainer.style.display = 'block';
    progressBar.style.width = '0%';

    try {
        // Step 1: Get all sources
        statusEl.innerHTML = '<span class="material-symbols-outlined">sync</span> Fetching sources...';
        progressBar.style.width = '10%';
        progressText.textContent = '10% - Fetching sources...';

        const sourcesResponse = await fetch(apiUrl(['sources']));

        if (!sourcesResponse.ok) {
            throw new Error('Failed to fetch sources');
        }

        const sources = await sourcesResponse.json();
        statusEl.innerHTML = `<span class="material-symbols-outlined">sync</span> Found ${sources.length} sources. Fetching flows...`;
        progressBar.style.width = '20%';
        progressText.textContent = '20% - Found ' + sources.length + ' sources';

        // Step 2: Fetch flows from all sources in parallel
        const allFlows = [];
        const timestamp = Date.now();
        let processedSources = 0;

        const flowPromises = sources.map(async (source) => {
            try {
                const response = await fetch(apiUrl([source.id, 'flows']));

                if (response.ok) {
                    const flows = await response.json();
                    // Get first available name from names object, or use id as fallback
                    const sourceLabel = (source.names && Object.keys(source.names).length > 0)
                        ? Object.values(source.names)[0]
                        : source.id;

                    const enrichedFlows = flows.map(flow => ({
                        id: `${source.id}::${flow.ref}`,
                        source: source.id,
                        sourceLabel: sourceLabel,
                        ref: flow.ref,
                        name: flow.name || '',
                        description: flow.description || '',
                        searchText: `${source.id} ${sourceLabel} ${flow.ref} ${flow.name || ''} ${flow.description || ''}`.toLowerCase(),
                        cachedAt: timestamp
                    }));
                    allFlows.push(...enrichedFlows);
                }
                processedSources++;

                // Update progress bar (20% to 80% for fetching)
                const fetchProgress = 20 + Math.round((processedSources / sources.length) * 60);
                progressBar.style.width = fetchProgress + '%';
                progressText.textContent = `${fetchProgress}% - Processing sources: ${processedSources}/${sources.length}`;
                statusEl.innerHTML = `<span class="material-symbols-outlined">sync</span> Processing sources: ${processedSources}/${sources.length}`;
            } catch (error) {
                console.warn(`Failed to fetch flows for source ${source.id}:`, error);
                processedSources++;
            }
        });

        await Promise.all(flowPromises);

        // Step 3: Save to IndexedDB
        statusEl.innerHTML = `<span class="material-symbols-outlined">sync</span> Saving ${allFlows.length} flows to cache...`;
        progressBar.style.width = '90%';
        progressText.textContent = '90% - Saving to cache...';

        await clearFlowsFromDB();
        await saveFlowsToDB(allFlows);

        // Step 4: Update status
        progressBar.style.width = '100%';
        progressText.textContent = '100% - Complete!';
        statusEl.innerHTML = `<span class="material-symbols-outlined" style="color: #28a745;">check_circle</span> Successfully indexed ${allFlows.length} flows from ${sources.length} sources!`;

        // Hide progress bar after 2 seconds
        setTimeout(() => {
            progressContainer.style.display = 'none';
        }, 2000);

        updateIndexStatus();

    } catch (error) {
        statusEl.innerHTML = `<span class="material-symbols-outlined" style="color: #dc3545;">error</span> Error: ${escapeHtml(error.message)}`;
        progressContainer.style.display = 'none';
        console.error('Indexing error:', error);
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalBtnText;
    }
}

/**
 * Clear flows cache
 */
async function clearFlowsCache() {
    if (!confirm('Are you sure you want to clear the cached flows?')) {
        return;
    }

    try {
        await clearFlowsFromDB();
        document.getElementById('index-status-text').innerHTML = '<span class="material-symbols-outlined" style="color: #ffc107;">info</span> Cache cleared';

        // Reset search UI
        document.getElementById('flowsearch-input').value = '';
        document.getElementById('flowsearch-clear-btn').style.display = 'none';
        document.getElementById('flowsearch-results-container').style.display = 'none';
        document.getElementById('flowsearch-empty-state').style.display = 'none';
    } catch (error) {
        alert('Error clearing cache: ' + error.message);
        console.error('Cache clear error:', error);
    }
}

/**
 * Debounced search for search-as-you-type
 */
function debouncedFlowSearch() {
    // Clear existing timer
    if (searchDebounceTimer) {
        clearTimeout(searchDebounceTimer);
    }

    const query = document.getElementById('flowsearch-input').value.trim();

    // Show/hide clear button immediately
    document.getElementById('flowsearch-clear-btn').style.display = query ? 'block' : 'none';

    // If query is empty, clear results immediately
    if (!query) {
        document.getElementById('flowsearch-results-container').style.display = 'none';
        document.getElementById('flowsearch-empty-state').style.display = 'none';
        return;
    }

    // Wait 300ms before searching
    searchDebounceTimer = setTimeout(() => {
        performFlowSearch();
    }, 300);
}

/**
 * Perform flow search (Google-like)
 */
async function performFlowSearch() {
    const searchInput = document.getElementById('flowsearch-input');
    const query = searchInput.value.trim();

    // Show/hide clear button
    document.getElementById('flowsearch-clear-btn').style.display = query ? 'block' : 'none';

    if (!query) {
        document.getElementById('flowsearch-results-container').style.display = 'none';
        document.getElementById('flowsearch-empty-state').style.display = 'none';
        return;
    }

    try {
        const startTime = performance.now();
        const allFlows = await getAllFlowsFromDB();

        if (allFlows.length === 0) {
            alert('No flows indexed yet. Please click "Index All Flows" first.');
            return;
        }

        // Get search options
        const options = {
            matchCase: document.getElementById('flowsearch-case-check').checked,
            wholeWords: document.getElementById('flowsearch-words-check').checked,
            isRegex: document.getElementById('flowsearch-regex-check').checked,
            isFuzzy: document.getElementById('flowsearch-fuzzy-check').checked
        };

        // Filter flows
        let results = [];
        if (options.isFuzzy) {
            results = allFlows.filter(flow =>
                fuzzyMatch(flow.searchText, query) ||
                fuzzyMatch(flow.ref, query) ||
                fuzzyMatch(flow.name, query) ||
                fuzzyMatch(flow.description, query)
            );
        } else if (options.isRegex) {
            try {
                const regex = new RegExp(query, options.matchCase ? '' : 'i');
                results = allFlows.filter(flow =>
                    regex.test(flow.searchText) ||
                    regex.test(flow.ref) ||
                    regex.test(flow.name) ||
                    regex.test(flow.description)
                );
            } catch (e) {
                alert('Invalid regular expression: ' + e.message);
                return;
            }
        } else {
            let searchText = query;
            if (!options.matchCase) {
                searchText = searchText.toLowerCase();
            }

            if (options.wholeWords) {
                const wordBoundary = new RegExp(`\\b${searchText}\\b`, options.matchCase ? '' : 'i');
                results = allFlows.filter(flow =>
                    wordBoundary.test(flow.searchText) ||
                    wordBoundary.test(flow.ref) ||
                    wordBoundary.test(flow.name) ||
                    wordBoundary.test(flow.description)
                );
            } else {
                results = allFlows.filter(flow => {
                    const text = options.matchCase ? flow.searchText : flow.searchText.toLowerCase();
                    const ref = options.matchCase ? flow.ref : flow.ref.toLowerCase();
                    const name = options.matchCase ? flow.name : flow.name.toLowerCase();
                    const desc = options.matchCase ? flow.description : flow.description.toLowerCase();

                    return text.includes(searchText) ||
                           ref.includes(searchText) ||
                           name.includes(searchText) ||
                           desc.includes(searchText);
                });
            }
        }

        const endTime = performance.now();
        const duration = ((endTime - startTime) / 1000).toFixed(2);

        // Save to recent searches (only if results found)
        if (results.length > 0) {
            saveRecentSearch(query);
        }

        // Display results
        if (results.length > 0) {
            currentSearchResults = results; // Store for export
            displayFlowSearchResults(results);
            document.getElementById('flowsearch-results-info').innerHTML =
                `About <strong>${results.length}</strong> result${results.length !== 1 ? 's' : ''} (${duration} seconds)`;
            document.getElementById('flowsearch-results-container').style.display = 'block';
            document.getElementById('flowsearch-empty-state').style.display = 'none';
        } else {
            currentSearchResults = [];
            document.getElementById('flowsearch-results-container').style.display = 'none';
            document.getElementById('flowsearch-empty-state').style.display = 'block';
        }

    } catch (error) {
        alert('Search error: ' + error.message);
        console.error('Search error:', error);
    }
}

/**
 * Clear flow search
 */
function clearFlowSearch() {
    document.getElementById('flowsearch-input').value = '';
    document.getElementById('flowsearch-clear-btn').style.display = 'none';
    document.getElementById('flowsearch-results-container').style.display = 'none';
    document.getElementById('flowsearch-empty-state').style.display = 'none';
}

/**
 * Save search to recent history
 */
function saveRecentSearch(query) {
    if (!query || query.length < 2) return;

    // Remove if already exists
    recentSearches = recentSearches.filter(s => s !== query);

    // Add to beginning
    recentSearches.unshift(query);

    // Keep only last 10
    recentSearches = recentSearches.slice(0, 10);

    // Save to localStorage
    localStorage.setItem('flowSearchHistory', JSON.stringify(recentSearches));
}

/**
 * Clear recent searches
 */
function clearRecentSearches() {
    recentSearches = [];
    localStorage.removeItem('flowSearchHistory');
    displayRecentSearches();
}

/**
 * Display recent searches as clickable chips
 */
function displayRecentSearches() {
    const container = document.getElementById('recent-searches-container');
    if (!container) return;

    if (recentSearches.length === 0) {
        container.style.display = 'none';
        return;
    }

    let html = '<div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-top: 10px;">';
    html += '<span style="font-size: 12px; color: #666;">Recent:</span>';

    recentSearches.forEach(search => {
        html += `<button
            onclick="runRecentSearch(${jsArg(search)})"
            style="padding: 4px 12px; background: #f1f3f4; border: 1px solid #dadce0; border-radius: 16px; color: #3c4043; font-size: 13px; cursor: pointer; transition: all 0.2s;"
            onmouseover="this.style.background='#e8eaed';"
            onmouseout="this.style.background='#f1f3f4';">${escapeHtml(search)}</button>`;
    });

    html += `<button
        onclick="clearRecentSearches()"
        style="padding: 4px 8px; background: transparent; border: none; color: #5f6368; font-size: 12px; cursor: pointer;"
        title="Clear history">✕</button>`;
    html += '</div>';

    container.innerHTML = html;
    container.style.display = 'block';
}

/**
 * Run a recent search
 */
function runRecentSearch(query) {
    document.getElementById('flowsearch-input').value = query;
    performFlowSearch();
}

/**
 * Export flow search results as CSV
 */
function exportFlowSearchResults() {
    if (currentSearchResults.length === 0) {
        alert('No results to export');
        return;
    }

    // Create CSV content
    const headers = ['Source', 'Source Label', 'Flow Reference', 'Flow Name', 'Description'];
    const csvRows = [headers.join(',')];

    currentSearchResults.forEach(flow => {
        const row = [
            escapeCsvValue(flow.source),
            escapeCsvValue(flow.sourceLabel),
            escapeCsvValue(flow.ref),
            escapeCsvValue(flow.name),
            escapeCsvValue(flow.description)
        ];
        csvRows.push(row.join(','));
    });

    const csvContent = csvRows.join('\n');

    // Create download link
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);

    link.setAttribute('href', url);
    link.setAttribute('download', `flow-search-results-${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/**
 * Escape CSV value (handle commas, quotes, newlines)
 */
function escapeCsvValue(value) {
    if (value == null) return '';
    value = String(value);

    // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
    if (value.includes(',') || value.includes('"') || value.includes('\n')) {
        value = '"' + value.replace(/"/g, '""') + '"';
    }

    return value;
}

/**
 * Legacy filter function - now redirects to search
 */
function filterFlowSearchResults() {
    // Redirect to new search function
    performFlowSearch();
}

/**
 * Clear filter - now redirects to clear search
 */
function clearFlowSearchFilter() {
    clearFlowSearch();
}

/**
 * Toggle search option (deprecated - now using checkboxes)
 */
function toggleFlowSearchOption(option) {
    // Legacy function - kept for backward compatibility
    // Now using checkboxes instead
}

/**
 * Highlight matching terms in text
 */
function highlightMatches(text, query) {
    if (!text || !query) return escapeHtml(text || '');

    const escapedText = escapeHtml(text);
    const searchTerm = escapeHtml(query.trim());

    if (!searchTerm) return escapedText;

    // Simple case-insensitive highlighting
    const regex = new RegExp(`(${searchTerm.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
    return escapedText.replace(regex, '<mark style="background: #fff59d; padding: 0 2px; border-radius: 2px; font-weight: 500;">$1</mark>');
}

/**
 * Display flow search results
 */
function displayFlowSearchResults(flows) {
    const resultsEl = document.getElementById('flowsearch-results');

    if (flows.length === 0) {
        resultsEl.innerHTML = '<div style="color: #666; padding: 40px; text-align: center;">No flows found</div>';
        return;
    }


    // Group flows by source
    const flowsBySource = {};
    flows.forEach(flow => {
        if (!flowsBySource[flow.source]) {
            flowsBySource[flow.source] = [];
        }
        flowsBySource[flow.source].push(flow);
    });

    let html = '';

    // Sort sources alphabetically
    const sortedSources = Object.keys(flowsBySource).sort();

    sortedSources.forEach(sourceName => {
        const sourceFlows = flowsBySource[sourceName];
        const sourceLabel = sourceFlows[0].sourceLabel;

        html += `
            <div style="margin-bottom: 20px;">
                <h3 style="color: #003D6D; font-size: 1.1em; margin-bottom: 10px; padding: 10px; background: #f8f9fa; border-left: 4px solid #0066cc; display: flex; align-items: center; gap: 8px;">
                    <span class="material-symbols-outlined">source</span>
                    ${escapeHtml(sourceLabel)} (${escapeHtml(sourceName)})
                    <span style="margin-left: auto; font-size: 0.9em; font-weight: normal; color: #666;">${sourceFlows.length} flow${sourceFlows.length !== 1 ? 's' : ''}</span>
                </h3>
                <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(350px, 1fr)); gap: 15px;">
        `;

        sourceFlows.forEach(flow => {
            const iconSvg = getFlowIcon(flow);
            const searchQuery = document.getElementById('flowsearch-input').value.trim();

            html += `
                <div class="flow-card" onclick="toggleFlowSearchActions(this)">
                    <div class="flow-header">
                        <div class="flow-icon">${iconSvg}</div>
                        <div class="flow-title">
                            <h4 class="flow-id">${highlightMatches(flow.ref, searchQuery)}</h4>
                            ${flow.name ? `<p class="flow-name">${highlightMatches(flow.name, searchQuery)}</p>` : ''}
                        </div>
                    </div>
                    ${flow.description ? `<div class="flow-description">${highlightMatches(flow.description, searchQuery)}</div>` : ''}
                    <div class="flow-click-hint">Click card to show actions</div>
                    <div class="flow-actions">
                        <div class="flow-actions-title">Use in Other Tabs</div>
                        <div class="flow-action-buttons">
                            <button class="flow-action-btn" onclick="event.stopPropagation(); useFlowSearchInTab(${jsArg(flow.source)}, ${jsArg(flow.ref)}, '', 'meta')" title="Get meta for this flow"><span class="material-symbols-outlined">info</span> Meta</button>
                            <button class="flow-action-btn" onclick="event.stopPropagation(); useFlowSearchInTab(${jsArg(flow.source)}, ${jsArg(flow.ref)}, '', 'keybuilder')" title="Use in key builder"><span class="material-symbols-outlined">vpn_key</span> Key Builder</button>
                            <button class="flow-action-btn" onclick="event.stopPropagation(); useFlowSearchInTab(${jsArg(flow.source)}, ${jsArg(flow.ref)}, '', 'data')" title="Use in data tab"><span class="material-symbols-outlined">analytics</span> Data</button>
                            <button class="flow-action-btn" onclick="event.stopPropagation(); useFlowSearchInTab(${jsArg(flow.source)}, ${jsArg(flow.ref)}, '', 'compare')" title="Use in compare tab"><span class="material-symbols-outlined">compare_arrows</span> Compare</button>
                        </div>
                    </div>
                </div>
            `;
        });

        html += `
                </div>
            </div>
        `;
    });

    resultsEl.innerHTML = html;
}

/**
 * Toggle flow search card actions
 */
function toggleFlowSearchActions(card) {
    const wasExpanded = card.classList.contains('expanded');

    // Collapse all other cards
    document.querySelectorAll('#flowsearch-results .flow-card').forEach(c => {
        c.classList.remove('expanded');
    });

    // Toggle current card
    if (!wasExpanded) {
        card.classList.add('expanded');
    }
}

/**
 * Use flow from search in another tab
 */
function useFlowSearchInTab(source, flowId, database, tabName) {
    // Fill the appropriate fields in the target tab
    switch(tabName) {
        case 'meta':
            document.getElementById('meta-source').value = source;
            document.getElementById('meta-flow').value = flowId;
            if (database) {
                document.getElementById('meta-database').value = database;
            }
            switchTab('meta');
            break;
        case 'keybuilder':
            document.getElementById('kb-source').value = source;
            document.getElementById('kb-flow').value = flowId;
            if (database) {
                document.getElementById('kb-database').value = database;
            }
            switchTab('keybuilder');
            break;
        case 'data':
            document.getElementById('data-source').value = source;
            document.getElementById('data-flow').value = flowId;
            if (database) {
                document.getElementById('data-database').value = database;
            }
            switchTab('data');
            break;
        case 'compare':
            document.getElementById('compare-source').value = source;
            document.getElementById('compare-flow').value = flowId;
            if (database) {
                document.getElementById('compare-database').value = database;
            }
            switchTab('compare');
            break;
    }
}
