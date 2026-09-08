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
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:3.2.0
import sdmxdl.Languages;
import sdmxdl.web.*;

void main() throws Exception {
    SdmxWebManager manager = SdmxWebManager.ofServiceLoader();

    Search<WebSource> search = Search.ofSources(manager.getSources().values(), Languages.ANY);
    search.search("european central", 5)
            .forEach(result -> IO.println(result.getItem().getId()));
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

## Related features

- [Discover sources]({{< relref "/features/discover-sources" >}})
- [Search flows]({{< relref "/features/search-flows" >}})
