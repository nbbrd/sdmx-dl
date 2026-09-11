---
title: "Search sources"
weight: 3
---

Find a source by name or topic when you don't know its exact ID, with typo tolerance.

{{< tabs "search-sources" >}}

{{< tab "API" >}}
{{< feature-status "search-sources" "api" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.web.*;

void main() throws Exception {
    SdmxWebManager
            .ofServiceLoader()
            .listSources(WebSourcesRequest.builder()
                    .query("european central")
                    .maxResults(5)
                    .build())
            .forEach(source -> IO.println(source.getId()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}
{{< feature-status "search-sources" "cli" >}}

```shell
sdmx-dl search sources "european central" -n 5
```
{{< /tab >}}

{{< tab "WS" >}}
{{< feature-status "search-sources" "ws" >}}

### REST

```shell
curl -X POST \
  -H "Content-Type: application/json" \
  localhost:4559/sdmx-dl/searchSources \
  --data "{\"query\":\"european central\",\"maxResults\":5}"
```

### gRPC
```shell
grpcurl -d '{"query":"european central","maxResults":5}' -plaintext localhost:4557 sdmxdl.grpc.SdmxWebManager.SearchSources
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- Ranking is hybrid: exact lexical matches (BM25) are fused with typo-tolerant trigram similarity, so both `"ecb"` and `"eurpean central"` find the European Central Bank.
- Sources marked as aliases are excluded from the search index.
- `WebSourcesRequest` also carries a `threshold` (a `Confidentiality` level) so that sources requiring stricter confidentiality can be excluded from the results; it defaults to allowing every source.
- When `query` is empty, `manager.listSources(...)` falls back to listing sources sorted by id instead of ranking by relevance.

## Related features

- [Discover sources]({{< relref "/features/discover-sources" >}})
- [Search flows]({{< relref "/features/search-flows" >}})
