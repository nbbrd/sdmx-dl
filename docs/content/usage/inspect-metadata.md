---
title: "Inspect metadata"
weight: 7
---

Look up descriptive metadata (title, units, notes, source-specific attributes) attached to a series or a flow, without downloading the observations themselves.

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
                    .detailOf(Detail.NO_DATA.name())
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

### REST
```shell
curl -G localhost:4559/sdmx-dl/v2/ECB/EXR/data \
  --data-urlencode "key=M.CHF.EUR.SP00.A" \
  --data-urlencode "detail=NO_DATA"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M.CHF.EUR.SP00.A","detail":"NO_DATA"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.GetData
```

For flow-level metadata (dimensions, attributes, codelists) instead of series-level, use `GetMeta`:

```shell
curl "localhost:4559/sdmx-dl/v2/ECB/EXR/meta"
grpcurl -d '{"source":"ECB","flow":"EXR"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.GetMeta
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- `detail=NO_DATA` is the series-level equivalent of the CLI's `fetch meta` and the API's `Detail.NO_DATA`: every flavor now shares the same request shape as [Retrieve data]({{< relref "/usage/retrieve-data" >}}), just with `detail` set to skip observations.
- Don't confuse this with flow/structure-level metadata (dimensions, attributes, codelists): that's `GetMeta`/`getMeta(...)` on every flavor � see [Browse dimensions and attributes]({{< relref "/usage/browse-structure" >}}).

## Related features

- [Retrieve data]({{< relref "/usage/retrieve-data" >}})
- [Browse dimensions and attributes]({{< relref "/usage/browse-structure" >}})
- [Browse codes]({{< relref "/usage/browse-codes" >}})
