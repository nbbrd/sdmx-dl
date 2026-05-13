# sdmx-dl - **easily download official statistics**<br>

[![Download](https://img.shields.io/github/release/nbbrd/sdmx-dl.svg)](https://github.com/nbbrd/sdmx-dl/releases/latest)
[![Changes](https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fnbbrd%2Fsdmx-dl%2Fbadges%2Funreleased-changes.json)](https://github.com/nbbrd/sdmx-dl/blob/develop/CHANGELOG.md)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/com/github/nbbrd/sdmx-dl/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/com/github/nbbrd/sdmx-dl/README.md)

**[sdmx-dl](https://github.com/nbbrd/sdmx-dl) is a tool designed to easily download official statistics.**  
It is mainly based on the [SDMX standard](https://sdmx.org/?page_id=3425) but can be extended with other APIs as well.

This project has three parts:
- [a Java library](https://nbbrd.github.io/sdmx-dl/docs/develop/api) that can be incorporated into other projects
- [a command-line tool](https://nbbrd.github.io/sdmx-dl/docs/develop/cli) that allows to easily browse and download data
- [a web service](https://nbbrd.github.io/sdmx-dl/docs/develop/ws) that serves as a bridge to any application or language

Its **documentation** is available at https://nbbrd.github.io/sdmx-dl/docs/.

## Why?

While being in the information age, it is surprisingly difficult to get free quality statistics, even from official sources.

There are many reasons to that situation:

- **Big tech lockup:** the huge data collected by big tech companies are locked behind paywalls and the only bits freely available are often redacted to the point of being useless.
- **Too many APIs:** NGOs and governmental agencies that adhere to the open data principles don't necessarily share a common design for their APIs and therefore require a separate development for each one.
- **Time-consuming handling:** data retrieval often implies tedious and error-prone manual steps.
- **No native support:** there are several open standards available but few-to-none are natively supported by mainstream applications.
- **No catalog:** there are no central catalog to discover data sources.

sdmx-dl is a SDMX-focused attempt to tackle these problems.

## Goals & features

- Provides an **easy**, **consistent** and **reliable** data retrieval
- Allows **data discovery** and **automation**
- Takes care of technical difficulties and data handling
- Is designed to be used by other applications
- Is bundled with a [pre-configured set of data sources](https://nbbrd.github.io/sdmx-dl/docs/develop/sources) covering major international organizations and national agencies (ECB, IMF, OECD, Eurostat, World Bank, ILO, BIS, INSEE, ...)

## Quick Start

**CLI** — download monthly CHF/EUR exchange rates from the ECB in one command ([JBang](https://www.jbang.dev/) required):
```shell
jbang sdmx-dl@nbbrd fetch data ECB EXR M.CHF.EUR.SP00.A
```

**Java library** — add the dependency to your `pom.xml`:
```xml
<dependency>
  <groupId>com.github.nbbrd.sdmx-dl</groupId>
  <artifactId>sdmx-dl-standalone</artifactId>
  <version>LATEST</version>
</dependency>
```
Then retrieve data in a few lines:
```java
SdmxWebManager
    .ofServiceLoader()
    .usingName("ECB")
    .getData(KeyRequest.builder()
        .flowOf("EXR")
        .keyOf("M.CHF+USD.EUR.SP00.A")
        .build())
    .forEach(series -> System.out.printf(Locale.ROOT, "%s: %d obs%n", series.getKey(), series.getObs().size()));
```

## Installing

**sdmx-dl CLI** runs on any desktop operating system such as Microsoft **Windows**, **Solaris OS**, Apple **macOS**, **Ubuntu** and other various **Linux** distributions.

| Platform                                                          | Command                                                                                    |
|-------------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| Windows ([Scoop](https://github.com/nbbrd/scoop-nbbrd))           | `scoop bucket add nbbrd https://github.com/nbbrd/scoop-nbbrd.git && scoop install sdmx-dl` |
| macOS / Linux ([Homebrew](https://github.com/nbbrd/homebrew-tap)) | `brew install nbbrd/tap/sdmx-dl`                                                           |
| Any ([JBang](https://www.jbang.dev/))                             | `jbang sdmx-dl@nbbrd <command> [<args>]`                                                   |

See [CLI installation](https://nbbrd.github.io/sdmx-dl/docs/develop/cli/installation) for all options including Docker, GitHub Actions, Maven, and Gradle.

## Developing

This project is written in Java and uses [Apache Maven](https://maven.apache.org/) as a build tool.  
It requires [Java 8 as minimum version](https://whichjdk.com/) and all its dependencies are hosted on [Maven Central](https://search.maven.org/).

The code can be built using any IDE or by just type-in the following commands in a terminal:
```shell
git clone https://github.com/nbbrd/sdmx-dl.git
cd sdmx-dl
mvn clean install
```

## Contributing

Any contribution is welcome and should be done through pull requests and/or issues.

## Licensing

The code of this project is licensed under the [European Union Public Licence (EUPL)](https://joinup.ec.europa.eu/page/eupl-text-11-12).

## Related work

This project is not the only one that deals with official statistics.  
Here is a non-exhaustive list of related work:

- [amattioc/SDMX](https://github.com/amattioc/SDMX) (SDMX / Java)
- [sosna/sdmx-rest4js](https://github.com/sosna/sdmx-rest4js) (SDMX / JavaScript)
- [dr-leo/pandaSDMX](https://github.com/dr-leo/pandaSDMX) (SDMX / Python)
- [rOpenGov/pxweb](https://github.com/rOpenGov/pxweb) (PXWEB / R)
- [ondata/opensdmx](https://github.com/ondata/opensdmx) (SDMX / Python)
