---
title: "Discover databases"
weight: 2
---

Find a database namespace within a multi-database source, when you don't know its exact name.

{{< tabs "discover-databases" >}}

{{< tab "API" >}}

`listDatabases(...)` always accepts an optional `query`/`maxResults`; omit them to simply list every database.

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    Provider ecb = SdmxWebManager.ofServiceLoader().usingName("ECB");

    // List every database
    ecb.listDatabases(DatabasesRequest.DEFAULT)
            .forEach(database -> IO.println(database.getRef()));

    // Search by name
    ecb.listDatabases(DatabasesRequest.builder()
                    .query("central")
                    .maxResults(5)
                    .build())
            .forEach(database -> IO.println(database.getRef()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl list databases ECB
sdmx-dl list databases ECB -q "central" -m 5
```
{{< /tab >}}

{{< tab "WS" >}}

### REST

```shell
curl "localhost:4559/sdmx-dl/v2/ECB/databases"

curl -G localhost:4559/sdmx-dl/v2/ECB/databases \
  --data-urlencode "query=central" \
  --data-urlencode "maxResults=5"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListDatabases

grpcurl -d '{"source":"ECB","query":"central","maxResults":5}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListDatabases
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- Discovery and search are the same call: pass a `query`/`maxResults` to search within a source, or omit them to list every database it exposes.
- Most sources expose a single default database, so this feature is only useful for multi-database providers.
- Search is scoped to one source at a time; use [Discover sources]({{< relref "/usage/discover-sources" >}}) first if you don't know which source to search within.

## Related features

- [Discover sources]({{< relref "/usage/discover-sources" >}})
- [Discover flows]({{< relref "/usage/discover-flows" >}})

