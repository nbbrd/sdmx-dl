---
title: "Discover sources"
weight: 1
---

See which data providers sdmx-dl can connect to, and find one by name or topic when you don't know its exact ID (with typo tolerance).

{{< tabs "discover-sources" >}}

{{< tab "API" >}}

`listSources(...)` always accepts an optional `query`/`maxResults`; omit them to simply list every source.

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.web.*;

void main() throws Exception {
    SdmxWebManager manager = SdmxWebManager.ofServiceLoader();

    // List every source
    manager.listSources(WebSourcesRequest.DEFAULT)
            .forEach(source -> IO.println(source.getId()));

    // Search by name or topic
    manager.listSources(WebSourcesRequest.builder()
                    .query("european central")
                    .maxResults(5)
                    .build())
            .forEach(source -> IO.println(source.getId()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl list sources
sdmx-dl list sources -q "european central" -m 5
```
{{< /tab >}}

{{< tab "WS" >}}

### REST

```shell
curl "localhost:4559/sdmx-dl/v2/sources"

curl -G localhost:4559/sdmx-dl/v2/sources \
  --data-urlencode "query=european central" \
  --data-urlencode "maxResults=5"
```

### gRPC
```shell
grpcurl -d '{}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListSources

grpcurl -d '{"query":"european central","maxResults":5}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListSources
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- Discovery and search are the same call: pass a `query`/`maxResults` to search, or omit them to list every source.
- Ranking is hybrid: exact lexical matches (BM25) are fused with typo-tolerant trigram similarity, so both `"ecb"` and `"eurpean central"` find the European Central Bank. When `query` is empty, results fall back to being sorted by id instead of ranked by relevance.
- Some sources are aliases of others (e.g. renamed or mirrored endpoints); the API exposes `WebSource.isAlias()` to filter them out (and they're excluded from search results), while the CLI table lists them alongside their target.
- `WebSourcesRequest` also carries a `threshold` (a `Confidentiality` level) so that sources requiring stricter confidentiality can be excluded from the results; it defaults to allowing every source.

## Related features

- [Discover databases]({{< relref "/features/discover-databases" >}})
- [Discover flows]({{< relref "/features/discover-flows" >}})



