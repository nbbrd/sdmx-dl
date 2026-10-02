package internal.sdmxdl.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.junit.jupiter.api.Test;
import sdmxdl.DataRequest;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptOptions;
import sdmxdl.script.ScriptTarget;
import sdmxdl.swing.ScriptViewer;

public class RSyntaxScriptViewerTest {

    @Test
    public void testFactories() {
        assertThat(ScriptViewer.isHighlightingAvailable()).isTrue();
        assertThat(ScriptViewer.auto()).isInstanceOf(RSyntaxScriptViewer.class);
        assertThat(ScriptViewer.highlighted()).isInstanceOf(RSyntaxScriptViewer.class);
        assertThat(ScriptViewer.plain()).isNotInstanceOf(RSyntaxScriptViewer.class);
    }

    @Test
    public void testThemes() {
        assertThat(Theme.class.getResource("themes/dark.xml")).isNotNull();
        assertThat(Theme.class.getResource("themes/default.xml")).isNotNull();
    }

    @Test
    public void testIsDark() {
        assertThat(RSyntaxScriptViewer.isDark(Color.WHITE)).isFalse();
        assertThat(RSyntaxScriptViewer.isDark(new Color(0xF2, 0xF2, 0xF2))).isFalse();
        assertThat(RSyntaxScriptViewer.isDark(Color.BLACK)).isTrue();
        assertThat(RSyntaxScriptViewer.isDark(new Color(0x46, 0x49, 0x4B))).isTrue();
    }

    @Test
    public void testSetScript() {
        RSyntaxScriptViewer x = new RSyntaxScriptViewer();
        RSyntaxTextArea textArea = (RSyntaxTextArea) ((RTextScrollPane) x.getComponent()).getTextArea();
        assertThat(textArea.isEditable()).isFalse();
        assertThat(textArea.getHighlightCurrentLine()).isFalse();

        x.setScript(generate("python/cli"));
        assertThat(textArea.getSyntaxEditingStyle()).isEqualTo(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        assertThat(textArea.getText()).contains("EXR");

        x.setScript(generate("r/rest"));
        assertThat(textArea.getSyntaxEditingStyle()).isEqualTo(SyntaxConstants.SYNTAX_STYLE_NONE);

        x.setScript(null);
        assertThat(textArea.getText()).isEmpty();
    }

    private static Script generate(String target) {
        return ScriptManager.ofServiceLoader()
                .generate(
                        ScriptTarget.parse(target),
                        "ECB",
                        DataRequest.builder().flowOf("EXR").build(),
                        ScriptOptions.DEFAULT);
    }
}
