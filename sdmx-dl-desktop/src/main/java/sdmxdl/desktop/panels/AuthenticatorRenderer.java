package sdmxdl.desktop.panels;

import org.kordamp.ikonli.materialdesign.MaterialDesign;
import sdmxdl.web.spi.Authenticator;

import javax.swing.*;

public enum AuthenticatorRenderer implements Renderer<Authenticator> {

    INSTANCE;

    @Override
    public String toText(Authenticator value, Runnable onUpdate) {
        return value.getAuthenticatorId();
    }

    @Override
    public Icon toIcon(Authenticator value, Runnable onUpdate) {
        return Renderer.getIcon(MaterialDesign.MDI_ACCOUNT_KEY);
    }
}
