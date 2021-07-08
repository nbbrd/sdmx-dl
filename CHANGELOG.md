# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Add support of multiple media-types in HTTP request header (RI)
- Add support of media-type charset in HTTP response header (RI)
- Add response event `#onSuccess(MediaType)` (RI)
- Add default user-agent `sdmx-dl/version` (RI) [#117](https://github.com/nbbrd/sdmx-dl/issues/117)
- Add source from Bundesbank [#104](https://github.com/nbbrd/sdmx-dl/issues/104)
- Add source from Economic and Social Commission for Asia and the Pacific [#99](https://github.com/nbbrd/sdmx-dl/issues/99)
- Add source from National Statistical Institute of Cambodia [#98](https://github.com/nbbrd/sdmx-dl/issues/98)
- Add source from SDMX Global Registry [#97](https://github.com/nbbrd/sdmx-dl/issues/97)
- Add source from Pacific Data Hub [#96](https://github.com/nbbrd/sdmx-dl/issues/96)
- Add source from UK Data Service [#93](https://github.com/nbbrd/sdmx-dl/issues/93)

### Changed
- Enforce https on ABS source [#108](https://github.com/nbbrd/sdmx-dl/issues/108)
- Improve CLI version option [#79](https://github.com/nbbrd/sdmx-dl/issues/79)
- Refactor SdmxWebAuthenticator as an SPI

### Fixed
- Fix parsing of blank labels
- Update ILO source with new endpoint [#107](https://github.com/nbbrd/sdmx-dl/issues/107)
- Fix key parsing when time dimension is not last in data structure [#110](https://github.com/nbbrd/sdmx-dl/issues/110)
- Fix key validity check on input [#118](https://github.com/nbbrd/sdmx-dl/issues/118)
- Fix parsing of media types in SDMX21 driver

## [3.0.0-beta.2] - 2021-05-03

This is the second beta release of **sdmx-dl**.   
sdmx-dl follows [semantic versioning](http://semver.org/).

_Note that sdmx-dl is still in heavy development and might change a lot between versions so you shouldn't use it in production._

This release adds new sources, the support of attributes and modify the CLI commands.  
These command modifications are quite extended and concern [command names, overall structure and output](https://github.com/nbbrd/sdmx-dl/wiki/cli-usage).

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

_Note that sdmx-dl is still in heavy development and might change a lot between versions so you shouldn't use it in production._

### Added
- Initial release

[Unreleased]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.2...HEAD
[3.0.0-beta.2]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.1...v3.0.0-beta.2
[3.0.0-beta.1]: https://github.com/nbbrd/sdmx-dl/releases/tag/v3.0.0-beta.1
