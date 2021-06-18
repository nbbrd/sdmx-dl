# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Add custom resource path in SDMX21 driver
- Add source from Bundesbank [#104](https://github.com/nbbrd/sdmx-dl/issues/104)
- Add support of multiple media types in HTTP requests
- Add basic alternative to RI REST client on Windows
- Add 'OnSuccess' event in RI REST client

### Changed
- Enforce https on ABS source [#108](https://github.com/nbbrd/sdmx-dl/issues/108)

### Fixed
- Fix parsing of blank labels
- Update ILO source with new endpoint [#107](https://github.com/nbbrd/sdmx-dl/issues/107)
- Fix key parsing when time dimension is not last in data structure [#110](https://github.com/nbbrd/sdmx-dl/issues/110)

## [3.0.0-beta.2] - 2021-05-03

This is the second beta release of **sdmx-dl**.   
sdmx-dl follows [semantic versioning](http://semver.org/).

_Note that sdmx-dl is still in heavy development and might change alot between versions so you shouldn't use it in production._

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

_Note that sdmx-dl is still in heavy development and might change alot between versions so you shouldn't use it in production._

### Added
- Initial release

[Unreleased]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.2...HEAD
[3.0.0-beta.2]: https://github.com/nbbrd/sdmx-dl/compare/v3.0.0-beta.1...v3.0.0-beta.2
[3.0.0-beta.1]: https://github.com/nbbrd/sdmx-dl/releases/tag/v3.0.0-beta.1
