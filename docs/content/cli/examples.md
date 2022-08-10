---
title: "Examples"
weight: 6
---

{{< shields_io/badge label="fetch" message="examples" color="dc322f" >}}

1. Download monthly (`M`) exchange rates (`EXR`) of Swiss franc (`CHF`) from the European Central Bank (`ECB`)  
   <pre>sdmx-dl fetch <b><a href='{{< relref "usage#fetch-data" >}}'>data</a></b> ECB EXR M.CHF.EUR.SP00.A</pre>  
2. Same as 1 with saving into a file  
   <pre>sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A <b><a href='{{< relref "options#output" >}}'>-o filename.csv</a></b></pre>
3. Same as 1 with saving into a compressed file  
   <pre>sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A -o filename.csv.gz <b><a href='{{< relref "options#gzipped" >}}'>-z</a></b></pre>
4. Same as 1 with displaying as human-readable table ([PowerShell](https://en.wikipedia.org/wiki/PowerShell) example)  
   <pre>sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A <b>| <a href="https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.utility/convertfrom-csv">ConvertFrom-Csv</a> | <a href="https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.utility/select-object">Select-Object -First 3</a> | <a href="https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.utility/format-table">Format-Table</a></b></pre>
   {{< expand "Output sample" >}}
   ```plain
   Series           ObsAttributes ObsPeriod           ObsValue
   ------           ------------- ---------           --------
   M.CHF.EUR.SP00.A OBS_STATUS=A  1999-01-01T00:00:00 1.605495
   M.CHF.EUR.SP00.A OBS_STATUS=A  1999-02-01T00:00:00 1.59785
   M.CHF.EUR.SP00.A OBS_STATUS=A  1999-03-01T00:00:00 1.595430434782609
   ```
   {{< /expand >}}
5. Same as 1 with displaying as human-readable table ([xsv](https://github.com/BurntSushi/xsv) example)  
   <pre>sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A <b>| <a href="https://github.com/BurntSushi/xsv#available-commands">xsv slice -l 3</a> | <a href="https://github.com/BurntSushi/xsv#available-commands">xsv table</a></b></pre>
   {{< expand "Output sample" >}}
   ```plain
   Series            ObsAttributes  ObsPeriod            ObsValue
   M.CHF.EUR.SP00.A  OBS_STATUS=A   1999-01-01T00:00:00  1.605495
   M.CHF.EUR.SP00.A  OBS_STATUS=A   1999-02-01T00:00:00  1.59785
   M.CHF.EUR.SP00.A  OBS_STATUS=A   1999-03-01T00:00:00  1.595430434782609
   ```
   {{< /expand >}}
6. Same as 1 with US dollar (`USD`) alongside Swiss franc  
   <pre>sdmx-dl fetch data ECB EXR M.<b>CHF+USD</b>.EUR.SP00.A</pre>
7. Same as 1 for every available currencies  
   <pre>sdmx-dl fetch data ECB EXR M<b>..</b>EUR.SP00.A</pre>
8. Download available series in a dataset  
   <pre>sdmx-dl fetch <b><a href='{{< relref "usage#fetch-keys" >}}'>keys</a></b> ECB EXR <b>.</b>CHF.EUR.SP00.A</pre>

{{< shields_io/badge label="list" message="examples" color="859900" >}}

1. List available datasets from the European Central Bank (`ECB`)  
   <pre>sdmx-dl list <b><a href='{{< relref "usage#list-flows" >}}'>flows</a></b> <b>ECB</b></pre>
2. List dimensions of the exchange rates (`EXR`)  
   <pre>sdmx-dl list <b><a href='{{< relref "usage#list-dimensions" >}}'>dimensions</a></b> ECB <b>EXR</b></pre>
3. List codes of the frequency dimension (`FREQ`)  
   <pre>sdmx-dl list <b><a href='{{< relref "usage#list-codes" >}}'>codes</a></b> ECB EXR <b>FREQ</b></pre>
