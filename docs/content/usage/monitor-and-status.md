---
title: "Monitor and status"
weight: 5
---

Check whether a source is healthy before relying on it in an automated job, and diagnose why it fails.

Two checks can be combined:

- **`MONITOR`** (default): asks the third-party monitor declared by the source (Upptime, UptimeRobot, …) for its status, uptime ratio and average response time. It is cheap and does not contact the source itself.
- **`ACCESS`**: sends a lightweight live request from your machine (or server) to the source and reports whether it can be reached, whether it answered successfully, how long it took and, on HTTP errors, the status code.

Both results are merged into a **verdict**:

| Verdict         | Meaning                                                                                        |
|-----------------|------------------------------------------------------------------------------------------------|
| `OK`            | The source works.                                                                              |
| `DEGRADED`      | The source answers with errors, or the monitor and the live check disagree.                     |
| `LOCAL_ISSUE`   | The monitor says up but the source cannot be reached from here: check proxy, firewall or SSL.   |
| `REMOTE_OUTAGE` | The monitor says down.                                                                         |
| `UNREACHABLE`   | The source cannot be reached and there is no monitor to tell why.                              |
| `UNKNOWN`       | Not enough information (no monitor defined and no live check requested, for example).          |

{{< tabs "monitor-and-status" >}}

{{< tab "API" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import java.util.EnumSet;
import sdmxdl.web.HealthCheck;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebHealthRequest;

void main() throws Exception {
    SdmxWebManager manager = SdmxWebManager.ofServiceLoader();
    WebHealthRequest request = WebHealthRequest
            .builder()
            .source("ECB")
            .source("IMF")
            .checks(EnumSet.of(HealthCheck.MONITOR, HealthCheck.ACCESS))
            .build();
    manager.checkHealth(request).forEach(IO::println);
}
```

The building blocks are also available separately: `manager.usingName("ECB").checkAccess()` performs the live check only.
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl check health ECB
sdmx-dl check health ECB IMF INSEE --checks monitor,access
sdmx-dl check health all
```

Add `--fail-on-issue` to exit with a non-zero code when any verdict is not `OK`, which is handy in scripts and CI jobs.
{{< /tab >}}

{{< tab "WS" >}}

### REST

```shell
curl "localhost:4559/sdmx-dl/v2/ECB/health"
curl "localhost:4559/sdmx-dl/v2/health?sources=ECB,IMF&checks=monitor,access"
curl "localhost:4559/sdmx-dl/v2/health?sources=all"
```

### gRPC
```shell
grpcurl -d '{"sources":["ECB"],"checks":["MONITOR","ACCESS"]}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.CheckHealth
```

### MCP

The `checkHealth` tool diagnoses a single source; AI assistants use it to explain why the other tools fail.
{{< /tab >}}

{{< /tabs >}}

{{< expand "CLI output sample" >}}
```plain
Source,Verdict,Status,UptimeRatio,AverageResponseTime,MonitorError,Reachable,Accessible,StatusCode,DurationInMillis,URI,AccessError
ECB,OK,UP,0.9754,1165,,YES,YES,,756,https://data-api.ecb.europa.eu/service/dataflow/all/all/latest,
IMF,OK,UP,0.9168999999999999,753,,YES,YES,,1585,https://api.imf.org/external/sdmx/2.1/dataflow/all/all/latest,
INSEE,OK,UP,0.9833,1213,,YES,YES,,862,https://bdm.insee.fr/series/sdmx/dataflow/all/all/latest,
```
{{< /expand >}}

{{< expand "REST output sample" >}}
```json
[{
  "source": "ECB",
  "verdict": "OK",
  "monitor": {
    "source": "ECB",
    "status": "UP",
    "uptimeRatio": 0.9754,
    "averageResponseTime": "1165"
  },
  "access": {
    "reachable": true,
    "accessible": true,
    "uri": "https://data-api.ecb.europa.eu/service/dataflow/all/all/latest",
    "duration": "645"
  }
}]
```
{{< /expand >}}

## Notes

- Only `MONITOR` is checked by default because it never contacts the source; request `ACCESS` explicitly when the monitor is missing or says up but your calls fail. This also keeps the web service from being used to probe sources on demand.
- Sources without a monitor endpoint report "No monitor defined" in the monitor error; combine with `ACCESS` to get a verdict anyway.
- An HTTP error (e.g. `503`) means the source is reachable but failing: the access report is reachable, not accessible, and carries the status code.
- Durations and average response times are in milliseconds; the uptime ratio is between 0 and 1.
- All flavors accept several sources (or `all`) in a single call, checked in parallel; an unknown source fails the whole request. MCP checks one source at a time.

## Related features

- [Discover sources]({{< relref "/usage/discover#sources" >}})
