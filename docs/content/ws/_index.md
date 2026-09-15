---
title: "Web service"
weight: 3
---

![_work-in-progress_](https://img.shields.io/badge/-work_in_progress-E2BC4A)

**sdmx-dl WS** is a web service that serves as a bridge to any application or language.  
This service has three endpoints:

- a [gRPC endpoint](#grpc-endpoint) which has the **best performances** but a limited set of clients
- a [REST endpoint](#rest-endpoint) which has a **wider range of clients** but is less efficient
- an [MCP endpoint](#mcp-endpoint) which lets **AI assistants and agents** browse and fetch data

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
Its default port is `4557`. For convenience, the [reflection protocol](https://grpc.io/docs/guides/reflection/) is enabled. The service is `sdmxdl.grpc.v2.SdmxWebManager`.

Call example using [gRPCurl](https://github.com/fullstorydev/grpcurl):
```shell
grpcurl -d "{\"source\":\"ECB\"}" -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListFlows
```

## REST endpoint

The REST endpoint has the best compatibility with a wide range of clients.  
Its default port is `4559`, under the `/sdmx-dl/v2` path prefix. For convenience, an [OpenAPI UI](https://swagger.io/tools/swagger-ui/) is available at [http://localhost:4559/q/swagger-ui](http://localhost:4559/q/swagger-ui).

Every operation is a plain HTTP `GET` with path/query parameters - there is no request body.

Call example using [curl](https://curl.se/):
```shell
curl "localhost:4559/sdmx-dl/v2/ECB/flows"
```

### Available operations

| RPC                | REST path                                       | Description                                                    |
|--------------------|--------------------------------------------------|-----------------------------------------------------------------|
| `GetAbout`         | `GET /about`                                     | Name and version of sdmx-dl.                                    |
| `ListSources`      | `GET /sources`                                   | List or search sources.                                         |
| `ListDatabases`    | `GET /{source}/databases`                        | List or search a source's databases.                            |
| `ListFlows`        | `GET /{source}/flows`                            | List or search a source's flows.                                |
| `GetMeta`          | `GET /{source}/{flow}/meta`                      | Flow-level structure (dimensions, attributes).                  |
| `ListDimensions`   | `GET /{source}/{flow}/dimensions`                | List or search a flow's dimensions.                              |
| `ListAttributes`   | `GET /{source}/{flow}/attributes`                | List or search a flow's attributes.                              |
| `ListCodes`        | `GET /{source}/{flow}/codes/{dimension}`         | List or search the codes of a dimension.                         |
| `ListAvailability` | `GET /{source}/{flow}/availability/{dimension}`  | Codes that actually occur under a key constraint.                |
| `GetData`          | `GET /{source}/{flow}/data`                      | Fetch observations for a key.                                    |
| `GetDataStream`    | `GET /{source}/{flow}/data:stream`               | Same as `GetData`, streamed observation by observation.          |
| `ListStatuses`     | `GET /statuses`                                  | Check the health of one, several, or all sources.                |

All paths above are relative to `/sdmx-dl/v2`.

## MCP endpoint

The [Model Context Protocol](https://modelcontextprotocol.io/) (MCP) endpoint lets AI assistants and agents explore sources, flows and data through a set of read-only tools.

It shares the same HTTP port as the [REST endpoint](#rest-endpoint) (default `4559`), on path `/mcp` (streamable HTTP transport).

A few things to keep in mind:

- Only sources marked as **public** are exposed; restricted/private sources are hidden.
- It is **read-only**: there is no way to modify configuration or state through it.
- Some fields are truncated or simplified to save tokens (for example, flow descriptions are capped in length, and metadata is returned as a skeleton without the codes of coded dimensions).
- List and search are unified: every listing tool accepts an optional `query`. When `query` is empty, entries are returned in their natural order (sorted or as defined) and truncated to `maxResults` (`0` = no limit). When `query` is non-empty, entries are ranked by relevance (hybrid BM25 + trigram search) and limited to `maxResults`.

Available tools:

| Tool               | Description                                                          |
|--------------------|-----------------------------------------------------------------------|
| `about`            | Get the name and version of sdmx-dl.                                   |
| `listSources`      | List or search available sources.                                      |
| `listDatabases`    | List or search the databases of a source.                              |
| `listFlows`        | List or search the flows (datasets) of a source.                       |
| `listDimensions`   | List or search the dimensions of a flow's structure.                   |
| `listAttributes`   | List or search the attributes of a flow's structure.                   |
| `getMeta`          | Get the structure (dimensions, attributes) skeleton of a flow.         |
| `listCodes`        | List or search the codes of a dimension or attribute.                  |
| `listAvailability` | Get the codes that actually occur for a dimension under a key.         |
| `getData`          | Fetch data series for a flow, optionally filtered by key/period.       |
| `status`           | Get the monitor status of a single source.                             |

The typical workflow is: find a source (`listSources`) → find a flow (`listFlows`) → inspect its dimensions/attributes (`getMeta` or `listDimensions`/`listAttributes`) → resolve dimension codes (`listCodes`) → fetch data (`getData`, preferring the structured `dimensions` map over a positional `key`).

Call example using [curl](https://curl.se/) against the streamable HTTP transport:
```shell
curl -X POST -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" localhost:4559/mcp --data "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"listSources\"}}"
```

Most MCP clients (e.g. IDE assistants) support configuring a remote MCP server by URL directly, without needing curl.

