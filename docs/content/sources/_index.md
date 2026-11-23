---
title: "Data sources"
weight: 8
---

**sdmx-dl** supports any [SDMX 2.1 RESTful web service](https://github.com/sdmx-twg/sdmx-rest/wiki) as well as a few variants.

{{< sources >}}

## Source icons

Each source can have an icon, for example to show it in a user interface (see [Discover]({{< relref "/usage/discover#icons" >}}) to get it).
Icons never come from the web at runtime nor from third-party favicon services: they are either **bundled** in the provider jars or **declared** with the source.
Sources without an icon fall back to the SDMX logo in the web service and the desktop application.

A custom source, defined in the file of the `sdmxdl.registry.sourcesFile` [execution property]({{< relref "cli/configuration#execution-properties" >}}), declares its icon in an `icon` element:

- a [`data:` URI](https://developer.mozilla.org/en-US/docs/Web/URI/Reference/Schemes/data) of a PNG or SVG image, so that confidential sources keep their icon local (SVG images with scripts, foreign objects or external references are rejected);
- or `sdmx-dl:icon/<name>` to reuse an icon bundled with sdmx-dl (e.g. `sdmx-dl:icon/ECB`).

```xml
<sources>
  <source>
    <name>MY_SOURCE</name>
    <description lang="en">My internal source</description>
    <driver>RI_SDMX21</driver>
    <confidentiality>RESTRICTED</confidentiality>
    <endpoint>https://sdmx.example.org/rest</endpoint>
    <icon>data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxNiAxNiI+PGNpcmNsZSBjeD0iOCIgY3k9IjgiIHI9IjciIGZpbGw9IiNlMzA2MTMiLz48L3N2Zz4=</icon>
  </source>
</sources>
```

## Try it out with a mock data source

The sources listed above are enabled by default, but sdmx-dl also ships a
**mock data source** that is disabled by default and therefore not listed:
it produces clearly-labeled, obviously fictional data — safe to explore
without confusing it with real statistics, and handy for demos or for testing
your own tooling against sdmx-dl.

Enable it with the `enableMockDriver` [execution property]({{< relref "cli/configuration#execution-properties" >}}), then list its built-in presets:
```shell
sdmx-dl list sources -DenableMockDriver=true
```

Available presets:

| Preset                   | Purpose                                                           |
|--------------------------|-------------------------------------------------------------------|
| `MOCK_SMALL`             | Small, credible-looking demo dataset (GDP, unemployment, CPI, FX) |
| `MOCK_LARGE`             | Larger dataset for performance/stress testing                     |
| `MOCK_EDGE`              | Edge-case data (missing observations, provisional flags, ...)     |
| `MOCK_QUIRKS_SLOW`       | Simulates slow-but-successful responses                           |
| `MOCK_QUIRKS_TIMEOUT`    | Simulates requests that never complete                            |
| `MOCK_QUIRKS_ERRORS`     | Simulates intermittent failures                                   |
| `MOCK_QUIRKS_RATE_LIMIT` | Simulates rate-limiting behavior                                  |
| `MOCK_QUIRKS_MALFORMED`  | Simulates semantically broken payloads                            |

Every series returned by the mock source carries a `MOCK=true` metadata entry
so it can be detected programmatically.
