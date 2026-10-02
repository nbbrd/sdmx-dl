package sdmxdl.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import sdmxdl.DataRequest;
import sdmxdl.FlowsRequest;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptOptions;
import sdmxdl.script.ScriptTarget;

public class ScriptPreviewPanelTest {

    @Test
    public void testGenerate() {
        ScriptPreviewPanel x = new ScriptPreviewPanel();
        assertThat(x.getScript()).isNull();
        assertThat(x.getTarget()).isNull();

        x.setManager(ScriptManager.ofServiceLoader());
        x.setSourceId("ECB");
        x.setRequest(DataRequest.builder().flowOf("EXR").build());
        assertThat(x.getTarget()).isNotNull();
        assertThat(x.getScript()).isNotNull();
        assertThat(x.getScript().getTarget()).isEqualTo(x.getTarget());

        x.setTarget(ScriptTarget.parse("python/cli"));
        assertThat(x.getScript().getTarget()).hasToString("python/cli");
        assertThat(x.getScript().getContent()).contains("ECB", "EXR");

        x.setOptions(ScriptOptions.builder().outputFile("out.csv").build());
        assertThat(x.getScript().getContent()).contains("out.csv");

        x.setRequest(FlowsRequest.builder().build());
        assertThat(x.getTarget()).hasToString("python/cli");
        assertThat(x.getScript().getContent()).contains("flows");

        x.setTarget(ScriptTarget.parse("unknown/cli"));
        assertThat(x.getTarget()).isNotEqualTo(ScriptTarget.parse("unknown/cli"));
        assertThat(x.getScript()).isNotNull();
    }

    @Test
    public void testViewerFactory() {
        List<Script> scripts = new ArrayList<>();
        ScriptPreviewPanel x = new ScriptPreviewPanel();
        x.setManager(ScriptManager.ofServiceLoader());
        x.setSourceId("ECB");
        x.setRequest(DataRequest.builder().flowOf("EXR").build());

        x.setViewerFactory(() -> new ScriptViewer() {
            final JLabel label = new JLabel();

            @Override
            public JComponent getComponent() {
                return label;
            }

            @Override
            public void setScript(@Nullable Script script) {
                scripts.add(script);
            }
        });
        assertThat(scripts).containsExactly(x.getScript());

        x.setTarget(ScriptTarget.parse("r/rest"));
        assertThat(scripts).hasSize(2).last().isEqualTo(x.getScript());
    }
}
