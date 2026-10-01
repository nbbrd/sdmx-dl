---
title: "Use in PowerShell"
weight: 3
---

Automate **sdmx-dl** with [PowerShell](https://learn.microsoft.com/powershell/), on Windows, Linux, or macOS.
PowerShell turns CSV and JSON into objects natively, which makes it convenient for filtering, reshaping, and error handling without extra tools.

{{< tabs "use-in-powershell" >}}

{{< tab "CLI" >}}

If `sdmx-dl` is on your `PATH`, a script can call it directly and parse its CSV output with `ConvertFrom-Csv`:

```powershell
$data = sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --last-n 12 | ConvertFrom-Csv
if ($LASTEXITCODE -ne 0) { throw "sdmx-dl failed with exit code $LASTEXITCODE" }

$data | Select-Object Series, ObsPeriod, ObsValue | Format-Table
```

To download several series into files, loop over their keys:

```powershell
$outDir = Join-Path $PSScriptRoot 'out'
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

foreach ($currency in 'CHF', 'USD', 'GBP') {
    sdmx-dl fetch data ECB EXR "M.$currency.EUR.SP00.A" --last-n 12 -o (Join-Path $outDir "ecb-$currency.csv")
    if ($LASTEXITCODE -ne 0) { throw "sdmx-dl failed for $currency" }
}
```
{{< /tab >}}

{{< tab "REST" >}}

If the web service is running, `Invoke-RestMethod` returns the JSON response as objects that can be reshaped directly (see the [response format]({{< relref "/ws#response-format" >}})):

```powershell
$response = Invoke-RestMethod -Uri 'http://localhost:4559/sdmx-dl/v2/ECB/EXR/data' `
    -Body @{ key = 'M.CHF.EUR.SP00.A'; lastN = 12 }

$response.data | ForEach-Object {
    $series = $_
    $series.obs | ForEach-Object {
        [pscustomobject]@{
            Series    = $series.key
            ObsPeriod = ($_.period -split '/')[0]
            ObsValue  = $_.value
        }
    }
} | Format-Table
```

The same pattern works for discovery endpoints such as `sources`, `flows`, or `dimensions`:

```powershell
Invoke-RestMethod 'http://localhost:4559/sdmx-dl/v2/ECB/flows' | Select-Object -First 5
```
{{< /tab >}}

{{< /tabs >}}

## Generate these scripts

sdmx-dl can [generate]({{< relref "/integration/generate-scripts" >}}) the scripts above for any request:

```shell
sdmx-dl script data ECB EXR M.CHF.EUR.SP00.A --last-n 12 -t powershell/cli
sdmx-dl script data ECB EXR M.CHF.EUR.SP00.A --last-n 12 -t powershell/rest
```

## Notes

- Native commands don't throw on failure; check `$LASTEXITCODE` after each `sdmx-dl` call (or set `$PSNativeCommandUseErrorActionPreference = $true` in PowerShell 7.3+).
- Replace `Format-Table` with `Export-Csv -NoTypeInformation out.csv` to save the result, or with `Out-GridView` for an interactive view on Windows.
- Without installation, replace `sdmx-dl` with `jbang sdmx-dl@nbbrd` (see [Installation]({{< relref "/cli/installation" >}})).
- To run a script on a schedule on Windows, register it with Task Scheduler, e.g. `Register-ScheduledTask` with `-Action (New-ScheduledTaskAction -Execute 'pwsh' -Argument '-File C:\path\to\fetch-ecb.ps1')`.
- More one-liners combining the CLI and PowerShell are available in the [CLI examples]({{< relref "/cli/examples" >}}).

## Related features

- [Retrieve data]({{< relref "/usage/retrieve#data" >}})
- [Monitor and status]({{< relref "/usage/monitor-and-status" >}})
- [Web service]({{< relref "/ws" >}})
