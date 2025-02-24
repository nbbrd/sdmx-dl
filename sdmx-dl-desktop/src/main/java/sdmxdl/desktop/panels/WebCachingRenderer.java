package sdmxdl.desktop.panels;

import org.kordamp.ikonli.materialdesign.MaterialDesign;
import sdmxdl.web.spi.WebCaching;

import javax.swing.*;

public enum WebCachingRenderer implements Renderer<WebCaching> {

    INSTANCE;

    @Override
    public String toText(WebCaching value, Runnable onUpdate) {
        return value.getWebCachingId();
    }

    @Override
    public Icon toIcon(WebCaching value, Runnable onUpdate) {
        return Renderer.getIcon(MaterialDesign.MDI_FILE_CLOUD);
    }
}
