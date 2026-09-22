---
title: "Use in R"
weight: 3
---

Automate **sdmx-dl** from R when you want to blend discovery or retrieval with analysis, plotting, or reporting.
Two common approaches are to call the CLI from R, or to query the local/remote web service directly.

{{< tabs "use-r-script" >}}

{{< tab "CLI" >}}

If `sdmx-dl` is on your `PATH`, R can launch it and then read the generated CSV:

```r
outfile <- tempfile(fileext = ".csv")

system2(
  command = "sdmx-dl",
  args = c("fetch", "data", "ECB", "EXR", "M.CHF.EUR.SP00.A", "--last-n", "12", "-o", outfile)
)

dat <- read.csv(outfile)
head(dat)
```

This is convenient when you already use the CLI interactively and want your R script to reuse the same commands.
{{< /tab >}}

{{< tab "REST" >}}

If the web service is running, R can fetch the JSON response directly and then reshape it in R:

```r
library(jsonlite)

url <- paste0(
  "http://localhost:4559/sdmx-dl/v2/ECB/EXR/data",
  "?key=M.CHF.EUR.SP00.A&lastNObservations=12"
)

payload <- fromJSON(url, simplifyDataFrame = FALSE)

dat <- do.call(rbind, lapply(payload$data, function(series) {
  do.call(rbind, lapply(series$obs, function(obs) {
    data.frame(
      Series = series$key,
      ObsPeriod = obs$period,
      ObsValue = obs$value
    )
  }))
}))

head(dat)
```

The same pattern also works for discovery endpoints such as `sources`, `flows`, or `dimensions`:

```r
library(jsonlite)

sources <- fromJSON("http://localhost:4559/sdmx-dl/v2/sources")
head(sources)
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- The CLI examples write CSV, while the REST endpoint returns JSON (`SdmxdlRestService2` is declared with `@Produces(APPLICATION_JSON)`), so use `jsonlite::fromJSON(...)` or a similar JSON client on the REST side.
- The CLI approach is usually simplest when `sdmx-dl` is already installed and configured on the machine running the script.
- The REST approach is often better when the script runs on another machine, or when you want several R processes to share one long-lived sdmx-dl service.
- If you need richer orchestration, combine discovery calls first (for example `list sources`, `list flows`, or `list dimensions`) and then build the final data request in R.

## Related features

- [Discover sources]({{< relref "/usage/discover-sources" >}})
- [Retrieve data]({{< relref "/usage/retrieve-data" >}})
- [Web service]({{< relref "/ws" >}})

