package internal.sdmxdl.swing;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Consumer;

@MightBePromoted
@lombok.experimental.UtilityClass
public class MoreSwing {

    public static @NonNull DocumentListener documentListenerOf(@NonNull Consumer<? super DocumentEvent> consumer) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                consumer.accept(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                consumer.accept(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                consumer.accept(e);
            }
        };
    }

    /**
     * Returns a {@link javax.swing.event.DocumentListener} that fires {@code action} only after
     * the user has stopped typing for {@code delayMs} milliseconds (debounce).
     * Each new document event restarts the countdown, so the action is never
     * called more frequently than once per {@code delayMs} window.
     */
    public static @NonNull DocumentListener debouncedDocumentListenerOf(int delayMs, @NonNull Runnable action) {
        javax.swing.Timer[] slot = {null};
        return documentListenerOf(e -> {
            if (slot[0] != null) slot[0].stop();
            slot[0] = new javax.swing.Timer(delayMs, evt -> action.run());
            slot[0].setRepeats(false);
            slot[0].start();
        });
    }

    public static @NonNull String escapeHtml(@NonNull String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
