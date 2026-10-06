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
- **Script SPI** (`sdmxdl.script.spi`): `ScriptGenerator`, used by `ScriptManager` to turn a source id and a `Request` into a script for a language and a transport (CLI, REST, …)
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
| `sdmx-dl-script`              | `ScriptGenerator` implementations (Python over CLI and REST)                                                       |

### 4. Delivery

End-user and integration artifacts:

| Module               | Purpose                                                              |
|----------------------|----------------------------------------------------------------------|
| `sdmx-dl-cli`        | picocli-based CLI (`fetch`, `list`, `check`, `debug`, `script` command groups) |
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
- **JDK per module**: most modules build on JDK 11+; `sdmx-dl-grpc` (Quarkus, `release 17`) is only part of the reactor on JDK 17+ (`java17-modules` profile). Check `java -version` and point `JAVA_HOME` to a JDK 17+ before building or testing it
- **JUnit 5** with parallel execution enabled (`junit.jupiter.execution.parallel.enabled=true`); **AssertJ** for assertions
- **Spotless** (`ratchetFrom origin/develop`) runs `check` in the `validate` phase, so only changed files are checked and a violation fails the build before tests run. Fix with `mvn spotless:apply -pl <module-name>`
- **Slow and network tests**: tests tagged `webQueries` hit live sources and are excluded by default (enable with `-PwebQueries`). The `sdmx-dl-grpc` Quarkus tests start the server and take a few minutes; run them only when that module changes
- **Script syntax tests**: `sdmx-dl-script` always checks generated scripts with bundled tree-sitter grammars (python, bash, R, Java). Tests tagged `scriptSyntax` use the real parsers installed on the machine (Python, R, bash/ShellCheck, PowerShell, Node.js with `@microsoft/powerquery-parser`, JDK 25+) and skip missing ones; enable with `-PscriptSyntax` (set `SDMXDL_SCRIPT_SYNTAX_STRICT=true` to fail instead of skipping)
- **Generated files**: the `package` phase of `sdmx-dl-grpc` copies the generated `openapi.json` and `openapi.yaml` to `docs/assets/` (`mvn package -pl sdmx-dl-grpc -Pyolo -DskipTests`); commit them together with REST/MCP changes
- **Local artifacts** (after `mvn install -Pyolo`): `sdmx-dl-cli/target/sdmx-dl-cli-<version>-bin.jar` (run with `java -jar`) and `sdmx-dl-grpc/target/sdmx-dl-grpc-<version>-runner.jar` (gRPC, HTTP/REST and MCP on port 4559; override with `-Dquarkus.http.port=`). Use them to capture real outputs for docs

## Documentation

- `docs/` is a [Hugo](https://gohugo.io) site (extended edition) using the `hugo-geekdoc` theme; content lives in `docs/content/`
- Check the build with `hugo --quiet -d <temp-dir> --cleanDestinationDir` from `docs/`, then delete the temp dir; never build into `docs/public`
- Shortcodes: theme ones (`tabs`/`tab` with a unique group name per page, `expand`, `hint`, `relref`) and custom ones in `docs/layouts/shortcodes/` (`sdmx-dl-version`, `sources`, `shields_io`)
- When moving or merging pages, add `aliases` in the front matter to keep old URLs working
- Examples and output samples should come from real runs of the local artifacts, not be invented

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
- Do not reformat unrelated code; run `mvn spotless:apply -pl <module-name>` on modules you changed
- Do not propose additional features or changes beyond the scope of the task
