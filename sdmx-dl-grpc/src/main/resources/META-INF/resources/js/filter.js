// ========================================================================
// CARD FILTER FUNCTIONS
// ========================================================================

/**
 * Calculate Levenshtein distance (edit distance) between two strings
 * Used for fuzzy matching
 * @param {string} str1 - First string
 * @param {string} str2 - Second string
 * @returns {number} - Edit distance
 */
function levenshteinDistance(str1, str2) {
    const len1 = str1.length;
    const len2 = str2.length;
    const matrix = Array(len1 + 1).fill(null).map(() => Array(len2 + 1).fill(0));

    for (let i = 0; i <= len1; i++) matrix[i][0] = i;
    for (let j = 0; j <= len2; j++) matrix[0][j] = j;

    for (let i = 1; i <= len1; i++) {
        for (let j = 1; j <= len2; j++) {
            const cost = str1[i - 1] === str2[j - 1] ? 0 : 1;
            matrix[i][j] = Math.min(
                matrix[i - 1][j] + 1,      // deletion
                matrix[i][j - 1] + 1,      // insertion
                matrix[i - 1][j - 1] + cost // substitution
            );
        }
    }

    return matrix[len1][len2];
}

/**
 * Fuzzy match - checks if query fuzzy matches text
 * @param {string} text - Text to search in
 * @param {string} query - Search query
 * @param {number} threshold - Maximum edit distance (default: 2)
 * @returns {boolean} - Whether text fuzzy matches query
 */
function fuzzyMatch(text, query, threshold = 2) {
    if (!query) return true;

    text = text.toLowerCase();
    query = query.toLowerCase();

    // Exact match
    if (text.includes(query)) return true;

    // Check if query fuzzy matches any word in text
    const words = text.split(/\s+/);
    for (const word of words) {
        // For short queries, use stricter threshold
        const adjustedThreshold = query.length <= 3 ? 1 : threshold;

        // Check full word match
        if (levenshteinDistance(word, query) <= adjustedThreshold) {
            return true;
        }

        // Check if query is substring with typos
        if (word.length >= query.length) {
            for (let i = 0; i <= word.length - query.length; i++) {
                const substring = word.substring(i, i + query.length);
                if (levenshteinDistance(substring, query) <= adjustedThreshold) {
                    return true;
                }
            }
        }
    }

    return false;
}

/**
 * Generic filter matcher utility
 * @param {string} text - Text to search in
 * @param {string} query - Search query
 * @param {Object} options - Filter options
 * @returns {boolean} - Whether text matches query
 */
function matchesFilter(text, query, options = {}) {
    const { matchCase = false, wholeWords = false, isRegex = false, isFuzzy = false } = options;

    if (!query) return true;

    const searchText = matchCase ? text : text.toLowerCase();
    const searchQuery = matchCase ? query : query.toLowerCase();

    if (isFuzzy) {
        return fuzzyMatch(searchText, searchQuery);
    } else if (isRegex) {
        try {
            const regex = new RegExp(searchQuery, matchCase ? '' : 'i');
            return regex.test(searchText);
        } catch (e) {
            // Invalid regex, fall back to substring
            return searchText.includes(searchQuery);
        }
    } else if (wholeWords) {
        const escapedQuery = searchQuery.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        const regex = new RegExp('\\b' + escapedQuery + '\\b', matchCase ? '' : 'i');
        return regex.test(searchText);
    } else {
        return searchText.includes(searchQuery);
    }
}

/**
 * Generic filter setup utility for card-based views
 * @param {Object} config - Filter configuration
 */
function setupCardFilter(config) {
    const {
        filterInputId,
        filterCountId,
        clearBtnId,
        matchCaseBtnId,
        wholeWordsBtnId,
        regexBtnId,
        fuzzyBtnId,
        cardSelector,
        filterTextAttr = 'data-filter-text',
        itemName = 'items'
    } = config;

    const filterInput = document.getElementById(filterInputId);
    const filterCount = document.getElementById(filterCountId);
    const clearBtn = document.getElementById(clearBtnId);
    const matchCaseBtn = document.getElementById(matchCaseBtnId);
    const wholeWordsBtn = document.getElementById(wholeWordsBtnId);
    const regexBtn = document.getElementById(regexBtnId);
    const fuzzyBtn = document.getElementById(fuzzyBtnId);

    if (!filterInput) return;

    function performFilter() {
        const query = filterInput.value.trim();
        const cards = document.querySelectorAll(cardSelector);

        if (!query) {
            cards.forEach(card => card.classList.remove('filtered-hidden'));
            if (filterCount) {
                filterCount.textContent = `${cards.length} ${itemName}`;
            }
            return;
        }

        let visibleCount = 0;
        const options = {
            matchCase: matchCaseBtn?.classList.contains('active'),
            wholeWords: wholeWordsBtn?.classList.contains('active'),
            isRegex: regexBtn?.classList.contains('active'),
            isFuzzy: fuzzyBtn?.classList.contains('active')
        };

        cards.forEach(card => {
            const filterText = card.getAttribute(filterTextAttr);
            const matches = matchesFilter(filterText, query, options);

            if (matches) {
                card.classList.remove('filtered-hidden');
                visibleCount++;
            } else {
                card.classList.add('filtered-hidden');
            }
        });

        if (filterCount) {
            filterCount.textContent = `${visibleCount} of ${cards.length}`;
        }
    }

    // Attach event listeners
    filterInput.addEventListener('input', performFilter);

    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            filterInput.value = '';
            performFilter();
        });
    }

    [matchCaseBtn, wholeWordsBtn, regexBtn, fuzzyBtn].forEach(btn => {
        if (btn) {
            btn.addEventListener('click', function() {
                this.classList.toggle('active');
                performFilter();
            });
        }
    });

    return performFilter;
}
