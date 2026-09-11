---
title: "Output and formats"
weight: 14
---

Pick the right output for your use case: Java objects for the library, CSV for shell pipelines, or JSON/protobuf for a remote service.

{{< tabs "output-and-formats" >}}

{{< tab "API" >}}
{{< feature-status "output-and-formats" "api" >}}

The API is object-centric and can be used with the supported format modules and managers.
A common entry point is the Java library or the standalone JAR distribution.

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:3.2.0
import sdmxdl.DataRequest;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    SdmxWebManager
            .ofServiceLoader()
            .usingName("ECB")
            .getData(KeyRequest.builder()
                    .flowOf("EXR")
                    .keyOf("M.CHF.EUR.SP00.A")
                    .build())
            .forEach(series -> IO.println(series.getKey()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}
{{< feature-status "output-and-formats" "cli" >}}

The CLI writes RFC4180 CSV to standard output by default and supports file output, gzip, append, and encoding options.

```shell
sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A -o chf.csv -z
```
{{< /tab >}}

{{< tab "WS" >}}
{{< feature-status "output-and-formats" "ws" >}}

The web service exposes two transport formats:


- gRPC: protobuf messages on port `4557`
- REST: JSON over HTTP on port `4559`

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M.CHF.EUR.SP00.A"}' -plaintext localhost:4557 sdmxdl.grpc.SdmxWebManager.GetData
```

### REST
```shell
curl -X POST -H "Content-Type: application/json" localhost:4559/sdmx-dl/data --data "{\"source\":\"ECB\",\"flow\":\"EXR\",\"key\":\"M.CHF.EUR.SP00.A\"}"
```
{{< /tab >}}

{{< /tabs >}}

## Supported file formats

sdmx-dl also reads/writes several standard SDMX and interchange formats (SDMX-ML Generic/Compact Data, SDMX-ML Structure, CSV, protobuf, Kryo) — see the [Formats]({{< relref "/formats" >}}) section for details.

## Notes

- This page is about *output shape per flavor*, not wire formats; see [Formats]({{< relref "/formats" >}}) for the on-disk/on-wire encodings.
- The CLI's `-o`/`-z`/`--encoding` options only affect how CSV is written (file vs stdout, gzip, character set) — the CSV field layout itself doesn't change.

## Related features

- [Retrieve data]({{< relref "/features/retrieve-data" >}})
