package internal.sdmxdl.desktop.util;

import javax.swing.*;

public final class XLabel extends JLabel {

    public XLabel() {
        setOpaque(true);
        JList<?> resource = new JList<>();
//        setBackground(resource.getSelectionForeground());
//        setForeground(resource.getSelectionBackground());
        setFont(resource.getFont().deriveFont(resource.getFont().getSize2D() * 2));
        setHorizontalAlignment(SwingConstants.CENTER);
    }
}
