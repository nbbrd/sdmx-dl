---
title: "Use in bash script"
weight: 2
---

Automate **sdmx-dl** on Linux and macOS with a `bash` script.
This is a good fit for cron jobs, CI pipelines, and small automation tasks that chain sdmx-dl with other command-line tools.

{{< tabs "use-in-bash" >}}

{{< tab "CLI" >}}

If `sdmx-dl` is on your `PATH`, a script can call it directly and pipe its CSV output to a CSV-aware tool such as [Miller](https://miller.readthedocs.io/):

```bash
#!/usr/bin/env bash
set -euo pipefail

sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --last-n 12 \
  | mlr --icsv --opprint cut -o -f Series,ObsPeriod,ObsValue
```

To download several series into files, loop over their keys:

```bash
#!/usr/bin/env bash
set -euo pipefail

outdir="$(dirname "$0")/out"
mkdir -p "$outdir"

for currency in CHF USD GBP; do
  sdmx-dl fetch data ECB EXR "M.$currency.EUR.SP00.A" --last-n 12 -o "$outdir/ecb-$currency.csv"
done
```
{{< /tab >}}

{{< tab "REST" >}}

If the web service is running, a script can query it with `curl` and reshape the JSON with [jq](https://jqlang.org/) (see the [response format]({{< relref "/ws#response-format" >}})):

```bash
#!/usr/bin/env bash
set -euo pipefail

echo "Series,ObsPeriod,ObsValue"
curl -fsS -G "http://localhost:4559/sdmx-dl/v2/ECB/EXR/data" \
  --data-urlencode "key=M.CHF.EUR.SP00.A" \
  --data-urlencode "lastN=12" \
  | jq -r '.data[] | .key as $key | .obs[] | [$key, (.period | split("/")[0]), .value] | @csv'
```
{{< /tab >}}

{{< /tabs >}}

## Generate these scripts

sdmx-dl can [generate]({{< relref "/integration/generate-scripts" >}}) the scripts above for any request:

```shell
sdmx-dl script data ECB EXR M.CHF.EUR.SP00.A --last-n 12 -t bash/cli
sdmx-dl script data ECB EXR M.CHF.EUR.SP00.A --last-n 12 -t bash/rest
```

## Notes

- `set -euo pipefail` makes the script stop at the first failing command, including a failing `sdmx-dl` call in the middle of a pipeline.
- The CLI's CSV output quotes fields that contain commas (e.g. `ObsAttributes`), so prefer a CSV-aware tool (`mlr`, `xsv`, `csvkit`, …) over `cut` or `awk` to select columns.
- Without installation, replace `sdmx-dl` with `jbang sdmx-dl@nbbrd` (see [Installation]({{< relref "/cli/installation" >}})).
- To run a script on a schedule, add it to your crontab (`crontab -e`), e.g. every Monday at 6am: `0 6 * * 1 /path/to/fetch-ecb.sh >> /path/to/fetch-ecb.log 2>&1`.
- `curl -f` makes HTTP errors (e.g. an unknown source) fail the script instead of passing an error body to `jq`.

## Related features

- [Retrieve data]({{< relref "/usage/retrieve#data" >}})
- [Monitor and status]({{< relref "/usage/monitor-and-status" >}})
- [Web service]({{< relref "/ws" >}})
