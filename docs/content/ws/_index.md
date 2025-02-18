---
title: "Web service"
weight: 3
---

![_work-in-progress_](https://img.shields.io/badge/-work_in_progress-E2BC4A)

**sdmx-dl WS** is a web service that serves as a bridge to any other application or language.  
This service has two endpoints:

- a [gRPC endpoint](#grpc-endpoint) which has the **best performances** but a limited set of clients
- a [REST endpoint](#rest-endpoint) which has a **wider range of clients** but is less efficient

These endpoints also provides specifications ([.proto files](https://grpc.io/docs/what-is-grpc/introduction/#working-with-protocol-buffers) and [OpenAPI](https://en.wikipedia.org/wiki/OpenAPI_Specification) respectively) that can be used to generate client code.
They are designed to operate locally as well as on remote machines.

## Getting started

An uber-jar (aka fat-jar) is available on the [GitHub release page](https://github.com/nbbrd/sdmx-dl/releases/) and on [Maven Central](https://search.maven.org/artifact/com.github.nbbrd.sdmx-dl/sdmx-dl-grpc).  
[Java 17+](https://whichjdk.com/) is required to run it.

To run it, just use the following command: `java -jar sdmx-dl-grpc-VERSION-runner.jar`

Custom config:

| Option                     | Description                                                                                        |
|----------------------------|----------------------------------------------------------------------------------------------------|
| `quarkus.grpc.server.port` | [The gRPC server port](https://quarkus.io/guides/all-config#quarkus-grpc_quarkus-grpc-server-port) |
| `quarkus.http.port`        | [The HTTP port](https://quarkus.io/guides/all-config#quarkus-vertx-http_quarkus-http-port)         |

More info at Quarkus [all configuration options page](https://quarkus.io/guides/all-config) and [HTTP reference page](https://quarkus.io/guides/http-reference).

## gRPC endpoint

The gRPC endpoint is the most efficient way to interact with the web service.  
Its default port is `4557`. For convenience, the [reflection protocol](https://grpc.io/docs/guides/reflection/) is enabled.

Call example using [gRPCurl](https://github.com/fullstorydev/grpcurl):
```shell
grpcurl -d "{\"source\":\"ECB\"}" -plaintext localhost:4556 sdmxdl.grpc.SdmxWebManager.GetFlows
```

## REST endpoint

The REST endpoint has the best compatibility with a wide range of clients.  
Its default port is `4559`. For convenience, an [OpenAPI UI](https://swagger.io/tools/swagger-ui/) is available at [http://localhost:4559/q/swagger-ui](http://localhost:4559/q/swagger-ui).

Call example using [curl](https://curl.se/):
```shell
curl -X POST -H "Content-Type: application/json" localhost:4559/sdmx-dl/flows --data "{\"source\":\"ECB\"}"
```
