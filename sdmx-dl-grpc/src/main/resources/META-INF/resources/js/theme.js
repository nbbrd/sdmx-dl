// ========================================================================
// COLOR THEME (loaded in <head> to avoid a flash of the wrong theme)
// ========================================================================

const THEME_STORAGE_KEY = 'sdmxdl-theme';

/**
 * Get the preferred color theme: the stored one, otherwise the system one
 * @returns {string} - 'light' or 'dark'
 */
function getPreferredTheme() {
    const stored = localStorage.getItem(THEME_STORAGE_KEY);
    if (stored === 'light' || stored === 'dark') {
        return stored;
    }
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

/**
 * Apply a color theme to the page and notify listeners (e.g. charts)
 * @param {string} theme - 'light' or 'dark'
 */
function applyTheme(theme) {
    document.documentElement.setAttribute('data-bs-theme', theme);
    const icon = document.querySelector('#theme-toggle-btn i');
    if (icon) {
        icon.className = theme === 'dark' ? 'bi bi-sun' : 'bi bi-moon-stars';
    }
    document.dispatchEvent(new CustomEvent('themechange', { detail: { theme } }));
}

/**
 * Switch between the light and dark themes, and remember the choice
 */
function toggleTheme() {
    const theme = document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
    localStorage.setItem(THEME_STORAGE_KEY, theme);
    applyTheme(theme);
}

document.documentElement.setAttribute('data-bs-theme', getPreferredTheme());

window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
    if (!localStorage.getItem(THEME_STORAGE_KEY)) {
        applyTheme(getPreferredTheme());
    }
});

window.addEventListener('DOMContentLoaded', () => applyTheme(getPreferredTheme()));
