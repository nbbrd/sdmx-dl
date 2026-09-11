---
title: "Inspect metadata"
weight: 6
---

Look up descriptive metadata (title, units, notes, source-specific attributes) attached to a series or a flow, without downloading the observations themselves.

{{< tabs "inspect-metadata" >}}

{{< tab "API" >}}
{{< feature-status "inspect-metadata" "api" >}}

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
{{< feature-status "inspect-metadata" "cli" >}}

```shell
sdmx-dl fetch meta ECB EXR M.CHF.EUR.SP00.A
```
{{< /tab >}}

{{< tab "WS" >}}
{{< feature-status "inspect-metadata" "ws" >}}

Not supported as a series-level `fetch meta` equivalent.


The closest WS equivalent is `GetMeta`, which exposes flow-level metadata (`source` + `flow`).

### REST
```shell
curl -X POST \
  -H "Content-Type: application/json" \
  localhost:4559/sdmx-dl/meta \
  --data "{\"source\":\"ECB\",\"flow\":\"EXR\"}"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR"}' -plaintext localhost:4557 sdmxdl.grpc.SdmxWebManager.GetMeta
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- This is one of the few genuinely non-equivalent features: CLI/API can inspect metadata at the **series** level (`fetch meta`, `Detail.NO_DATA`), while WS only exposes it at the **flow/structure** level (`GetMeta`).
- If you only need the dataset's structure (dimensions, attributes, codelists), `GetMeta`/`getMeta(...)` is the right call on every flavor — see [Browse codes]({{< relref "/features/browse-codes" >}}).

## Related features

- [Retrieve data]({{< relref "/features/retrieve-data" >}})
- [Browse codes]({{< relref "/features/browse-codes" >}})
