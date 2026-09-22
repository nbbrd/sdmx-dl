---
title: "Use in Excel"
weight: 2
---

Bring **sdmx-dl** output into Excel when you want lightweight analysis, ad-hoc filtering, or a workbook that non-developers can refresh.
Two common approaches are to import a CSV produced by the CLI, or to connect Excel directly to the REST endpoint's JSON output.

{{< tabs "use-excel-sheet" >}}

{{< tab "From CLI CSV" >}}

Use the CLI to produce a CSV file, then open or import that file in Excel:

```shell
sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --last-n 12 -o ecb-chf.csv
```

In Excel, open `ecb-chf.csv` directly or use **Data → From Text/CSV**.
This approach works well for scheduled exports, shared folders, or manual refresh workflows.
{{< /tab >}}

{{< tab "From REST" >}}

Excel Power Query can also pull JSON directly from the web service.
Create a blank query and use this M script:

```powerquery
let
    Source = Json.Document(
        Web.Contents(
            "http://localhost:4559",
            [
                RelativePath = "sdmx-dl/v2/ECB/EXR/data",
                Query = [key = "M.CHF.EUR.SP00.A", lastNObservations = "12"]
            ]
        )
    ),
    SeriesTable = Table.FromRecords(Source[data]),
    ExpandedObs = Table.ExpandListColumn(SeriesTable, "obs"),
    ExpandedObsRecord = Table.ExpandRecordColumn(ExpandedObs, "obs", {"period", "value"}, {"ObsPeriod", "ObsValue"}),
    RenamedColumns = Table.RenameColumns(ExpandedObsRecord, {{"key", "Series"}})
in
    RenamedColumns
```

For simpler discovery endpoints such as `/sources` or `/{source}/flows`, you can often skip the expansion steps and convert the returned JSON list straight into a table.
{{< /tab >}}

{{< /tabs >}}

## Notes

- The CLI route is the simplest option when Excel only needs a file to open or refresh from disk.
- The REST route avoids an intermediate file and lets Power Query refresh directly from a running sdmx-dl service, but you usually need one or two transformation steps because the response is JSON rather than flat CSV.
- If your workbook is shared, document whether it depends on a local service (`localhost:4559`) or on a remote one available to every user.
- Start with discovery calls if the final dataset is not fixed yet; for example, identify the source and flow first, then wire the final data URL into Excel.

## Related features

- [Discover flows]({{< relref "/usage/discover-flows" >}})
- [Retrieve data]({{< relref "/usage/retrieve-data" >}})
- [Web service]({{< relref "/ws" >}})

