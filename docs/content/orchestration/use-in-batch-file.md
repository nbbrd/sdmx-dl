---
title: "Use in batch file"
weight: 1
---

Automate repeated **sdmx-dl** CLI calls on Windows by putting them in a `.bat` file.
This is a good fit for scheduled jobs, repeatable downloads, and small orchestration tasks that do not need a full programming language.

{{< tabs "use-batch-file" >}}

{{< tab "With launcher" >}}

If `sdmx-dl.bat` is already on your `PATH`, a batch file can call it directly:

```bat
@echo off
setlocal

set "OUTDIR=%~dp0out"
if not exist "%OUTDIR%" mkdir "%OUTDIR%"

sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --last-n 12 -o "%OUTDIR%\ecb-chf.csv"

endlocal
```

{{< /tab >}}

{{< tab "Without installation" >}}

You can also batch calls without installing anything by invoking the CLI jar directly:

```bat
@echo off
setlocal

set "SDMX_DL_JAR=C:\tools\sdmx-dl-cli-{{< sdmx-dl-version >}}-bin.jar"
set "OUTDIR=%~dp0out"
if not exist "%OUTDIR%" mkdir "%OUTDIR%"

java -jar "%SDMX_DL_JAR%" fetch data ECB EXR M.CHF.EUR.SP00.A --last-n 12 -o "%OUTDIR%\ecb-chf.csv"

endlocal
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- A batch file is just orchestration around the CLI: each line runs a normal `sdmx-dl` command, so you can mix discovery (`list ...`), retrieval (`fetch ...`), and checks (`check ...`) in the same script.
- Use `-o` when a command already supports writing to a file; use `>`/`>>` when you want standard output redirection instead.
- Inside a `.bat` file, loop variables use doubled percent signs (`%%C`). If you type the same loop directly in `cmd.exe`, use a single percent sign instead (`%C`).
- Some commands already support batching by themselves, such as `sdmx-dl check status ECB IMF`, so prefer a single CLI call when it already matches your workflow.
- For more advanced control flow, error handling, or CSV post-processing, [PowerShell]({{< relref "/cli/examples" >}}) may be more convenient, but `.bat` files remain a simple zero-dependency option.

## Related features

- [Discover sources]({{< relref "/usage/discover-sources" >}})
- [Retrieve data]({{< relref "/usage/retrieve-data" >}})
- [Monitor and status]({{< relref "/usage/monitor-and-status" >}})
