package sdmxdl.swing;

import internal.sdmxdl.swing.RSyntaxScriptViewer;
import java.awt.Font;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.jspecify.annotations.Nullable;
import sdmxdl.script.Script;

/**
 * Read-only view of a generated script, used by {@link ScriptPreviewPanel}.
 * <p>
 * Implement this interface to plug a syntax-highlighting editor; the target language and the media
 * type of the script are available in {@link Script#getTarget()} and {@link Script#getMediaType()}.
 * <p>
 * A built-in highlighting viewer is provided by {@link #highlighted()} if
 * <a href="https://github.com/bobbylight/RSyntaxTextArea">RSyntaxTextArea</a>
 * ({@code com.fifesoft:rsyntaxtextarea}) is on the classpath; this optional dependency must be added
 * explicitly by the application.
 */
public interface ScriptViewer {

    /**
     * Gets the component that displays the script.
     *
     * @return a non-null component, always the same instance
     */
    @NonNull JComponent getComponent();

    /**
     * Sets the script to display.
     *
     * @param script a script, or {@code null} to clear the view
     */
    void setScript(@Nullable Script script);

    /**
     * Creates the best available viewer: {@link #highlighted()} if possible, {@link #plain()} otherwise.
     *
     * @return a new non-null viewer
     */
    @StaticFactoryMethod
    static @NonNull ScriptViewer auto() {
        return isHighlightingAvailable() ? highlighted() : plain();
    }

    /**
     * Checks if {@link #highlighted()} can be used.
     *
     * @return {@code true} if RSyntaxTextArea is on the classpath
     */
    static boolean isHighlightingAvailable() {
        try {
            Class.forName("org.fife.ui.rsyntaxtextarea.RSyntaxTextArea", false, ScriptViewer.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError ex) {
            return false;
        }
    }

    /**
     * Creates a viewer that highlights the syntax of scripts, based on RSyntaxTextArea.
     * <p>
     * Its colors follow the current look-and-feel, either light or dark.
     *
     * @return a new non-null viewer
     * @throws IllegalStateException if RSyntaxTextArea is not on the classpath
     * @see #isHighlightingAvailable()
     */
    @StaticFactoryMethod
    static @NonNull ScriptViewer highlighted() throws IllegalStateException {
        if (!isHighlightingAvailable()) {
            throw new IllegalStateException("RSyntaxTextArea is not on the classpath");
        }
        return new RSyntaxScriptViewer();
    }

    /**
     * Creates a viewer that displays scripts as plain monospaced text.
     *
     * @return a new non-null viewer
     */
    @StaticFactoryMethod
    static @NonNull ScriptViewer plain() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(
                new Font(Font.MONOSPACED, Font.PLAIN, textArea.getFont().getSize()));
        JScrollPane scrollPane = new JScrollPane(textArea);
        return new ScriptViewer() {
            @Override
            public @NonNull JComponent getComponent() {
                return scrollPane;
            }

            @Override
            public void setScript(@Nullable Script script) {
                textArea.setText(script != null ? script.getContent() : "");
                textArea.setCaretPosition(0);
            }
        };
    }
}
