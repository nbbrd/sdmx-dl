package internal.sdmxdl.desktop;

import com.formdev.flatlaf.FlatIconColors;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.util.function.Supplier;

public final class ButtonBuilder {

    private AbstractAction action;

    private Ikon ikon;

    private String toolTipText;

    public ButtonBuilder action(AbstractAction action) {
        this.action = action;
        return this;
    }

    public ButtonBuilder ikon(Ikon ikon) {
        this.ikon = ikon;
        return this;
    }

    public ButtonBuilder toolTipText(String toolTipText) {
        this.toolTipText = toolTipText;
        return this;
    }


    public JButton buildButton() {
        return build(JButton::new);
    }

    public <T extends AbstractButton> T build(Supplier<T> factory) {
        T result = factory.get();
        result.setAction(action);
        result.setIcon(FontIcon.of(ikon, 16, UIManager.getColor(FlatIconColors.ACTIONS_GREYINLINE.key)));
        result.setRolloverIcon(FontIcon.of(ikon, 16, UIManager.getColor(FlatIconColors.ACTIONS_GREY.key)));
        result.setToolTipText(toolTipText);
        return result;
    }
}
