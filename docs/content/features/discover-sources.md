---
title: "Discover sources"
weight: 1
---

See which data providers sdmx-dl can connect to, before picking one to query.

{{< tabs "discover-sources" >}}

{{< tab "API" >}}
{{< feature-status "discover-sources" "api" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.web.*;

void main() throws Exception {
    SdmxWebManager
        .ofServiceLoader()
        .listSources(WebSourcesRequest.DEFAULT)
        .forEach(source -> IO.println(source.getId()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}
{{< feature-status "discover-sources" "cli" >}}

```shell
sdmx-dl list sources
```
{{< /tab >}}

{{< tab "WS" >}}
{{< feature-status "discover-sources" "ws" >}}

### REST

```shell
curl -X POST \
  -H "Content-Type: application/json" \
  localhost:4559/sdmx-dl/sources \
  --data "{}"
```

### gRPC
```shell
grpcurl -d '{}' -plaintext localhost:4557 sdmxdl.grpc.SdmxWebManager.GetSources
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- Some sources are aliases of others (e.g. renamed or mirrored endpoints); the API exposes `WebSource.isAlias()` to filter them out, while the CLI table lists them alongside their target.

## Related features

- [Search sources]({{< relref "/features/search-sources" >}}) - find a source when you don't know its exact ID.
- [Discover flows]({{< relref "/features/discover-flows" >}})
