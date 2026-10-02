package sdmxdl.script.std;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import internal.sdmxdl.script.spi.ScriptGeneratorLoader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sdmxdl.DataRequest;
import sdmxdl.Detail;
import sdmxdl.FlowsRequest;
import sdmxdl.MetaRequest;
import sdmxdl.Request;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptOptions;
import sdmxdl.script.ScriptTarget;
import sdmxdl.script.spi.ScriptGenerator;

public class ScriptGeneratorsTest {

    @Test
    public void testTargets() {
        assertThat(ScriptManager.ofServiceLoader().getTargets())
                .extracting(ScriptTarget::toString)
                .containsExactlyInAnyOrder(
                        "bash/cli",
                        "bash/rest",
                        "batch/cli",
                        "jbang/java",
                        "jupyter/cli",
                        "jupyter/rest",
                        "powerquery/rest",
                        "powershell/cli",
                        "powershell/rest",
                        "python/cli",
                        "python/rest",
                        "r/cli",
                        "r/rest");
    }

    @ParameterizedTest
    @MethodSource("generators")
    public void testCompliance(ScriptGenerator generator) {
        assertThat(generator.getScriptRequestTypes()).contains(DataRequest.class, FlowsRequest.class);
        assertThat(generator.getScriptPropertyNames())
                .allMatch(name -> name.startsWith(ScriptGenerator.SCRIPT_PROPERTY_PREFIX + "."));

        for (Request request : requests()) {
            for (ScriptOptions options : options()) {
                Script script = generator.generateScript("my source", request, options);
                assertThat(script.getTarget()).isEqualTo(generator.getScriptTarget());
                assertThat(script.getFileExtension()).isEqualTo(generator.getScriptFileExtension());
                assertThat(script.getMediaType())
                        .isEqualTo(generator.getScriptMediaType())
                        .matches("[a-z]+/[a-z0-9.+-]+");
                assertThat(script.getContent())
                        .isNotBlank()
                        .endsWith("\n")
                        .doesNotContain("{{#", "{{/", "{{^", "<%")
                        .doesNotContain("\n\n\n")
                        .doesNotStartWith("\n");
                script.getWarnings()
                        .forEach(warning -> assertThat(script.getContent()).contains(warning));
            }
        }

        assertThatIllegalArgumentException()
                .isThrownBy(() -> generator.generateScript(
                        "ECB", MetaRequest.builder().flowOf("EXR").build(), ScriptOptions.DEFAULT));
    }

    static List<ScriptGenerator> generators() {
        return ScriptGeneratorLoader.load();
    }

    static List<Request> requests() {
        return Stream.of(
                        DataRequest.builder()
                                .flowOf("EXR")
                                .keyOf("M.CHF.EUR.SP00.A")
                                .lastNObservations(12)
                                .build(),
                        DataRequest.builder()
                                .databaseOf("db")
                                .flowOf("ECB,EXR,1.0")
                                .languagesOf("fr")
                                .startPeriodOf("2020-02")
                                .endPeriodOf("2021")
                                .firstNObservations(3)
                                .detail(Detail.DATA_ONLY)
                                .build(),
                        FlowsRequest.DEFAULT,
                        FlowsRequest.builder()
                                .query("it's \"exchange\" 100% $rates #(lf)")
                                .maxResults(5)
                                .plainDescription(true)
                                .maxDescriptionLength(80)
                                .build())
                .collect(Collectors.toList());
    }

    static List<ScriptOptions> options() {
        return Stream.of(
                        ScriptOptions.DEFAULT,
                        ScriptOptions.builder()
                                .cliLauncherOf("java", "-jar", "C:\\my tools\\sdmx-dl-cli-bin.jar")
                                .restEndpointOf("http://example.com/api/")
                                .outputFile("out dir\\it's.csv")
                                .build())
                .collect(Collectors.toList());
    }
}
