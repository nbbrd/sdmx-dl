// ========================================================================
// KEY AUTOCOMPLETE
// ========================================================================

const availabilityCache = new Map(); // availability URL -> Promise of codes map (or null on error)
const keyDimensionsCache = new Map(); // [source, flow, database] -> Promise of dimensions (or null on error)

/**
 * Get the codes of a dimension that have data, given the codes selected in the other dimensions
 * @param {{source: string, flow: string, database: string}} context - Flow of the key
 * @param {Object[]} dimensions - Dimensions of the flow, in key order
 * @param {number} dimIndex - Index of the dimension
 * @param {string[]} keyParts - Current key parts (empty or '+' means wildcard)
 * @returns {Promise<Object|null>} - Map of available codes to labels, or null if not available
 */
function fetchAvailableCodes(context, dimensions, dimIndex, keyParts) {
    const key = dimensions
        .map((dim, i) => (i === dimIndex || !keyParts[i] || keyParts[i] === '+') ? '' : keyParts[i])
        .join('.');
    const { source, flow, database } = context;
    const url = apiUrl([source, flow, 'availability'], { key, dimension: dimensions[dimIndex].id, database });
    if (!availabilityCache.has(url)) {
        availabilityCache.set(url, getJSON(url)
            .then(codesOf)
            .catch(error => {
                console.warn('Availability not supported or failed:', error.message);
                return null;
            }));
    }
    return availabilityCache.get(url);
}

/**
 * Get the dimensions of a flow, without their codes (see getKeyDimensionCodes)
 * @param {{source: string, flow: string, database: string}} context - Flow of the key
 * @returns {Promise<Object[]|null>} - Dimensions in key order, or null on error
 */
function getKeyDimensions(context) {
    const id = JSON.stringify([context.source, context.flow, context.database || '']);
    if (!keyDimensionsCache.has(id)) {
        keyDimensionsCache.set(id, getJSON(apiUrl([context.source, context.flow, 'dimensions'], { database: context.database }))
            .then(result => itemsOf(result).sort((a, b) => (a.index || 0) - (b.index || 0)))
            .catch(error => {
                console.warn('Key autocomplete: dimensions not available:', error.message);
                keyDimensionsCache.delete(id);
                return null;
            }));
    }
    return keyDimensionsCache.get(id);
}

/**
 * Get the codes of a dimension, loading them on first use
 * @param {{source: string, flow: string, database: string}} context - Flow of the key
 * @param {Object} dimension - Dimension as returned by getKeyDimensions
 * @returns {Promise<Object>} - Map of codes to labels (empty if the dimension has no codelist)
 */
async function getKeyDimensionCodes(context, dimension) {
    if (!dimension.codelist) {
        return {};
    }
    if (!dimension.codelist.codes) {
        dimension.codelist.codes = getJSON(apiUrl([context.source, context.flow, 'codes', dimension.id], { database: context.database }))
            .then(codesOf)
            .catch(error => {
                console.warn('Key autocomplete: codes not available:', error.message);
                delete dimension.codelist.codes;
                return {};
            });
    }
    return dimension.codelist.codes;
}

/**
 * Add autocompletion of dimension codes to a key input.
 * The code being typed is the one after the last dot (and after the last '+' for multiple values).
 * @param {HTMLInputElement} input - Key input
 * @param {Function} getContext - Returns the current {source, flow, database, useAvailability, dimensions?}
 *                                or null when no flow is set; dimensions are loaded if not provided
 */
function attachKeyAutocomplete(input, getContext) {
    if (!input || input.dataset.keyAutocomplete) return;
    input.dataset.keyAutocomplete = 'true';

    let dimensionCount = 0;

    const currentToken = part => (part !== '+' && part.includes('+')) ? part.substring(part.lastIndexOf('+') + 1) : part;

    const fetch = async () => {
        const context = getContext();
        if (!context || !context.source || !context.flow) return null;

        const dimensions = context.dimensions || await getKeyDimensions(context);
        if (!dimensions) return null;
        dimensionCount = dimensions.length;

        const parts = input.value.split('.');
        const dimIndex = parts.length - 1;
        if (dimIndex >= dimensions.length) return null;

        const [allCodes, available] = await Promise.all([
            getKeyDimensionCodes(context, dimensions[dimIndex]),
            context.useAvailability ? fetchAvailableCodes(context, dimensions, dimIndex, parts) : Promise.resolve(null)
        ]);

        const codes = available
            ? Object.fromEntries(Object.entries(allCodes).filter(([code]) => code in available))
            : allCodes;
        const lastPart = parts[dimIndex];
        const token = currentToken(lastPart).toLowerCase();
        const items = Object.keys(codes)
            .filter(code => code.toLowerCase().startsWith(token) || String(codes[code]).toLowerCase().includes(token))
            .sort()
            .map(code => ({ value: code, label: codes[code] }));

        if (items.length > 0 && (lastPart === '' || lastPart === '+')) {
            items.unshift({ value: '+', label: 'All values' });
        }
        return items;
    };

    const apply = item => {
        const parts = input.value.split('.');
        const last = parts[parts.length - 1];
        const token = currentToken(last);
        parts[parts.length - 1] = last.substring(0, last.length - token.length) + item.value;
        input.value = parts.join('.') + (parts.length < dimensionCount ? '.' : '');
    };

    const onClosedKeydown = e => {
        if (e.key === 'Tab' && dimensionCount > 0 && input.value
            && input.value.split('.').length < dimensionCount && !input.value.endsWith('.')) {
            e.preventDefault();
            input.value += '.';
            input.dispatchEvent(new Event('input', { bubbles: true }));
        }
    };

    attachSuggestions(input, {
        fetch,
        apply,
        reopen: true,
        showOnFocus: true,
        selectKeys: ['Enter', 'Tab', '.'],
        onClosedKeydown
    });
}
/**
 * Get the key autocomplete context of a tab from its source, flow and database fields
 * @param {string} prefix - Prefix of the field ids (e.g. 'data')
 * @returns {Object|null} - Context for attachKeyAutocomplete
 */
function getTabKeyContext(prefix) {
    const source = document.getElementById(`${prefix}-source`)?.value.trim();
    const flow = document.getElementById(`${prefix}-flow`)?.value.trim();
    const database = document.getElementById(`${prefix}-database`)?.value.trim();
    return source && flow ? { source, flow, database, useAvailability: true } : null;
}

/**
 * Add key autocompletion to the key fields of the Data and Compare tabs
 */
function setupKeyAutocomplete() {
    ['data', 'compare'].forEach(prefix =>
        attachKeyAutocomplete(document.getElementById(`${prefix}-key`), () => getTabKeyContext(prefix)));
}
