package sdmxdl.script.std;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import sdmxdl.About;
import sdmxdl.DataRequest;
import sdmxdl.FlowsRequest;
import sdmxdl.Request;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptOptions;
import sdmxdl.script.ScriptTarget;

/**
 * Checks that the generated scripts follow the examples of the integration section of the documentation.
 */
public class DocumentationExamplesTest {

    private static final DataRequest DATA = DataRequest.builder()
            .flowOf("EXR")
            .keyOf("M.CHF.EUR.SP00.A")
            .lastNObservations(12)
            .build();

    @Test
    public void testBashCliData() {
        assertThat(generate("bash/cli", DATA))
                .isEqualTo("#!/usr/bin/env bash\n"
                        + "set -euo pipefail\n"
                        + "\n"
                        + "sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --last-n 12 \\\n"
                        + "  | mlr --icsv --ocsv cut -o -f Series,ObsPeriod,ObsValue\n");
    }

    @Test
    public void testBashCliFlows() {
        assertThat(generate("bash/cli", FlowsRequest.DEFAULT))
                .isEqualTo("#!/usr/bin/env bash\n"
                        + "set -euo pipefail\n"
                        + "\n"
                        + "sdmx-dl list flows ECB \\\n"
                        + "  | mlr --icsv --ocsv cut -o -f Ref,Name,Description\n");
    }

    @Test
    public void testBashRestData() {
        assertThat(generate("bash/rest", DATA))
                .isEqualTo(
                        "#!/usr/bin/env bash\n"
                                + "set -euo pipefail\n"
                                + "\n"
                                + "echo Series,ObsPeriod,ObsValue\n"
                                + "curl -fsS -G http://localhost:4559/sdmx-dl/v2/ECB/EXR/data \\\n"
                                + "  --data-urlencode key=M.CHF.EUR.SP00.A \\\n"
                                + "  --data-urlencode lastN=12 \\\n"
                                + "  | jq -r '(.data // [])[] | .key as $key | (.obs // [])[] | [$key, (.period | split(\"/\")[0]), (.value // 0)] | @csv'\n");
    }

    @Test
    public void testBashRestFlows() {
        assertThat(generate("bash/rest", FlowsRequest.DEFAULT))
                .isEqualTo("#!/usr/bin/env bash\n"
                        + "set -euo pipefail\n"
                        + "\n"
                        + "echo Ref,Name,Description\n"
                        + "curl -fsS -G http://localhost:4559/sdmx-dl/v2/ECB/flows \\\n"
                        + "  | jq -r '.[] | [.ref // \"\", .name // \"\", .description // \"\"] | @csv'\n");
    }

    @Test
    public void testBatchCliData() {
        assertThat(generate("batch/cli", DATA))
                .isEqualTo("@echo off\r\n"
                        + "setlocal\r\n"
                        + "\r\n"
                        + "call sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --last-n 12\r\n"
                        + "if errorlevel 1 exit /b %errorlevel%\r\n"
                        + "\r\n"
                        + "endlocal\r\n");
    }

    @Test
    public void testBatchCliFlows() {
        assertThat(generate("batch/cli", FlowsRequest.DEFAULT))
                .isEqualTo("@echo off\r\n"
                        + "setlocal\r\n"
                        + "\r\n"
                        + "call sdmx-dl list flows ECB\r\n"
                        + "if errorlevel 1 exit /b %errorlevel%\r\n"
                        + "\r\n"
                        + "endlocal\r\n");
    }

    @Test
    public void testJBangJavaData() {
        assertThat(generate("jbang/java", DATA))
                .isEqualTo("//usr/bin/env jbang \"$0\" \"$@\" ; exit $?\n"
                        + "//JAVA 25+\n"
                        + "//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:" + About.VERSION + "\n"
                        + "\n"
                        + "import java.io.PrintWriter;\n"
                        + "import sdmxdl.DataRequest;\n"
                        + "import sdmxdl.web.SdmxWebManager;\n"
                        + "\n"
                        + "void main() throws Exception {\n"
                        + "    var result = SdmxWebManager.ofServiceLoader()\n"
                        + "        .usingName(\"ECB\")\n"
                        + "        .getData(DataRequest.builder()\n"
                        + "            .flowOf(\"EXR\")\n"
                        + "            .keyOf(\"M.CHF.EUR.SP00.A\")\n"
                        + "            .lastNObservations(12)\n"
                        + "            .build());\n"
                        + "\n"
                        + "    try (var out = new PrintWriter(System.out)) {\n"
                        + "        out.println(\"Series,ObsPeriod,ObsValue\");\n"
                        + "        result.forEach(series -> series.getObs().forEach(obs ->\n"
                        + "            out.println(csv(series.getKey()) + \",\" + csv(obs.getPeriod().getStart()) + \",\" + csv(obs.getValue()))));\n"
                        + "    }\n"
                        + "}\n"
                        + "\n"
                        + "String csv(Object value) {\n"
                        + "    var text = value == null ? \"\" : value.toString();\n"
                        + "    return text.contains(\",\") || text.contains(\"\\\"\") || text.contains(\"\\n\") || text.contains(\"\\r\")\n"
                        + "        ? \"\\\"\" + text.replace(\"\\\"\", \"\\\"\\\"\") + \"\\\"\"\n"
                        + "        : text;\n"
                        + "}\n");
    }

    @Test
    public void testJBangJavaFlows() {
        assertThat(generate("jbang/java", FlowsRequest.DEFAULT))
                .isEqualTo("//usr/bin/env jbang \"$0\" \"$@\" ; exit $?\n"
                        + "//JAVA 25+\n"
                        + "//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:" + About.VERSION + "\n"
                        + "\n"
                        + "import java.io.PrintWriter;\n"
                        + "import sdmxdl.FlowsRequest;\n"
                        + "import sdmxdl.web.SdmxWebManager;\n"
                        + "\n"
                        + "void main() throws Exception {\n"
                        + "    var result = SdmxWebManager.ofServiceLoader()\n"
                        + "        .usingName(\"ECB\")\n"
                        + "        .listFlows(FlowsRequest.builder()\n"
                        + "            .build());\n"
                        + "\n"
                        + "    try (var out = new PrintWriter(System.out)) {\n"
                        + "        out.println(\"Ref,Name,Description\");\n"
                        + "        result.forEach(item -> out.println(csv(item.getRef()) + \",\" + csv(item.getName()) + \",\" + csv(item.getDescription())));\n"
                        + "    }\n"
                        + "}\n"
                        + "\n"
                        + "String csv(Object value) {\n"
                        + "    var text = value == null ? \"\" : value.toString();\n"
                        + "    return text.contains(\",\") || text.contains(\"\\\"\") || text.contains(\"\\n\") || text.contains(\"\\r\")\n"
                        + "        ? \"\\\"\" + text.replace(\"\\\"\", \"\\\"\\\"\") + \"\\\"\"\n"
                        + "        : text;\n"
                        + "}\n");
    }

    @Test
    public void testJupyterCliData() {
        assertThat(generate("jupyter/cli", DATA))
                .isEqualTo("import os\n"
                        + "import subprocess\n"
                        + "import tempfile\n"
                        + "\n"
                        + "import pandas as pd\n"
                        + "\n"
                        + "with tempfile.NamedTemporaryFile(suffix=\".csv\", delete=False) as tmp:\n"
                        + "    outfile = tmp.name\n"
                        + "\n"
                        + "subprocess.run([\"sdmx-dl\", \"fetch\", \"data\", \"ECB\", \"EXR\", \"M.CHF.EUR.SP00.A\", \"--last-n\", \"12\", \"-o\", outfile], check=True)\n"
                        + "\n"
                        + "df = pd.read_csv(outfile)[[\"Series\", \"ObsPeriod\", \"ObsValue\"]]\n"
                        + "os.unlink(outfile)\n"
                        + "df\n");
    }

    @Test
    public void testJupyterCliFlows() {
        assertThat(generate("jupyter/cli", FlowsRequest.DEFAULT))
                .isEqualTo("import os\n"
                        + "import subprocess\n"
                        + "import tempfile\n"
                        + "\n"
                        + "import pandas as pd\n"
                        + "\n"
                        + "with tempfile.NamedTemporaryFile(suffix=\".csv\", delete=False) as tmp:\n"
                        + "    outfile = tmp.name\n"
                        + "\n"
                        + "subprocess.run([\"sdmx-dl\", \"list\", \"flows\", \"ECB\", \"-o\", outfile], check=True)\n"
                        + "\n"
                        + "df = pd.read_csv(outfile)[[\"Ref\", \"Name\", \"Description\"]]\n"
                        + "os.unlink(outfile)\n"
                        + "df\n");
    }

    @Test
    public void testJupyterRestData() {
        assertThat(generate("jupyter/rest", DATA))
                .isEqualTo("import pandas as pd\n"
                        + "import requests\n"
                        + "\n"
                        + "response = requests.get(\"http://localhost:4559/sdmx-dl/v2/ECB/EXR/data\", params={\"key\": \"M.CHF.EUR.SP00.A\", \"lastN\": 12})\n"
                        + "response.raise_for_status()\n"
                        + "payload = response.json()\n"
                        + "\n"
                        + "records = []\n"
                        + "for series in payload.get(\"data\", []):\n"
                        + "    for obs in series.get(\"obs\", []):\n"
                        + "        records.append({\n"
                        + "            \"Series\": series[\"key\"],\n"
                        + "            \"ObsPeriod\": obs[\"period\"].split(\"/\")[0],\n"
                        + "            \"ObsValue\": obs.get(\"value\", 0.0),\n"
                        + "        })\n"
                        + "\n"
                        + "df = pd.DataFrame(records, columns=[\"Series\", \"ObsPeriod\", \"ObsValue\"])\n"
                        + "df\n");
    }

    @Test
    public void testJupyterRestFlows() {
        assertThat(generate("jupyter/rest", FlowsRequest.DEFAULT))
                .isEqualTo("import pandas as pd\n"
                        + "import requests\n"
                        + "\n"
                        + "response = requests.get(\"http://localhost:4559/sdmx-dl/v2/ECB/flows\")\n"
                        + "response.raise_for_status()\n"
                        + "payload = response.json()\n"
                        + "\n"
                        + "df = pd.DataFrame([[item.get(\"ref\", \"\"), item.get(\"name\", \"\"), item.get(\"description\", \"\")] for item in payload], columns=[\"Ref\", \"Name\", \"Description\"])\n"
                        + "df\n");
    }

    @Test
    public void testPowerQueryRestData() {
        assertThat(generate("powerquery/rest", DATA))
                .isEqualTo("let\n"
                        + "    Source = Json.Document(\n"
                        + "        Web.Contents(\n"
                        + "            \"http://localhost:4559\",\n"
                        + "            [\n"
                        + "                RelativePath = \"sdmx-dl/v2/ECB/EXR/data\",\n"
                        + "                Query = [key = \"M.CHF.EUR.SP00.A\", lastN = \"12\"]\n"
                        + "            ]\n"
                        + "        )\n"
                        + "    ),\n"
                        + "    SeriesTable = Table.FromRecords(Source[data]),\n"
                        + "    ExpandedObs = Table.ExpandListColumn(SeriesTable, \"obs\"),\n"
                        + "    ExpandedObsRecord = Table.ExpandRecordColumn(ExpandedObs, \"obs\", {\"period\", \"value\"}, {\"ObsPeriod\", \"ObsValue\"}),\n"
                        + "    RenamedColumns = Table.RenameColumns(ExpandedObsRecord, {{\"key\", \"Series\"}}),\n"
                        + "    SelectedColumns = Table.SelectColumns(RenamedColumns, {\"Series\", \"ObsPeriod\", \"ObsValue\"}),\n"
                        + "    TrimmedPeriod = Table.TransformColumns(SelectedColumns, {{\"ObsPeriod\", each Text.BeforeDelimiter(_, \"/\"), type text}})\n"
                        + "in\n"
                        + "    TrimmedPeriod\n");
    }

    @Test
    public void testPowerQueryRestFlows() {
        assertThat(generate("powerquery/rest", FlowsRequest.DEFAULT))
                .isEqualTo("let\n"
                        + "    Source = Json.Document(\n"
                        + "        Web.Contents(\n"
                        + "            \"http://localhost:4559\",\n"
                        + "            [\n"
                        + "                RelativePath = \"sdmx-dl/v2/ECB/flows\"\n"
                        + "            ]\n"
                        + "        )\n"
                        + "    ),\n"
                        + "    ItemsTable = Table.FromRecords(Source, {\"ref\", \"name\", \"description\"}, MissingField.UseNull),\n"
                        + "    RenamedColumns = Table.RenameColumns(ItemsTable, {{\"ref\", \"Ref\"}, {\"name\", \"Name\"}, {\"description\", \"Description\"}})\n"
                        + "in\n"
                        + "    RenamedColumns\n");
    }

    @Test
    public void testPowerShellCliData() {
        assertThat(generate("powershell/cli", DATA))
                .isEqualTo("$data = & sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --last-n 12 | ConvertFrom-Csv\n"
                        + "if ($LASTEXITCODE -ne 0) { throw \"sdmx-dl failed with exit code $LASTEXITCODE\" }\n"
                        + "\n"
                        + "$data | Select-Object Series, ObsPeriod, ObsValue | ConvertTo-Csv -NoTypeInformation\n");
    }

    @Test
    public void testPowerShellCliFlows() {
        assertThat(generate("powershell/cli", FlowsRequest.DEFAULT))
                .isEqualTo("$data = & sdmx-dl list flows ECB | ConvertFrom-Csv\n"
                        + "if ($LASTEXITCODE -ne 0) { throw \"sdmx-dl failed with exit code $LASTEXITCODE\" }\n"
                        + "\n"
                        + "$data | Select-Object Ref, Name, Description | ConvertTo-Csv -NoTypeInformation\n");
    }

    @Test
    public void testPowerShellRestData() {
        assertThat(generate("powershell/rest", DATA))
                .isEqualTo("$response = Invoke-RestMethod -Uri 'http://localhost:4559/sdmx-dl/v2/ECB/EXR/data' `\n"
                        + "    -Body @{ key = 'M.CHF.EUR.SP00.A'; lastN = 12 }\n"
                        + "\n"
                        + "$response.data | ForEach-Object {\n"
                        + "    $series = $_\n"
                        + "    $series.obs | ForEach-Object {\n"
                        + "        [pscustomobject]@{\n"
                        + "            Series    = $series.key\n"
                        + "            ObsPeriod = ($_.period -split '/')[0]\n"
                        + "            ObsValue  = if ($null -eq $_.value) { 0.0 } else { $_.value }\n"
                        + "        }\n"
                        + "    }\n"
                        + "} | ConvertTo-Csv -NoTypeInformation\n");
    }

    @Test
    public void testPowerShellRestFlows() {
        assertThat(generate("powershell/rest", FlowsRequest.DEFAULT))
                .isEqualTo(
                        "$response = Invoke-RestMethod -Uri 'http://localhost:4559/sdmx-dl/v2/ECB/flows'\n"
                                + "\n"
                                + "$response | Select-Object @{ Name = 'Ref'; Expression = { $_.ref } }, @{ Name = 'Name'; Expression = { $_.name } }, @{ Name = 'Description'; Expression = { $_.description } } | ConvertTo-Csv -NoTypeInformation\n");
    }

    @Test
    public void testRCliData() {
        assertThat(generate("r/cli", DATA))
                .isEqualTo("outfile <- tempfile(fileext = \".csv\")\n"
                        + "\n"
                        + "status <- system2(\n"
                        + "  command = \"sdmx-dl\",\n"
                        + "  args = shQuote(c(\"fetch\", \"data\", \"ECB\", \"EXR\", \"M.CHF.EUR.SP00.A\", \"--last-n\", \"12\", \"-o\", outfile))\n"
                        + ")\n"
                        + "if (status != 0) stop(\"sdmx-dl failed with exit code \", status)\n"
                        + "\n"
                        + "dat <- read.csv(outfile, check.names = FALSE)[, c(\"Series\", \"ObsPeriod\", \"ObsValue\"), drop = FALSE]\n"
                        + "unlink(outfile)\n"
                        + "\n"
                        + "write.csv(dat, stdout(), row.names = FALSE)\n");
    }

    @Test
    public void testRCliFlows() {
        assertThat(generate("r/cli", FlowsRequest.DEFAULT))
                .isEqualTo("outfile <- tempfile(fileext = \".csv\")\n"
                        + "\n"
                        + "status <- system2(\n"
                        + "  command = \"sdmx-dl\",\n"
                        + "  args = shQuote(c(\"list\", \"flows\", \"ECB\", \"-o\", outfile))\n"
                        + ")\n"
                        + "if (status != 0) stop(\"sdmx-dl failed with exit code \", status)\n"
                        + "\n"
                        + "dat <- read.csv(outfile, check.names = FALSE)[, c(\"Ref\", \"Name\", \"Description\"), drop = FALSE]\n"
                        + "unlink(outfile)\n"
                        + "\n"
                        + "write.csv(dat, stdout(), row.names = FALSE)\n");
    }

    @Test
    public void testRRestData() {
        assertThat(generate("r/rest", DATA))
                .isEqualTo("library(jsonlite)\n"
                        + "\n"
                        + "or_else <- function(x, default) if (is.null(x)) default else x\n"
                        + "\n"
                        + "payload <- fromJSON(\"http://localhost:4559/sdmx-dl/v2/ECB/EXR/data?key=M.CHF.EUR.SP00.A&lastN=12\", simplifyDataFrame = FALSE)\n"
                        + "\n"
                        + "dat <- do.call(rbind, lapply(payload$data, function(series) {\n"
                        + "  do.call(rbind, lapply(series$obs, function(obs) {\n"
                        + "    data.frame(\n"
                        + "      Series = series$key,\n"
                        + "      ObsPeriod = sub(\"/.*\", \"\", obs$period),\n"
                        + "      ObsValue = or_else(obs$value, 0)\n"
                        + "    )\n"
                        + "  }))\n"
                        + "}))\n"
                        + "\n"
                        + "write.csv(dat, stdout(), row.names = FALSE)\n");
    }

    @Test
    public void testRRestFlows() {
        assertThat(generate("r/rest", FlowsRequest.DEFAULT))
                .isEqualTo("library(jsonlite)\n"
                        + "\n"
                        + "or_else <- function(x, default) if (is.null(x)) default else x\n"
                        + "\n"
                        + "payload <- fromJSON(\"http://localhost:4559/sdmx-dl/v2/ECB/flows\", simplifyDataFrame = FALSE)\n"
                        + "\n"
                        + "dat <- do.call(rbind, lapply(payload, function(item) {\n"
                        + "  data.frame(\n"
                        + "    Ref = or_else(item$ref, \"\"),\n"
                        + "    Name = or_else(item$name, \"\"),\n"
                        + "    Description = or_else(item$description, \"\")\n"
                        + "  )\n"
                        + "}))\n"
                        + "\n"
                        + "write.csv(dat, stdout(), row.names = FALSE)\n");
    }

    private static String generate(String target, Request request) {
        return ScriptManager.ofServiceLoader()
                .generate(ScriptTarget.parse(target), "ECB", request, ScriptOptions.DEFAULT)
                .getContent();
    }
}
