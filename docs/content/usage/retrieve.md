---
title: "Retrieve"
weight: 4
aliases:
  - /usage/retrieve-data/
  - /usage/narrow-your-request/
  - /usage/inspect-metadata/
---

Fetch observations and metadata for the key you've settled on.

All flavors share the same request shape: `source`, `flow`, [`key`]({{< relref "/usage/browse#keys" >}}), plus optional `database`, `languages`, `detail`, and the [period and observation-count filters](#narrow-your-request) described below.

## Data

Download observations from a dataset using a source, flow, and key.

{{< tabs "retrieve-data" >}}

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
                    .build())
            .forEach(series -> IO.println(series.getKey()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A
```
{{< /tab >}}

{{< tab "WS" >}}

### REST

```shell
curl -G localhost:4559/sdmx-dl/v2/ECB/EXR/data \
  --data-urlencode "key=M.CHF.EUR.SP00.A"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M.CHF.EUR.SP00.A"}' -plaintext localhost:4559 sdmxdl.grpc.v2.SdmxWebManager.GetData
```
{{< /tab >}}

{{< /tabs >}}

{{< expand "CLI output sample" >}}
```plain
Series,ObsAttributes,ObsPeriod,ObsValue
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",1999-01-01T00:00:00,1.605495
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",1999-02-01T00:00:00,1.59785
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",1999-03-01T00:00:00,1.5954304347826
...
```
{{< /expand >}}

- WS additionally offers `GetDataStream`/`GET /sdmx-dl/v2/{source}/{flow}/data:stream` to stream large responses instead of buffering the full dataset.

## Narrow your request

Restrict a data request to a date range and/or a limited number of observations, so you download only what you need instead of a series' full history.

Both narrowing mechanisms are just extra fields on the same data request (`DataRequest`/`Query` in the API, options on `fetch data` in the CLI, fields on `GetData` in the WS), so they can be used separately or together.

### By period

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

#### REST

```shell
curl -G localhost:4559/sdmx-dl/v2/ECB/EXR/data \
  --data-urlencode "key=M.CHF.EUR.SP00.A" \
  --data-urlencode "start=2020" \
  --data-urlencode "end=2022-12"
```

#### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M.CHF.EUR.SP00.A","start":"2020","end":"2022-12"}' -plaintext localhost:4559 sdmxdl.grpc.v2.SdmxWebManager.GetData
```
{{< /tab >}}

{{< /tabs >}}

{{< expand "CLI output sample" >}}
```plain
Series,ObsAttributes,ObsPeriod,ObsValue
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",2020-01-01T00:00:00,1.07645
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",2020-02-01T00:00:00,1.06478
...
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",2022-11-01T00:00:00,.9841545454545
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",2022-12-01T00:00:00,.9864904761905
```
{{< /expand >}}

Bounds are inclusive and accept reduced-precision ISO-8601 (`"2020"`, `"2020-12"`, `"2020-12-01"`) on every flavor.

### By observation count

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

#### REST

```shell
curl -G localhost:4559/sdmx-dl/v2/ECB/EXR/data \
  --data-urlencode "key=M.CHF.EUR.SP00.A" \
  --data-urlencode "firstN=3" \
  --data-urlencode "lastN=2"
```

#### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M.CHF.EUR.SP00.A","firstN":3,"lastN":2}' -plaintext localhost:4559 sdmxdl.grpc.v2.SdmxWebManager.GetData
```
{{< /tab >}}

{{< /tabs >}}

{{< expand "CLI output sample" >}}
```plain
Series,ObsAttributes,ObsPeriod,ObsValue
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",1999-01-01T00:00:00,1.605495
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",1999-02-01T00:00:00,1.59785
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",1999-03-01T00:00:00,1.5954304347826
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",2026-07-01T00:00:00,.9255739130434782
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",2026-08-01T00:00:00,.9361857142857143
```
{{< /expand >}}

`first-n` and `last-n` can be combined (as above) to get both ends of a series in one call.

### Combining both

Period and observation-count limits apply together in the same request, e.g. "the last 3 observations up to end of 2022, starting no earlier than 2020":

```shell
sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --start 2020 --end 2022-12 --last-n 3
```

{{< expand "CLI output sample" >}}
```plain
Series,ObsAttributes,ObsPeriod,ObsValue
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",2022-10-01T00:00:00,.9790523809524
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",2022-11-01T00:00:00,.9841545454545
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",2022-12-01T00:00:00,.9864904761905
```
{{< /expand >}}

## Metadata

Look up descriptive metadata (title, units, notes, source-specific attributes) attached to a series, without downloading the observations themselves.

{{< tabs "inspect-metadata" >}}

{{< tab "API" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    SdmxWebManager
            .ofServiceLoader()
            .usingName("ECB")
            .getData(DataRequest.builder()
                    .flowOf("EXR")
                    .keyOf("M.CHF.EUR.SP00.A")
                    .detail(Detail.NO_DATA)
                    .build())
            .forEach(series -> series.getMeta().forEach((concept, value) ->
                    IO.println(concept + " = " + value)));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl fetch meta ECB EXR M.CHF.EUR.SP00.A
```
{{< /tab >}}

{{< tab "WS" >}}

`GetData`/`GetDataStream` accept the same `detail` parameter as the CLI/API; pass `NO_DATA` to get series-level metadata without downloading observations.

#### REST
```shell
curl -G localhost:4559/sdmx-dl/v2/ECB/EXR/data \
  --data-urlencode "key=M.CHF.EUR.SP00.A" \
  --data-urlencode "detail=NO_DATA"
```

#### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M.CHF.EUR.SP00.A","detail":"NO_DATA"}' -plaintext localhost:4559 sdmxdl.grpc.v2.SdmxWebManager.GetData
```

For flow-level metadata (dimensions, attributes, codelists) instead of series-level, use `GetMeta`:

```shell
curl "localhost:4559/sdmx-dl/v2/ECB/EXR/meta"
grpcurl -d '{"source":"ECB","flow":"EXR"}' -plaintext localhost:4559 sdmxdl.grpc.v2.SdmxWebManager.GetMeta
```
{{< /tab >}}

{{< /tabs >}}

{{< expand "CLI output sample" >}}
```plain
Series,Concept,Value
M.CHF.EUR.SP00.A,COLLECTION,A
M.CHF.EUR.SP00.A,UNIT,CHF
M.CHF.EUR.SP00.A,SOURCE_AGENCY,4F0
M.CHF.EUR.SP00.A,DECIMALS,4
M.CHF.EUR.SP00.A,UNIT_INDEX_BASE,99Q1=100
M.CHF.EUR.SP00.A,UNIT_MULT,0
M.CHF.EUR.SP00.A,TITLE,Swiss franc/Euro ECB reference exchange rate
M.CHF.EUR.SP00.A,TIME_FORMAT,P1M
M.CHF.EUR.SP00.A,TITLE_COMPL,"ECB reference exchange rate, Swiss franc/Euro, 2.15 pm (C.E.T.)"
```
{{< /expand >}}

- `detail=NO_DATA` is the series-level equivalent of the CLI's `fetch meta` and the API's `Detail.NO_DATA`: same request as [Data](#data), just with `detail` set to skip observations.
- Don't confuse this with flow/structure-level metadata (dimensions, attributes, codelists): that's `GetMeta`/`getMeta(...)` on every flavor — see [Browse]({{< relref "/usage/browse#dimensions-and-attributes" >}}).
