package sdmxdl.swing;

import lombok.AccessLevel;
import lombok.NonNull;
import sdmxdl.Confidentiality;

import javax.swing.*;
import java.awt.*;

@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfidentialityBadge implements Icon {

    private static final Color BLUE = new Color(0x00, 0x78, 0xD4);
    private static final Color AMBER = new Color(0xFF, 0xB9, 0x00);
    private static final Color ORANGE = new Color(0xFF, 0x8C, 0x00);
    private static final Color RED = new Color(0xD1, 0x0F, 0x0F);

    /**
     * Wraps {@code base} with a small colored dot in the bottom-right corner
     * when the source is not fully public.  The dot color matches the ECB
     * confidentiality regime: blue → unrestricted, amber → restricted,
     * orange → confidential, red → secret.
     */
    public static @NonNull Icon wrap(@NonNull Icon base, @NonNull Confidentiality confidentiality) {
        if (confidentiality == Confidentiality.PUBLIC) return base;
        return new ConfidentialityBadge(base, confidentiality);
    }

    private final @NonNull Icon base;
    private final @NonNull Confidentiality confidentiality;

    @Override
    public int getIconWidth() {
        return base.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return base.getIconHeight();
    }

    @Override
    public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
        base.paintIcon(c, g, x, y);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int d = 10;
            int bx = x + base.getIconWidth() - d;
            int by = y + base.getIconHeight() - d;
            // White border so the dot stands out on any background
            g2.setColor(Color.WHITE);
            g2.fillOval(bx - 1, by - 1, d + 2, d + 2);
            g2.setColor(confidentialityBadgeColor(confidentiality));
            g2.fillOval(bx, by, d, d);
        } finally {
            g2.dispose();
        }
    }

    private static Color confidentialityBadgeColor(@NonNull Confidentiality confidentiality) {
        switch (confidentiality) {
            case UNRESTRICTED:
                return BLUE;
            case RESTRICTED:
                return AMBER;
            case CONFIDENTIAL:
                return ORANGE;
            case SECRET:
                return RED;
            default:
                return Color.GRAY;
        }
    }
}
