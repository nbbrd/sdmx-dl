package internal.sdmxdl.desktop;

import lombok.NonNull;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

public final class MouseListeners {

    private MouseListeners() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static @NonNull MouseListener onDoubleClick(@NonNull Runnable listener) {
        return onDoubleClick(ignore -> listener.run());
    }

    public static @NonNull MouseListener onDoubleClick(@NonNull Consumer<? super MouseEvent> listener) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) listener.accept(e);
            }
        };
    }
}
