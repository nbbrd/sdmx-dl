---
title: "Retrieve data"
weight: 9
---

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
                    .startPeriodOf("2020")
                    .endPeriodOf("2022-12")
                    .lastNObservations(3)
                    .build())
            .forEach(series -> IO.println(series.getKey()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A
```

With filters:

```shell
sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --start 2020 --end 2022-12 --last-n 3
```
{{< /tab >}}

{{< tab "WS" >}}

### REST

```shell
curl -G localhost:4559/sdmx-dl/v2/ECB/EXR/data \
  --data-urlencode "key=M.CHF.EUR.SP00.A" \
  --data-urlencode "startPeriod=2020" \
  --data-urlencode "endPeriod=2022-12" \
  --data-urlencode "lastNObservations=3"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M.CHF.EUR.SP00.A"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.GetData
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- All flavors share the same request shape: `source`, `flow`, `key`, plus optional `database`, `languages`, and the filters described in [Narrow your request]({{< relref "/features/narrow-your-request" >}}).
- WS additionally offers `GetDataStream`/`GET /sdmx-dl/v2/{source}/{flow}/data:stream` to stream large responses instead of buffering the full dataset.

## Related features

- [Discover sources]({{< relref "/features/discover-sources" >}})
- [Discover flows]({{< relref "/features/discover-flows" >}})
- [Inspect metadata]({{< relref "/features/inspect-metadata" >}})


