---
title: "Browse codes"
weight: 7
---

Look up the human-readable labels behind a coded dimension's values (e.g. `FREQ=M` means "Monthly"), so you can build a valid key or interpret one.

{{< tabs "browse-codes" >}}

{{< tab "API" >}}
{{< feature-status "browse-codes" "api" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    SdmxWebManager
            .ofServiceLoader()
            .usingName("ECB")
            .listCodes(CodesRequest.builder()
                    .flowOf("EXR")
                    .concept("FREQ")
                    .build())
            .forEach((code, label) -> IO.println(code + " = " + label));
}
```

`listDimensions`/`listAttributes` return a flow's coded components, and `listCodes` resolves a chosen concept's codes; all three accept an optional `query`/`maxResults` to search/limit results, the same way as [Search flows]({{< relref "/features/search-flows" >}}).
{{< /tab >}}

{{< tab "CLI" >}}
{{< feature-status "browse-codes" "cli" >}}

```shell
sdmx-dl list codes ECB EXR FREQ
```
{{< /tab >}}

{{< tab "WS" >}}
{{< feature-status "browse-codes" "ws" >}}

Not supported as a dedicated web-service endpoint.


The closest WS equivalent is to use availability for a constrained key, or to inspect structure metadata.

### REST availability
```shell
curl -X POST \
  -H "Content-Type: application/json" \
  localhost:4559/sdmx-dl/availability \
  --data "{\"source\":\"ECB\",\"flow\":\"EXR\",\"key\":\"M..EUR.SP00.A\",\"dimension\":1}"
```

### gRPC availability
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M..EUR.SP00.A","dimension":1}' -plaintext localhost:4557 sdmxdl.grpc.SdmxWebManager.GetAvailability
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- This lists **all defined codes** for a dimension, regardless of whether they actually occur in the dataset. To narrow codes down to what's actually available under a key constraint, use [Check availability]({{< relref "/features/check-availability" >}}) instead.
- CLI has a dedicated `list codes` command; the API now offers the equivalent `Provider.listCodes(...)` (alongside `listDimensions`/`listAttributes`). WS still reaches the same data through structure/availability objects.

## Related features

- [Inspect metadata]({{< relref "/features/inspect-metadata" >}})
- [Check availability]({{< relref "/features/check-availability" >}})

