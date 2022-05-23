# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- ![API] Add URL connection factory in Network 
- ![API] Add `SdmxWebSource#monitorWebsite` field
- ![API] Add `SdmxWebSource#getDescription(LanguagePriorityList)` method
- ![PROVIDER] Add support of POST requests in HTTP backend
- ![CLI] Add `list/availability` command
- ![CLI] Add `MonitorWebsite` column in `list/sources` command

### Changed

- ![PROVIDER] Improve error reporting on content-type in HTTP response header
- ![CLI] Use option instead of property to enable curl backend
- ![CLI] Remove cache folder option
- ![CLI] Replace position column with index column in `list/concepts` command

### Fixed

- ![PROVIDER] Fix uptime parsing on multi-thread environment
- ![PROVIDER] Fix parsing of HTTP response header in curl backend
- ![PROVIDER] Fix NPE on missing HTTP response message
- ![PROVIDER] Fix `DataflowRef` validation
- ![PROVIDER] Fix Statistics Canada revisions [#252](https://github.com/nbbrd/sdmx-dl/issues/252)
- ![CLI] Fix missing charsets in native image
- ![CLI] Fix registration of system SSL in native image

## [3.0.0-beta.6] - 2022-04-21

This is the sixth beta release of **sdmx-dl**.   
sdmx-dl follows [semantic versioning](http://semver.org/).

This release focuses on API refactoring to allow future improvements.
It introduces a mechanism that validates the input parameters to give a better feedback in case of error.
A few sources are also added.

> **Disclaimer**: sdmx-dl is still in development and is available <ins>for test only</ins>. **Do not use in production!**

### Added

- ![API] Add parameters validity check [#138](https://github.com/nbbrd/sdmx-dl/issues/138)
- ![API] Add multi-language descriptions in `SdmxWebSource` [#203](https://github.com/nbbrd/sdmx-dl/issues/203)
- ![SOURCE] Add source from El Salvador Labour Market Information System [#202](https://github.com/nbbrd/sdmx-dl/issues/202)
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
sdmx-dl follows [semantic versioning](http://semver.org/).

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
sdmx-dl follows [semantic versioning](http://semver.org/).

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
sdmx-dl follows [semantic versioning](http://semver.org/).

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
sdmx-dl follows [semantic versioning](http://semver.org/).

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
sdmx-dl follows [semantic versioning](http://semver.org/).

_Note that sdmx-dl is still in heavy development and might change a lot between versions, so you shouldn't use it in
production._

### Added

- Initial release

[Unreleased]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.6...HEAD
[3.0.0-beta.6]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.5...v3.0.0-beta.6
[3.0.0-beta.5]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.4...v3.0.0-beta.5
[3.0.0-beta.4]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.3...v3.0.0-beta.4
[3.0.0-beta.3]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.2...v3.0.0-beta.3
[3.0.0-beta.2]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.1...v3.0.0-beta.2
[3.0.0-beta.1]: https://github.com/nbbrd/sdmx-dl/releases/tag/v3.0.0-beta.1

[API]: https://img.shields.io/badge/-API-068C09
[BUILD]: https://img.shields.io/badge/-BUILD-e4e669
[CLI]: https://img.shields.io/badge/-CLI-F813F7
[FORMAT]: https://img.shields.io/badge/-FORMAT-5319E7
[PROVIDER]: https://img.shields.io/badge/-PROVIDER-BC0250
[SOURCE]: https://img.shields.io/badge/-SOURCE-E2BC4A
