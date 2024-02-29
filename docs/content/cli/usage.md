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

```mermaid
%%{init:{'themeVariables': {'textColor': '#fdf6e3', 'fontSize': '12px'},'flowchart':{'nodeSpacing': 5, 'rankSpacing': 30}}}%%
flowchart TB
    r{{sdmx-dl}}
    r --- f([fetch]) --- data & meta & keys
    r --- l([list]) --- sources & flows & dimensions & attributes & codes & availability & features & drivers
    r --- c([check]) --- status & access & config
    r --- s([setup]) --- completion & launcher

    classDef default fill:#93a1a1,stroke-width:0px 
    linkStyle default stroke:#93a1a1
 
    classDef fx fill:#dc322f
    class f,data,meta,keys fx;
    click f "#fetch" "fetch command"
    click data "#fetch-data" "fetch data command"
    click meta "#fetch-meta" "fetch meta command"
    click keys "#fetch-keys" "fetch keys command"
   
    classDef lx fill:#859900
    class l,sources,flows,dimensions,attributes,codes,availability,features,drivers lx;
    click l "#list" "list command"
    click sources "#list-sources" "list sources command"
    click flows "#list-flows" "list flows command"
    click dimensions "#list-dimensions" "list dimensions command"
    click attributes "#list-attributes" "list attributes command"
    click codes "#list-codes" "list codes command"
    click availability "#list-availability" "list availability command"
    click features "#list-features" "list features command"
    click drivers "#list-drivers" "list drivers command"
   
    classDef cx fill:#268bd2
    class c,status,access,config cx;
    click c "#check" "check command"
    click status "#check-status" "check status command"
    click access "#check-access" "check access command"
    click config "#check-config" "check config command"
    
    classDef sx fill:#b58900
    class s,completion,launcher sx;
    click s "#setup" "setup command"
    click completion "#setup-completion" "setup completion command"
    click launcher "#setup-launcher" "setup launcher command"
```

{{< shields_io/badge label="fetch" color="dc322f" >}}

Download time series.

Subcommands:
[data](#fetch-data),
[meta](#fetch-meta),
[keys](#fetch-keys)

[Examples]({{< relref "examples#fetch-examples" >}})

{{< shields_io/badge label="list" color="859900" >}}

List resources and structural metadata.

Subcommands:
[sources](#list-sources),
[flows](#list-flows),
[dimensions](#list-dimensions),
[attributes](#list-attributes),
[codes](#list-codes),
[availability](#list-availability),
[features](#list-features),
[drivers](#list-drivers)

[Examples]({{< relref "examples#list-examples" >}})

{{< shields_io/badge label="check" color="268bd2" >}}

Check resources and services.

Subcommands:
[status](#check-status),
[access](#check-access),
[config](#check-config)

{{< shields_io/badge label="setup" color="b58900" >}}

Setup sdmx-dl.

Subcommands:
[completion](#setup-completion),
[launcher](#setup-launcher)

## Commands details

{{< shields_io/badge label="fetch" message="data" color="dc322f" >}}

Download time series observations.

Example: <code>sdmx-dl <font color="#dc322f">fetch data</font> <abbr title="source">ECB</abbr> <abbr title="flow">EXR</abbr> <abbr title="key">M.USD+CHF.EUR.SP00.A</abbr></code>  

{{< tabs "fetch-data" >}}
{{< tab "Parameters" >}}

1. [`source`](../datatypes#source) - Data source name.
2. [`flow`](../datatypes#flow) - Data flow reference.
3. [`key`](../datatypes#key) - Data key.

{{< /tab >}}
{{< tab "Options" >}}

Main options:
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.

Other options:
[`CSV`](../options#csv),
[`Network`](../options#network)

{{< /tab >}}
{{< tab "Output" >}}

CSV columns:
1. [`Series:key`](../datatypes#key)
2. [`ObsAttributes:map`](../datatypes#map)
3. [`ObsPeriod:datetime`](../datatypes#datetime)
4. [`ObsValue:number`](../datatypes#number)

{{< /tab >}}
{{< /tabs >}}

{{< expand "Output sample" >}}
<small>{{< include file="/tmp/usage/fetch-data-sample.md" >}}</small>
{{< /expand >}}

{{< shields_io/badge label="fetch" message="meta" color="dc322f" >}}

Download time series metadata.  

Example: <code>sdmx-dl <font color="#dc322f">fetch meta</font> <abbr title="source">ECB</abbr> <abbr title="flow">EXR</abbr> <abbr title="key">M.USD+CHF.EUR.SP00.A</abbr></code>  

{{< tabs "fetch-meta" >}}
{{< tab "Parameters" >}}

1. [`source`](../datatypes#source) - Data source name.
2. [`flow`](../datatypes#flow) - Data flow reference.
3. [`key`](../datatypes#key) - Data key.

{{< /tab >}}
{{< tab "Options" >}}

Main options:
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

{{< /tab >}}
{{< tab "Output" >}}

CSV columns:
1. [`Series:key`](../datatypes#key)
2. [`Concept:string`](../datatypes#string)
3. [`Value:string`](../datatypes#string)

{{< /tab >}}
{{< /tabs >}}

{{< expand "Output sample" >}}
<small>{{< include file="/tmp/usage/fetch-meta-sample.md" >}}</small>
{{< /expand >}}

{{< shields_io/badge label="fetch" message="keys" color="dc322f" >}}

Download time series keys.  

Example: <code>sdmx-dl <font color="#dc322f">fetch keys</font> <abbr title="source">ECB</abbr> <abbr title="flow">EXR</abbr> <abbr title="key">M.USD+CHF.EUR.SP00.A</abbr></code>

{{< tabs "fetch-keys" >}}
{{< tab "Parameters" >}}

1. [`source`](../datatypes#source) - Data source name.
2. [`flow`](../datatypes#flow) - Data flow reference.
3. [`key`](../datatypes#key) - Data key.

{{< /tab >}}
{{< tab "Options" >}}

Main options:
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

{{< /tab >}}
{{< tab "Output" >}}

CSV columns:
1. [`Series:key`](../datatypes#key)

{{< /tab >}}
{{< /tabs >}}

{{< expand "Output sample" >}}
<small>{{< include file="/tmp/usage/fetch-keys-sample.md" >}}</small>
{{< /expand >}}

{{< shields_io/badge label="list" message="sources" color="859900" >}}

List data source names and properties.  

Example: <code>sdmx-dl <font color="#859900">list sources</font></code>  

{{< tabs "list-sources" >}}
{{< tab "Parameters" >}}

- _no parameters_

{{< /tab >}}
{{< tab "Options" >}}

Main options:
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.

Other options: 
[`CSV`](../options#csv)

{{< /tab >}}
{{< tab "Output" >}}

CSV columns:
1. [`Name:source`](../datatypes#source)
2. [`Description:string`](../datatypes#string)
3. [`Aliases:list`](../datatypes#list)
4. [`Driver:string`](../datatypes#string)
5. [`Endpoint:uri`](../datatypes#uri)
6. [`Properties:map`](../datatypes#list)
7. [`Website:url`](../datatypes#url)
8. [`Monitor:uri`](../datatypes#uri)
9. [`MonitorWebsite:url`](../datatypes#url)
10. [`Languages:list`](../datatypes#list)

{{< /tab >}}
{{< /tabs >}}

{{< expand "Output sample" >}}
<small>{{< include file="/tmp/usage/list-sources-sample.md" >}}</small>
{{< /expand >}}

{{< shields_io/badge label="list" message="flows" color="859900" >}}

List data flows.  

Example: <code>sdmx-dl <font color="#859900">list flows</font> <abbr title="source">ECB</abbr></code>  

{{< tabs "list-flows" >}}
{{< tab "Parameters" >}}

1. [`source`](../datatypes#source) - Data source name.

{{< /tab >}}
{{< tab "Options" >}}

Main options:
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

{{< /tab >}}
{{< tab "Output" >}}

CSV columns:
1. [`Ref:flow`](../datatypes#flow)
2. [`Name:string`](../datatypes#string)
3. [`Description:string`](../datatypes#string)

{{< /tab >}}
{{< /tabs >}}

{{< expand "Output sample" >}}
<small>{{< include file="/tmp/usage/list-flows-sample.md" >}}</small>
{{< /expand >}}

{{< shields_io/badge label="list" message="dimensions" color="859900" >}}

List data flow dimensions.  

Example: <code>sdmx-dl <font color="#859900">list dimensions</font> <abbr title="source">ECB</abbr> <abbr title="flow">EXR</abbr></code>  

{{< tabs "list-dimensions" >}}
{{< tab "Parameters" >}}

1. [`source`](../datatypes#source) - Data source name.
2. [`flow`](../datatypes#flow) - Data flow reference.

{{< /tab >}}
{{< tab "Options" >}}

Main options:
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

{{< /tab >}}
{{< tab "Output" >}}

CSV columns:
1. [`Name:string`](../datatypes#string)
2. [`Label:string`](../datatypes#string)
3. [`Coded:bool`](../datatypes#bool)
4. [`Index:int`](../datatypes#int)

{{< /tab >}}
{{< /tabs >}}

{{< expand "Output sample" >}}
<small>{{< include file="/tmp/usage/list-dimensions-sample.md" >}}</small>
{{< /expand >}}

{{< shields_io/badge label="list" message="attributes" color="859900" >}}

List data flow attributes.

Example: <code>sdmx-dl <font color="#859900">list attributes</font> <abbr title="source">ECB</abbr> <abbr title="flow">EXR</abbr></code>

{{< tabs "list-attributes" >}}
{{< tab "Parameters" >}}

1. [`source`](../datatypes#source) - Data source name.
2. [`flow`](../datatypes#flow) - Data flow reference.

{{< /tab >}}
{{< tab "Options" >}}

Main options:
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--sort`](../options#sort) - Sort output.

Other options:
[`CSV`](../options#csv),
[`Network`](../options#network)

{{< /tab >}}
{{< tab "Output" >}}

CSV columns:
1. [`Name:string`](../datatypes#string)
2. [`Label:string`](../datatypes#string)
3. [`Coded:bool`](../datatypes#bool)
4. [`Relationship:enum`](../datatypes#enum)

{{< /tab >}}
{{< /tabs >}}

{{< expand "Output sample" >}}
<small>{{< include file="/tmp/usage/list-attributes-sample.md" >}}</small>
{{< /expand >}}

{{< shields_io/badge label="list" message="codes" color="859900" >}}

List codes from data flow concept.  

Example: <code>sdmx-dl <font color="#859900">list codes</font> <abbr title="source">ECB</abbr> <abbr title="flow">EXR</abbr> <abbr title="concept">FREQ</abbr></code>  

{{< tabs "list-codes" >}}
{{< tab "Parameters" >}}

1. [`source`](../datatypes#source) - Data source name.
2. [`flow`](../datatypes#flow) - Data flow reference.
3. [`concept`](../datatypes#string) - Concept name.

{{< /tab >}}
{{< tab "Options" >}}

Main options:
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

{{< /tab >}}
{{< tab "Output" >}}

CSV columns:
1. [`Code:string`](../datatypes#string)
2. [`Label:string`](../datatypes#string)

{{< /tab >}}
{{< /tabs >}}

{{< expand "Output sample" >}}
<small>{{< include file="/tmp/usage/list-codes-sample.md" >}}</small>
{{< /expand >}}

{{< shields_io/badge label="list" message="availability" color="859900" >}}

List available dimension codes.

Example: <code>sdmx-dl <font color="#859900">list availability</font> <abbr title="source">ECB</abbr> <abbr title="flow">EXR</abbr> <abbr title="key">M.CHF...</abbr> <abbr title="index">4</abbr></code>

{{< tabs "list-availability" >}}
{{< tab "Parameters" >}}

1. [`source`](../datatypes#source) - Data source name.
2. [`flow`](../datatypes#flow) - Data flow reference.
3. [`key`](../datatypes#key) - Data key.
4. [`index`](../datatypes#int) - Zero-based index of key dimension.

{{< /tab >}}
{{< tab "Options" >}}

Main options:
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--sort`](../options#sort) - Sort output.

Other options:
[`CSV`](../options#csv),
[`Network`](../options#network)

{{< /tab >}}
{{< tab "Output" >}}

CSV columns:
1. [`Code:string`](../datatypes#string)

{{< /tab >}}
{{< /tabs >}}

{{< expand "Output sample" >}}
<small>{{< include file="/tmp/usage/list-availability-sample.md" >}}</small>
{{< /expand >}}

{{< shields_io/badge label="list" message="features" color="859900" >}}

List supported features of a data source.  

Example: <code>sdmx-dl <font color="#859900">list features</font> <abbr title="source">ECB</abbr></code>  

{{< tabs "list-features" >}}
{{< tab "Parameters" >}}

1. [`source`](../datatypes#source) - Data source name.

{{< /tab >}}
{{< tab "Options" >}}

Main options:
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

{{< /tab >}}
{{< tab "Output" >}}

CSV columns:
1. [`SupportedFeature:enum`](../datatypes#enum)

{{< /tab >}}
{{< /tabs >}}

{{< expand "Output sample" >}}
<small>{{< include file="/tmp/usage/list-features-sample.md" >}}</small>
{{< /expand >}}

{{< shields_io/badge label="list" message="drivers" color="859900" >}}

List driver names and properties.  

Example: <code>sdmx-dl <font color="#859900">list drivers</font></code>  

{{< tabs "list-drivers" >}}
{{< tab "Parameters" >}}

- _no parameters_

{{< /tab >}}
{{< tab "Options" >}}

Main options:
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.

Other options: 
[`CSV`](../options#csv)

{{< /tab >}}
{{< tab "Output" >}}

CSV columns:
1. [`SupportedFeature:enum`](../datatypes#enum)

{{< /tab >}}
{{< /tabs >}}

{{< expand "Output sample" >}}
<small>{{< include file="/tmp/usage/list-drivers-sample.md" >}}</small>
{{< /expand >}}

{{< shields_io/badge label="check" message="status" color="268bd2" >}}

Check service availability.  

Example: <code>sdmx-dl <font color="#268bd2">check status</font> <abbr title="source">ECB</abbr></code>  

{{< tabs "check-status" >}}
{{< tab "Parameters" >}}

1. [`sources`](../datatypes#list) - Data source names.

{{< /tab >}}
{{< tab "Options" >}}

Main options:
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--no-parallel`](../options#no-parallel) - Disable parallel queries.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

{{< /tab >}}
{{< tab "Output" >}}

CSV columns:
1. [`Source:source`](../datatypes#source)
2. [`Status:enum`](../datatypes#enum)
3. [`UptimeRatio:double`](../datatypes#double)
4. [`AverageResponseTime:double`](../datatypes#double)
5. [`ErrorMessage:string`](../datatypes#string)

{{< /tab >}}
{{< /tabs >}}

{{< expand "Output sample" >}}
<small>{{< include file="/tmp/usage/check-status-sample.md" >}}</small>
{{< /expand >}}

{{< shields_io/badge label="check" message="access" color="268bd2" >}}

Check service accessibility.  

Example: <code>sdmx-dl <font color="#268bd2">check access</font> <abbr title="source">ECB</abbr></code>  

{{< tabs "check-access" >}}
{{< tab "Parameters" >}}

1. [`sources`](../datatypes#list) - Data source names.

{{< /tab >}}
{{< tab "Options" >}}

Main options:
- [`-s, --sources<file>`](../options#sources) - File that provides data source definitions.
- [`-l, --languages<langs>`](../options#languages) - Language priority list.
- [`--no-parallel`](../options#no-parallel) - Disable parallel queries.
- [`--sort`](../options#sort) - Sort output.

Other options: 
[`CSV`](../options#csv),
[`Network`](../options#network)

{{< /tab >}}
{{< tab "Output" >}}

CSV columns:
1. [`Source:source`](../datatypes#source)
2. [`Accessible:enum`](../datatypes#enum)
3. [`DurationInMillis:int`](../datatypes#int)
4. [`ErrorMessage:string`](../datatypes#string)

{{< /tab >}}
{{< /tabs >}}

{{< expand "Output sample" >}}
<small>{{< include file="/tmp/usage/check-access-sample.md" >}}</small>
{{< /expand >}}

{{< shields_io/badge label="check" message="config" color="268bd2" >}}

Check sdmx-dl configuration.

Example: <code>sdmx-dl <font color="#268bd2">check config</font></code>  

{{< tabs "check-config" >}}
{{< tab "Parameters" >}}

- _no parameters_

{{< /tab >}}

{{< tab "Options" >}}
Main options:

- _no options_

Other options: 
[`CSV`](../options#csv)

{{< /tab >}}
{{< tab "Output" >}}

CSV columns:
1. [`Scope:enum`](../datatypes#enum)
2. [`PropertyKey:string`](../datatypes#string)
3. [`PropertyValue:string`](../datatypes#string)
4. [`Category:enum`](../datatypes#enum)

{{< /tab >}}
{{< /tabs >}}

{{< expand "Output sample" >}}
<small>{{< include file="/tmp/usage/check-config-sample.md" >}}</small>
{{< /expand >}}

{{< shields_io/badge label="setup" message="completion" color="b58900" >}}<br>

{{< shields_io/badge label="setup" message="launcher" color="b58900" >}}<br>
