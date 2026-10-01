---
title: "Use in Python"
weight: 6
---

Automate **sdmx-dl** from a plain Python script, using only the standard library.
This is a good fit for scheduled jobs and data pipelines; for interactive analysis with pandas, see [Use in Jupyter]({{< relref "/integration/use-in-jupyter" >}}).

{{< tabs "use-in-python" >}}

{{< tab "CLI" >}}

If `sdmx-dl` is on your `PATH`, a script can run it with `subprocess` and parse its CSV output with the `csv` module:

```python
import csv
import io
import subprocess
import sys

result = subprocess.run(
    ["sdmx-dl", "fetch", "data", "ECB", "EXR", "M.CHF.EUR.SP00.A", "--last-n", "12"],
    capture_output=True, text=True, check=True,
)

writer = csv.writer(sys.stdout)
writer.writerow(["Series", "ObsPeriod", "ObsValue"])
for row in csv.DictReader(io.StringIO(result.stdout)):
    writer.writerow([row["Series"], row["ObsPeriod"], row["ObsValue"]])
```
{{< /tab >}}

{{< tab "REST" >}}

If the web service is running, a script can query it with `urllib` and reshape the JSON response (see the [response format]({{< relref "/ws#response-format" >}})):

```python
import csv
import json
import sys
import urllib.parse
import urllib.request

params = urllib.parse.urlencode({"key": "M.CHF.EUR.SP00.A", "lastN": 12})
with urllib.request.urlopen(f"http://localhost:4559/sdmx-dl/v2/ECB/EXR/data?{params}") as response:
    payload = json.load(response)

writer = csv.writer(sys.stdout)
writer.writerow(["Series", "ObsPeriod", "ObsValue"])
for series in payload["data"]:
    for obs in series["obs"]:
        writer.writerow([series["key"], obs["period"].split("/")[0], obs["value"]])
```
{{< /tab >}}

{{< /tabs >}}

## Generate these scripts

sdmx-dl can [generate]({{< relref "/integration/generate-scripts" >}}) the scripts above for any request:

```shell
sdmx-dl script data ECB EXR M.CHF.EUR.SP00.A --last-n 12 -t python/cli
sdmx-dl script data ECB EXR M.CHF.EUR.SP00.A --last-n 12 -t python/rest
```

## Notes

- Both examples use only the standard library; swap in `requests` or `pandas` if they're already part of your project.
- `check=True` raises `subprocess.CalledProcessError` when `sdmx-dl` fails, and `urlopen` raises `urllib.error.HTTPError` on HTTP errors, so failures stop the script instead of producing empty output.
- **Windows launcher**: `subprocess` doesn't resolve `.bat`/`.cmd` launchers from a bare command name; use the full path to the launcher, or call the jar with `["java", "-jar", "path/to/sdmx-dl-cli-bin.jar", ...]`.
- The REST approach is often better when the script runs on another machine than sdmx-dl, or when several scripts share one long-lived service and its cache.

## Related features

- [Retrieve data]({{< relref "/usage/retrieve#data" >}})
- [Use in Jupyter]({{< relref "/integration/use-in-jupyter" >}})
- [Web service]({{< relref "/ws" >}})
