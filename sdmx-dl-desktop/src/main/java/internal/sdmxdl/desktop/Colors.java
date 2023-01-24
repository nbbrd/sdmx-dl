package internal.sdmxdl.desktop;

import internal.RGB;

import java.awt.*;

public class Colors {

    /**
     * Check if a color is more dark than light. Useful if an entity of this
     * color is to be labeled: Use white label on a "dark" color and black label
     * on a "light" color.
     *
     * @param color Color to check.
     * @return True if this is a "dark" color, false otherwise.
     */
    public static boolean isDark(Color color) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;

        return RGB.isDark(r, g, b);
    }

    public static String getHexString(Color color) {
        return Integer.toHexString((color.getRGB() & 0xffffff) | 0x1000000).substring(1);
    }
}
