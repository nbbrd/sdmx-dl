package internal.sdmxdl.desktop.experiments;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

/**
 * Generic list cell renderer that paints an icon, a main (bold) line and a secondary
 * (muted) line directly via {@link Graphics2D} — no nested Swing components are composed.
 * Long text is clipped to the available width with a trailing ellipsis,
 * computed with a binary search over {@link FontMetrics#stringWidth}.
 *
 * @param <T> the type of value displayed by the list
 */
final class ListItemRenderer<T> extends JComponent implements ListCellRenderer<T> {

    /**
     * Icon provider: receives the item and a {@code repaint} callback so async icons
     * (e.g. favicons loaded in the background) can trigger a list repaint when they arrive.
     */
    @FunctionalInterface
    interface IconProvider<T> {
        Icon getIcon(T item, Runnable repaint);
    }

    // Matches BrowsePanel.PRIMARY / CHIP_BORDER so renderers look consistent by default
    private static final Color ACCENT_COLOR = new Color(0x00, 0x3D, 0x6A);
    private static final Color SEPARATOR_COLOR = new Color(0xE0, 0xE0, 0xE0);

    private static final int PAD_H = 10;
    private static final int PAD_V = 6;
    private static final int ICON_GAP = 8;
    private static final int ACCENT_W = 3;
    private static final int TEXT_GAP = 2;
    private static final String ELLIPSIS = "…";

    private final IconProvider<T> iconProvider;
    private final Function<T, String> mainTextFn;
    private final Function<T, String> secondaryTextFn;
    private final Function<T, String> tooltipFn;

    // Mutable rendering state — set in getListCellRendererComponent, read in paintComponent
    private Icon currentIcon;
    private String currentMain = "";
    private String currentSecondary = "";
    private boolean isSelected;

    ListItemRenderer(
            IconProvider<T> iconProvider,
            Function<T, String> mainTextFn,
            Function<T, String> secondaryTextFn,
            Function<T, String> tooltipFn) {
        this.iconProvider = iconProvider;
        this.mainTextFn = mainTextFn;
        this.secondaryTextFn = secondaryTextFn;
        this.tooltipFn = tooltipFn;
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends T> list, T value, int index, boolean selected, boolean cellHasFocus) {
        this.currentIcon = (value != null) ? iconProvider.getIcon(value, list::repaint) : null;
        this.currentMain = (value != null) ? mainTextFn.apply(value) : "";
        this.currentSecondary = (value != null) ? secondaryTextFn.apply(value) : "";
        this.isSelected = selected;
        setBackground(selected ? list.getSelectionBackground() : list.getBackground());
        setForeground(selected ? list.getSelectionForeground() : list.getForeground());
        setFont(list.getFont());
        setToolTipText((value != null && tooltipFn != null) ? tooltipFn.apply(value) : null);
        return this;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();

            // Background
            g.setColor(getBackground());
            g.fillRect(0, 0, w, h);

            // Left accent bar
            g.setColor(ACCENT_COLOR);
            g.fillRect(0, 0, ACCENT_W, h);

            // Icon (vertically centred)
            int contentX = ACCENT_W + PAD_H;
            if (currentIcon != null) {
                int iconY = (h - currentIcon.getIconHeight()) / 2;
                currentIcon.paintIcon(this, g, contentX, iconY);
                contentX += currentIcon.getIconWidth() + ICON_GAP;
            }

            int availW = w - contentX - PAD_H;

            // Fonts & metrics
            Font mainFont = getFont().deriveFont(Font.BOLD, 13f);
            Font secFont = getFont().deriveFont(Font.PLAIN, 11f);
            FontMetrics mainFm = g.getFontMetrics(mainFont);
            FontMetrics secFm = g.getFontMetrics(secFont);

            // Vertical layout: centre the two-line block
            int blockH = mainFm.getHeight() + TEXT_GAP + secFm.getHeight();
            int blockTop = Math.max(PAD_V, (h - blockH) / 2);

            // Main text
            g.setFont(mainFont);
            g.setColor(getForeground());
            g.drawString(ellipsize(currentMain, mainFm, availW),
                    contentX, blockTop + mainFm.getAscent());

            // Secondary text (muted when not selected)
            g.setFont(secFont);
            g.setColor(isSelected ? getForeground() : new Color(0x66, 0x66, 0x66));
            g.drawString(ellipsize(currentSecondary, secFm, availW),
                    contentX, blockTop + mainFm.getHeight() + TEXT_GAP + secFm.getAscent());

            // Bottom separator (only when not selected)
            if (!isSelected) {
                g.setColor(SEPARATOR_COLOR);
                g.drawLine(ACCENT_W, h - 1, w - 1, h - 1);
            }
        } finally {
            g.dispose();
        }
    }

    /** Clips {@code text} to {@code maxWidth} px using binary search, appending "…" if needed. */
    private static String ellipsize(String text, FontMetrics fm, int maxWidth) {
        if (text == null || text.isEmpty()) return "";
        if (fm.stringWidth(text) <= maxWidth) return text;
        int ellipsisW = fm.stringWidth(ELLIPSIS);
        int available = maxWidth - ellipsisW;
        if (available <= 0) return ELLIPSIS;
        int lo = 0, hi = text.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;
            if (fm.stringWidth(text.substring(0, mid)) <= available) lo = mid;
            else hi = mid - 1;
        }
        return text.substring(0, lo) + ELLIPSIS;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 54);
    }
}

