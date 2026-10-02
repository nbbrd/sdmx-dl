package sdmxdl.script;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Collection;
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
        assertThatIllegalArgumentException().isThrownBy(() -> ScriptTarget.parse("Python/cli"));
        assertThatIllegalArgumentException().isThrownBy(() -> ScriptTarget.parse(" python/cli"));

        assertThat(ScriptTarget.of("power-query_2", "rest")).hasToString("power-query_2/rest");
        assertThatIllegalArgumentException().isThrownBy(() -> ScriptTarget.of("a/b", "cli"));
        assertThatIllegalArgumentException().isThrownBy(() -> ScriptTarget.of("", "cli"));
        assertThatIllegalArgumentException().isThrownBy(() -> ScriptTarget.of("python", "CLI"));
    }

    @Test
    public void testRank() {
        DataRequest data = DataRequest.builder().flowOf("EXR").build();

        ScriptManager x = ScriptManager.builder()
                .generator(new MockedGenerator(PYTHON_CLI, "builtin", DataRequest.class))
                .generator(new MockedGenerator(PYTHON_CLI, "builtin-flows", FlowsRequest.class))
                .generator(new MockedGenerator(
                        PYTHON_CLI,
                        "external",
                        DataRequest.class,
                        ScriptGenerator.EXTERNAL_SCRIPT_RANK,
                        Collections.emptyList()))
                .generator(new MockedGenerator(
                        PYTHON_CLI,
                        "external-2",
                        DataRequest.class,
                        ScriptGenerator.EXTERNAL_SCRIPT_RANK,
                        Collections.emptyList()))
                .build();

        assertThat(x.generate(PYTHON_CLI, "ECB", data, ScriptOptions.DEFAULT).getContent())
                .describedAs("highest rank wins, first one on equal ranks")
                .isEqualTo("external");
        assertThat(x.generate(PYTHON_CLI, "ECB", FlowsRequest.DEFAULT, ScriptOptions.DEFAULT)
                        .getContent())
                .describedAs("override is done per request type")
                .isEqualTo("builtin-flows");
    }

    @Test
    public void testProperties() {
        String supported = ScriptGenerator.SCRIPT_PROPERTY_PREFIX + ".python.supported";
        String other = ScriptGenerator.SCRIPT_PROPERTY_PREFIX + ".python.other";
        DataRequest data = DataRequest.builder().flowOf("EXR").build();

        ScriptManager x = ScriptManager.builder()
                .generator(new MockedGenerator(
                        PYTHON_CLI,
                        "first",
                        DataRequest.class,
                        ScriptGenerator.BUILTIN_SCRIPT_RANK,
                        Collections.singletonList(supported)))
                .generator(new MockedGenerator(R_REST, "second", DataRequest.class))
                .build();

        assertThat(x.getPropertyNames(PYTHON_CLI)).containsExactly(supported);
        assertThat(x.getPropertyNames(R_REST)).isEmpty();
        assertThat(x.getPropertyNames(ScriptTarget.of("cobol", "cli"))).isEmpty();

        assertThat(x.generate(PYTHON_CLI, "ECB", data, ScriptOptions.DEFAULT).getWarnings())
                .isEmpty();
        assertThat(x.generate(
                                PYTHON_CLI,
                                "ECB",
                                data,
                                ScriptOptions.builder()
                                        .property(supported, "true")
                                        .property(other, "true")
                                        .build())
                        .getWarnings())
                .containsExactly("existing", "Unsupported property '" + other + "' was ignored");
        assertThat(x.generate(
                                R_REST,
                                "ECB",
                                data,
                                ScriptOptions.builder()
                                        .property(supported, "true")
                                        .build())
                        .getWarnings())
                .containsExactly("existing", "Unsupported property '" + supported + "' was ignored");
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
        private final int rank;
        private final Collection<String> propertyNames;

        MockedGenerator(ScriptTarget target, String content, Class<? extends Request> type) {
            this(target, content, type, BUILTIN_SCRIPT_RANK, Collections.emptyList());
        }

        @Override
        public @NonNull ScriptTarget getScriptTarget() {
            return target;
        }

        @Override
        public int getScriptRank() {
            return rank;
        }

        @Override
        public @NonNull Collection<String> getScriptPropertyNames() {
            return propertyNames;
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
                @NonNull String sourceId, @NonNull Request request, @NonNull ScriptOptions options) {
            Script.Builder result = Script.builder()
                    .target(target)
                    .fileExtension("txt")
                    .mediaType("text/plain")
                    .content(content);
            if (!options.getProperties().isEmpty()) {
                result.warning("existing");
            }
            return result.build();
        }
    }
}
