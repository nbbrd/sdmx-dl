package internal.sdmxdl.desktop;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;

public class FontIcons {

    public static Icon get(Ikon ikon, int scale, JComponent ref) {
        return FontIcon.of(ikon, ref.getFont().getSize() * scale, ref.getForeground());
    }
}
