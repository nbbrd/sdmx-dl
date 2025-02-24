package sdmxdl.desktop.panels;

import org.kordamp.ikonli.materialdesign.MaterialDesign;
import sdmxdl.web.spi.Networking;

import javax.swing.*;

public enum NetworkingRenderer implements Renderer<Networking> {

    INSTANCE;

    @Override
    public String toText(Networking value, Runnable onUpdate) {
        return value.getNetworkingId();
    }

    @Override
    public Icon toIcon(Networking value, Runnable onUpdate) {
        return Renderer.getIcon(MaterialDesign.MDI_ACCESS_POINT_NETWORK);
    }
}
