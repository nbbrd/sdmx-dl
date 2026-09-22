---
title: "Use in Jupyter"
weight: 4
---

Bring **sdmx-dl** into Jupyter notebooks when you want interactive exploration, live charting, or reproducible analysis alongside your data fetching.

{{< tabs "use-in-jupyter" >}}

{{< tab "CLI" >}}

Jupyter can call `sdmx-dl` using shell commands and then load the output:

```python
import subprocess
import pandas as pd
import tempfile
import os

outfile = tempfile.NamedTemporaryFile(mode='w', suffix='.csv', delete=False).name

subprocess.run([
    'sdmx-dl', 'fetch', 'data', 'ECB', 'EXR', 'M.CHF.EUR.SP00.A',
    '--last-n', '12',
    '-o', outfile
], check=True)

df = pd.read_csv(outfile)
os.unlink(outfile)
df
```

This approach is ideal when you already use the CLI interactively and want your notebook to reuse the same commands.
{{< /tab >}}

{{< tab "REST" >}}

If the web service is running, notebooks can fetch JSON directly and reshape it:

```python
import requests
import pandas as pd

url = "http://localhost:4559/sdmx-dl/v2/ECB/EXR/data?key=M.CHF.EUR.SP00.A&lastNObservations=12"
payload = requests.get(url).json()

records = []
for series in payload['data']:
    for obs in series['obs']:
        records.append({
            'Series': series['key'],
            'ObsPeriod': obs['period'],
            'ObsValue': obs['value']
        })

df = pd.DataFrame(records)
df
```

The same pattern works for discovery endpoints like `sources`, `flows`, or `dimensions`:

```python
import requests
import pandas as pd

sources = requests.get("http://localhost:4559/sdmx-dl/v2/sources").json()
df = pd.DataFrame(sources)
df.head()
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- **Windows launcher**: On Windows, if `subprocess` cannot find `sdmx-dl`, ensure the launcher is on your `PATH`. You may need to create it first. Alternatively, use the full path to the jar or `.bat` file in the subprocess call.
- The CLI examples produce CSV files that `pandas.read_csv(...)` can load directly.
- The REST examples return JSON (via `requests.get(...).json()`), which you reshape with `pandas.DataFrame(...)` or list comprehension.
- The CLI approach is simplest when `sdmx-dl` is already installed and available on your notebook server.
- The REST approach is often better when the notebook server and the sdmx-dl service run on different machines, or when you want to query the service from multiple notebooks without invoking subprocess.
- For real-time or exploratory work, combine discovery calls first (e.g. list sources, list flows, list dimensions) to interactively build your final data request before committing it to analysis code.
- Both approaches integrate well with `pandas`, `plotly`, `matplotlib`, and other data science libraries.

## Related features

- [Discover sources]({{< relref "/usage/discover-sources" >}})
- [Retrieve data]({{< relref "/usage/retrieve-data" >}})
- [Web service]({{< relref "/ws" >}})

