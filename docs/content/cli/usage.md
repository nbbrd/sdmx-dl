---
title: "Usage"
weight: 3
---

By default, all commands print the result on the [standard output](https://en.wikipedia.org/wiki/Standard_streams#Standard_output_(stdout)).  
Most commands produce [RFC4180](https://tools.ietf.org/html/rfc4180) compliant [CSV](https://en.wikipedia.org/wiki/Comma-separated_values) content.

Command arguments are composed of options and positional parameters. Options have a name, positional parameters are usually the values that follow the options, but they may be mixed. The general pattern is:
<pre>
sdmx-dl <u>fetch data</u> <u>ECB EXR M.CHF.EUR.SP00.A</u> <u>-o chf.csv</u>
         <i>command</i>         <i>parameters</i>          <i>options</i>
</pre>

All commands share the following options:
- [`-h, --help`](../options#help) - Show an help message and exit.
- [`-v, --verbose`](../options#verbose) -  Enable verbose mode.

## Commands summary

The commands follow a **verb+noun hierarchy**.

{{< mermaid class="text-center" >}}
%%{init:{'themeVariables': {'textColor': '#fdf6e3', 'fontSize': '12px'},'flowchart':{'nodeSpacing': 5, 'rankSpacing': 30}}}%%
flowchart TB
    r{{sdmx-dl}}
    r --- f([fetch]) --- data & meta & keys
    r --- l([list]) --- sources & flows & concepts & codes & features & drivers
    r --- c([check]) --- status & access & config
    r --- s([setup]) --- completion & launcher
    
    classDef default fill:#93a1a1,stroke-width:0px 
    linkStyle default stroke:#93a1a1
    
    classDef fx fill:#dc322f
    class f,data,meta,keys fx;
    
    classDef lx fill:#859900
    class l,sources,flows,concepts,codes,features,drivers lx;
    
    classDef cx fill:#268bd2
    class c,status,access,config cx;
    
    classDef sx fill:#b58900
    class s,completion,launcher,ping sx;
{{< /mermaid >}}

<a id="fetch" href="#fetch">![fetch](https://img.shields.io/badge/fetch--dc322f?style=flat-square)</a>

Download time series.

Subcommands:
[data](#fetch-data),
[meta](#fetch-meta),
[keys](#fetch-keys)

<a id="list" href="#list">![list](https://img.shields.io/badge/list--859900?style=flat-square)</a>

List resources and structural metadata.

Subcommands:
[sources](#list-sources),
[flows](#list-flows),
[concepts](#list-concepts),
[codes](#list-codes),
[features](#list-features),
[drivers](#list-drivers)

<a id="check" href="#check">![check](https://img.shields.io/badge/check--268bd2?style=flat-square)</a>

Check resources and services.

Subcommands:
[status](#check-status),
[access](#check-access),
[config](#check-config)

<a id="setup" href="#setup">![setup](https://img.shields.io/badge/setup--b58900?style=flat-square)</a>

Setup sdmx-dl.

Subcommands:
[completion](#setup-completion),
[launcher](#setup-launcher)

## Commands details

<a id="fetch-data" href="#fetch-data">![fetch data](https://img.shields.io/badge/fetch-data-dc322f?style=flat-square)</a>

Download time series observations.

Example: `sdmx-dl fetch data ECB EXR M.USD+CHF.EUR.SP00.A`  

Parameters:
- [`source`](../datatypes#source) - Data source name.
- [`flow`](../datatypes#flow) - Data flow reference.
- [`key`](../datatypes#key) - Data key.

Main options:  
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

Output format:
[
    [`Series:key`](../datatypes#key),
    [`ObsAttributes:map`](../datatypes#map),
    [`ObsPeriod:datetime`](../datatypes#datetime),
    [`ObsValue:number`](../datatypes#number)
]

{{< expand "Output sample" >}}

Series | ObsAttributes | ObsPeriod | ObsValue
--- | --- | --- | ---
M.CHF.EUR.SP00.A | OBS_STATUS=A |1999-01-01T00:00:00 | 1.605495
M.CHF.EUR.SP00.A | OBS_STATUS=A |1999-02-01T00:00:00 | 1.59785
M.CHF.EUR.SP00.A | OBS_STATUS=A |1999-03-01T00:00:00 | 1.595430434782609
M.CHF.EUR.SP00.A | OBS_STATUS=A |1999-04-01T00:00:00 | 1.601531818181818

{{< /expand >}}

<a id="fetch-meta" href="#fetch-meta">![fetch meta](https://img.shields.io/badge/fetch-meta-dc322f?style=flat-square)</a>

Download time series metadata.  

Example: `sdmx-dl fetch meta ECB EXR M.USD+CHF.EUR.SP00.A`  

Parameters:
- [`source`](../datatypes#source) - Data source name.
- [`flow`](../datatypes#flow) - Data flow reference.
- [`key`](../datatypes#key) - Data key.

Main options:  
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

Output format: 
[
    [`Series:key`](../datatypes#key),
    [`Concept:string`](../datatypes#string),
    [`Value:string`](../datatypes#string)
]

{{< expand "Output sample" >}}

Series | Concept | Value
--- | --- | ---
M.CHF.EUR.SP00.A | COLLECTION | A
M.CHF.EUR.SP00.A | UNIT | CHF
M.CHF.EUR.SP00.A | DECIMALS | 4
M.CHF.EUR.SP00.A | SOURCE_AGENCY | 4F0
M.CHF.EUR.SP00.A | UNIT_MULT | 0
M.CHF.EUR.SP00.A | TITLE | Swiss franc/Euro

{{< /expand >}}

<a id="fetch-keys" href="#fetch-keys">![fetch keys](https://img.shields.io/badge/fetch-keys-dc322f?style=flat-square)</a>

Download time series keys.  

Example: `sdmx-dl fetch keys ECB EXR .USD+CHF.EUR.SP00.A`  

Parameters:
- [`source`](../datatypes#source) - Data source name.
- [`flow`](../datatypes#flow) - Data flow reference.
- [`key`](../datatypes#key) - Data key.

Main options:  
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

Output format: 
[
    [`Series:key`](../datatypes#key)
]

{{< expand "Output sample" >}}

| Series |
| --- |
| A.CHF.EUR.SP00.A |
| D.CHF.EUR.SP00.A |
| H.CHF.EUR.SP00.A |
| M.CHF.EUR.SP00.A |

{{< /expand >}}

<a id="list-sources" href="#list-sources">![list sources](https://img.shields.io/badge/list-sources-859900?style=flat-square)</a>

List data source names and properties.  

Example: `sdmx-dl list sources`  

Main options:  
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.

Other options: 
[`CSV`](../options#csv)

Output format: 
[
    [`Name:source`](../datatypes#source),
    [`Description:string`](../datatypes#string),
    [`Aliases:list`](../datatypes#list),
    [`Driver:string`](../datatypes#string),
    [`Dialect:string`](../datatypes#string),
    [`Endpoint:url`](../datatypes#url),
    [`Properties:map`](../datatypes#list),
    [`Website:url`](../datatypes#url),
    [`Monitor:string`](../datatypes#monitor)
]

{{< expand "Output sample" >}}

|Name|Description|Aliases|Driver|Dialect|Endpoint|Properties|Website|Monitor|
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
|ABS|Australian Bureau of Statistics||ri:abs||https://stat.data.abs.gov.au/restsdmx/sdmx.ashx||https://stat.data.abs.gov.au|provider=UptimeRobot,id=m783847060-975767bc3a033ea3f3ac8ca2|
|ECB|European Central Bank||ri:sdmx21|ECB2020|https://sdw-wsrest.ecb.europa.eu/service|detailSupported=true|https://sdw.ecb.europa.eu|provider=UptimeRobot,id=m783846981-b55d7e635c5cdc16e16bac2a|
|ESTAT|Eurostat|EUROSTAT|connectors:eurostat||https://ec.europa.eu/eurostat/SDMX/diss-web/rest||https://ec.europa.eu/eurostat/data/database|provider=UptimeRobot,id=m783847077-390f706bd3acf8fb640e48df|

{{< /expand >}}

<a id="list-flows" href="#list-flows">![list flows](https://img.shields.io/badge/list-flows-859900?style=flat-square)</a>

List data flows.  

Example: `sdmx-dl list flows ECB`  

Parameters:
- [`source`](../datatypes#source) - Data source name.

Main options:  
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

Output format: 
[
    [`Ref:flow`](../datatypes#flow),
    [`Label:string`](../datatypes#string)
]

{{< expand "Output sample" >}}

|Ref         |Label                         |
|------------|------------------------------|
|ECB:AME(1.0)|AMECO                         |
|ECB:BKN(1.0)|Banknotes statistics          |
|ECB:BLS(1.0)|Bank Lending Survey Statistics|

{{< /expand >}}

<a id="list-concepts" href="#list-concepts">![list concepts](https://img.shields.io/badge/list-concepts-859900?style=flat-square)</a>

List data flow concepts.  

Example: `sdmx-dl list concepts ECB EXR`  

Parameters:
- [`source`](../datatypes#source) - Data source name.
- [`flow`](../datatypes#flow) - Data flow reference.

Main options:  
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

Output format: 
[
    [`Concept:string`](../datatypes#string),
    [`Label:string`](../datatypes#string),
    [`Type:enum`](../datatypes#enum),
    [`Coded:bool`](../datatypes#bool),
    [`Position:int`](../datatypes#int)
]

{{< expand "Output sample" >}}

|Concept     |Label                         |Type     |Coded|Position|
|------------|------------------------------|---------|-----|--------|
|FREQ        |Frequency                     |dimension|true |1       |
|CURRENCY    |Currency                      |dimension|true |2       |
|CURRENCY_DENOM|Currency denominator          |dimension|true |3       |
|EXR_TYPE    |Exchange rate type            |dimension|true |4       |
|EXR_SUFFIX  |Series variation - EXR context|dimension|true |5       |
|TIME_FORMAT |Time format code              |attribute|false|        |

{{< /expand >}}

<a id="list-codes" href="#list-codes">![list codes](https://img.shields.io/badge/list-codes-859900?style=flat-square)</a>

List codes from data flow concept.  

Example: `sdmx-dl list codes ECB EXR FREQ`  

Parameters:
- [`source`](../datatypes#source) - Data source name.
- [`flow`](../datatypes#flow) - Data flow reference.
- [`concept`](../datatypes#string) - Concept name.

Main options:  
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

Output format: 
[
    [`Code:string`](../datatypes#string),
    [`Label:string`](../datatypes#string)
]

{{< expand "Output sample" >}}

|Code        |Label                         |
|------------|------------------------------|
|A           |Annual                        |
|Q           |Quarterly                     |
|B           |Daily - businessweek          |

{{< /expand >}}

<a id="list-features" href="#list-features">![list features](https://img.shields.io/badge/list-features-859900?style=flat-square)</a>

List supported features of a data source.  

Example: `sdmx-dl list features ECB`  

Parameters:
- [`source`](../datatypes#source) - Data source name.

Main options:  
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

Output format: 
[
    [`SupportedFeature:enum`](../datatypes#enum)
]

{{< expand "Output sample" >}}

|SupportedFeature|
|----------------|
|SERIES_KEYS_ONLY|

{{< /expand >}}

<a id="list-drivers" href="#list-drivers">![list drivers](https://img.shields.io/badge/list-drivers-859900?style=flat-square)</a>

List driver names and properties.  

Example: `sdmx-dl list drivers`  

Main options:  
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.

Other options: 
[`CSV`](../options#csv)

Output format: 
[
    [`SupportedFeature:enum`](../datatypes#enum)
]

{{< expand "Output sample" >}}

|Name        |SupportedProperties                                                                                   |
|------------|------------------------------------------------------------------------------------------------------|
|ri:dotstat  |connectTimeout,readTimeout,maxRedirects,preemptiveAuthentication                                      |
|ri:nbb      |connectTimeout,readTimeout,maxRedirects,preemptiveAuthentication                                      |
|ri:sdmx21   |connectTimeout,readTimeout,maxRedirects,preemptiveAuthentication,detailSupported,trailingSlashRequired|

{{< /expand >}}

<a id="check-status" href="#check-status">![check status](https://img.shields.io/badge/check-status-268bd2?style=flat-square)</a>

Check service availability.  

Example: `sdmx-dl check status ECB`  

Parameters:
- [`sources`](../datatypes#list) - Data source names.

Main options:  
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--no-parallel`](../options#no-parallel) - Disable parallel queries.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

Output format: 
[
    [`Source:source`](../datatypes#source),
    [`State:enum`](../datatypes#enum),
    [`UptimeRatio:double`](../datatypes#double),
    [`AverageResponseTime:double`](../datatypes#double),
    [`ErrorMessage:string`](../datatypes#string)
]

{{< expand "Output sample" >}}

| Source | State | UptimeRatio | AverageResponseTime | ErrorMessage |
| --- | --- | --- | --- | --- |
| ECB | UP | 99.691 | | |

{{< /expand >}}

<a id="check-access" href="#check-access">![check access](https://img.shields.io/badge/check-access-268bd2?style=flat-square)</a>

Check service accessibility.  

Example: `sdmx-dl check access ECB`  

Parameters:
- [`sources`](../datatypes#list) - Data source names.

Main options:  
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--no-parallel`](../options#no-parallel) - Disable parallel queries.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

Output format: 
[
    [`Source:source`](../datatypes#source),
    [`Accessible:enum`](../datatypes#enum),
    [`DurationInMillis:int`](../datatypes#int),
    [`ErrorMessage:string`](../datatypes#string)
]

{{< expand "Output sample" >}}

| Source | Accessible| DurationInMillis | ErrorMessage |
| --- | --- | --- | --- |
| ECB | YES | 726 | |

{{< /expand >}}

<a id="check-config" href="#check-config">![check config](https://img.shields.io/badge/check-config-268bd2?style=flat-square)</a>

Check sdmx-dl configuration.

Example: `sdmx-dl check config`  

Other options: 
[`CSV`](../options#csv)

Output format: 
[
    [`Scope:enum`](../datatypes#enum),
    [`PropertyKey:string`](../datatypes#string),
    [`PropertyValue:string`](../datatypes#string),
    [`Category:enum`](../datatypes#enum)
]

{{< expand "Output sample" >}}

| Scope | PropertyKey | PropertyValue | Category| 
| --- | --- | --- | --- | 
| `SYSTEM` | `sources` | `C:\temp\some-sources.xml` | `WIDE_OPTION` | 
| `SYSTEM` | `org.fusesource.jansi.Ansi.disable` | `true` | `OTHER` | 
| `GLOBAL` | `sources` | `C:\Users\ABC\other-sources.xml` | `WIDE_OPTION` | 
| `LOCAL` | `sdmx-dl.check.status.verbose` | `true` | `NARROW_OPTION` | 

{{< /expand >}}

<a id="setup-completion" href="#setup-completion">![ping](https://img.shields.io/badge/setup-completion-b58900?style=flat-square)</a>

<a id="setup-launcher" href="#setup-launcher">![setup launcher](https://img.shields.io/badge/setup-launcher-b58900?style=flat-square)</a>
