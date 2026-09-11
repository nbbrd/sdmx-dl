---
title: "Retrieve data"
weight: 11
---

Download observations from a dataset using a source, flow, and key.

{{< tabs "retrieve-data" >}}

{{< tab "API" >}}
{{< feature-status "retrieve-data" "api" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:3.2.0
import sdmxdl.DataRequest;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    SdmxWebManager
            .ofServiceLoader()
            .usingName("ECB")
            .getData(KeyRequest.builder()
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
{{< feature-status "retrieve-data" "cli" >}}

```shell
sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A
```

With filters:

```shell
sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --start 2020 --end 2022-12 --last-n 3
```
{{< /tab >}}

{{< tab "WS" >}}
{{< feature-status "retrieve-data" "ws" >}}

### REST

```shell
curl -X POST \
  -H "Content-Type: application/json" \
  localhost:4559/sdmx-dl/data \
  --data "{\"source\":\"ECB\",\"flow\":\"EXR\",\"key\":\"M.CHF.EUR.SP00.A\",\"start_period\":\"2020\",\"end_period\":\"2022-12\",\"last_n_observations\":3}"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M.CHF.EUR.SP00.A"}' -plaintext localhost:4557 sdmxdl.grpc.SdmxWebManager.GetData
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- All flavors share the same request shape: `source`, `flow`, `key`, plus optional `database`, `languages`, and the filters described in [Narrow your request]({{< relref "/features/narrow-your-request" >}}).
- WS additionally offers `GetDataStream`/`/sdmx-dl/dataStream` to stream large responses instead of buffering the full dataset.

## Related features

- [Discover sources]({{< relref "/features/discover-sources" >}})
- [Search flows]({{< relref "/features/search-flows" >}})
