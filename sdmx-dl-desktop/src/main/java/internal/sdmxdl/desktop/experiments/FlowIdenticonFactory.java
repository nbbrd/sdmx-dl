package internal.sdmxdl.desktop.experiments;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates symmetric 5×5 pixel-art identicons for SDMX flow references,
 * matching the visual output of {@code getFlowIcon()} in {@code browse.html}:
 * <ul>
 *   <li>Hash the flow ref string with the same JS-equivalent algorithm.</li>
 *   <li>Select a base color from the palette by {@code hash % palette.length}.</li>
 *   <li>For each of the 5×5 cells, use a seeded pseudo-random value ({@code sin}-based)
 *       to decide fill/no-fill, then mirror left↔right so the pattern is symmetric.</li>
 *   <li>Each filled cell gets a variable opacity in [0.7, 1.0] for depth.</li>
 * </ul>
 * Results are cached so each id is only rendered once.
 */
final class FlowIdenticonFactory {

    /** Color palette matching the one in {@code getFlowIcon()} of browse.html. */
    private static final Color[] IDENTICON_COLORS = {
            new Color(0x09, 0x69, 0xDA), new Color(0x1F, 0x88, 0x3D),
            new Color(0xCF, 0x22, 0x2E), new Color(0x82, 0x50, 0xDF),
            new Color(0xBF, 0x39, 0x89), new Color(0xFB, 0x85, 0x00),
            new Color(0x05, 0x50, 0xAE), new Color(0x11, 0x63, 0x29),
            new Color(0xA4, 0x0E, 0x26), new Color(0x66, 0x39, 0xBA),
            new Color(0xD1, 0x24, 0x2F), new Color(0x09, 0x69, 0xDA),
            new Color(0x21, 0x8B, 0xFF), new Color(0x7D, 0x4E, 0x57),
            new Color(0x95, 0x38, 0x00), new Color(0x66, 0x39, 0xBA),
            new Color(0x8B, 0x5C, 0xF6), new Color(0x3B, 0x82, 0xF6),
            new Color(0x10, 0xB9, 0x81), new Color(0xF5, 0x9E, 0x0B)
    };

    private static final int SIZE = 32;    // icon pixel size
    private static final int GRID = 5;     // cells per side
    private static final int CELL = 6;     // px per cell (5×6=30, centred in 32 with 1px offset)
    private static final int OFFSET = (SIZE - CELL * GRID) / 2; // 1px padding on each side

    private static final Map<String, Icon> CACHE = new HashMap<>();

    private FlowIdenticonFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Returns the cached identicon for {@code id}, creating it on first call.
     */
    static Icon getIcon(String id) {
        return CACHE.computeIfAbsent(id, FlowIdenticonFactory::create);
    }

    private static Icon create(String id) {
        int hash = flowHash(id);
        int absHash = (hash == Integer.MIN_VALUE) ? 0 : Math.abs(hash);
        Color base = IDENTICON_COLORS[absHash % IDENTICON_COLORS.length];

        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background: #f6f8fa (same as HTML)
        g.setColor(new Color(0xF6, 0xF8, 0xFA));
        g.fillRoundRect(0, 0, SIZE, SIZE, 4, 4);

        // Symmetric 5×5 grid: generate left half + centre, mirror to right
        int halfCols = (GRID + 1) / 2; // ceil(5/2) = 3 → cols 0,1,2
        for (int row = 0; row < GRID; row++) {
            for (int col = 0; col < halfCols; col++) {
                int seed = absHash + row * GRID + col;
                if (seededRandom(seed) > 0.5) {
                    double opacity = 0.7 + seededRandom(seed + 100) * 0.3;
                    int alpha = Math.min(255, (int) (opacity * 255));
                    g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
                    g.fillRect(OFFSET + col * CELL, OFFSET + row * CELL, CELL, CELL);
                    // Mirror left↔right (skip the middle column)
                    if (col < GRID / 2) {
                        g.fillRect(OFFSET + (GRID - 1 - col) * CELL, OFFSET + row * CELL, CELL, CELL);
                    }
                }
            }
        }
        g.dispose();
        return new ImageIcon(img);
    }

    /** Same hash as the JS {@code hashCode(str)} in browse.html. */
    private static int flowHash(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = ((hash << 5) - hash) + str.charAt(i);
        }
        return hash;
    }

    /** Same PRNG as the JS {@code seededRandom(seed)} in browse.html. */
    private static double seededRandom(int seed) {
        double x = Math.sin(seed) * 10_000;
        return x - Math.floor(x);
    }
}

