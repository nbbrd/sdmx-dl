package sdmxdl.desktop.panels;

import org.kordamp.ikonli.materialdesign.MaterialDesign;

import javax.swing.*;

public enum SwingWorkerRenderer implements Renderer<SwingWorker<?, ?>> {

    INSTANCE;

    @Override
    public String toText(SwingWorker<?, ?> value, Runnable onUpdate) {
        return "Loading";
    }

    @Override
    public Icon toIcon(SwingWorker<?, ?> value, Runnable onUpdate) {
        return Renderer.getIcon(MaterialDesign.MDI_CLOUD_DOWNLOAD);
    }
}
