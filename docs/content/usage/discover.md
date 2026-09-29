---
title: "Discover"
weight: 2
aliases:
  - /usage/discover-sources/
  - /usage/discover-databases/
  - /usage/discover-flows/
---

Find the source, database, and flow you want to query.

Discovery and search are the same call at every level: pass a `query`/`maxResults` to search, or omit them to list everything.
Ranking is hybrid: exact lexical matches (BM25) are fused with typo-tolerant trigram similarity, so partial or approximate queries still surface the right result.
When `query` is empty, results fall back to being sorted by id instead of ranked by relevance.

## Sources

See which data providers sdmx-dl can connect to, and find one by name or topic when you don't know its exact ID.

{{< tabs "discover-sources" >}}

{{< tab "API" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.web.*;

void main() throws Exception {
    SdmxWebManager manager = SdmxWebManager.ofServiceLoader();

    // List every source
    manager.listSources(WebSourcesRequest.DEFAULT)
            .forEach(source -> IO.println(source.getId()));

    // Search by name or topic
    manager.listSources(WebSourcesRequest.builder()
                    .query("european central")
                    .maxResults(5)
                    .build())
            .forEach(source -> IO.println(source.getId()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl list sources
sdmx-dl list sources -q "european central" -m 5
```
{{< /tab >}}

{{< tab "WS" >}}

### REST

```shell
curl "localhost:4559/sdmx-dl/v2/sources"

curl -G localhost:4559/sdmx-dl/v2/sources \
  --data-urlencode "query=european central" \
  --data-urlencode "maxResults=5"
```

### gRPC
```shell
grpcurl -d '{}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListSources

grpcurl -d '{"query":"european central","maxResults":5}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListSources
```
{{< /tab >}}

{{< /tabs >}}

{{< expand "CLI output sample (search)" >}}
```plain
Name,Description,Aliases,Driver,Confidentiality,...
ECB,European Central Bank,,RI_SDMX21,PUBLIC,...
ECB_RESTR,European Central Bank (restricted access),,RI_SDMX21,RESTRICTED,...
IMF_SDMX_CENTRAL,International Monetary Fund (SDMX Central),,RI_SDMX21,PUBLIC,...
EC_DG_COMP,European Commission - Directorate General for Competition,,DIALECTS_ESTAT,PUBLIC,...
EC_DG_ECFIN,European Commission - Directorate-General for Economic and Financial Affairs,,DIALECTS_ESTAT,PUBLIC,...
```
Other columns: `Endpoint`, `Properties`, `Website`, `Monitor`, `MonitorWebsite`, `Languages`.
{{< /expand >}}

- Both `"ecb"` and `"eurpean central"` find the European Central Bank.
- Some sources are aliases of others (e.g. renamed or mirrored endpoints); the API exposes `WebSource.isAlias()` to filter them out (and they're excluded from search results), while the CLI table lists them alongside their target.
- `WebSourcesRequest` also carries a `threshold` (a `Confidentiality` level) so that sources requiring stricter confidentiality can be excluded from the results; it defaults to allowing every source.

## Databases

Find a database namespace within a multi-database source, when you don't know its exact name.

{{< tabs "discover-databases" >}}

{{< tab "API" >}}

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

{{< expand "CLI output sample" >}}
```plain
Ref,Name
```
`ECB` exposes a single default database, so the list is empty.
{{< /expand >}}

- Most sources expose a single default database, so this is only useful for multi-database providers.
- Search is scoped to one source at a time; [discover sources](#sources) first if you don't know which source to search within.

## Flows

See which datasets a source publishes, and find one by topic when you don't know its exact flow ID.

{{< tabs "discover-flows" >}}

{{< tab "API" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    Provider ecb = SdmxWebManager.ofServiceLoader().usingName("ECB");

    // List every flow
    ecb.listFlows(FlowsRequest.DEFAULT)
            .forEach(flow -> IO.println(flow.getRef()));

    // Search by topic
    ecb.listFlows(FlowsRequest.builder()
                    .query("exchange rates")
                    .maxResults(5)
                    .build())
            .forEach(flow -> IO.println(flow.getRef()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl list flows ECB
sdmx-dl list flows ECB -q "exchange rates" -m 5
```
{{< /tab >}}

{{< tab "WS" >}}

### REST

```shell
curl "localhost:4559/sdmx-dl/v2/ECB/flows"

curl -G localhost:4559/sdmx-dl/v2/ECB/flows \
  --data-urlencode "query=exchange rates" \
  --data-urlencode "maxResults=5"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListFlows

grpcurl -d '{"source":"ECB","query":"exchange rates","maxResults":5}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListFlows
```
{{< /tab >}}

{{< /tabs >}}

{{< expand "CLI output sample (search)" >}}
```plain
Ref,Name,Description
ECB:EXR(1.0),Exchange Rates,
ECB.DISS:EXR_PUB(1.0),Exchange Rates - Published series,
ECB.DISS:MOBILE_EXR(1.0),Exchange rates,Dataflow for the exchange rates section of the ECB Statistical Tablet Application.
ECB:FXI(1.0),Foreign Exchange Statistics,
ECB:SEE(1.0),Securities exchange - Trading Statistics,
```
{{< /expand >}}

- Search is scoped to a single source; [discover sources](#sources) first if you don't know which source to search within.
- Some sources expose several databases; pass a `database` to scope the listing (defaults to the source's single/default database otherwise) — see [Databases](#databases).

## Next step

- [Browse]({{< relref "/usage/browse" >}}) the structure of the flow you've found to build a valid key.
