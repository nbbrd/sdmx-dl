---
title: "sdmx-dl"
---

{{< hint type="caution" >}}
sdmx-dl is still in development and is available <ins>for test only</ins>. **Do not use in production!**<br>
{{< /hint >}}


<img src="SDMX_logo.svg" alt="logo" width="200px" /><br>
sdmx-dl - **download SDMX data**<br>

[![Download](https://img.shields.io/github/release/nbbrd/sdmx-dl.svg)](https://github.com/nbbrd/sdmx-dl/releases/latest)

## What is sdmx-dl?

[SDMX](https://sdmx.org/?page_id=3425) is a standard for exchanging statistical data and [sdmx-dl](https://github.com/nbbrd/sdmx-dl) is a tool to download that data.

This project has two parts:
- [a Java library](api) that can be incorporated into other projects
- [a command-line tool](cli) that allows to easily browse and download data

## Why sdmx-dl?

SDMX is a standard but has the following issues:
- Slightly different interpretation from each provider
- Incomplete; doesn't' cover some technical details
- No native support in mainstream applications (Excel, RStudio, …)

## Goals & features

- Provides easy and reliable **data retrieval**
- Allows **data discovery** and **automation**
- Corrects deviations from the standard
- Takes care of technical difficulties
- Is designed to be integrated into other applications

## Acknowledgement & licence

This project is licenced as [EUPL-1.2](https://joinup.ec.europa.eu/page/eupl-text-11-12) and uses the following open-source libraries:
- https://github.com/amattioc/SDMX (EUPL-1.2)
- https://github.com/remkop/picocli (Apache-2.0)
- https://github.com/Hakky54/sslcontext-kickstart (Apache-2.0)
- https://github.com/EsotericSoftware/kryo (BSD-3-Clause)
- https://github.com/Tuupertunut/PowerShellLibJava (MIT)
