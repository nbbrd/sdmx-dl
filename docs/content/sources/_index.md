---
title: "Data sources"
weight: 5
---

**sdmx-dl** supports any [SDMX 2.1 RESTful web service](https://github.com/sdmx-twg/sdmx-rest/wiki) as well as a few variants.

{{< sources >}}

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
