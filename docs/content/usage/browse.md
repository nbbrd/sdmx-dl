---
title: "Browse"
weight: 3
aliases:
  - /usage/browse-structure/
  - /usage/browse-codes/
  - /usage/browse-availability/
---

Explore a flow's structure and narrow down valid keys before retrieving data.

A flow is described by ordered **dimensions** (which make up the key) and **attributes** (extra descriptive metadata, not part of the key).
Coded dimensions and attributes draw their values from a codelist.

## Keys

A key selects one or more series of a flow. It has one code per dimension, in dimension order, separated by dots:

| Key                    | Meaning                                                             |
|------------------------|---------------------------------------------------------------------|
| `M.CHF.EUR.SP00.A`     | Exactly one series (`FREQ=M`, `CURRENCY=CHF`, …).                   |
| `M..EUR.SP00.A`        | An empty position matches any code (here: every currency).          |
| `M.CHF+USD.EUR.SP00.A` | `+` selects several codes in the same position (here: CHF and USD). |
| `all`                  | Every series of the flow.                                           |

The same key syntax is used by every flavor (API `keyOf(...)`, CLI argument, WS `key` parameter).
Use [Dimensions and attributes](#dimensions-and-attributes) to find the dimension order, [Codes](#codes) to find valid codes, and [Availability](#availability) to check which codes actually occur.

## Dimensions and attributes

List the dimensions and attributes that make up a flow's structure — their names, labels, whether they're coded, and (for dimensions) their position in the key.

{{< tabs "browse-structure" >}}

{{< tab "API" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    Provider<sdmxdl.web.WebSource> ecb = SdmxWebManager.ofServiceLoader().usingName("ECB");

    ecb.listDimensions(DimensionsRequest.builder().flowOf("EXR").build())
            .forEach(dimension -> IO.println(dimension.getId() + " = " + dimension.getName()));

    ecb.listAttributes(AttributesRequest.builder().flowOf("EXR").build())
            .forEach(attribute -> IO.println(attribute.getId() + " = " + attribute.getName()));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl list dimensions ECB EXR
sdmx-dl list attributes ECB EXR
```
{{< /tab >}}

{{< tab "WS" >}}

### REST
```shell
curl "localhost:4559/sdmx-dl/v2/ECB/EXR/dimensions"
curl "localhost:4559/sdmx-dl/v2/ECB/EXR/attributes"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListDimensions
grpcurl -d '{"source":"ECB","flow":"EXR"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListAttributes
```
{{< /tab >}}

{{< /tabs >}}

{{< expand "CLI output sample" >}}
```plain
Name,Label,Coded,Index
FREQ,Frequency,true,0
CURRENCY,Currency,true,1
CURRENCY_DENOM,Currency denominator,true,2
EXR_TYPE,Exchange rate type,true,3
EXR_SUFFIX,Series variation - EXR context,true,4
```

```plain
Name,Label,Coded,Relationship
BREAKS,Breaks,false,SERIES
COLLECTION,Collection indicator,true,SERIES
COMPILATION,Compilation,false,GROUP
...
OBS_CONF,Observation confidentiality,true,OBSERVATION
OBS_STATUS,Observation status,true,OBSERVATION
...
```
{{< /expand >}}

- Both calls accept an optional `query`/`maxResults` to search/limit results, the same way as [discovery]({{< relref "/usage/discover" >}}).
- Dimensions are ordered and make up the positional [key](#keys) used by [Retrieve data]({{< relref "/usage/retrieve#data" >}}) and [Availability](#availability).
- A dimension or attribute marked `coded` has its values drawn from a codelist — resolve them with [Codes](#codes).

## Codes

Look up the human-readable labels behind a coded dimension's values (e.g. `FREQ=M` means "Monthly"), so you can build a valid key or interpret one.

{{< tabs "browse-codes" >}}

{{< tab "API" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    SdmxWebManager
            .ofServiceLoader()
            .usingName("ECB")
            .listCodes(CodesRequest.builder()
                    .flowOf("EXR")
                    .concept("FREQ")
                    .build())
            .forEach((code, label) -> IO.println(code + " = " + label));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl list codes ECB EXR FREQ
```
{{< /tab >}}

{{< tab "WS" >}}

### REST
```shell
curl "localhost:4559/sdmx-dl/v2/ECB/EXR/codes/FREQ"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","concept":"FREQ"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListCodes
```
{{< /tab >}}

{{< /tabs >}}

{{< expand "CLI output sample" >}}
```plain
Code,Label
A,Annual
Q,Quarterly
B,Daily - businessweek
S,"Half-yearly, semester (value introduced in 2009 but H existed and is used in ESCB context)"
D,Daily
E,Event (not supported)
W,Weekly
H,Half-yearly
M,Monthly
N,Minutely
```
{{< /expand >}}

- This lists **all defined codes** for a dimension, regardless of whether they actually occur in the dataset. To narrow codes down to what's actually available under a key constraint, use [Availability](#availability) instead.
- Accepts an optional `query`/`maxResults` to search/limit results.
- The gRPC `concept` field is the same value as the REST path segment; use a dimension or attribute id from [Dimensions and attributes](#dimensions-and-attributes).

## Availability

Narrow down which codes are actually usable for one dimension, once other dimensions of the key are already fixed — useful for building valid keys interactively instead of guessing.

{{< tabs "browse-availability" >}}

{{< tab "API" >}}

```java
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    SdmxWebManager
            .ofServiceLoader()
            .usingName("ECB")
            .listAvailability(AvailabilityRequest.builder()
                    .flowOf("EXR")
                    .keyOf("M..EUR.SP00.A")
                    .dimension("CURRENCY")
                    .build())
            .forEach((code, label) -> IO.println(code + " = " + label));
}
```
{{< /tab >}}

{{< tab "CLI" >}}

```shell
sdmx-dl list availability ECB EXR M..EUR.SP00.A 1
```
{{< /tab >}}

{{< tab "WS" >}}

### REST

```shell
curl -G localhost:4559/sdmx-dl/v2/ECB/EXR/availability/CURRENCY \
  --data-urlencode "key=M..EUR.SP00.A"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M..EUR.SP00.A","dimension":"CURRENCY"}' -plaintext localhost:4557 sdmxdl.grpc.v2.SdmxWebManager.ListAvailability
```
{{< /tab >}}

{{< /tabs >}}

{{< expand "CLI output sample" >}}
```plain
Code,Label
ARS,Argentine peso
AUD,Australian dollar
BGN,Bulgarian lev
BRL,Brazilian real
CAD,Canadian dollar
CHF,Swiss franc
CNY,Chinese yuan renminbi
...
```
{{< /expand >}}

- Unlike [Codes](#codes), this only returns codes that actually occur in the data under the given constraint — not every code defined in the codelist.
- The dimension is **not** referenced the same way across flavors: the API and WS (`AvailabilityRequest.dimension`/gRPC `dimension` field/REST `/availability/{dimension}` path segment) take the dimension by its **id** (e.g. `CURRENCY`, as returned by [Dimensions and attributes](#dimensions-and-attributes)), while the CLI conveniently accepts a zero-based **index** into the key instead (e.g. `1` for the second component of `M..EUR.SP00.A`, which is `CURRENCY`) and resolves it to an id internally before calling the same request.

## Next step

- [Retrieve]({{< relref "/usage/retrieve" >}}) data and metadata for the key you've settled on.
