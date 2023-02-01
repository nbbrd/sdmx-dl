package internal.sdmxdl.desktop;

import ec.util.various.swing.JCommand;
import lombok.NonNull;

import java.awt.*;

public final class NoOpCommand extends JCommand<Component> {

    public static final NoOpCommand INSTANCE = new NoOpCommand();

    @Override
    public boolean isEnabled(@NonNull Component component) {
        return false;
    }

    @Override
    public void execute(@NonNull Component component) {
    }
}
