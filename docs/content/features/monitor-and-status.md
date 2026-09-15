---
title: "Monitor and status"
weight: 11
---

Check whether a source is up, and how it has been performing, before relying on it in an automated job.

{{< tabs "monitor-and-status" >}}

{{< tab "API" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    SdmxWebManager manager = SdmxWebManager.ofServiceLoader();
    IO.println(manager.getMonitorReport("ECB"));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl check status ECB
```

`check status` also accepts several sources at once, or the keyword `all` to check every configured source in parallel:

```shell
sdmx-dl check status ECB IMF INSEE
sdmx-dl check status all
```
{{< /tab >}}

{{< tab "WS" >}}

### REST

```shell
curl "localhost:4559/sdmx-dl/v2/statuses?sources=ECB"
```

`ListStatuses` also accepts several comma-separated sources, or `all` to check every configured source:

```shell
curl "localhost:4559/sdmx-dl/v2/statuses?sources=ECB,IMF,INSEE"
curl "localhost:4559/sdmx-dl/v2/statuses?sources=all"
```

### gRPC
```shell
grpcurl -d '{"sources":["ECB"]}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListStatuses
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- A report includes a status (UP/DOWN/UNKNOWN), an uptime ratio, and an average response time; the CLI table adds an error message column when a check fails.
- Only sources that declare a monitor endpoint in their configuration can be checked; others report "No monitor defined".
- WS's `ListStatuses` mirrors the CLI's ability to batch-check several sources (or `all`) in a single call, instead of one request per source.

## Related features

- [Discover sources]({{< relref "/features/discover-sources" >}})
- [Authentication and credentials]({{< relref "/features/auth-and-credentials" >}})

