package internal.sdmxdl.swing;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import javax.swing.JComponent;
import javax.swing.UIManager;
import lombok.NonNull;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jspecify.annotations.Nullable;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptTarget;
import sdmxdl.swing.ScriptViewer;

/**
 * Script viewer based on RSyntaxTextArea, an optional dependency.
 * <p>
 * This class must only be loaded if {@link ScriptViewer#isHighlightingAvailable()} returns {@code true}.
 */
public final class RSyntaxScriptViewer implements ScriptViewer {

    private final RSyntaxTextArea textArea = new RSyntaxTextArea();
    private final RTextScrollPane scrollPane = new RTextScrollPane(textArea);

    public RSyntaxScriptViewer() {
        applyTheme(textArea, isDarkLookAndFeel() ? "dark.xml" : "default.xml");
        textArea.setEditable(false);
        textArea.setHighlightCurrentLine(false);
        textArea.setCodeFoldingEnabled(true);
    }

    @Override
    public @NonNull JComponent getComponent() {
        return scrollPane;
    }

    @Override
    public void setScript(@Nullable Script script) {
        textArea.setSyntaxEditingStyle(
                script != null ? getSyntaxStyle(script.getTarget()) : SyntaxConstants.SYNTAX_STYLE_NONE);
        textArea.setText(script != null ? script.getContent() : "");
        textArea.setCaretPosition(0);
    }

    static String getSyntaxStyle(ScriptTarget target) {
        switch (target.getLanguage()) {
            case ScriptTarget.BASH_LANGUAGE:
                return SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
            case ScriptTarget.BATCH_LANGUAGE:
                return SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH;
            case ScriptTarget.JBANG_LANGUAGE:
                return SyntaxConstants.SYNTAX_STYLE_JAVA;
            case ScriptTarget.JUPYTER_LANGUAGE:
            case ScriptTarget.PYTHON_LANGUAGE:
                return SyntaxConstants.SYNTAX_STYLE_PYTHON;
            default:
                return SyntaxConstants.SYNTAX_STYLE_NONE;
        }
    }

    static boolean isDarkLookAndFeel() {
        Color background = UIManager.getColor("TextArea.background");
        if (background == null) {
            background = UIManager.getColor("Panel.background");
        }
        return background != null && isDark(background);
    }

    static boolean isDark(Color color) {
        double luminance = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
        return luminance < 128;
    }

    private static void applyTheme(RSyntaxTextArea textArea, String name) {
        try (InputStream stream = Theme.class.getResourceAsStream("themes/" + name)) {
            if (stream != null) {
                Theme.load(stream).apply(textArea);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
