package sdmxdl.desktop.panels;

import org.kordamp.ikonli.materialdesign.MaterialDesign;
import sdmxdl.web.spi.Registry;

import javax.swing.*;

public enum RegistryRenderer implements Renderer<Registry> {

    INSTANCE;

    @Override
    public String toText(Registry value, Runnable onUpdate) {
        return value.getRegistryId();
    }

    @Override
    public Icon toIcon(Registry value, Runnable onUpdate) {
        return Renderer.getIcon(MaterialDesign.MDI_SITEMAP);
    }
}
