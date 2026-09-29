---
title: "Getting started"
weight: 1
---

This walkthrough answers a simple question from start to finish — *how has the Swiss franc moved against the euro recently?* — using the [command-line tool]({{< relref "/cli" >}}).
Every step has an equivalent in the [Java library]({{< relref "/api" >}}) and the [web service]({{< relref "/ws" >}}); follow the links to see them.

{{< hint type=tip >}}
No installation needed: prefix every command with `jbang sdmx-dl@nbbrd` instead of `sdmx-dl` (see [Installation]({{< relref "/cli/installation" >}})).
{{< /hint >}}

## 1. Find the source

Search the available data providers by name or topic:

```shell
sdmx-dl list sources -q "european central" -m 3
```

```plain
Name,Description,Driver,Confidentiality,...
ECB,European Central Bank,RI_SDMX21,PUBLIC,...
ECB_RESTR,European Central Bank (restricted access),RI_SDMX21,RESTRICTED,...
IMF_SDMX_CENTRAL,International Monetary Fund (SDMX Central),RI_SDMX21,PUBLIC,...
```

The European Central Bank is `ECB`. → [Discover sources]({{< relref "/usage/discover#sources" >}})

## 2. Find the flow

Search the datasets published by `ECB`:

```shell
sdmx-dl list flows ECB -q "exchange rates" -m 5
```

```plain
Ref,Name,Description
ECB:EXR(1.0),Exchange Rates,
ECB.DISS:EXR_PUB(1.0),Exchange Rates - Published series,
ECB.DISS:MOBILE_EXR(1.0),Exchange rates,Dataflow for the exchange rates section of the ECB Statistical Tablet Application.
ECB:FXI(1.0),Foreign Exchange Statistics,
ECB:SEE(1.0),Securities exchange - Trading Statistics,
```

The exchange rates flow is `EXR`. → [Discover flows]({{< relref "/usage/discover#flows" >}})

## 3. Understand the key

List the dimensions of `EXR`; they make up the [key]({{< relref "/usage/browse#keys" >}}), in this order:

```shell
sdmx-dl list dimensions ECB EXR
```

```plain
Name,Label,Coded,Index
FREQ,Frequency,true,0
CURRENCY,Currency,true,1
CURRENCY_DENOM,Currency denominator,true,2
EXR_TYPE,Exchange rate type,true,3
EXR_SUFFIX,Series variation - EXR context,true,4
```

So a key looks like `FREQ.CURRENCY.CURRENCY_DENOM.EXR_TYPE.EXR_SUFFIX`. → [Browse dimensions and attributes]({{< relref "/usage/browse#dimensions-and-attributes" >}})

## 4. Pick the codes

Look up the codes of each dimension, for example the frequency:

```shell
sdmx-dl list codes ECB EXR FREQ
```

```plain
Code,Label
A,Annual
Q,Quarterly
B,Daily - businessweek
D,Daily
M,Monthly
...
```

Doing the same for the other dimensions gives `M` (monthly), `CHF` (Swiss franc), `EUR` (euro), `SP00` (spot) and `A` (average). → [Browse codes]({{< relref "/usage/browse#codes" >}})

Not every combination exists. Leave a position empty to check which codes are actually available for it — here, the exchange rate type (index `3`) of monthly average CHF/EUR rates:

```shell
sdmx-dl list availability ECB EXR M.CHF.EUR..A 3
```

```plain
Code,Label
SP00,Spot
```

The key is `M.CHF.EUR.SP00.A`. → [Browse availability]({{< relref "/usage/browse#availability" >}})

## 5. Retrieve the data

Fetch the last 3 observations of that series:

```shell
sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --last-n 3
```

```plain
Series,ObsAttributes,ObsPeriod,ObsValue
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",2026-06-01T00:00:00,.9205454545454544
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",2026-07-01T00:00:00,.9255739130434782
M.CHF.EUR.SP00.A,"OBS_STATUS=A,OBS_CONF=F",2026-08-01T00:00:00,.9361857142857143
```

Add `-o chf.csv` to save the result to a file. → [Retrieve data]({{< relref "/usage/retrieve#data" >}}), [Narrow your request]({{< relref "/usage/retrieve#narrow-your-request" >}})

## Next steps

- Explore each step in detail, in every flavor: [Discover]({{< relref "/usage/discover" >}}), [Browse]({{< relref "/usage/browse" >}}), [Retrieve]({{< relref "/usage/retrieve" >}}).
- Automate this workflow in a script, Excel, R, or Python: [Automation & integration]({{< relref "/integration" >}}).
