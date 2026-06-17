package sdmxdl.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Swing {@link Icon} that renders the SDMX logo using Java2D drawing calls.
 * <p>
 * Derived from {@code sdmxdl/desktop/SDMX_logo.svg} (viewBox 40 30 152 152).
 * The two interlocked arrow shapes are painted with two shades of blue.
 */
public final class SdmxLogo implements Icon {

    private static final Color COLOR_LIGHT = new Color(0x60, 0xB5, 0xEF);
    private static final Color COLOR_DARK = new Color(0x42, 0xA0, 0xD3);

    // SVG viewBox origin and dimensions
    private static final float VB_X = 40f;
    private static final float VB_Y = 30f;
    private static final float VB_SIZE = 152f;

    private final int size;

    public SdmxLogo(int size) {
        this.size = size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Map viewBox coordinates to icon coordinates
            g2.translate(x, y);
            float scale = size / VB_SIZE;
            g2.scale(scale, scale);
            g2.translate(-VB_X, -VB_Y);

            g2.setColor(COLOR_LIGHT);
            g2.fill(createLeftArrow());
            g2.fill(createRightArrow());

            g2.setColor(COLOR_DARK);
            g2.fill(createOverlap());
        } finally {
            g2.dispose();
        }
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    /**
     * Left/bottom arrow shape (st1 — #60B5EF).
     * <p>
     * SVG path: {@code M81.5,112.7l-5.8-19.3L95,99.2l-4.8,4.8l28.6,28.6l22.9-22.9
     * L90.7,59.5c-1.2-1.1-3-1.1-4.2,0l-45.6,45.7c-1.2,1.2-1.2,3.1,0,4.2
     * l48.9,48.1c1.2,1.1,3,1.1,4.2,0l20.9-20.9L86.2,108L81.5,112.7z}
     */
    private static Path2D createLeftArrow() {
        Path2D p = new Path2D.Float();
        p.moveTo(81.5f, 112.7f);
        p.lineTo(75.7f, 93.4f);       // l-5.8,-19.3
        p.lineTo(95.0f, 99.2f);        // L95,99.2
        p.lineTo(90.2f, 104.0f);       // l-4.8,4.8
        p.lineTo(118.8f, 132.6f);      // l28.6,28.6
        p.lineTo(141.7f, 109.7f);      // l22.9,-22.9
        p.lineTo(90.7f, 59.5f);        // L90.7,59.5
        p.curveTo(89.5f, 58.4f, 87.7f, 58.4f, 86.5f, 59.5f);   // c-1.2-1.1-3-1.1-4.2,0
        p.lineTo(40.9f, 105.2f);       // l-45.6,45.7
        p.curveTo(39.7f, 106.4f, 39.7f, 108.3f, 40.9f, 109.4f); // c-1.2,1.2-1.2,3.1,0,4.2
        p.lineTo(89.8f, 157.5f);       // l48.9,48.1
        p.curveTo(91.0f, 158.6f, 92.8f, 158.6f, 94.0f, 157.5f); // c1.2,1.1,3,1.1,4.2,0
        p.lineTo(114.9f, 136.6f);      // l20.9,-20.9
        p.lineTo(86.2f, 108.0f);       // L86.2,108
        p.lineTo(81.5f, 112.7f);       // L81.5,112.7
        p.closePath();
        return p;
    }

    /**
     * Right/top arrow shape (st1 — #60B5EF).
     * <p>
     * SVG path: {@code M191.1,104.6l-48.9-48.1c-1.2-1.1-3-1.1-4.2,0l-21.1,21.1
     * l28.9,28.9l4.7-4.7l5.8,19.3l-19.3-5.8l4.8-4.8L113,81.5l-22.7,22.7
     * l50.9,50.3c1.2,1.1,3,1.1,4.2,0l45.6-45.6C192.2,107.7,192.2,105.7,191.1,104.6z}
     */
    private static Path2D createRightArrow() {
        Path2D p = new Path2D.Float();
        p.moveTo(191.1f, 104.6f);
        p.lineTo(142.2f, 56.5f);       // l-48.9,-48.1
        p.curveTo(141.0f, 55.4f, 139.2f, 55.4f, 138.0f, 56.5f); // c-1.2-1.1-3-1.1-4.2,0
        p.lineTo(116.9f, 77.6f);       // l-21.1,21.1
        p.lineTo(145.8f, 106.5f);      // l28.9,28.9
        p.lineTo(150.5f, 101.8f);      // l4.7,-4.7
        p.lineTo(156.3f, 121.1f);      // l5.8,19.3
        p.lineTo(137.0f, 115.3f);      // l-19.3,-5.8
        p.lineTo(141.8f, 110.5f);      // l4.8,-4.8
        p.lineTo(113.0f, 81.5f);       // L113,81.5
        p.lineTo(90.3f, 104.2f);       // l-22.7,22.7
        p.lineTo(141.2f, 154.5f);      // l50.9,50.3
        p.curveTo(142.4f, 155.6f, 144.2f, 155.6f, 145.4f, 154.5f); // c1.2,1.1,3,1.1,4.2,0
        p.lineTo(191.0f, 108.9f);      // l45.6,-45.6
        p.curveTo(192.2f, 107.7f, 192.2f, 105.7f, 191.1f, 104.6f); // C192.2,107.7,192.2,105.7,191.1,104.6
        p.closePath();
        return p;
    }

    /**
     * Central overlap diamond (st2 — #42A0D3).
     * <p>
     * SVG path: {@code M90.3,104.2L113,81.5l28.9,28.9l-22.7,22.7
     * C119.3,133.2,91.5,105.4,90.3,104.2z}
     */
    private static Path2D createOverlap() {
        Path2D p = new Path2D.Float();
        p.moveTo(90.3f, 104.2f);
        p.lineTo(113.0f, 81.5f);       // L113,81.5
        p.lineTo(141.9f, 110.4f);      // l28.9,28.9
        p.lineTo(119.2f, 133.1f);      // l-22.7,22.7
        p.curveTo(119.3f, 133.2f, 91.5f, 105.4f, 90.3f, 104.2f); // C119.3,133.2,91.5,105.4,90.3,104.2
        p.closePath();
        return p;
    }
}

