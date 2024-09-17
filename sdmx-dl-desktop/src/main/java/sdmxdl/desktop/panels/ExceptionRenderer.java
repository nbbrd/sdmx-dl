package sdmxdl.desktop.panels;

import internal.sdmxdl.desktop.util.JDocument;
import internal.sdmxdl.desktop.util.StackTracePrinter;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import sdmxdl.desktop.MainComponent;

import javax.swing.*;

import static j2html.TagCreator.*;

public enum ExceptionRenderer implements Renderer<Exception> {

    INSTANCE;

    @Override
    public String toHeaderText(Exception value, Runnable onUpdate) {
        return "Error " + value.getClass().getSimpleName();
    }

    @Override
    public String toText(Exception value, Runnable onUpdate) {
        return html(text("Error "), kbd(value.getClass().getSimpleName())).render();
    }

    @Override
    public String toTooltip(Exception value, Runnable onUpdate) {
        return "<html>" + StackTracePrinter.htmlBuilder().toString(value);
    }

    @Override
    public Icon toIcon(Exception value, Runnable onUpdate) {
        return Renderer.getIcon(MaterialDesign.MDI_CLOSE_NETWORK);
    }

    @Override
    public JDocument<Exception> toView(MainComponent main, Exception value) {
        JDocument<Exception> result = new JDocument<>();
        result.addComponent("Stacktrace", new ExceptionPanel(), ExceptionPanel::setException);
        result.setModel(value);
        return result;
    }
}
