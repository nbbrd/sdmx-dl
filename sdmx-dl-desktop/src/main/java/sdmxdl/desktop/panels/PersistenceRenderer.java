package sdmxdl.desktop.panels;

import org.kordamp.ikonli.materialdesign.MaterialDesign;
import sdmxdl.ext.Persistence;

import javax.swing.*;

public enum PersistenceRenderer implements Renderer<Persistence> {

    INSTANCE;

    @Override
    public String toText(Persistence value, Runnable onUpdate) {
        return value.getPersistenceId();
    }

    @Override
    public Icon toIcon(Persistence value, Runnable onUpdate) {
        return Renderer.getIcon(MaterialDesign.MDI_CONTENT_SAVE);
    }
}
