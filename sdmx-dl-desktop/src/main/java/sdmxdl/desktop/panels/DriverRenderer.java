package sdmxdl.desktop.panels;

import internal.sdmxdl.desktop.util.JDocument;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import sdmxdl.desktop.MainComponent;
import sdmxdl.web.spi.Driver;

import javax.swing.*;

public enum DriverRenderer implements Renderer<Driver> {

    INSTANCE;

    @Override
    public String toText(Driver value, Runnable onUpdate) {
        return value.getDriverId();
    }

    @Override
    public Icon toIcon(Driver value, Runnable onUpdate) {
        return Renderer.getIcon(MaterialDesign.MDI_CHIP);
    }

    @Override
    public JDocument<Driver> toView(MainComponent main, Driver value) {
        JDocument<Driver> result = new JDocument<>();
        result.addComponent("Settings", new DriverPanel(), DriverPanel::setModel);
        result.setModel(value);
        return result;
    }
}
