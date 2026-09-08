---
title: "Discover flows"
weight: 2
---

See which datasets a source publishes, before picking one to query for data.

{{< tabs "discover-flows" >}}

{{< tab "API" >}}
{{< feature-status "discover-flows" "api" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:3.2.0
import sdmxdl.DatabaseRequest;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    SdmxWebManager manager = SdmxWebManager.ofServiceLoader();

    manager.usingName("ECB")
            .getFlows(DatabaseRequest.builder().build())
            .forEach(flow -> IO.println(flow.getRef()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}
{{< feature-status "discover-flows" "cli" >}}

```shell
sdmx-dl list flows ECB
```
{{< /tab >}}

{{< tab "WS" >}}
{{< feature-status "discover-flows" "ws" >}}

### REST

```shell
curl -X POST \
  -H "Content-Type: application/json" \
  localhost:4559/sdmx-dl/flows \
  --data "{\"source\":\"ECB\"}"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB"}' -plaintext localhost:4557 sdmxdl.grpc.SdmxWebManager.GetFlows
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- Some sources expose several databases; pass a `database` to scope the listing (defaults to the source's single/default database otherwise).

## Related features

- [Discover sources]({{< relref "/features/discover-sources" >}})
- [Search flows]({{< relref "/features/search-flows" >}}) - find a flow by topic when you don't know its exact ID.
