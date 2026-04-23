# AGENTS.md — Easily download official statistics

## Overview

**sdmx-dl** is a Java library (and toolset) for easily downloading official statistics from [SDMX](https://sdmx.org) data sources.
It provides a unified, provider-agnostic API to access SDMX web services and file-based sources, abstracting away protocol and format differences.

Key capabilities:
- Retrieve flows, structures, series, and observations from any registered SDMX source
- Support for multiple data formats (SDMX-ML/XML, CSV, Protocol Buffers, Kryo)
- Extensible via SPI: drivers, monitors, authenticators, caching, networking, persistence
- Delivered as an embeddable library, a CLI tool, a Swing desktop app, and a gRPC server
- Licensed under [EUPL](https://joinup.ec.europa.eu/page/eupl-text-11-12); maintained by the [National Bank of Belgium](https://www.nbb.be)

## Architecture

The project is a Maven multi-module build organised in four layers:

### 1. API (`sdmx-dl-api`)

Core domain model and SPI contracts. All other modules depend on this.
- **Domain model**: `Key`, `Series`, `Obs`, `Flow`, `FlowRef`, `Structure`, `Codelist`, `DataSet`, `DataRepository`, …
- **Entry points**: `SdmxWebManager` (web sources) and `SdmxFileManager` (file sources)
- **Web SPI** (`sdmxdl.web.spi`): `Driver`, `Monitor`, `Registry`, `Networking`, `Authenticator`, `WebCaching`, `SSLFactory`, `URLConnectionFactory`
- **File SPI** (`sdmxdl.file.spi`): `Reader`, `FileCaching`
- **Extension SPI** (`sdmxdl.ext`): `Cache`, `Persistence`, `FileFormat`
- SPIs are discovered at runtime via Java `ServiceLoader`

### 2. Format (`sdmx-dl-format-*`)

Serialization/deserialization implementations, used by providers and caching:

| Module                    | Purpose                    |
|---------------------------|----------------------------|
| `sdmx-dl-format-base`     | Shared format utilities    |
| `sdmx-dl-format-xml`      | SDMX-ML (XML) parsing      |
| `sdmx-dl-format-csv`      | CSV output via picocsv     |
| `sdmx-dl-format-kryo`     | Kryo binary caching format |
| `sdmx-dl-format-protobuf` | Protocol Buffers format    |

### 3. Provider (`sdmx-dl-provider-*`)

`Driver` and `Monitor` implementations that connect to real data sources:

| Module                        | Purpose                                                                                                            |
|-------------------------------|--------------------------------------------------------------------------------------------------------------------|
| `sdmx-dl-provider-base`       | Shared provider utilities (REST client base, caching helpers)                                                      |
| `sdmx-dl-provider-ri`         | **Reference Implementation** — SDMX 2.1 REST drivers, Upptime/UptimeRobot monitors, HTTP networking, vault caching |
| `sdmx-dl-provider-dialects`   | Source-specific dialect adaptations on top of the RI                                                               |
| `sdmx-dl-provider-connectors` | Adapter wrapping legacy SDMX Connectors library                                                                    |
| `sdmx-dl-provider-px`         | PX (PC-Axis) file format provider                                                                                  |

### 4. Delivery

End-user and integration artifacts:

| Module               | Purpose                                                              |
|----------------------|----------------------------------------------------------------------|
| `sdmx-dl-cli`        | picocli-based CLI (`fetch`, `list`, `check`, `debug` command groups) |
| `sdmx-dl-desktop`    | Swing desktop GUI                                                    |
| `sdmx-dl-grpc`       | gRPC server exposing the API over the network                        |
| `sdmx-dl-standalone` | Self-contained fat-jar distribution bundling providers               |
| `sdmx-dl-bom`        | Maven Bill of Materials for consumers                                |


## Build & Test

```shell
mvn clean install                 # full build + tests + enforcer checks
mvn clean install -Pyolo          # skip all checks (fast local iteration)
mvn test -pl <module-name> -Pyolo # fast test a single module
mvn test -pl <module-name> -am    # full test a single module
```

- **Java 8 target** with JPMS `module-info.java` compiled separately on JDK 9+ (see `java8-with-jpms` profile in root POM)
- **JUnit 5** with parallel execution enabled (`junit.jupiter.execution.parallel.enabled=true`); **AssertJ** for assertions

## Key Conventions

- **Lombok**: use lombok annotations when possible. Config in `lombok.config`: `addNullAnnotations=jspecify`, `builder.className=Builder`
- **Nullability**: `@org.jspecify.annotations.Nullable` for nullable; `@lombok.NonNull` for non-null parameters. Return types use `@Nullable` or the `OrNull` suffix (e.g., `getThingOrNull`)
- **Design annotations** use annotations from `java-design-util` such as `@VisibleForTesting`, `@StaticFactoryMethod`, `@DirectImpl`, `@MightBeGenerated`, `@MightBePromoted`
- **Internal packages**: `internal.<project>.*` are implementation details; public API lives in the root and `spi` packages
- **Static analysis**: `forbiddenapis` (no `jdk-unsafe`, `jdk-deprecated`, `jdk-internal`, `jdk-non-portable`, `jdk-reflection`), `modernizer`
- **Reproducible builds**: `project.build.outputTimestamp` is set in the root POM
- **Formatting/style**: 
  - Use IntelliJ IDEA default code style for Java
  - Follow existing formatting and match naming conventions exactly
  - Follow the principles of "Effective Java"
  - Follow the principles of "Clean Code"
- **Java/JVM**: 
  - Target version defined in root POM properties; some modules may require higher versions
  - Use modern Java feature compatible with defined version

## Agent behavior

- Do respect existing architecture, coding style, and conventions
- Do prefer minimal, reviewable changes
- Do preserve backward compatibility
- Do not introduce new dependencies without justification
- Do not rewrite large sections for cleanliness
- Do not reformat code
- Do not propose additional features or changes beyond the scope of the task
