---
title: "Check availability"
weight: 8
---

Narrow down which codes are actually usable for one dimension, once other dimensions of the key are already fixed — useful for building valid keys interactively instead of guessing.

{{< tabs "check-availability" >}}

{{< tab "API" >}}
{{< feature-status "check-availability" "api" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    try (Connection conn = SdmxWebManager.ofServiceLoader().getConnection("ECB", Languages.ANY)) {
        conn.getAvailableDimensionCodes(DatabaseRef.NO_DATABASE, FlowRef.parse("EXR"), Key.parse("M..EUR.SP00.A"), 1)
                .forEach(IO::println);
    }
}
```
{{< /tab >}}

{{< tab "CLI" >}}
{{< feature-status "check-availability" "cli" >}}

```shell
sdmx-dl list availability ECB EXR M..EUR.SP00.A 1
```
{{< /tab >}}

{{< tab "WS" >}}
{{< feature-status "check-availability" "ws" >}}

### REST

```shell
curl -X POST \
  -H "Content-Type: application/json" \
  localhost:4559/sdmx-dl/availability \
  --data "{\"source\":\"ECB\",\"flow\":\"EXR\",\"key\":\"M..EUR.SP00.A\",\"dimension\":1}"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M..EUR.SP00.A","dimension":1}' -plaintext localhost:4557 sdmxdl.grpc.SdmxWebManager.GetAvailability
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- Unlike [Browse codes]({{< relref "/features/browse-codes" >}}), this only returns codes that actually occur in the data under the given constraint — not every code defined in the codelist.
- The dimension is referenced by its zero-based index in the key (e.g. `1` for the second component of `M..EUR.SP00.A`), consistently across API, CLI, and WS.

## Related features

- [Browse codes]({{< relref "/features/browse-codes" >}})
- [Retrieve data]({{< relref "/features/retrieve-data" >}})
