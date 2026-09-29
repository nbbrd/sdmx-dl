---
title: "Automation & integration"
weight: 2
aliases:
  - /orchestration/
---

Automate your workflow with scripts and scheduled jobs, or integrate sdmx-dl into the tools and applications you already use.

## Which approach?

Every integration below relies on one of three approaches:

| Approach                                          | What you need                                                                | Output                       | Best for                                                                                          |
|---------------------------------------------------|------------------------------------------------------------------------------|------------------------------|---------------------------------------------------------------------------------------------------|
| **Call the [CLI]({{< relref "/cli" >}})**          | `sdmx-dl` [installed]({{< relref "/cli/installation" >}}) on the machine running the script | CSV (file or standard output) | Scripts and scheduled jobs on a single machine; reusing commands you already run by hand.         |
| **Query the [web service]({{< relref "/ws" >}})**  | A running sdmx-dl service (local or remote) and an HTTP client               | [JSON]({{< relref "/ws#response-format" >}}) | Tools that speak HTTP (Excel, R, Python); sharing one service and its cache between users or processes. |
| **Use the [Java library]({{< relref "/api" >}})**  | Java, plus a build tool or [jbang](https://www.jbang.dev/)                    | Java objects                 | Full control and custom logic in Java, without spawning processes or running a service.          |

## Integrations

Every page uses the same running example: the last 12 monthly CHF/EUR exchange rates from the ECB (`ECB`, `EXR`, `M.CHF.EUR.SP00.A`), ending as a table with `Series`, `ObsPeriod`, and `ObsValue` columns.

### Scripting the CLI

- [Use in batch file]({{< relref "/integration/use-in-batch-file" >}}) - Automate repeated CLI calls from a Windows `.bat` script.
- [Use in bash script]({{< relref "/integration/use-in-bash" >}}) - Automate CLI or REST calls on Linux and macOS, and schedule them with cron.
- [Use in PowerShell]({{< relref "/integration/use-in-powershell" >}}) - Parse CLI or REST output as objects, on Windows, Linux, or macOS.

### Tools and languages

- [Use in Excel]({{< relref "/integration/use-in-excel" >}}) - Import CSV files or connect Power Query directly to the REST endpoint.
- [Use in R]({{< relref "/integration/use-in-r" >}}) - Call the CLI or REST endpoint from R and load the results as data frames.
- [Use in Python]({{< relref "/integration/use-in-python" >}}) - Call the CLI or REST endpoint from a plain Python script, standard library only.
- [Use in Jupyter]({{< relref "/integration/use-in-jupyter" >}}) - Fetch data in Python notebooks with subprocess or HTTP, reshape with pandas.

### Java

- [Use in jbang script]({{< relref "/integration/use-in-jbang" >}}) - Single-file Java scripts with zero project setup.

