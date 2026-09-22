---
title: "Browse availability"
weight: 6
---

Narrow down which codes are actually usable for one dimension, once other dimensions of the key are already fixed — useful for building valid keys interactively instead of guessing.

{{< tabs "browse-availability" >}}

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
            .listAvailability(AvailabilityRequest.builder()
                    .flowOf("EXR")
                    .keyOf("M..EUR.SP00.A")
                    .dimension("FREQ")
                    .build())
            .forEach((code, label) -> IO.println(code + " = " + label));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl list availability ECB EXR M..EUR.SP00.A 1
```
{{< /tab >}}

{{< tab "WS" >}}

### REST

```shell
curl -G localhost:4559/sdmx-dl/v2/ECB/EXR/availability/FREQ \
  --data-urlencode "key=M..EUR.SP00.A"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M..EUR.SP00.A","dimension":"FREQ"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListAvailability
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- Unlike [Browse codes]({{< relref "/usage/browse-codes" >}}), this only returns codes that actually occur in the data under the given constraint — not every code defined in the codelist.
- The dimension is **not** referenced the same way across flavors: the API and WS (`AvailabilityRequest.dimension`/gRPC `dimension` field/REST `/availability/{dimension}` path segment) take the dimension by its **id** (e.g. `FREQ`, as returned by [Browse dimensions and attributes]({{< relref "/usage/browse-structure" >}})), while the CLI conveniently accepts a zero-based **index** into the key instead (e.g. `1` for the second component of `M..EUR.SP00.A`) and resolves it to an id internally before calling the same request.

## Related features

- [Browse codes]({{< relref "/usage/browse-codes" >}})
- [Browse dimensions and attributes]({{< relref "/usage/browse-structure" >}})
- [Retrieve data]({{< relref "/usage/retrieve-data" >}})
