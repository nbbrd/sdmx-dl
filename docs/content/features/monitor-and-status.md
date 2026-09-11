---
title: "Monitor and status"
weight: 13
---

Check whether a source is up, and how it has been performing, before relying on it in an automated job.

{{< tabs "monitor-and-status" >}}

{{< tab "API" >}}
{{< feature-status "monitor-and-status" "api" >}}

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
{{< feature-status "monitor-and-status" "cli" >}}

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
{{< feature-status "monitor-and-status" "ws" >}}

### REST

```shell
curl -X POST \
  -H "Content-Type: application/json" \
  localhost:4559/sdmx-dl/monitorReport \
  --data "{\"source\":\"ECB\"}"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB"}' -plaintext localhost:4557 sdmxdl.grpc.SdmxWebManager.GetMonitorReport
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- A report includes a status (UP/DOWN/UNKNOWN), an uptime ratio, and an average response time; the CLI table adds an error message column when a check fails.
- Only sources that declare a monitor endpoint in their configuration can be checked; others report "No monitor defined".

## Related features

- [Discover sources]({{< relref "/features/discover-sources" >}})
- [Authentication and credentials]({{< relref "/features/auth-and-credentials" >}})
