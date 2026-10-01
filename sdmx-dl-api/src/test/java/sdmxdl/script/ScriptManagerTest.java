package sdmxdl.script;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Collections;
import java.util.Set;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import sdmxdl.DataRequest;
import sdmxdl.FlowsRequest;
import sdmxdl.Request;
import sdmxdl.script.spi.ScriptGenerator;

public class ScriptManagerTest {

    private static final ScriptTarget PYTHON_CLI = ScriptTarget.of("python", ScriptTarget.CLI_TRANSPORT);
    private static final ScriptTarget R_REST = ScriptTarget.of("r", ScriptTarget.REST_TRANSPORT);

    @Test
    public void testTarget() {
        assertThat(ScriptTarget.parse("python/cli")).isEqualTo(PYTHON_CLI).hasToString("python/cli");
        assertThatIllegalArgumentException().isThrownBy(() -> ScriptTarget.parse("python"));
        assertThatIllegalArgumentException().isThrownBy(() -> ScriptTarget.parse("/cli"));
        assertThatIllegalArgumentException().isThrownBy(() -> ScriptTarget.parse("python/"));
        assertThatIllegalArgumentException().isThrownBy(() -> ScriptTarget.parse("python/cli/x"));
    }

    @Test
    public void testNoOp() {
        ScriptManager x = ScriptManager.noOp();
        assertThat(x.getTargets()).isEmpty();
        assertThat(x.isSupported(PYTHON_CLI, DataRequest.class)).isFalse();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.generate(PYTHON_CLI, "ECB", FlowsRequest.DEFAULT, ScriptOptions.DEFAULT));
    }

    @Test
    public void testGenerate() {
        ScriptManager x = ScriptManager.builder()
                .generator(new MockedGenerator(PYTHON_CLI, "first", DataRequest.class))
                .generator(new MockedGenerator(PYTHON_CLI, "second", FlowsRequest.class))
                .generator(new MockedGenerator(PYTHON_CLI, "third", DataRequest.class))
                .generator(new MockedGenerator(R_REST, "fourth", DataRequest.class))
                .build();

        assertThat(x.getTargets()).containsExactly(PYTHON_CLI, R_REST);
        assertThat(x.getRequestTypes(PYTHON_CLI)).containsExactly(DataRequest.class, FlowsRequest.class);
        assertThat(x.getRequestTypes(R_REST)).containsExactly(DataRequest.class);
        assertThat(x.isSupported(R_REST, FlowsRequest.class)).isFalse();

        DataRequest data = DataRequest.builder().flowOf("EXR").build();
        assertThat(x.generate(PYTHON_CLI, "ECB", data, ScriptOptions.DEFAULT).getContent())
                .isEqualTo("first");
        assertThat(x.generate(PYTHON_CLI, "ECB", FlowsRequest.DEFAULT, ScriptOptions.DEFAULT)
                        .getContent())
                .isEqualTo("second");
        assertThat(x.generate(R_REST, "ECB", data, ScriptOptions.DEFAULT).getContent())
                .isEqualTo("fourth");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.generate(R_REST, "ECB", FlowsRequest.DEFAULT, ScriptOptions.DEFAULT))
                .withMessageContaining("Unsupported request type");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.generate(ScriptTarget.of("cobol", "cli"), "ECB", data, ScriptOptions.DEFAULT))
                .withMessageContaining("Unknown target 'cobol/cli'")
                .withMessageContaining("python/cli");
    }

    @lombok.AllArgsConstructor
    private static final class MockedGenerator implements ScriptGenerator {

        private final ScriptTarget target;
        private final String content;
        private final Class<? extends Request> type;

        @Override
        public @NonNull ScriptTarget getScriptTarget() {
            return target;
        }

        @Override
        public int getScriptRank() {
            return BUILTIN_SCRIPT_RANK;
        }

        @Override
        public @NonNull String getScriptFileExtension() {
            return "txt";
        }

        @Override
        public @NonNull Set<Class<? extends Request>> getScriptRequestTypes() {
            return Collections.singleton(type);
        }

        @Override
        public @NonNull Script generateScript(
                @NonNull String source, @NonNull Request request, @NonNull ScriptOptions options) {
            return Script.builder()
                    .target(target)
                    .fileExtension("txt")
                    .content(content)
                    .build();
        }
    }
}
