package internal.sdmxdl.desktop.util;

import com.formdev.flatlaf.FlatIconColors;
import org.kordamp.ikonli.Ikon;

import javax.swing.*;
import java.util.function.Supplier;

public final class ButtonBuilder {

    private Action action;

    private Ikon ikon;

    private String toolTipText;

    public ButtonBuilder action(Action action) {
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

    public JButton build() {
        return buildWith(JButton::new);
    }

    public <T extends AbstractButton> T buildWith(Supplier<T> factory) {
        T result = factory.get();
        result.setAction(action);
        result.setIcon(Ikons.of(ikon, 16, FlatIconColors.ACTIONS_GREYINLINE.key));
        result.setRolloverIcon(Ikons.of(ikon, 16, FlatIconColors.ACTIONS_GREY.key));
        result.setToolTipText(toolTipText);
        return result;
    }
}
