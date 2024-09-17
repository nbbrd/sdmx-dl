package sdmxdl.desktop.panels;

import org.kordamp.ikonli.materialdesign.MaterialDesign;
import sdmxdl.web.spi.Monitor;

import javax.swing.*;

public enum MonitorRenderer implements Renderer<Monitor> {

    INSTANCE;

    @Override
    public String toText(Monitor value, Runnable onUpdate) {
        return value.getMonitorId();
    }

    @Override
    public Icon toIcon(Monitor value, Runnable onUpdate) {
        return Renderer.getIcon(MaterialDesign.MDI_GAUGE);
    }
}
