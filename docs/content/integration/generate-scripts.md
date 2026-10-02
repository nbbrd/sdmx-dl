---
title: "Generate scripts"
weight: 9
---

Let **sdmx-dl** write the integration script for you.
Describe a request once (source, flow, key, filters…) and get a ready-to-run script in the language and transport of your choice, already reshaped into a table like the examples of this section.

The script is only generated, never executed: copy it into your own workflow and run it there.

## Targets

A target is written as `<language>/<transport>`, where the transport is the [approach]({{< relref "/integration#which-approach" >}}) used by the script: `cli`, `rest` or `java`.

| Target            | Script                                     | Output                                   | Example page                                                        |
|-------------------|--------------------------------------------|------------------------------------------|---------------------------------------------------------------------|
| `batch/cli`       | Windows `.bat` file                        | Raw CLI CSV                              | [Use in batch file]({{< relref "/integration/use-in-batch-file" >}}) |
| `bash/cli`        | bash script, reshaped with Miller (`mlr`)  | CSV                                      | [Use in bash script]({{< relref "/integration/use-in-bash" >}})     |
| `bash/rest`       | bash script with `curl` and `jq`           | CSV                                      | [Use in bash script]({{< relref "/integration/use-in-bash" >}})     |
| `powershell/cli`  | PowerShell script                          | CSV                                      | [Use in PowerShell]({{< relref "/integration/use-in-powershell" >}}) |
| `powershell/rest` | PowerShell script                          | CSV                                      | [Use in PowerShell]({{< relref "/integration/use-in-powershell" >}}) |
| `powerquery/rest` | Power Query (M) query for Excel            | Table                                    | [Use in Excel]({{< relref "/integration/use-in-excel" >}})          |
| `r/cli`           | R script                                   | CSV                                      | [Use in R]({{< relref "/integration/use-in-r" >}})                  |
| `r/rest`          | R script (`jsonlite`)                      | CSV                                      | [Use in R]({{< relref "/integration/use-in-r" >}})                  |
| `python/cli`      | Python 3.7+ script, standard library only  | CSV                                      | [Use in Python]({{< relref "/integration/use-in-python" >}})        |
| `python/rest`     | Python 3.7+ script, standard library only  | CSV                                      | [Use in Python]({{< relref "/integration/use-in-python" >}})        |
| `jupyter/cli`     | Notebook cell with pandas                  | DataFrame                                | [Use in Jupyter]({{< relref "/integration/use-in-jupyter" >}})      |
| `jupyter/rest`    | Notebook cell with pandas                  | DataFrame                                | [Use in Jupyter]({{< relref "/integration/use-in-jupyter" >}})      |
| `jbang/java`      | Single-file Java script for jbang          | CSV                                      | [Use in jbang script]({{< relref "/integration/use-in-jbang" >}})   |

Every target supports two requests:
- **data**: fetch observations, as `Series`, `ObsPeriod`, `ObsValue` columns;
- **flows**: list or search the flows of a source, as `Ref`, `Name`, `Description` columns.

The list of targets is extensible (see [Custom targets](#custom-targets)); ask your installation for the current list with `sdmx-dl script targets`.

## Usage

{{< tabs "generate-scripts" >}}

{{< tab "CLI" >}}

Use the [`script`]({{< relref "/cli/usage#script" >}}) command with the same parameters as `fetch data` or `list flows`, plus a `-t, --target`:

```shell
sdmx-dl script data ECB EXR M.CHF.EUR.SP00.A --last-n 12 -t r/rest
sdmx-dl script flows ECB -q "exchange rates" -t powershell/cli --script-file flows.ps1
sdmx-dl script targets
```
{{< /tab >}}

{{< tab "REST" >}}

Call the [web service]({{< relref "/ws#rest-endpoint" >}}) operations ending with `:script`; they take the same parameters as their regular counterpart, plus a `target`:

```shell
curl "localhost:4559/sdmx-dl/v2/ECB/EXR/data:script?key=M.CHF.EUR.SP00.A&lastN=12&target=bash/rest"
curl "localhost:4559/sdmx-dl/v2/ECB/flows:script?query=exchange&target=python/cli"
curl "localhost:4559/sdmx-dl/v2/script/targets"
```

The response is a JSON object with the `target`, the `fileExtension`, the script `content` and its `warnings`.
When the `restEndpoint` parameter is absent, REST scripts point to the server that generated them.
{{< /tab >}}

{{< tab "MCP" >}}

AI assistants connected to the [MCP endpoint]({{< relref "/ws#mcp-endpoint" >}}) can call the `listScriptTargets`, `generateDataScript` and `generateFlowsScript` tools, e.g. when asked *"give me an R script that downloads the CHF/EUR exchange rate"*.
{{< /tab >}}

{{< tab "API" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.DataRequest;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptOptions;
import sdmxdl.script.ScriptTarget;

void main() {
    var script = ScriptManager.ofServiceLoader().generate(
            ScriptTarget.parse("python/rest"),
            "ECB",
            DataRequest.builder().flowOf("EXR").keyOf("M.CHF.EUR.SP00.A").lastNObservations(12).build(),
            ScriptOptions.DEFAULT);
    IO.print(script.getContent());
}
```

The generators are provided by the `sdmx-dl-script` module, which is included in `sdmx-dl-standalone`.
{{< /tab >}}

{{< /tabs >}}

## Options

Besides the request itself, a few options adapt the script to the machine that will run it:

| Option         | CLI               | REST / MCP     | Default                             | Description                                                                 |
|----------------|-------------------|----------------|-------------------------------------|-----------------------------------------------------------------------------|
| Target         | `-t, --target`    | `target`       | `python/cli`                        | Language and transport of the script.                                       |
| CLI launcher   | `--cli-launcher`  | `cliLauncher`  | `sdmx-dl`                           | Command used by `cli` scripts to launch sdmx-dl, e.g. `jbang,sdmx-dl@nbbrd`. |
| REST endpoint  | `--rest-endpoint` | `restEndpoint` | `http://localhost:4559/sdmx-dl/v2`  | Base URI of the web service used by `rest` scripts.                         |
| Output file    | `-o, --output`    | `outputFile`   | standard output                     | File written by the script.                                                 |
| Properties     | `-P, --property`  | `property`     | none                                | Generator-specific `name=value` pairs (repeatable).                         |

The CLI also has a `--script-file` option to save the script itself into a file instead of printing it.

Properties let a generator offer options of its own without changing the other ones.
Their names start with `sdmxdl.script.` and the supported ones are listed per target by `script targets` (CLI) or `listScriptTargets` (web service and MCP).
Unsupported properties are ignored with a warning.
In gRPC, properties are a `map<string, string>` field of the script options.

## Warnings

Some targets cannot honor every request parameter; for example, the web service does not strip markup from flow descriptions, and a Power Query query returns a table instead of writing a file.
In that case, the script is still generated, with a warning:
- as a comment at the top of the script;
- in the `warnings` field of the REST, gRPC and MCP responses.

## Custom targets

Script generation is an extension point of the [Java library]({{< relref "/api" >}}).
To add a language, or to replace a built-in generator, implement the `sdmxdl.script.spi.ScriptGenerator` interface and register it with the Java `ServiceLoader` (`META-INF/services` or `provides` in `module-info.java`):
- `getScriptTarget()` declares the target, e.g. `julia/rest`;
- `getScriptRequestTypes()` declares the supported requests (`DataRequest`, `FlowsRequest`, …);
- `generateScript(sourceId, request, options)` returns the script content, its file extension and its warnings;
- `getScriptPropertyNames()` optionally declares generator-specific properties, named `sdmxdl.script.<language>.<name>` by convention;
- `getScriptRank()` decides which generator wins when several support the same target and request type; use `EXTERNAL_SCRIPT_RANK` to override a built-in one, for some request types or all of them.

Once the module is on the classpath, its targets appear in every consumer: CLI, web service and MCP.

## Related features

- [Retrieve data]({{< relref "/usage/retrieve#data" >}})
- [Discover flows]({{< relref "/usage/discover" >}})
- [Web service]({{< relref "/ws" >}})
