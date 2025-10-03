package internal.sdmxdl.desktop.util;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Consumer;

@MightBePromoted
@lombok.experimental.UtilityClass
public class Documents {

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

    public static @NonNull String getText(@NonNull DocumentEvent event) {
        try {
            return event.getDocument().getText(0, event.getDocument().getLength());
        } catch (javax.swing.text.BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }
}
