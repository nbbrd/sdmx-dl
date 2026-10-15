// ========================================================================
// KEY BUILDER FUNCTIONALITY
// ========================================================================

// Key Builder functionality
let currentStructure = null;
let dimensionValues = {};
let keyBuilderCallbackUrl = null; // Callback URL for sending key data

// Availability state
let keyBuilderContext = null; // { source, flow, database } of the loaded structure
let useAvailability = true;
let guidedAvailabilityGeneration = 0;

/**
 * Get the codes of a dimension that have data, given the codes selected in the other dimensions
 * @param {number} dimIndex - Index of the dimension
 * @param {string[]} keyParts - Current key parts (empty or '+' means wildcard)
 * @returns {Promise<Object|null>} - Map of available codes to labels, or null if not available
 */
function getAvailableCodes(dimIndex, keyParts) {
    if (!useAvailability || !keyBuilderContext || !currentStructure) {
        return Promise.resolve(null);
    }
    return fetchAvailableCodes(keyBuilderContext, currentStructure.dimensions, dimIndex, keyParts);
}

/**
 * Enable or disable the filtering of codes by data availability
 * @param {boolean} enabled - Whether to filter codes
 */
function setUseAvailability(enabled) {
    useAvailability = enabled;
    refreshGuidedAvailability();
    if (document.getElementById('hierarchy-key-container').style.display !== 'none') {
        renderHierarchyBuilder();
    }
}

/**
 * Rebuild the options of the guided mode selects according to data availability
 */
async function refreshGuidedAvailability() {
    if (!currentStructure || !currentStructure.dimensions) return;

    const generation = ++guidedAvailabilityGeneration;
    const dimensions = currentStructure.dimensions;
    const keyParts = dimensions.map(dim => document.getElementById(`dim-${dim.id}`)?.value || '');

    const results = await Promise.all(dimensions.map((dim, index) =>
        dim.codelist && dim.codelist.codes ? getAvailableCodes(index, keyParts) : Promise.resolve(null)));

    if (generation !== guidedAvailabilityGeneration) return;

    dimensions.forEach((dim, index) => {
        const select = document.getElementById(`dim-${dim.id}`);
        if (!select || select.tagName !== 'SELECT') return;

        const fullCodes = JSON.parse(select.dataset.fullCodelist);
        const available = results[index];
        const selected = select.value;
        const codes = Object.keys(fullCodes).sort()
            .filter(code => !available || code in available || code === selected);

        while (select.options.length > 2) {
            select.remove(2);
        }
        codes.forEach(code => {
            const option = document.createElement('option');
            option.value = code;
            option.textContent = `${code} - ${fullCodes[code]}`;
            if (available && !(code in available)) {
                option.textContent += ' (no data)';
            }
            select.appendChild(option);
        });
        select.value = selected;
        if (select.tomselect) {
            // sync() only adds options, so drop all the previous ones first (keeps native order)
            select.tomselect.clearOptions(() => false);
            select.tomselect.sync();
        }

        const helpText = document.getElementById(`help-${dim.id}`);
        const total = Object.keys(fullCodes).length;
        helpText.textContent = available
            ? `${Object.keys(available).filter(code => code in fullCodes).length} of ${total} codes available`
            : `${total} available codes`;
    });
}

/**
 * Hide the hierarchy nodes that have no data at the current level
 * @param {number} level - Current dimension level
 */
async function filterHierarchyByAvailability(level) {
    const keyParts = currentStructure.dimensions.map(dim => hierarchySelections[dim.id] || '');
    const available = await getAvailableCodes(level, keyParts);
    if (!available || getHierarchyPath().length !== level) return;

    const tree = document.getElementById('hierarchy-tree-root');
    if (!tree) return;

    let count = 0;
    tree.querySelectorAll('.hierarchy-node[data-code]').forEach(node => {
        const code = node.dataset.code;
        if (code === '+' || code in available) {
            if (code !== '+') count++;
        } else {
            node.remove();
        }
    });
    const stats = document.getElementById('hierarchy-stats');
    if (stats) {
        stats.textContent = `${count} codes with available data`;
    }
}



function switchKeyInputMode(mode) {
    const guidedMode = document.getElementById('dimensions-container');
    const quickMode = document.getElementById('quick-key-container');
    const hierarchyMode = document.getElementById('hierarchy-key-container');
    const toggleButtons = document.querySelectorAll('.key-input-mode-toggle button');

    toggleButtons.forEach(btn => btn.classList.remove('active'));

    // Hide all modes
    guidedMode.style.display = 'none';
    quickMode.style.display = 'none';
    hierarchyMode.style.display = 'none';

    if (mode === 'guided') {
        guidedMode.style.display = 'block';
        toggleButtons[0].classList.add('active');
    } else if (mode === 'quick') {
        quickMode.style.display = 'block';
        toggleButtons[1].classList.add('active');

        // Focus on quick input
        setTimeout(() => {
            document.getElementById('quick-key-input').focus();
        }, 100);
    } else if (mode === 'hierarchy') {
        hierarchyMode.style.display = 'block';
        toggleButtons[2].classList.add('active');

        // Initialize hierarchy builder if structure is loaded
        if (currentStructure && currentStructure.dimensions) {
            renderHierarchyBuilder();
        }
    }
}


// Hierarchy Builder State
let hierarchySelections = {};
let hierarchyExpanded = {};


// ========================================================================
// HIERARCHY BUILDER FUNCTIONS
// ========================================================================

/**
 * Render the hierarchy builder as a true tree:
 * Level 1 = Dimension 1 values
 * Level 2 = Dimension 2 values (children of each Dimension 1 value)
 * Level 3 = Dimension 3 values (children of each Dimension 2 value)
 * etc.
 */
function renderHierarchyBuilder() {
    if (!currentStructure || !currentStructure.dimensions) return;

    const container = document.getElementById('hierarchy-builder-content');
    let html = '';

    // Progress info
    const selectedPath = getHierarchyPath();
    const currentLevel = selectedPath.length;
    const totalLevels = currentStructure.dimensions.length;

    html += '<div class="tree-progress">';
    html += '<div class="tree-progress-label">Key Building Progress</div>';
    html += '<div class="tree-progress-bar-container">';
    const progress = (currentLevel / totalLevels) * 100;
    html += `<div class="tree-progress-bar" style="width: ${progress}%"></div>`;
    html += '</div>';
    html += `<div class="tree-progress-text">${currentLevel} of ${totalLevels} dimensions selected</div>`;
    html += '</div>';

    // Show current path (breadcrumb)
    if (selectedPath.length > 0) {
        html += '<div class="hierarchy-breadcrumb">';
        selectedPath.forEach((item, index) => {
            const dim = currentStructure.dimensions[index];
            html += `<div class="hierarchy-breadcrumb-item" onclick="navigateToLevel(${index})" title="${escapeHtml(dim.name || dim.id)}">`;
            html += `<strong>${escapeHtml(dim.id)}:</strong> ${escapeHtml(item.label)}`;
            html += '</div>';
            if (index < selectedPath.length - 1) {
                html += '<span class="hierarchy-breadcrumb-separator">→</span>';
            }
        });
        html += '</div>';
    }

    // Current dimension to select
    const currentDimension = currentStructure.dimensions[currentLevel];

    if (currentDimension) {
        html += '<div class="hierarchy-dimension">';
        html += '<div class="hierarchy-dimension-header">';
        html += '<div class="hierarchy-dimension-info">';
        html += `<h4>Select ${escapeHtml(currentDimension.name || currentDimension.id)}</h4>`;
        html += `<div class="hierarchy-dimension-id">Dimension ${currentLevel + 1} of ${totalLevels}: ${escapeHtml(currentDimension.id)}</div>`;
        html += '</div>';
        html += '</div>';

        // Search box
        if (currentDimension.codelist && currentDimension.codelist.codes) {
            html += '<div class="hierarchy-search">';
            html += `<input type="text" class="form-control" id="hierarchy-search-input" placeholder="Search codes..." onkeyup="filterHierarchyTree(this.value)" />`;
            html += '</div>';

            const codes = currentDimension.codelist.codes;
            const codeEntries = Object.entries(codes);

            html += `<div class="hierarchy-stats" id="hierarchy-stats">${codeEntries.length} available codes</div>`;

            // Render tree of available codes
            html += '<div class="hierarchy-tree" id="hierarchy-tree-root">';

            // Add wildcard option
            html += renderHierarchyTreeNode(currentLevel, '+', 'All values (wildcard)', true);

            // Add all code options
            codeEntries.sort((a, b) => a[0].localeCompare(b[0])).forEach(([code, label]) => {
                html += renderHierarchyTreeNode(currentLevel, code, label, false);
            });

            html += '</div>';
        } else {
            // No codelist - text input
            html += '<div class="p-3">';
            html += `<input type="text" class="form-control" id="hierarchy-manual-input" placeholder="Enter value or use + for wildcard" />`;
            html += '<div class="d-flex gap-2 mt-2">';
            html += '<button class="btn btn-primary flex-fill" type="button" onclick="selectManualValue()">Confirm Value</button>';
            html += '<button class="btn btn-warning flex-fill" type="button" onclick="selectHierarchyValue(\'+\')">Use Wildcard (+)</button>';
            html += '</div>';
            html += '</div>';
        }

        html += '</div>';
    } else {
        // All dimensions selected - show completion
        html += '<div class="hierarchy-dimension hierarchy-complete">';
        html += '<div class="hierarchy-dimension-header">';
        html += '<div class="hierarchy-dimension-info">';
        html += `<h4>${icon('check-circle', 'text-success')} Key Complete!</h4>`;
        html += '<div class="hierarchy-dimension-id">All dimensions have been selected</div>';
        html += '</div>';
        html += '</div>';
        html += '<div class="p-3 text-center">';
        html += '<p class="text-success-emphasis mb-3">Your SDMX key has been successfully built.</p>';
        html += `<button class="btn btn-primary" type="button" onclick="navigateToLevel(0)">${icon('arrow-counterclockwise')} Start Over</button>`;
        html += '</div>';
        html += '</div>';
    }

    container.innerHTML = html;
    updateGeneratedKey();

    if (currentDimension && currentDimension.codelist && currentDimension.codelist.codes) {
        filterHierarchyByAvailability(currentLevel);
    }
}

/**
 * Render a single tree node (represents one code option at current dimension level)
 */
function renderHierarchyTreeNode(level, code, label, isWildcard) {
    const cssClass = isWildcard ? 'hierarchy-node-wildcard' : 'hierarchy-node-code';

    let html = `<div class="hierarchy-node ${cssClass}" data-code="${escapeHtml(code)}" onclick="selectHierarchyValue(${jsArg(code)})">`;
    html += '<div class="hierarchy-node-content">';

    if (isWildcard) {
        html += icon('asterisk', 'hierarchy-wildcard-icon');
    }

    html += '<div class="hierarchy-node-label">';
    html += `<div class="hierarchy-node-code">${escapeHtml(code)}</div>`;
    html += `<div class="hierarchy-node-name">${escapeHtml(label)}</div>`;
    html += '</div>';

    // Show arrow indicating there are more levels
    const currentPath = getHierarchyPath();
    if (currentPath.length < currentStructure.dimensions.length - 1) {
        html += icon('chevron-right', 'hierarchy-node-indicator text-body-secondary');
    } else {
        html += icon('check-lg', 'hierarchy-node-indicator text-success');
    }

    html += '</div>';
    html += '</div>';

    return html;
}

/**
 * Get the current selection path as an array
 */
function getHierarchyPath() {
    const path = [];
    for (let i = 0; i < currentStructure.dimensions.length; i++) {
        const dim = currentStructure.dimensions[i];
        if (hierarchySelections[dim.id]) {
            const code = hierarchySelections[dim.id];
            const label = code === '+' ? 'All values' :
                          (dim.codelist?.codes?.[code] || code);
            path.push({ code, label, dimensionId: dim.id });
        } else {
            break;
        }
    }
    return path;
}

/**
 * Navigate back to a specific level in the tree
 */
function navigateToLevel(level) {
    // Clear selections from this level onwards
    for (let i = level; i < currentStructure.dimensions.length; i++) {
        const dim = currentStructure.dimensions[i];
        delete hierarchySelections[dim.id];
    }
    renderHierarchyBuilder();
}

/**
 * Select a value at the current dimension level
 */
function selectHierarchyValue(code) {
    const currentPath = getHierarchyPath();
    const currentLevel = currentPath.length;

    if (currentLevel < currentStructure.dimensions.length) {
        const currentDimension = currentStructure.dimensions[currentLevel];
        hierarchySelections[currentDimension.id] = code;
        renderHierarchyBuilder();
    }
}

/**
 * Select manual input value
 */
function selectManualValue() {
    const input = document.getElementById('hierarchy-manual-input');
    if (input && input.value.trim()) {
        selectHierarchyValue(input.value.trim());
    }
}

/**
 * Filter the tree display based on search text
 */
function filterHierarchyTree(searchText) {
    const tree = document.getElementById('hierarchy-tree-root');
    if (!tree) return;

    const search = searchText.toLowerCase();
    const nodes = tree.querySelectorAll('.hierarchy-node');

    if (!search) {
        // Show all nodes
        nodes.forEach(node => {
            node.style.display = '';
        });
        return;
    }

    // Filter nodes
    nodes.forEach(node => {
        const codeEl = node.querySelector('.hierarchy-node-code');
        const nameEl = node.querySelector('.hierarchy-node-name');

        if (codeEl && nameEl) {
            const code = codeEl.textContent.toLowerCase();
            const name = nameEl.textContent.toLowerCase();

            if (code.includes(search) || name.includes(search)) {
                node.style.display = '';
            } else {
                node.style.display = 'none';
            }
        }
    });
}

// Loads the dimensions skeleton and their codes, avoiding the full structure (attributes included)
async function fetchKeyBuilderDimensions(source, flow, database) {
    const dimensions = itemsOf(await getJSON(apiUrl([source, flow, 'dimensions'], { database })));
    dimensions.sort((a, b) => (a.index || 0) - (b.index || 0));
    await Promise.all(dimensions
        .filter(dim => dim.codelist)
        .map(async dim => {
            const result = await getJSON(apiUrl([source, flow, 'codes', dim.id], { database }));
            dim.codelist.codes = codesOf(result);
        }));
    return dimensions;
}

function loadKeyBuilderStructure() {
    const source = document.getElementById('kb-source').value;
    const flow = document.getElementById('kb-flow').value;
    const database = document.getElementById('kb-database').value;

    if (!source || !flow) {
        notify('Please enter both source ID and flow ID', 'warning');
        return;
    }

    // Show loading
    const dimensionsContainer = document.getElementById('dimensions-container');
    const dimensionsList = document.getElementById('dimensions-list');
    dimensionsList.innerHTML = loadingMessage('Loading structure...');
    dimensionsContainer.style.display = 'block';

    fetchKeyBuilderDimensions(source, flow, database)
    .then(dimensions => {
        if (dimensions.length > 0) {
            currentStructure = { dimensions };
            keyBuilderContext = { source, flow, database };
            availabilityCache.clear();
            dimensionValues = {};
            hierarchySelections = {}; // Reset hierarchy selections
            hierarchyExpanded = {}; // Reset hierarchy expanded state
            // The browser may restore the checkbox state without firing a change event
            useAvailability = document.getElementById('kb-use-availability').checked;
            renderDimensionInputs(dimensions);
            if (useAvailability) {
                refreshGuidedAvailability();
            }

            // Show mode toggle and setup quick input
            document.getElementById('key-input-mode-toggle').style.display = 'flex';
            document.getElementById('kb-availability-option').style.display = 'block';
            setupQuickKeyInput();

            // Show Generated SDMX Key section and render action buttons
            document.getElementById('built-key-container').style.display = 'block';
            renderKeyActions();

            // Initialize hierarchy builder
            renderHierarchyBuilder();
        } else {
            throw new Error('No structure dimensions found');
        }
    })
    .catch(error => {
        dimensionsList.innerHTML = errorMessage(error.message);
    });
}

function renderDimensionInputs(dimensions) {
    const dimensionsList = document.getElementById('dimensions-list');
    dimensionsList.innerHTML = '';

    dimensions.forEach((dimension, index) => {
        const dimRow = document.createElement('div');
        dimRow.className = 'dimension-row';

        // Dimension label
        const label = document.createElement('div');
        label.className = 'dimension-label';
        label.innerHTML = `
            <div>${escapeHtml(dimension.name || dimension.id)}</div>
            <span class="dim-id">${escapeHtml(dimension.id)}</span>
        `;

        // Dimension input
        const inputDiv = document.createElement('div');
        inputDiv.className = 'dimension-input';

        if (dimension.codelist && dimension.codelist.codes) {
            // Create select dropdown with codes
            const select = document.createElement('select');
            select.className = 'form-select';
            select.id = `dim-${dimension.id}`;
            select.dataset.dimensionIndex = index;

            // Add empty option
            const emptyOption = document.createElement('option');
            emptyOption.value = '';
            emptyOption.textContent = '-- Select a value --';
            select.appendChild(emptyOption);

            // Add wildcard option
            const wildcardOption = document.createElement('option');
            wildcardOption.value = '+';
            wildcardOption.textContent = '+ (All values)';
            select.appendChild(wildcardOption);

            // Add all codes initially
            const codes = dimension.codelist.codes;
            Object.keys(codes).sort().forEach(code => {
                const option = document.createElement('option');
                option.value = code;
                option.textContent = `${code} - ${codes[code]}`;
                select.appendChild(option);
            });

            // Store full codelist for reference
            select.dataset.fullCodelist = JSON.stringify(codes);

            select.addEventListener('change', async () => {
                updateGeneratedKey();
                if (useAvailability) {
                    refreshGuidedAvailability();
                }
            });
            inputDiv.appendChild(select);

            // Add help text (will be updated dynamically)
            const helpText = document.createElement('div');
            helpText.className = 'help-text';
            helpText.id = `help-${dimension.id}`;
            helpText.textContent = `${Object.keys(codes).length} available codes`;
            inputDiv.appendChild(helpText);

        } else {
            // Create text input with wildcard button
            const inputWrapper = document.createElement('div');
            inputWrapper.className = 'input-group';

            const input = document.createElement('input');
            input.type = 'text';
            input.className = 'form-control';
            input.id = `dim-${dimension.id}`;
            input.dataset.dimensionIndex = index;
            input.placeholder = 'Enter value or leave empty';
            input.addEventListener('input', async () => {
                updateGeneratedKey();
            });

            const wildcardBtn = document.createElement('button');
            wildcardBtn.className = 'btn btn-outline-secondary wildcard-btn';
            wildcardBtn.type = 'button';
            wildcardBtn.textContent = 'Use +';
            wildcardBtn.onclick = async (e) => {
                e.preventDefault();
                input.value = '+';
                updateGeneratedKey();
            };

            inputWrapper.appendChild(input);
            inputWrapper.appendChild(wildcardBtn);
            inputDiv.appendChild(inputWrapper);

            const helpText = document.createElement('div');
            helpText.className = 'help-text';
            helpText.id = `help-${dimension.id}`;
            helpText.textContent = 'No codelist available - enter value manually';
            inputDiv.appendChild(helpText);
        }

        dimRow.appendChild(label);
        dimRow.appendChild(inputDiv);
        dimensionsList.appendChild(dimRow);
    });

    dimensionsList.querySelectorAll('select').forEach(enhanceDimensionSelect);
    updateGeneratedKey();
}

/**
 * Turn a dimension select into a searchable dropdown
 * @param {HTMLSelectElement} select - Native select, kept in sync and still holding the value
 */
function enhanceDimensionSelect(select) {
    if (typeof TomSelect === 'undefined') return;
    new TomSelect(select, {
        allowEmptyOption: true,
        maxOptions: null,
        sortField: [{ field: '$score' }, { field: '$order' }],
        create: false
    });
}

function updateGeneratedKey() {
    if (!currentStructure || !currentStructure.dimensions) {
        return;
    }

    const keyParts = [];

    // Check which mode is active
    const hierarchyMode = document.getElementById('hierarchy-key-container');
    const isHierarchyMode = hierarchyMode && hierarchyMode.style.display !== 'none';

    currentStructure.dimensions.forEach(dimension => {
        let value = '';

        if (isHierarchyMode && hierarchySelections.hasOwnProperty(dimension.id)) {
            // Use hierarchy builder selection
            value = hierarchySelections[dimension.id];
        } else {
            // Use guided mode input
            const input = document.getElementById(`dim-${dimension.id}`);
            if (input) {
                value = input.value.trim();
            }
        }

        keyParts.push(value || '');
    });

    const generatedKey = keyParts.join('.');
    document.getElementById('generated-key').textContent = generatedKey || '-';
}


function copyKeyToClipboard(btn) {
    const keyText = document.getElementById('generated-key').textContent;
    if (keyText === '-') {
        notify('Please build a key first', 'warning');
        return;
    }

    navigator.clipboard.writeText(keyText).then(() => {
        notify('Key copied!', 'success');
    }).catch(err => {
        notify('Failed to copy: ' + err, 'danger');
    });
}

function useKeyInDataTab() {
    const keyText = document.getElementById('generated-key').textContent;
    if (keyText === '-') {
        notify('Please build a key first', 'warning');
        return;
    }

    // Copy values to data tab
    document.getElementById('data-source').value = document.getElementById('kb-source').value;
    document.getElementById('data-flow').value = document.getElementById('kb-flow').value;
    document.getElementById('data-database').value = document.getElementById('kb-database').value;
    document.getElementById('data-key').value = keyText;

    // Switch to data tab
    switchTab('data');

    flashTab('data');
}

function useKeyInCompareTab() {
    const keyText = document.getElementById('generated-key').textContent;
    if (keyText === '-') {
        notify('Please build a key first', 'warning');
        return;
    }

    // Copy values to compare tab
    document.getElementById('compare-source').value = document.getElementById('kb-source').value;
    document.getElementById('compare-flow').value = document.getElementById('kb-flow').value;
    document.getElementById('compare-database').value = document.getElementById('kb-database').value;
    document.getElementById('compare-key').value = keyText;

    // Switch to compare tab
    switchTab('compare');

    flashTab('compare');
}

function resetKeyBuilder() {
    if (!currentStructure || !currentStructure.dimensions) {
        return;
    }

    // Reset guided mode inputs
    currentStructure.dimensions.forEach(dimension => {
        const input = document.getElementById(`dim-${dimension.id}`);
        if (input?.tomselect) {
            input.tomselect.clear(true);
        } else if (input) {
            input.value = '';
        }
    });

    // Reset quick key input
    const quickInput = document.getElementById('quick-key-input');
    if (quickInput) {
        quickInput.value = '';
    }

    // Reset hierarchy selections
    hierarchySelections = {};

    // Re-render hierarchy builder if in hierarchy mode
    const hierarchyMode = document.getElementById('hierarchy-key-container');
    if (hierarchyMode && hierarchyMode.style.display !== 'none') {
        renderHierarchyBuilder();
    }

    updateGeneratedKey();
    if (useAvailability) {
        refreshGuidedAvailability();
    }
}

function renderKeyActions() {
    const keyActionsDiv = document.getElementById('key-actions');
    if (!keyActionsDiv) return;

    if (keyBuilderCallbackUrl) {
        // Render Send button for callback mode
        keyActionsDiv.innerHTML = `
            <button class="btn btn-primary" type="button" onclick="sendKeyToCallback()">${icon('send')} Send</button>
            <button class="btn btn-outline-secondary" type="button" onclick="resetKeyBuilder()">${icon('arrow-clockwise')} Reset</button>
        `;
    } else {
        // Render regular buttons
        keyActionsDiv.innerHTML = `
            <button class="btn btn-outline-primary" type="button" onclick="copyKeyToClipboard(this)">${icon('copy')} Copy Key</button>
            <button class="btn btn-primary" type="button" onclick="useKeyInDataTab()">${icon('arrow-right')} Use in Data Tab</button>
            <button class="btn btn-outline-primary" type="button" onclick="useKeyInCompareTab()">${icon('arrow-left-right')} Use in Compare Tab</button>
            <button class="btn btn-outline-secondary" type="button" onclick="resetKeyBuilder()">${icon('arrow-clockwise')} Reset</button>
        `;
    }
}

async function sendKeyToCallback() {
    const keyText = document.getElementById('generated-key').textContent;
    if (keyText === '-') {
        notify('Please build a key first', 'warning');
        return;
    }

    if (!keyBuilderCallbackUrl) {
        notify('No callback URL configured', 'warning');
        return;
    }

    const data = {
        source: document.getElementById('kb-source').value,
        flow: document.getElementById('kb-flow').value,
        database: document.getElementById('kb-database').value,
        key: keyText
    };

    try {
        const response = await fetch(keyBuilderCallbackUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        // Show success message
        notify('Key data sent successfully!', 'success');

        // Optionally close window if opened as popup
        if (window.opener) {
            window.close();
        }
    } catch (error) {
        notify('Failed to send data: ' + error.message, 'danger');
        console.error('Error sending to callback:', error);
    }
}

function setupQuickKeyInput() {
    const quickInput = document.getElementById('quick-key-input');
    attachKeyAutocomplete(quickInput, () => keyBuilderContext && currentStructure
        ? { ...keyBuilderContext, dimensions: currentStructure.dimensions, useAvailability }
        : null);
    if (quickInput && !quickInput.dataset.generatedKeySync) {
        quickInput.dataset.generatedKeySync = 'true';
        quickInput.addEventListener('input', () => {
            document.getElementById('generated-key').textContent = quickInput.value || '-';
        });
    }
}