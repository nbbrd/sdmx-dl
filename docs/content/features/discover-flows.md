---
title: "Discover flows"
weight: 3
---

See which datasets a source publishes, and find one by topic within that source when you don't know its exact flow ID.

{{< tabs "discover-flows" >}}

{{< tab "API" >}}

`listFlows(...)` always accepts an optional `query`/`maxResults`; omit them to simply list every flow.

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    Provider ecb = SdmxWebManager.ofServiceLoader().usingName("ECB");

    // List every flow
    ecb.listFlows(FlowsRequest.DEFAULT)
            .forEach(flow -> IO.println(flow.getRef()));

    // Search by topic
    ecb.listFlows(FlowsRequest.builder()
                    .query("exchange rates")
                    .maxResults(5)
                    .build())
            .forEach(flow -> IO.println(flow.getRef()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl list flows ECB
sdmx-dl list flows ECB -q "exchange rates" -m 5
```
{{< /tab >}}

{{< tab "WS" >}}

### REST

```shell
curl "localhost:4559/sdmx-dl/v2/ECB/flows"

curl -G localhost:4559/sdmx-dl/v2/ECB/flows \
  --data-urlencode "query=exchange rates" \
  --data-urlencode "maxResults=5"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListFlows

grpcurl -d '{"source":"ECB","query":"exchange rates","maxResults":5}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListFlows
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- Discovery and search are the same call: pass a `query`/`maxResults` to search within a source, or omit them to list every flow it publishes.
- Ranking is hybrid: lexical (BM25) fused with typo-tolerant trigram matching, so partial or approximate queries still surface the right flow.
- Search is scoped to a single source; use [Discover sources]({{< relref "/features/discover-sources" >}}) first if you don't know which source to search within.
- Some sources expose several databases; pass a `database` to scope the listing (defaults to the source's single/default database otherwise) — see [Discover databases]({{< relref "/features/discover-databases" >}}).

## Related features

- [Discover sources]({{< relref "/features/discover-sources" >}})
- [Discover databases]({{< relref "/features/discover-databases" >}})



