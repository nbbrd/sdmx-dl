---
title: "Search flows"
weight: 4
---

Find a dataset by topic within a source when you don't know its exact flow ID.

{{< tabs "search-flows" >}}

{{< tab "API" >}}
{{< feature-status "search-flows" "api" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    SdmxWebManager
            .ofServiceLoader()
            .usingName("ECB")
            .listFlows(FlowsRequest.builder()
                    .query("exchange rates")
                    .maxResults(5)
                    .build())
            .forEach(flow -> IO.println(flow.getRef()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}
{{< feature-status "search-flows" "cli" >}}

```shell
sdmx-dl search flows ECB "exchange rates" -n 5
```
{{< /tab >}}

{{< tab "WS" >}}
{{< feature-status "search-flows" "ws" >}}

### REST

```shell
curl -X POST \
  -H "Content-Type: application/json" \
  localhost:4559/sdmx-dl/searchFlows \
  --data "{\"source\":\"ECB\",\"query\":\"exchange rates\",\"maxResults\":5}"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","query":"exchange rates","maxResults":5}' -plaintext localhost:4557 sdmxdl.grpc.SdmxWebManager.SearchFlows
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- Ranking is hybrid: lexical (BM25) fused with typo-tolerant trigram matching, so partial or approximate queries still surface the right flow.
- Search is scoped to a single source; use [Search sources]({{< relref "/features/search-sources" >}}) first if you don't know which source to search within.
- `Provider.listFlows(...)` accepts the `query`/`maxResults` directly on `FlowsRequest`, so a separate `Search.ofFlows(...)` call on an already-fetched list is only needed when you want access to the numeric relevance `getScore()` — see [Discover flows]({{< relref "/features/discover-flows" >}}).

## Related features

- [Discover flows]({{< relref "/features/discover-flows" >}})
- [Search sources]({{< relref "/features/search-sources" >}})

