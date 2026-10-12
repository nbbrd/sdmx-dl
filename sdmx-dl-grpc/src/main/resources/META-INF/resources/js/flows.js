// ========================================================================
// FLOWS TAB FUNCTIONALITY
// ========================================================================

/**
 * Fetch and display flows for a source
 */
function getFlows() {
    const source = document.getElementById('flows-source').value;
    const database = document.getElementById('flows-database').value;

    if (!source) {
        alert('Please enter a source ID');
        return;
    }

    const copyDropdown = document.getElementById('flows-copy-dropdown');
    const prettyView = document.getElementById('flows-pretty-view');

    // Show pretty view initially
    prettyView.style.display = 'block';
    copyDropdown.style.display = 'none';

    const url = apiUrl([source, 'flows'], { database });

    getJSON(url)
    .then(data => {
        copyDropdown.style.display = 'inline-block';

        // Store response data and request params for copying
        window.flowsResponseData = data;
        window.flowsRequestUrl = url;
        window.flowsRequestParams = { source, database };

        // Render pretty view
        renderPrettyFlows(data);

        // Setup filter functionality
        setupFlowsFilter();
    })
    .catch(error => {
        prettyView.style.display = 'none';
        alert(`Error fetching flows: ${error.message}`);
    });
}

/**
 * Generate a GitHub-style identicon based on flow ID hash
 * @param {Object} flow - Flow object with id/ref
 * @returns {string} - SVG identicon HTML
 */
function getFlowIcon(flow) {
    const id = flow.ref || flow.id || 'default';

    // Simple hash function for string
    function hashCode(str) {
        let hash = 0;
        for (let i = 0; i < str.length; i++) {
            const char = str.charCodeAt(i);
            hash = ((hash << 5) - hash) + char;
            hash = hash & hash; // Convert to 32bit integer
        }
        return Math.abs(hash);
    }

    // Seeded random number generator
    function seededRandom(seed) {
        const x = Math.sin(seed++) * 10000;
        return x - Math.floor(x);
    }

    const hash = hashCode(id);

    // Color palette - vibrant colors like GitHub
    const colors = [
        '#0969DA', '#1F883D', '#CF222E', '#8250DF', '#BF3989',
        '#FB8500', '#0550AE', '#116329', '#A40E26', '#6639BA',
        '#D1242F', '#0969DA', '#218BFF', '#7D4E57', '#953800',
        '#6639BA', '#8B5CF6', '#3B82F6', '#10B981', '#F59E0B'
    ];

    // Select base color from hash
    const colorIndex = hash % colors.length;
    const baseColor = colors[colorIndex];

    // Generate 5x5 grid pattern (symmetric for better appearance)
    const gridSize = 5;
    const cellSize = 32 / gridSize;
    let svg = `<svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">`;

    // Background
    svg += `<rect width="32" height="32" fill="#f6f8fa"/>`;

    // Generate pattern - only need to generate left half + middle, mirror to right
    for (let row = 0; row < gridSize; row++) {
        for (let col = 0; col < Math.ceil(gridSize / 2); col++) {
            const seed = hash + row * gridSize + col;
            const shouldFill = seededRandom(seed) > 0.5;

            if (shouldFill) {
                const x = col * cellSize;
                const y = row * cellSize;
                const opacity = 0.7 + seededRandom(seed + 100) * 0.3;

                // Draw left/middle cell
                svg += `<rect x="${x}" y="${y}" width="${cellSize}" height="${cellSize}" fill="${baseColor}" opacity="${opacity}"/>`;

                // Mirror to right side (except middle column)
                if (col < Math.floor(gridSize / 2)) {
                    const mirrorX = (gridSize - 1 - col) * cellSize;
                    svg += `<rect x="${mirrorX}" y="${y}" width="${cellSize}" height="${cellSize}" fill="${baseColor}" opacity="${opacity}"/>`;
                }
            }
        }
    }

    svg += '</svg>';
    return svg;
}

function renderPrettyFlows(flows) {
    const container = document.getElementById('flows-pretty-content');
    let html = '';

    // Advanced filter field
    html += '<div class="filter-box">';
    html += '<div class="filter-input-row">';
    html += '<div class="filter-input-container">';
    html += '<input type="text" id="flows-filter-input" placeholder="Filter flows by ID, name, or description..." />';
    html += '<div class="filter-inline-buttons">';
    html += '<button class="filter-clear-btn" id="flows-clear-filter" title="Clear Filter">✕</button>';
    html += '<button class="filter-inline-btn" id="flows-match-case" title="Match Case">Cc</button>';
    html += '<button class="filter-inline-btn" id="flows-whole-words" title="Whole Words">W</button>';
    html += '<button class="filter-inline-btn" id="flows-regex" title="Regex">.*</button>';
    html += '<button class="filter-inline-btn" id="flows-fuzzy" title="Fuzzy Search">≈</button>';
    html += '</div>';
    html += '</div>';
    html += '<div class="filter-actions">';
    html += '<span id="flows-filter-count" class="filter-count"></span>';
    html += '</div>';
    html += '</div>';
    html += '</div>';

    // Flows grid
    html += '<div class="flows-grid">';

    flows.forEach(flow => {
        // Create filterable text for this flow (name, reference, description only)
        const filterText = [
            flow.ref || flow.id || '',
            flow.name || '',
            flow.description || ''
        ].join(' ').toLowerCase();

        html += `<div class="flow-card" data-filter-text="${escapeHtml(filterText)}" onclick="toggleFlowActions(this)">`;

        // Header with icon and title
        html += '<div class="flow-header">';
        html += `<div class="flow-icon">${getFlowIcon(flow)}</div>`;
        html += '<div class="flow-title">';
        html += `<h4 class="flow-id">${escapeHtml(flow.ref || flow.id || 'N/A')}</h4>`;

        if (flow.name) {
            html += `<p class="flow-name">${escapeHtml(flow.name)}</p>`;
        }

        html += '</div>';
        html += '</div>';

        // Description
        if (flow.description) {
            html += `<div class="flow-description">${escapeHtml(flow.description)}</div>`;
        }

        // Meta info
        html += '<div class="flow-meta">';

        if (flow.structureRef) {
            html += '<div class="flow-meta-item">';
            html += '📋 Structure: ';
            html += `<code>${escapeHtml(flow.structureRef)}</code>`;
            html += '</div>';
        }

        html += '</div>';

        // Click hint
        html += '<div class="flow-click-hint">Click card to show actions</div>';

        // Action buttons section (hidden by default)
        html += '<div class="flow-actions">';
        html += '<div class="flow-actions-title">Use in Other Tabs</div>';
        html += '<div class="flow-action-buttons">';
        html += `<button class="flow-action-btn" onclick="event.stopPropagation(); useFlowInTab(${jsArg(flow.ref || flow.id || '')}, 'meta')" title="Get meta for this flow"><span class="material-symbols-outlined">info</span> Meta</button>`;
        html += `<button class="flow-action-btn" onclick="event.stopPropagation(); useFlowInTab(${jsArg(flow.ref || flow.id || '')}, 'keybuilder')" title="Use in key builder"><span class="material-symbols-outlined">vpn_key</span> Key Builder</button>`;
        html += `<button class="flow-action-btn" onclick="event.stopPropagation(); useFlowInTab(${jsArg(flow.ref || flow.id || '')}, 'data')" title="Use in data tab"><span class="material-symbols-outlined">analytics</span> Data</button>`;
        html += `<button class="flow-action-btn" onclick="event.stopPropagation(); useFlowInTab(${jsArg(flow.ref || flow.id || '')}, 'compare')" title="Use in compare tab"><span class="material-symbols-outlined">compare_arrows</span> Compare</button>`;
        html += '</div>';
        html += '</div>';

        html += '</div>'; // Close flow-card
    });

    html += '</div>'; // Close flows-grid

    container.innerHTML = html;
}

function setupFlowsFilter() {
    const filterInput = document.getElementById('flows-filter-input');
    const filterCount = document.getElementById('flows-filter-count');
    const clearBtn = document.getElementById('flows-clear-filter');
    const matchCaseBtn = document.getElementById('flows-match-case');
    const wholeWordsBtn = document.getElementById('flows-whole-words');
    const regexBtn = document.getElementById('flows-regex');
    const fuzzyBtn = document.getElementById('flows-fuzzy');

    function performFilter() {
        const query = filterInput.value.trim();
        const cards = document.querySelectorAll('.flow-card');

        if (!query) {
            cards.forEach(card => card.classList.remove('filtered-hidden'));
            const total = cards.length;
            filterCount.textContent = `${total} flows`;
            return;
        }

        let visibleCount = 0;
        const matchCase = matchCaseBtn.classList.contains('active');
        const wholeWords = wholeWordsBtn.classList.contains('active');
        const isRegex = regexBtn.classList.contains('active');
        const isFuzzy = fuzzyBtn.classList.contains('active');

        cards.forEach(card => {
            const filterText = card.getAttribute('data-filter-text');

            const matches = matchesFilter(filterText, query, {
                matchCase,
                wholeWords,
                isRegex,
                isFuzzy
            });

            if (matches) {
                card.classList.remove('filtered-hidden');
                visibleCount++;
            } else {
                card.classList.add('filtered-hidden');
            }
        });

        const total = cards.length;
        filterCount.textContent = `${visibleCount} of ${total}`;
    }

    filterInput.addEventListener('input', performFilter);

    matchCaseBtn.addEventListener('click', function() {
        this.classList.toggle('active');
        performFilter();
    });

    wholeWordsBtn.addEventListener('click', function() {
        this.classList.toggle('active');
        performFilter();
    });

    regexBtn.addEventListener('click', function() {
        this.classList.toggle('active');
        performFilter();
    });

    fuzzyBtn.addEventListener('click', function() {
        this.classList.toggle('active');
        performFilter();
    });

    clearBtn.addEventListener('click', function() {
        filterInput.value = '';
        matchCaseBtn.classList.remove('active');
        wholeWordsBtn.classList.remove('active');
        regexBtn.classList.remove('active');
        fuzzyBtn.classList.remove('active');
        performFilter();
    });

    filterInput.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            clearBtn.click();
        }
    });

    // Initialize the filter count display
    performFilter();
}

function toggleFlowActions(cardElement) {
    // Close all other expanded cards
    const allCards = document.querySelectorAll('.flow-card');
    allCards.forEach(card => {
        if (card !== cardElement) {
            card.classList.remove('expanded');
        }
    });

    // Toggle the clicked card
    cardElement.classList.toggle('expanded');
}

function useFlowInTab(flowId, tabName) {
    // Get the current source from the flows tab
    const currentSource = document.getElementById('flows-source').value;
    const currentDatabase = document.getElementById('flows-database').value;

    // Fill the appropriate fields in the target tab
    switch(tabName) {
        case 'meta':
            if (currentSource) {
                document.getElementById('meta-source').value = currentSource;
            }
            document.getElementById('meta-flow').value = flowId;
            if (currentDatabase) {
                document.getElementById('meta-database').value = currentDatabase;
            }
            switchTab('meta');
            break;
        case 'keybuilder':
            if (currentSource) {
                document.getElementById('kb-source').value = currentSource;
            }
            document.getElementById('kb-flow').value = flowId;
            if (currentDatabase) {
                document.getElementById('kb-database').value = currentDatabase;
            }
            switchTab('keybuilder');
            break;
        case 'data':
            if (currentSource) {
                document.getElementById('data-source').value = currentSource;
            }
            document.getElementById('data-flow').value = flowId;
            if (currentDatabase) {
                document.getElementById('data-database').value = currentDatabase;
            }
            switchTab('data');
            break;
        case 'compare':
            if (currentSource) {
                document.getElementById('compare-source').value = currentSource;
            }
            document.getElementById('compare-flow').value = flowId;
            if (currentDatabase) {
                document.getElementById('compare-database').value = currentDatabase;
            }
            switchTab('compare');
            break;
    }

    // Show a brief visual feedback
    const targetTab = Array.from(document.querySelectorAll('.tab')).find(tab =>
        tab.textContent.toLowerCase().includes(tabName.toLowerCase()) ||
        (tabName === 'keybuilder' && tab.textContent.includes('Key Builder')) ||
        (tabName === 'meta' && tab.textContent.includes('Meta'))
    );

    if (targetTab) {
        targetTab.style.background = '#00cc00';
        targetTab.style.color = 'white';
        setTimeout(() => {
            targetTab.style.background = '';
            targetTab.style.color = '';
        }, 1000);
    }
}
