package sdmxdl.script.std.python;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import sdmxdl.DataRequest;
import sdmxdl.Detail;
import sdmxdl.FlowsRequest;
import sdmxdl.MetaRequest;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptOptions;
import sdmxdl.script.ScriptTarget;

public class PythonCliScriptGeneratorTest {

    private final PythonCliScriptGenerator x = new PythonCliScriptGenerator();

    @Test
    public void testServiceLoader() {
        assertThat(ScriptManager.ofServiceLoader().getTargets()).contains(ScriptTarget.parse("python/cli"));
    }

    @Test
    public void testDataMatchesDocumentation() {
        DataRequest request = DataRequest.builder()
                .flowOf("EXR")
                .keyOf("M.CHF.EUR.SP00.A")
                .lastNObservations(12)
                .build();

        Script script = x.generateScript("ECB", request, ScriptOptions.DEFAULT);

        assertThat(script.getTarget()).isEqualTo(ScriptTarget.of("python", "cli"));
        assertThat(script.getFileExtension()).isEqualTo("py");
        assertThat(script.getWarnings()).isEmpty();
        assertThat(script.getContent())
                .isEqualTo("import contextlib\n"
                        + "import csv\n"
                        + "import io\n"
                        + "import subprocess\n"
                        + "import sys\n"
                        + "\n"
                        + "result = subprocess.run(\n"
                        + "    [\"sdmx-dl\", \"fetch\", \"data\", \"ECB\", \"EXR\", \"M.CHF.EUR.SP00.A\", \"--last-n\", \"12\"],\n"
                        + "    capture_output=True, text=True, check=True,\n"
                        + ")\n"
                        + "\n"
                        + "with contextlib.nullcontext(sys.stdout) as output:\n"
                        + "    writer = csv.writer(output)\n"
                        + "    writer.writerow([\"Series\", \"ObsPeriod\", \"ObsValue\"])\n"
                        + "    for row in csv.DictReader(io.StringIO(result.stdout)):\n"
                        + "        writer.writerow([row[\"Series\"], row[\"ObsPeriod\"], row[\"ObsValue\"]])\n");
    }

    @Test
    public void testDataWithAllOptions() {
        DataRequest request = DataRequest.builder()
                .databaseOf("db")
                .flowOf("ECB,EXR,1.0")
                .languagesOf("fr")
                .startPeriodOf("2020-02")
                .endPeriodOf("2021")
                .firstNObservations(3)
                .detail(Detail.DATA_ONLY)
                .build();
        ScriptOptions options = ScriptOptions.builder()
                .cliLauncherOf("java", "-jar", "C:\\tools\\sdmx-dl-cli-bin.jar")
                .outputFile("out\\ecb.csv")
                .build();

        Script script = x.generateScript("ECB", request, options);

        assertThat(script.getWarnings())
                .containsExactly("Detail 'DATA_ONLY' is not supported by the CLI; FULL is used instead");
        assertThat(script.getContent())
                .isEqualTo("# Warning: Detail 'DATA_ONLY' is not supported by the CLI; FULL is used instead\n"
                        + "\n"
                        + "import csv\n"
                        + "import io\n"
                        + "import subprocess\n"
                        + "\n"
                        + "result = subprocess.run(\n"
                        + "    [\"java\", \"-jar\", \"C:\\\\tools\\\\sdmx-dl-cli-bin.jar\", \"fetch\", \"data\", \"ECB\", \"ECB,EXR,1.0\", \"all\", \"-d\", \"db\", \"-l\", \"fr\", \"--start\", \"2020-02\", \"--end\", \"2021\", \"--first-n\", \"3\"],\n"
                        + "    capture_output=True, text=True, check=True,\n"
                        + ")\n"
                        + "\n"
                        + "with open(\"out\\\\ecb.csv\", \"w\", newline=\"\", encoding=\"utf-8\") as output:\n"
                        + "    writer = csv.writer(output)\n"
                        + "    writer.writerow([\"Series\", \"ObsPeriod\", \"ObsValue\"])\n"
                        + "    for row in csv.DictReader(io.StringIO(result.stdout)):\n"
                        + "        writer.writerow([row[\"Series\"], row[\"ObsPeriod\"], row[\"ObsValue\"]])\n");
    }

    @Test
    public void testFlows() {
        FlowsRequest request = FlowsRequest.builder()
                .query("exchange \"rates\"")
                .maxResults(5)
                .plainDescription(true)
                .maxDescriptionLength(80)
                .build();

        Script script = x.generateScript("ECB", request, ScriptOptions.DEFAULT);

        assertThat(script.getWarnings()).isEmpty();
        assertThat(script.getContent())
                .contains(
                        "    [\"sdmx-dl\", \"list\", \"flows\", \"ECB\", \"-q\", \"exchange \\\"rates\\\"\", \"-m\", \"5\", \"--plain-description\", \"--max-description-length\", \"80\"],\n")
                .contains("    writer.writerow([\"Ref\", \"Name\", \"Description\"])\n")
                .contains("        writer.writerow([row[\"Ref\"], row[\"Name\"], row[\"Description\"]])\n");
    }

    @Test
    public void testUnsupported() {
        assertThat(x.getScriptRequestTypes()).doesNotContain(MetaRequest.class);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.generateScript(
                        "ECB", MetaRequest.builder().flowOf("EXR").build(), ScriptOptions.DEFAULT));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.generateScript(
                        "ECB",
                        FlowsRequest.DEFAULT,
                        ScriptOptions.builder().cliLauncherOf().build()));
    }
}
