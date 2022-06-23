---
title: "Examples"
weight: 6
---

<a id="fetch" href="#fetch">![fetch](https://img.shields.io/badge/fetch-examples-dc322f?style=flat-square)</a>

1. Download monthly (`M`) exchange rates (`EXR`) of Swiss franc (`CHF`) from the European Central Bank (`ECB`)  
   <pre>sdmx-dl fetch <b>data</b> ECB EXR M.CHF.EUR.SP00.A</pre>  
2. Same as 1 with saving into a file  
   <pre>sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A <b>-o C:\somewhere\file.csv</b></pre>
3. Same as 1 with saving into a compressed file  
   <pre>sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A -o C:\somewhere\file.csv<b>.gz</b></pre>
4. Same as 1 with displaying as human-readable table ([PowerShell](https://en.wikipedia.org/wiki/PowerShell) example)  
   <pre>sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A <b>| <a href="https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.utility/convertfrom-csv">ConvertFrom-Csv</a> | <a href="https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.utility/select-object">Select-Object -First 3</a> | <a href="https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.utility/format-table">Format-Table</a></b></pre>
   {{< expand "Output sample" >}}
   ```
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
   ```
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
   <pre>sdmx-dl fetch <b>keys</b> ECB EXR <b>.</b>CHF.EUR.SP00.A</pre>

<a id="list" href="#list">![list](https://img.shields.io/badge/list-examples-859900?style=flat-square)</a>

1. List available datasets from the European Central Bank (`ECB`)  
   <pre>sdmx-dl list <b>flows ECB</b></pre>
2. List dimensions of the exchange rates (`EXR`)  
   <pre>sdmx-dl list <b>concepts</b> ECB <b>EXR</b></pre>
3. List codes of the frequency dimension (`FREQ`)  
   <pre>sdmx-dl list <b>codes</b> ECB EXR <b>FREQ</b></pre>
