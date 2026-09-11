---
title: "Search databases"
weight: 5
---

Find a database namespace within a multi-database source, when you don't know its exact name.

{{< tabs "search-databases" >}}

{{< tab "API" >}}
{{< feature-status "search-databases" "api" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    SdmxWebManager
            .ofServiceLoader()
            .usingName("ECB")
            .listDatabases(DatabasesRequest.builder()
                    .query("central")
                    .maxResults(5)
                    .build())
            .forEach(database -> IO.println(database.getRef()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}
{{< feature-status "search-databases" "cli" >}}

```shell
sdmx-dl search databases ECB "central" -n 5
```
{{< /tab >}}

{{< tab "WS" >}}
{{< feature-status "search-databases" "ws" >}}

### REST

```shell
curl -X POST \
  -H "Content-Type: application/json" \
  localhost:4559/sdmx-dl/searchDatabases \
  --data "{\"source\":\"ECB\",\"query\":\"central\",\"maxResults\":5}"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","query":"central","maxResults":5}' -plaintext localhost:4557 sdmxdl.grpc.SdmxWebManager.SearchDatabases
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- Most sources expose a single default database, so this feature is only useful for multi-database providers.
- Search is scoped to one source at a time.
- `Provider.listDatabases(...)` accepts the `query`/`maxResults` directly on `DatabasesRequest`, so a separate `Search.ofDatabases(...)` call on an already-fetched list is only needed when you want access to the numeric relevance `getScore()`.

## Related features

- [Discover flows]({{< relref "/features/discover-flows" >}})
- [Search sources]({{< relref "/features/search-sources" >}})

