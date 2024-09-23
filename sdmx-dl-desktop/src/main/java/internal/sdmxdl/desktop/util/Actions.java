package internal.sdmxdl.desktop.util;

import lombok.NonNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Consumer;

public final class Actions {

    private Actions() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static @NonNull Action onActionPerformed(@NonNull Runnable action) {
        return onActionPerformed(ignore -> action.run());
    }


    public static @NonNull Action onActionPerformed(@NonNull Consumer<? super ActionEvent> action) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.accept(e);
            }
        };
    }

    public static <C extends AbstractButton> @NonNull C hideWhenDisabled(@NonNull C c) {
        c.setVisible(c.isEnabled());
        c.addPropertyChangeListener(HIDE);
        return c;
    }

    private static final PropertyChangeListener HIDE = evt -> {
        if ("enabled".equals(evt.getPropertyName()) && evt.getSource() instanceof JComponent) {
            ((JComponent) evt.getSource()).setVisible((Boolean) evt.getNewValue());
        }
    };
}
