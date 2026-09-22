---
title: "Browse codes"
weight: 5
---

Look up the human-readable labels behind a coded dimension's values (e.g. `FREQ=M` means "Monthly"), so you can build a valid key or interpret one.

{{< tabs "browse-codes" >}}

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
            .listCodes(CodesRequest.builder()
                    .flowOf("EXR")
                    .concept("FREQ")
                    .build())
            .forEach((code, label) -> IO.println(code + " = " + label));
}
```

`listCodes` resolves the codes of a chosen concept; it accepts an optional `query`/`maxResults` to search/limit results, the same way as [Browse dimensions and attributes]({{< relref "/usage/browse-structure" >}}).
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl list codes ECB EXR FREQ
```
{{< /tab >}}

{{< tab "WS" >}}

### REST
```shell
curl "localhost:4559/sdmx-dl/v2/ECB/EXR/codes/FREQ"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","concept":"FREQ"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListCodes
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- This lists **all defined codes** for a dimension, regardless of whether they actually occur in the dataset. To narrow codes down to what's actually available under a key constraint, use [Browse availability]({{< relref "/usage/browse-availability" >}}) instead.
- CLI has a dedicated `list codes` command; the API offers the equivalent `Provider.listCodes(...)`; WS exposes it as the `ListCodes` RPC/`/codes/{dimension}` REST endpoint (its `concept` field on gRPC is the same value as the REST path segment).
- To find out which dimension/concept names are available for a flow first, see [Browse dimensions and attributes]({{< relref "/usage/browse-structure" >}}).

## Related features

- [Browse dimensions and attributes]({{< relref "/usage/browse-structure" >}})
- [Inspect metadata]({{< relref "/usage/inspect-metadata" >}})
- [Browse availability]({{< relref "/usage/browse-availability" >}})
