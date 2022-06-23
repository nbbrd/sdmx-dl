---
title: "Data types"
weight: 5
---

## Common

### `file`
### `encoding`
### `char`
### `string`
### `locale`
### `datetime`
The date-time format is [ISO-8601](https://en.wikipedia.org/wiki/ISO_8601).

### `number`
A number is a numeric value.

**Format:** depends on the locale and number pattern.

__Note:__ The number format provider has been changed in [JDK9 (see JEP252)](https://openjdk.java.net/jeps/252) so the formatting might be different when using an old JDK. For example, NaN value (aka "not-a-number") is represented by `NaN` in JDK9+ but was represented by `U+FFFD REPLACEMENT CHARACTER` in JDK8. Fortunately, the use of the newer provider (`CLDR`) can be enforced by setting explicitly the system property `java.locale.providers`: 
```properties
java.locale.providers=CLDR
```

### `url`
### `bool`
### `int`

## SDMX

### `source`

A source is a string that identifies of a data source.  

**Format:** nothing specific but usually short and uppercase.  

{{< expand "Examples" >}}

| source | description |
| --- | --- |
| `ABS` | Australian Bureau of Statistics |
| `ECB` | European Central Bank |
| `NBB` | National Bank of Belgium |

{{< /expand >}}

### `flow`

A flow is a string that identifies a multi-dimensional dataset.  

**Format:** _TODO_

{{< expand "Examples" >}}

| flow | description |
| --- | --- |
| `EXR` | Exchange Rates |
| `ABS_REGIONAL_ASGS` | Regional Statistics, ASGS 2011, 2011-2016 |

{{< /expand >}}

### `key`

A key is a string that identifies a data subset of a dataset
(_In a cube, it would be a slice identifier_).  
A key can identify single or multiple time series.

**Format:** either an ordered list of dimension values separated by a dot (`.`) or the special keyword `all`.  

{{< expand "Examples" >}}

| key | type | description |
| --- | --- | --- |
| `M.CHF.EUR.SP00.A` | single | Monthly; Swiss franc; … |
| `M+D.CHF.EUR.SP00.A` | multiple | Monthly + Daily; Swiss franc; … |
| `M.CHF+USD.EUR.SP00.A` | multiple | Monthly; Swiss franc + US dollar; … |
| `M..EUR.SP00.A` | multiple | Monthly; all currencies; … |
| `all` | multiple | everything |

{{< /expand >}}

### `langs`

