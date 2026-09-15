---
title: "Narrow your request"
weight: 8
---

Restrict a data request to a date range and/or a limited number of observations, so you download only what you need instead of a series' full history.

Both narrowing mechanisms are just extra fields on the same data request (`DataRequest`/`Query` in the API, options on `fetch data` in the CLI, fields on `GetData` in the WS), so they can be used separately or together.

## By period

{{< tabs "narrow-by-period" >}}

{{< tab "API" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.DataRequest;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    SdmxWebManager
            .ofServiceLoader()
            .usingName("ECB")
            .getData(DataRequest.builder()
                    .flowOf("EXR")
                    .keyOf("M.CHF.EUR.SP00.A")
                    .startPeriodOf("2020")
                    .endPeriodOf("2022-12")
                    .build())
            .forEach(series -> IO.println(series.getKey()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --start 2020 --end 2022-12
```
{{< /tab >}}

{{< tab "WS" >}}

### REST

```shell
curl -G localhost:4559/sdmx-dl/v2/ECB/EXR/data \
  --data-urlencode "key=M.CHF.EUR.SP00.A" \
  --data-urlencode "startPeriod=2020" \
  --data-urlencode "endPeriod=2022-12"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M.CHF.EUR.SP00.A","start_period":"2020","end_period":"2022-12"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.GetData
```
{{< /tab >}}

{{< /tabs >}}

Bounds are inclusive and accept reduced-precision ISO-8601 (`"2020"`, `"2020-12"`, `"2020-12-01"`) on every flavor.

## By observation count

{{< tabs "narrow-by-count" >}}

{{< tab "API" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.DataRequest;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    SdmxWebManager
            .ofServiceLoader()
            .usingName("ECB")
            .getData(DataRequest.builder()
                    .flowOf("EXR")
                    .keyOf("M.CHF.EUR.SP00.A")
                    .firstNObservations(3)
                    .lastNObservations(2)
                    .build())
            .forEach(series -> IO.println(series.getKey()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --first-n 3 --last-n 2
```
{{< /tab >}}

{{< tab "WS" >}}

### REST

```shell
curl -G localhost:4559/sdmx-dl/v2/ECB/EXR/data \
  --data-urlencode "key=M.CHF.EUR.SP00.A" \
  --data-urlencode "firstNObservations=3" \
  --data-urlencode "lastNObservations=2"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M.CHF.EUR.SP00.A","first_n_observations":3,"last_n_observations":2}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.GetData
```
{{< /tab >}}

{{< /tabs >}}

`first-n` and `last-n` can be combined (as above) to get both ends of a series in one call.

## Combining both

Period and observation-count limits apply together in the same request, e.g. "the last 3 observations up to end of 2022, starting no earlier than 2020":

```shell
sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --start 2020 --end 2022-12 --last-n 3
```

## Related features

- [Retrieve data]({{< relref "/features/retrieve-data" >}})
- [Browse availability]({{< relref "/features/browse-availability" >}})

