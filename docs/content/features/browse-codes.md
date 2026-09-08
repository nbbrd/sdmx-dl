---
title: "Browse codes"
weight: 7
---

Look up the human-readable labels behind a coded dimension's values (e.g. `FREQ=M` means "Monthly"), so you can build a valid key or interpret one.

{{< tabs "browse-codes" >}}

{{< tab "API" >}}
{{< feature-status "browse-codes" "api" >}}

Not supported as a dedicated high-level API call.

The closest API equivalent is to retrieve flow metadata and inspect the codes attached to a coded dimension.

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:3.2.0
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    try (Connection conn = SdmxWebManager.ofServiceLoader().getConnection("ECB", Languages.ANY)) {
        MetaSet meta = conn.getMeta(DatabaseRef.NO_DATABASE, FlowRef.parse("EXR"));
        Dimension freq = meta.getStructure().getDimensions().stream()
                .filter(d -> d.getId().equals("FREQ"))
                .findFirst()
                .orElseThrow();
        freq.getCodes().forEach((code, label) -> IO.println(code + " = " + label));
    }
}
```
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
- CLI has a dedicated `list codes` command; API/WS reach the same data through metadata/structure objects.

## Related features

- [Inspect metadata]({{< relref "/features/inspect-metadata" >}})
- [Check availability]({{< relref "/features/check-availability" >}})
