---
title: "Discover"
weight: 2
aliases:
  - /usage/discover-sources/
  - /usage/discover-databases/
  - /usage/discover-flows/
---

Find the source, database, and flow you want to query.

Discovery and search are the same call at every level: pass a `query` to search, or omit it to list everything. By default, a search returns the 10 best matches; set `maxResults` to change it (`0` = no limit). Results also give the `total` number of matches, and `offset` skips the first ones to page through them. A search also gives the relevance `score` of each match, from 1 (best) down to 0 (excluded); scores rank the matches of one search and can't be compared across searches.
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
            .getItems()
            .forEach(source -> IO.println(source.getId()));

    // Search by name or topic
    manager.listSources(WebSourcesRequest.builder()
                    .query("european central")
                    .maxResults(5)
                    .build())
            .getItems()
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
grpcurl -d '{}' -plaintext localhost:4559 sdmxdl.grpc.v2.SdmxWebManager.ListSources

grpcurl -d '{"query":"european central","maxResults":5}' -plaintext localhost:4559 sdmxdl.grpc.v2.SdmxWebManager.ListSources
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
- Sources can be restricted to a maximum confidentiality level (`PUBLIC`, `RESTRICTED`, …): `confidentialityThreshold` in the API, `--max-confidentiality` in the CLI, `maxConfidentiality` in the WS. It defaults to allowing every source, except when searching flows across all sources: only public sources are searched then, so that confidential ones do not ask for credentials.

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
            .getItems()
            .forEach(database -> IO.println(database.getRef()));

    // Search by name
    ecb.listDatabases(DatabasesRequest.builder()
                    .query("central")
                    .maxResults(5)
                    .build())
            .getItems()
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
grpcurl -d '{"source":"ECB"}' -plaintext localhost:4559 sdmxdl.grpc.v2.SdmxWebManager.ListDatabases

grpcurl -d '{"source":"ECB","query":"central","maxResults":5}' -plaintext localhost:4559 sdmxdl.grpc.v2.SdmxWebManager.ListDatabases
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
            .getItems()
            .forEach(flow -> IO.println(flow.getRef()));

    // Search by topic
    ecb.listFlows(FlowsRequest.builder()
                    .query("exchange rates")
                    .maxResults(5)
                    .build())
            .getItems()
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
grpcurl -d '{"source":"ECB"}' -plaintext localhost:4559 sdmxdl.grpc.v2.SdmxWebManager.ListFlows

grpcurl -d '{"source":"ECB","query":"exchange rates","maxResults":5}' -plaintext localhost:4559 sdmxdl.grpc.v2.SdmxWebManager.ListFlows
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

- Search is scoped to a single source; [discover sources](#sources) first if you don't know which source to search within, or [search across sources](#flows-across-sources).
- Some sources expose several databases; pass a `database` to scope the listing (defaults to the source's single/default database otherwise) — see [Databases](#databases).
- Descriptions may contain markup (e.g. HTML tags) and be quite long; pass `plainText` to strip markup and `truncate` to limit their length (CLI: `--plain-text` and `--truncate`; gRPC: `plain_text` and `truncate`). The MCP tools return plain descriptions truncated to 200 characters by default.

## Flows across sources

Find a dataset by topic when you don't know which source publishes it: the flows of several sources (all by default) are searched at once and ranked together.

{{< tabs "discover-flows-across-sources" >}}

{{< tab "API" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import java.time.Duration;
import sdmxdl.web.*;

void main() throws Exception {
    WebFlowsResult result = SdmxWebManager.ofServiceLoader()
            .searchFlows(WebFlowsRequest.builder()
                    .source("ECB").source("ESTAT").source("BIS").source("OECD")
                    .query("gdp")
                    .maxResults(4)
                    .timeout(Duration.ofSeconds(8))
                    .build());

    result.getItems().forEach(entry -> IO.println(entry.getSource().getId() + " " + entry.getFlow().getRef()));
    result.getFailures().forEach(failure -> IO.println(failure.getSource() + " failed: " + failure.getCause()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl search flows "exchange rates"
sdmx-dl search flows gdp --within ECB,ESTAT,BIS,OECD --timeout 8000 -m 4
```
{{< /tab >}}

{{< tab "WS" >}}

### REST

```shell
curl -G localhost:4559/sdmx-dl/v2/flows \
  --data-urlencode "query=gdp" \
  --data-urlencode "sources=ECB,ESTAT,BIS,OECD" \
  --data-urlencode "timeout=8000" \
  --data-urlencode "maxResults=4"
```

### gRPC
```shell
grpcurl -d '{"sources":["ECB","ESTAT","BIS","OECD"],"query":"gdp","timeout":8000,"maxResults":4}' -plaintext localhost:4559 sdmxdl.grpc.v2.SdmxWebManager.SearchFlows
```
{{< /tab >}}

{{< /tabs >}}

{{< expand "CLI output sample" >}}
```plain
Source,Database,Ref,Name,Description,Score
ESTAT,,ESTAT:PRC_REM_GDP(1.0),GDP weights used in the calculation of the global specific indicator,,0.976062
ESTAT,,ESTAT:NAIDA_10_GDP(1.0),GDP and main aggregates - international data cooperation - annual data,,0.969231
ESTAT,,ESTAT:NAIDQ_10_GDP(1.0),GDP and main aggregates - international data cooperation - quarterly data,,0.968498
OECD,,OECD.CFE.TOU:DSD_TOURISM_KEY@DF_KEY_IND_PC(1.0),Key tourism economic indicators,Tourism GDP corresponds to the part of GDP genera…,0.861891
```
{{< /expand >}}

- Sources are queried in parallel and every database of each source is searched; each result tells its `source` and `database`.
- A source that fails or doesn't answer within `timeout` (milliseconds, `0` for no limit) doesn't fail the search: it is reported in `failures` (on stderr for the CLI), e.g. `Failed to search 'INSEE': No answer within PT1.5S`.
- Without a query, results are sorted by source, database and flow; `total`, `maxResults` and `offset` work as for the other listings.
- Searching all sources can be slow on a cold cache since every source's flows must be downloaded; restrict the sources and set a timeout for interactive use. The MCP `searchFlows` tool only searches public sources and waits 30 seconds by default.

## Next step

- [Browse]({{< relref "/usage/browse" >}}) the structure of the flow you've found to build a valid key.
