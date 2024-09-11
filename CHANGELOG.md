# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- ![API] Add confidentiality property to WebSource [#518](https://github.com/nbbrd/sdmx-dl/issues/518)

### Changed

- ![SOURCE] Update ILO website URL [#780](https://github.com/nbbrd/sdmx-dl/issues/780)

### Fixed

- ![CLI] Fix sources file env variable in CLI [#779](https://github.com/nbbrd/sdmx-dl/issues/779)

## [3.0.0-beta.13] - 2024-09-06

This is the thirteenth beta release of **sdmx-dl**.  
sdmx-dl follows [semantic versioning](https://semver.org/).

This release improves the modularity of the API and continues to clean up the configuration.  
The experimental PxWebDriver has also been improved but is not yet ready for daily use.  
The OECD, INEGI and ILO endpoints have been updated.

> **Disclaimer**: sdmx-dl is still in development and is available <ins>for test only</ins>. **Do not use in
> production!**

### Added

- ![API] Move FileFormat and Persistence to API
- ![API] Add registry API to load custom web sources
- ![PROVIDER] Add support of databases in PxWebDriver
- ![PROVIDER] Add parsing of language in PxWebDriver
- ![PROVIDER] Add websites in PxWebDriver
- ![DESKTOP] Improve desktop UI
- ![GRPC] Add webservice transcoding in gRPC

### Changed

- ![API] Force use of screaming-snake-case pattern on driver ID
- ![PROVIDER] Rename source-file property from `sdmxdl.sources` to `sdmxdl.registry.sourcesFile`
- ![PROVIDER] Rename user-agent property from `http.agent` to `sdmxdl.driver.userAgent`
- ![PROVIDER] Rename dump-folder property from `sdmxdl.ri.web.dump.folder` to `sdmxdl.driver.dumpFolder`
- ![PROVIDER] Replace curl-backend property `sdmxdl.networking.curlBackend` with url-backend property `sdmxdl.networking.urlBackend`
- ![CLI] Replace `list drivers` command with `list plugins` command

### Fixed

- ![SOURCE] Update OECD source [#543](https://github.com/nbbrd/sdmx-dl/issues/543)
- ![SOURCE] Update INEGI source [#639](https://github.com/nbbrd/sdmx-dl/issues/639)
- ![SOURCE] Update ILO source [#690](https://github.com/nbbrd/sdmx-dl/issues/690)

## [3.0.0-beta.12] - 2023-10-16

This is the twelfth beta release of **sdmx-dl**.  
sdmx-dl follows [semantic versioning](https://semver.org/).

This release brings a more flexible configuration system: cache, network and language can be specified by source if
needed and environment variables are used as default values.  
The API has been refactored to make it both simpler and more flexible.  
The ECB endpoint has also been updated.

> **Disclaimer**: sdmx-dl is still in development and is available <ins>for test only</ins>. **Do not use in
> production!**

### Added

- ![API] Add support of partial ISO date/time in TimeInterval
- ![PROVIDER] Allow caching & networking configuration by environment
  variables [#516](https://github.com/nbbrd/sdmx-dl/issues/516)
- ![PROVIDER] Allow caching & networking configuration per source [#493](https://github.com/nbbrd/sdmx-dl/issues/493)

### Fixed

- ![PROVIDER] Fix file locking in cache

### Changed

- ![API] Refactor cache API [#500](https://github.com/nbbrd/sdmx-dl/issues/500)
- ![API] Refactor network API [#503](https://github.com/nbbrd/sdmx-dl/issues/503)
- ![API] Remove dialect API [#505](https://github.com/nbbrd/sdmx-dl/issues/505)
- ![API] Refactor listeners API [#506](https://github.com/nbbrd/sdmx-dl/issues/506)
- ![API] Handle languages per connection instead of per manager [#507](https://github.com/nbbrd/sdmx-dl/issues/507)
- ![API] Simplify naming and structure [#508](https://github.com/nbbrd/sdmx-dl/issues/508)
- ![API] Change pattern of drivers properties
- ![API] Improve support of ISO-8601 duration
- ![SOURCE] Update ECB endpoint [#495](https://github.com/nbbrd/sdmx-dl/issues/495)
- ![FORMAT] Refactor file format API [#502](https://github.com/nbbrd/sdmx-dl/issues/502)
- ![PROVIDER] Move curl backend to an external project
- ![PROVIDER] Move PxWebDriver to its own module
- ![PROVIDER] Move dialects drivers to their own module
- ![CLI] Use environment variables as default values
- ![GRPC] Migrate gRPC module to Quarkus framework

## [3.0.0-beta.11] - 2023-06-02

This is the eleventh beta release of **sdmx-dl**.  
sdmx-dl follows [semantic versioning](https://semver.org/).

This release adds new sources from Eurostat and the European Commission.
It also improves performance of several drivers alongside the usual bug fixes.

> **Disclaimer**: sdmx-dl is still in development and is available <ins>for test only</ins>. **Do not use in
> production!**

### Added

- ![API] Add feature descriptor `DATA_QUERY_ALL_KEYWORD`
- ![SOURCE] Add sources from Directorates General of the European
  Commission [#414](https://github.com/nbbrd/sdmx-dl/issues/414)
- ![SOURCE] Add source from Eurostat International trade in goods [#415](https://github.com/nbbrd/sdmx-dl/issues/415)

### Fixed

- ![PROVIDER] Fix URL squashing in curl backend [#417](https://github.com/nbbrd/sdmx-dl/issues/417)
- ![PROVIDER] Fix cache when two sources share the same host [#413](https://github.com/nbbrd/sdmx-dl/issues/413)

### Changed

- ![API] Improve request for available key codes
- ![SOURCE] Improve source from Economic and Social Commission for Asia and the
  Pacific [#418](https://github.com/nbbrd/sdmx-dl/issues/418)
- ![FORMAT] Use protobuf as default cache format
- ![PROVIDER] Improve Eurostat driver [#416](https://github.com/nbbrd/sdmx-dl/issues/416)
- ![PROVIDER] Add support of `DATA_QUERY_ALL_KEYWORD` feature in RI
- ![PROVIDER] Replace error by empty stream when no data is available

### Removed

- ![API] Remove feature descriptor `DATA_QUERY_KEY`

## [3.0.0-beta.10] - 2023-02-13

This is a bugfix release for the deployment of v3.0.0-beta.9.  
See v3.0.0-beta.9 for complete changelog.

### Fixed

- Fix missing javadoc resource

## [3.0.0-beta.9] - 2023-02-13

This is the ninth beta release of **sdmx-dl**.  
sdmx-dl follows [semantic versioning](https://semver.org/).

This release brings the API closer to the SDMX standard. It fixes an important problem with Eurostat. It also adds two
proof of concept for future use.

> **Disclaimer**: sdmx-dl is still in development and is available <ins>for test only</ins>. **Do not use in
> production!**

### Added

- ![API] Add support of time intervals in observations [#394](https://github.com/nbbrd/sdmx-dl/issues/394)
- ![PROVIDER] Add support of redirections in curl backend [#363](https://github.com/nbbrd/sdmx-dl/issues/363)
- ![DESKTOP] Add desktop application proof of concept [#401](https://github.com/nbbrd/sdmx-dl/issues/401)
- ![GRPC] Add gRPC service proof of concept [#402](https://github.com/nbbrd/sdmx-dl/issues/402)

### Changed

- ![API] Sort observations chronologically [#396](https://github.com/nbbrd/sdmx-dl/issues/396)
- ![API] Enforce non-null observations fields [#396](https://github.com/nbbrd/sdmx-dl/issues/396)
- ![API] Invert fields in `DataDetail` [#396](https://github.com/nbbrd/sdmx-dl/issues/396)
- ![API] Set default values to `DataQuery` [#396](https://github.com/nbbrd/sdmx-dl/issues/396)
- ![API] Rename field `Dimension#label` as `Dimension#name` [#395](https://github.com/nbbrd/sdmx-dl/issues/395)
- ![API] Rename field `Attribute#label` as `Attribute#name` [#395](https://github.com/nbbrd/sdmx-dl/issues/395)
- ![API] Rename field `DataStructure#label` as `DataStructure#name` [#395](https://github.com/nbbrd/sdmx-dl/issues/395)
- ![API] Rename field `SdmxWebSource#name` as `SdmxWebSource#id` [#395](https://github.com/nbbrd/sdmx-dl/issues/395)
- ![API] Rename field `SdmxWebSource#description`
  as `SdmxWebSource#name` [#395](https://github.com/nbbrd/sdmx-dl/issues/395)
- ![API] Set field `Dataflow#description` as optional [#395](https://github.com/nbbrd/sdmx-dl/issues/395)
- ![API] Set field `Obs#getPeriod` as `TimeInterval` [#394](https://github.com/nbbrd/sdmx-dl/issues/394)
- ![FORMAT] Improve parsing of time formats [#394](https://github.com/nbbrd/sdmx-dl/issues/394)
- ![FORMAT] Rename module `sdmx-dl-format-util ` as `smdx-dl-format-base`
- ![FORMAT] Rename module `sdmx-dl-provider-util ` as `smdx-dl-provider-base`

### Fixed

- ![FORMAT] Fix language consistency in names and descriptions [#397](https://github.com/nbbrd/sdmx-dl/issues/397)
- ![PROVIDER] Fix Eurostat endpoint [#346](https://github.com/nbbrd/sdmx-dl/issues/346)

## [3.0.0-beta.8] - 2022-11-22

This is the eighth beta release of **sdmx-dl**.  
sdmx-dl follows [semantic versioning](https://semver.org/).

This release brings support of description in data flows. It also updates a few sources alongside the usual bugfixes.

> **Disclaimer**: sdmx-dl is still in development and is available <ins>for test only</ins>. **Do not use in
> production!**

### Added

- ![API] Add field `Dataflow#description` [#287](https://github.com/nbbrd/sdmx-dl/issues/287)
- ![SOURCE] Add source from UN International Children's Emergency Fund [#95](https://github.com/nbbrd/sdmx-dl/issues/95)

### Changed

- ![API] Improve code coherence by replacing `Dataflow#of(...)` and `DataQuery#of(...)` with builders
- ![API] Rename field `Dataflow#label` as `Dataflow#name`
- ![API] Move `SdmxCubeUtil` from provider util to API
- ![SOURCE] Modify ISTAT endpoint [#339](https://github.com/nbbrd/sdmx-dl/issues/339)
- ![CLI] Modify `list/flows` command headers to follow `Dataflow` changes
- ![DOC] Enforce https in doc URL

### Fixed

- ![BUILD] Fix dependency inheritance in BOM

## [3.0.0-beta.7] - 2022-08-10

This is the seventh beta release of **sdmx-dl**.   
sdmx-dl follows [semantic versioning](https://semver.org/).

This release reshapes CLI commands, simplifies the network configuration and improves startup time.
It introduces a new dedicated documentation to replace the wiki.
It finalizes the support of SDMX time formats and attribute relationship in the API.
A new source is also added alongside the usual bugfixes.

> **Disclaimer**: sdmx-dl is still in development and is available <ins>for test only</ins>. **Do not use in
> production!**

### Added

- ![API] Add URL connection factory in Network
- ![API] Add `SdmxWebSource#monitorWebsite` field
- ![API] Add `SdmxWebSource#getDescription(LanguagePriorityList)` method
- ![API] Add support of attribute relationship [#81](https://github.com/nbbrd/sdmx-dl/issues/81)
- ![SOURCE] Add source from National Statistical Office of Thailand [#262](https://github.com/nbbrd/sdmx-dl/issues/262)
- ![FORMAT] Add support of time range in time formats
- ![PROVIDER] Add support of POST requests in HTTP backend
- ![PROVIDER] Add driver for International Monetary Fund source [#156](https://github.com/nbbrd/sdmx-dl/issues/156)
- ![CLI] Add `list/availability` command
- ![CLI] Add `MonitorWebsite` column in `list/sources` command
- ![CLI] Add `Languages` column in `list/sources` command [#266](https://github.com/nbbrd/sdmx-dl/issues/266)

### Changed

- ![PROVIDER] Improve error reporting on content-type in HTTP response header
- ![CLI] Use option instead of property to enable curl backend
- ![CLI] Remove cache folder option
- ![CLI] Replace position column with index column in `list/concepts` command
- ![CLI] Split command `list/concepts` into `list/dimensions` and `list/attributes`
- ![CLI] Improve startup time when using custom sources
- ![CLI] Remove `sdmx-dl-provider-connectors` dependency
- ![CLI] Remove custom name of shaded binary
- ![DOC] Migrate documentation to GitHub Pages [#268](https://github.com/nbbrd/sdmx-dl/issues/268)

### Fixed

- ![PROVIDER] Fix uptime parsing on multi-thread environment
- ![PROVIDER] Fix parsing of HTTP response header in curl backend
- ![PROVIDER] Fix NPE on missing HTTP response message
- ![PROVIDER] Fix `DataflowRef` validation
- ![PROVIDER] Fix Statistics Canada revisions [#252](https://github.com/nbbrd/sdmx-dl/issues/252)
- ![CLI] Fix missing charsets in native image
- ![CLI] Fix registration of system SSL in native image
- ![CLI] Fix usage of dependency-reduced pom

## [3.0.0-beta.6] - 2022-04-21

This is the sixth beta release of **sdmx-dl**.   
sdmx-dl follows [semantic versioning](https://semver.org/).

This release focuses on API refactoring to allow future improvements.
It introduces a mechanism that validates the input parameters to give a better feedback in case of error.
A few sources are also added.

> **Disclaimer**: sdmx-dl is still in development and is available <ins>for test only</ins>. **Do not use in
> production!**

### Added

- ![API] Add parameters validity check [#138](https://github.com/nbbrd/sdmx-dl/issues/138)
- ![API] Add multi-language descriptions in `SdmxWebSource` [#203](https://github.com/nbbrd/sdmx-dl/issues/203)
- ![SOURCE] Add source from El Salvador Labour Market Information
  System [#202](https://github.com/nbbrd/sdmx-dl/issues/202)
- ![SOURCE] Add source from Statistics Luxembourg [#245](https://github.com/nbbrd/sdmx-dl/issues/245)
- ![BUILD] Add Maven BOM

### Changed

- ![API] Refactor data queries [#218](https://github.com/nbbrd/sdmx-dl/issues/218)
- ![API] Refactor features discovery [#89](https://github.com/nbbrd/sdmx-dl/issues/89)
- ![API] Simplify class hierarchy [#222](https://github.com/nbbrd/sdmx-dl/issues/222)
- ![API] Simplify class naming scheme [#220](https://github.com/nbbrd/sdmx-dl/issues/220)
- ![API] Refactor dialects handling [#227](https://github.com/nbbrd/sdmx-dl/issues/227)
- ![API] Simplify module names
- ![API] Refactor format and provider utilities
- ![SOURCE] Enforce https on ISTAT source
- ![PROVIDER] Change `FileDriver` to be optional

### Fixed

- ![FORMAT] Fix missing series header in compact data [#172](https://github.com/nbbrd/sdmx-dl/issues/172)
- ![PROVIDER] Fix input validation in Statistics Canada driver [#171](https://github.com/nbbrd/sdmx-dl/issues/171)
- ![PROVIDER] Fix curl certificate revocation checks on missing/offline check lists
- ![CLI] Fix Java package requirement in Homebrew distribution [#206](https://github.com/nbbrd/sdmx-dl/issues/206)
- ![CLI] Fix use of reflection in native image
- ![CLI] Fix missing resources in native image

## [3.0.0-beta.5] - 2021-11-23

This is the fifth beta release of **sdmx-dl**.   
sdmx-dl follows [semantic versioning](https://semver.org/).

_Note that sdmx-dl is still in heavy development and might change a lot between versions, so you shouldn't use it in
production._

This release adds the support of most SDMX time formats. The execution time of CLI is reduced greatly by using a smarter
cache and by avoiding to load unnecessary resources. A few sources are also added, including a random data generator
that can be used for tests/demos.

### Added

- Add support of codelist in API [#158](https://github.com/nbbrd/sdmx-dl/issues/158)
- Add native driver for INSEE [#157](https://github.com/nbbrd/sdmx-dl/issues/157)
- Add native driver for Eurostat [#155](https://github.com/nbbrd/sdmx-dl/issues/155)
- Add second layer of cache in CLI
- Add availability check on driver SPI [#162](https://github.com/nbbrd/sdmx-dl/issues/162)
- Add optional RNG driver [#163](https://github.com/nbbrd/sdmx-dl/issues/163)
- Add cache-folder-path option in CLI
- Add source from Statistics Canada [#165](https://github.com/nbbrd/sdmx-dl/issues/165)
- Add support of non-calendar reporting periods

### Changed

- Refactor network API to avoid unnecessary resource loading
- Invert default activation of auto-proxy option in CLI
- Simplify name of auto-proxy option in CLI
- Replace multiple parameters with `DataRef` in API
- Change `SdmxWebSource#endpoint` type to URI
- Change `SdmxWebSource#monitor` type to URI
- Change cache default folder to `java.io.tmpdir/sdmx-dl/VERSION`
- Refactor parsing of SDMX time formats

### Fixed

- Improve error reporting in CLI
- Restore setup-completion command
- Fix setup commands names
- Fix datetime parsing of undefined freq
- Fix dimension ordering when used in key
- Fix external references in flows
- Migrate ABS source to the new server [#168](https://github.com/nbbrd/sdmx-dl/issues/168)

## [3.0.0-beta.4] - 2021-10-21

This is the fourth beta release of **sdmx-dl**.   
sdmx-dl follows [semantic versioning](https://semver.org/).

_Note that sdmx-dl is still in heavy development and might change a lot between versions, so you shouldn't use it in
production._

This release simplifies the CLI options and fixes web monitors by using
a [new self-hosted provider](https://nbbrd.github.io/sdmx-upptime/).  
The CLI binary is now also available in a [JBang catalog](https://github.com/nbbrd/jbang-catalog) and in
a [snapshot repository](https://s01.oss.sonatype.org/content/repositories/snapshots/com/github/nbbrd/sdmx-dl/sdmx-dl-cli/)
.

### Added

- Add a link to documentation in CLI [#137](https://github.com/nbbrd/sdmx-dl/issues/137)
- Add support of average response time in monitors [#144](https://github.com/nbbrd/sdmx-dl/issues/144)
- Add Maven deployment of CLI
- Add distribution to JBang [#141](https://github.com/nbbrd/sdmx-dl/issues/141)

### Changed

- Rename command check.properties as check.config [#136](https://github.com/nbbrd/sdmx-dl/issues/136)
- Migrate web monitors to Upptime [#142](https://github.com/nbbrd/sdmx-dl/issues/142)
- Refactor cache to handle web monitors
- Simplify CSV command options [#88](https://github.com/nbbrd/sdmx-dl/issues/88)

### Fixed

- Fix missing content in subsequent calls to fetch-meta command [#154](https://github.com/nbbrd/sdmx-dl/issues/154)
- Fix detection of console encoding

## [3.0.0-beta.3] - 2021-09-13

This is the third beta release of **sdmx-dl**.   
sdmx-dl follows [semantic versioning](https://semver.org/).

_Note that sdmx-dl is still in heavy development and might change a lot between versions, so you shouldn't use it in
production._

This release adds new sources and improves feedback on request parameters such as Key and Flow.  
The CLI binary is now available in some popular package managers.  
Finally, sdmx-dl advertises itself on web queries with the user-agent `sdmx-dl/3.0.0-beta.3`.

### Added

- Add support of multiple media-types in HTTP request header (RI)
- Add support of media-type charset in HTTP response header (RI)
- Add response event `#onSuccess(MediaType)` (RI)
- Add default user-agent `sdmx-dl/version` (RI) [#117](https://github.com/nbbrd/sdmx-dl/issues/117)
- Add source from Bundesbank [#104](https://github.com/nbbrd/sdmx-dl/issues/104)
- Add source from Economic and Social Commission for Asia and the
  Pacific [#99](https://github.com/nbbrd/sdmx-dl/issues/99)
- Add source from National Statistical Institute of Cambodia [#98](https://github.com/nbbrd/sdmx-dl/issues/98)
- Add source from SDMX Global Registry [#97](https://github.com/nbbrd/sdmx-dl/issues/97)
- Add source from Pacific Data Hub [#96](https://github.com/nbbrd/sdmx-dl/issues/96)
- Add source from UK Data Service [#93](https://github.com/nbbrd/sdmx-dl/issues/93)
- Add distribution to Scoop (Windows) [#83](https://github.com/nbbrd/sdmx-dl/issues/83)
- Add distribution to Homebrew (macOS & Linux) [#84](https://github.com/nbbrd/sdmx-dl/issues/84)

### Changed

- Enforce https on ABS source [#108](https://github.com/nbbrd/sdmx-dl/issues/108)
- Improve CLI version option [#79](https://github.com/nbbrd/sdmx-dl/issues/79)
- Refactor SdmxWebAuthenticator as an SPI
- Improve feedback on missing data flow [#123](https://github.com/nbbrd/sdmx-dl/issues/123)

### Fixed

- Fix parsing of blank labels
- Update ILO source with new endpoint [#107](https://github.com/nbbrd/sdmx-dl/issues/107)
- Fix key parsing when time dimension is not last in data structure [#110](https://github.com/nbbrd/sdmx-dl/issues/110)
- Fix key validity check on input [#118](https://github.com/nbbrd/sdmx-dl/issues/118)
- Fix parsing of media types in SDMX21 driver

## [3.0.0-beta.2] - 2021-05-03

This is the second beta release of **sdmx-dl**.   
sdmx-dl follows [semantic versioning](https://semver.org/).

_Note that sdmx-dl is still in heavy development and might change a lot between versions, so you shouldn't use it in
production._

This release adds new sources, the support of attributes and modify the CLI commands.  
These command modifications are quite extended and
concern [command names, overall structure and output](https://github.com/nbbrd/sdmx-dl/wiki/cli-usage).

### Added

- Add FileDriver to allow demo data
- Add initial support of native image
- Add file reading feedback
- Improve file performance in worst-case scenarios
- Add source from Bank for International Settlements (BIS)
- Add source from Norges Bank (NB)
- Add web service endpoint monitoring
- Add reading of attributes
- Add library deployment to Maven Central
- Add GPG signature of binaries
- Add [basic documentation](https://github.com/nbbrd/sdmx-dl/wiki/)

### Changed

- Modify CLI [command name, structure and output](https://github.com/nbbrd/sdmx-dl/wiki/cli-usage)

## [3.0.0-beta.1] - 2020-09-25

This is the first beta release of **sdmx-dl**.   
sdmx-dl follows [semantic versioning](https://semver.org/).

_Note that sdmx-dl is still in heavy development and might change a lot between versions, so you shouldn't use it in
production._

### Added

- Initial release

[Unreleased]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.13...HEAD
[3.0.0-beta.13]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.12...v3.0.0-beta.13
[3.0.0-beta.12]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.11...v3.0.0-beta.12
[3.0.0-beta.11]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.10...v3.0.0-beta.11
[3.0.0-beta.10]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.9...v3.0.0-beta.10
[3.0.0-beta.9]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.8...v3.0.0-beta.9
[3.0.0-beta.8]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.7...v3.0.0-beta.8
[3.0.0-beta.7]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.6...v3.0.0-beta.7
[3.0.0-beta.6]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.5...v3.0.0-beta.6
[3.0.0-beta.5]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.4...v3.0.0-beta.5
[3.0.0-beta.4]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.3...v3.0.0-beta.4
[3.0.0-beta.3]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.2...v3.0.0-beta.3
[3.0.0-beta.2]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.1...v3.0.0-beta.2
[3.0.0-beta.1]: https://github.com/nbbrd/sdmx-dl/releases/tag/v3.0.0-beta.1
[API]: https://img.shields.io/badge/-API-068C09
[BUILD]: https://img.shields.io/badge/-BUILD-e4e669
[CLI]: https://img.shields.io/badge/-CLI-F813F7
[DESKTOP]: https://img.shields.io/badge/-DESKTOP-F813F7
[GRPC]: https://img.shields.io/badge/-GRPC-F813F7
[FORMAT]: https://img.shields.io/badge/-FORMAT-5319E7
[PROVIDER]: https://img.shields.io/badge/-PROVIDER-BC0250
[SOURCE]: https://img.shields.io/badge/-SOURCE-E2BC4A
[DOC]: https://img.shields.io/badge/-DOC-e4e669
