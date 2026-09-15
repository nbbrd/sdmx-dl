---
title: "Browse dimensions and attributes"
weight: 4
---

List the dimensions and attributes that make up a flow's structure — their names, labels, whether they're coded, and (for dimensions) their position in the key — before browsing a dimension's codes or building a key.

{{< tabs "browse-structure" >}}

{{< tab "API" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    Provider<sdmxdl.web.WebSource> ecb = SdmxWebManager.ofServiceLoader().usingName("ECB");

    ecb.listDimensions(DimensionsRequest.builder().flowOf("EXR").build())
            .forEach(dimension -> IO.println(dimension.getId() + " = " + dimension.getName()));

    ecb.listAttributes(AttributesRequest.builder().flowOf("EXR").build())
            .forEach(attribute -> IO.println(attribute.getId() + " = " + attribute.getName()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl list dimensions ECB EXR
sdmx-dl list attributes ECB EXR
```
{{< /tab >}}

{{< tab "WS" >}}

### REST
```shell
curl "localhost:4559/sdmx-dl/v2/ECB/EXR/dimensions"
curl "localhost:4559/sdmx-dl/v2/ECB/EXR/attributes"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListDimensions
grpcurl -d '{"source":"ECB","flow":"EXR"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListAttributes
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- Both calls accept an optional `query`/`maxResults` to search/limit results, the same way as [Discover flows]({{< relref "/features/discover-flows" >}}).
- Dimensions are ordered and make up the positional `key` used by [Retrieve data]({{< relref "/features/retrieve-data" >}}) and [Browse availability]({{< relref "/features/browse-availability" >}}); attributes carry extra descriptive metadata and aren't part of the key.
- A dimension or attribute marked `coded` has its values drawn from a codelist — resolve them with [Browse codes]({{< relref "/features/browse-codes" >}}).
- CLI has dedicated `list dimensions`/`list attributes` commands; the API offers the equivalent `Provider.listDimensions(...)`/`listAttributes(...)`; WS exposes them as the `ListDimensions`/`ListAttributes` RPCs and `/dimensions`/`/attributes` REST endpoints.

## Related features

- [Inspect metadata]({{< relref "/features/inspect-metadata" >}})
- [Browse codes]({{< relref "/features/browse-codes" >}})

