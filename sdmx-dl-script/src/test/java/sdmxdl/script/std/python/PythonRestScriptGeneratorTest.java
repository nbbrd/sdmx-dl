package sdmxdl.script.std.python;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import sdmxdl.CodesRequest;
import sdmxdl.DataRequest;
import sdmxdl.Detail;
import sdmxdl.FlowsRequest;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptOptions;
import sdmxdl.script.ScriptTarget;

public class PythonRestScriptGeneratorTest {

    private final PythonRestScriptGenerator x = new PythonRestScriptGenerator();

    @Test
    public void testServiceLoader() {
        assertThat(ScriptManager.ofServiceLoader().getTargets()).contains(ScriptTarget.parse("python/rest"));
    }

    @Test
    public void testData() {
        DataRequest request = DataRequest.builder()
                .flowOf("EXR")
                .keyOf("M.CHF.EUR.SP00.A")
                .lastNObservations(12)
                .build();

        Script script = x.generateScript("ECB", request, ScriptOptions.DEFAULT);

        assertThat(script.getTarget()).isEqualTo(ScriptTarget.of("python", "rest"));
        assertThat(script.getWarnings()).isEmpty();
        assertThat(script.getContent())
                .isEqualTo(
                        "import contextlib\n"
                                + "import csv\n"
                                + "import json\n"
                                + "import sys\n"
                                + "import urllib.parse\n"
                                + "import urllib.request\n"
                                + "\n"
                                + "params = urllib.parse.urlencode({\"key\": \"M.CHF.EUR.SP00.A\", \"lastN\": 12})\n"
                                + "with urllib.request.urlopen(f\"http://localhost:4559/sdmx-dl/v2/ECB/EXR/data?{params}\") as response:\n"
                                + "    payload = json.load(response)\n"
                                + "\n"
                                + "with contextlib.nullcontext(sys.stdout) as output:\n"
                                + "    writer = csv.writer(output)\n"
                                + "    writer.writerow([\"Series\", \"ObsPeriod\", \"ObsValue\"])\n"
                                + "    for series in payload.get(\"data\", []):\n"
                                + "        for obs in series.get(\"obs\", []):\n"
                                + "            writer.writerow([series[\"key\"], obs[\"period\"].split(\"/\")[0], obs.get(\"value\", 0.0)])\n");
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
                .restEndpointOf("http://example.com/api/")
                .build();

        Script script = x.generateScript("my source", request, options);

        assertThat(script.getWarnings()).isEmpty();
        assertThat(script.getContent())
                .contains(
                        "params = urllib.parse.urlencode({\"database\": \"db\", \"languages\": \"fr\", \"start\": \"2020-02\", \"end\": \"2021\", \"firstN\": 3, \"detail\": \"DATA_ONLY\"})\n")
                .contains(
                        "with urllib.request.urlopen(f\"http://example.com/api/my%20source/ECB%2CEXR%2C1.0/data?{params}\") as response:\n");
    }

    @Test
    public void testFlows() {
        FlowsRequest request = FlowsRequest.builder()
                .plainDescription(true)
                .maxDescriptionLength(80)
                .build();

        Script script = x.generateScript(
                "ECB", request, ScriptOptions.builder().outputFile("flows.csv").build());

        assertThat(script.getWarnings()).hasSize(2);
        assertThat(script.getContent())
                .isEqualTo(
                        "# Warning: Plain description is not supported by the web service; descriptions are kept as is\n"
                                + "# Warning: Max description length is not supported by the web service; descriptions are not truncated\n"
                                + "\n"
                                + "import csv\n"
                                + "import json\n"
                                + "import urllib.request\n"
                                + "\n"
                                + "with urllib.request.urlopen(\"http://localhost:4559/sdmx-dl/v2/ECB/flows\") as response:\n"
                                + "    payload = json.load(response)\n"
                                + "\n"
                                + "with open(\"flows.csv\", \"w\", newline=\"\", encoding=\"utf-8\") as output:\n"
                                + "    writer = csv.writer(output)\n"
                                + "    writer.writerow([\"Ref\", \"Name\", \"Description\"])\n"
                                + "    for item in payload:\n"
                                + "        writer.writerow([item.get(\"ref\", \"\"), item.get(\"name\", \"\"), item.get(\"description\", \"\")])\n");
    }

    @Test
    public void testUnsupported() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.generateScript(
                        "ECB",
                        CodesRequest.builder().flowOf("EXR").concept("FREQ").build(),
                        ScriptOptions.DEFAULT));
    }
}
