---
title: "sdmx-dl"
---

{{< hint type="caution" >}}
sdmx-dl is still in development and is available <ins>for test only</ins>. **Do not use in production!**<br>
{{< /hint >}}


<img src="SDMX_logo.svg" alt="logo" width="200px" /><br>
sdmx-dl - **Easily download official statistics**<br>

[![Download](https://img.shields.io/github/release/nbbrd/sdmx-dl.svg)](https://github.com/nbbrd/sdmx-dl/releases/latest)

## About

**[sdmx-dl](https://github.com/nbbrd/sdmx-dl) is a tool designed to easily download official statistics.**  
It is mainly based on the [SDMX standard](https://sdmx.org/?page_id=3425) but can be extended with other APIs as well.

This project has two parts:
- [a Java library](api) that can be incorporated into other projects
- [a command-line tool](cli) that allows to easily browse and download data

Its **documentation** is available at https://nbbrd.github.io/sdmx-dl/docs/.

### Why?

While being in the information age, it is surprisingly difficult to get free quality statistics, even from official sources.

There are many reasons to that situation:

- **Big tech:** the huge data collected by big tech companies are locked behind paywalls and the only bits freely available are often redacted to the point of being useless.
- **APIs:** NGOs and governmental agencies that adhere to the open data principles don't necessarily share a common design for their APIs and therefore require a separate development for each one.
- **Handling:** data retrieval often implies tedious and error-prone manual steps.
- **Native support:** there are several open standards available but few-to-none are natively supported by mainstream applications.
- **Catalog:** there are no central catalog to discover data sources.

sdmx-dl is a SDMX-focused attempt to tackle these problems.

### Goals & features

- Provides an **easy**, **consistent** and **reliable** data retrieval
- Allows **data discovery** and **automation**
- Takes care of technical difficulties and data handling
- Is designed to be used by other applications
- Is bundled with a [pre-configured set of data sources](sources)

## Installing

**sdmx-dl CLI** runs on any desktop operating system such as Microsoft **Windows**, **Solaris OS**, Apple **macOS**, **Ubuntu** and other various **Linux** distributions.
See [CLI installation](cli/installation) for more details.

## Developing

This project is written in Java and uses [Apache Maven](https://maven.apache.org/) as a build tool.  
It requires [Java 8 as minimum version](https://whichjdk.com/) and all its dependencies are hosted on [Maven Central](https://search.maven.org/).

The code can be build using any IDE or by just type-in the following commands in a terminal:
```shell
git clone https://github.com/nbbrd/sdmx-dl.git
cd sdmx-dl
mvn clean install
```

## Contributing

Any contribution is welcome and should be done through pull requests and/or issues.

## Licensing

The code of this project is licensed under the [European Union Public Licence (EUPL)](https://joinup.ec.europa.eu/page/eupl-text-11-12).
