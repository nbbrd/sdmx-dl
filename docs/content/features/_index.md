---
title: "Features"
weight: 6
# Note: sdmx-dl also ships a desktop GUI (sdmx-dl-desktop/sdmx-dl-swing) which is still in
# beta and intentionally not documented here yet; revisit once it stabilizes.
# The MCP server (sdmx-dl-grpc) is beta but has a short overview in /ws under "MCP endpoint".
---

**sdmx-dl Features** documents one capability per page, with examples for each flavor:

- Java library (`API`)
- Command-line tool (`CLI`)
- Web service (`WS`: REST + gRPC)

New here? Follow the steps below in order. Already know what you need? Jump straight to the [capability matrix](#capability-matrix).

## How it works

Most tasks follow the same three steps, regardless of the flavor you use.

{{< shields_io/badge label="1" message="find" color="2aa198" >}}

Find the source and flow you want to query.

- [Discover sources]({{< relref "/features/discover-sources" >}}) - See which data providers are available.
- [Search sources]({{< relref "/features/search-sources" >}}) - Find a source by name or topic.
- [Discover flows]({{< relref "/features/discover-flows" >}}) - List the datasets published by a source.
- [Search flows]({{< relref "/features/search-flows" >}}) - Find a dataset by topic within a source.
- [Search databases]({{< relref "/features/search-databases" >}}) - Find a database namespace within a source.

{{< shields_io/badge label="2" message="shape" color="d33682" >}}

Understand the dataset and narrow down what you'll fetch.

- [Inspect metadata]({{< relref "/features/inspect-metadata" >}}) - Look up descriptive metadata for a series or flow.
- [Browse codes]({{< relref "/features/browse-codes" >}}) - Look up the labels behind a dimension's codes.
- [Check availability]({{< relref "/features/check-availability" >}}) - Narrow codes down to what's actually available under a key.
- [Narrow your request]({{< relref "/features/narrow-your-request" >}}) - Restrict data to a date range and/or a limited number of observations.

{{< shields_io/badge label="3" message="fetch" color="dc322f" >}}

Retrieve the data.

- [Retrieve data]({{< relref "/features/retrieve-data" >}}) - Download observations for a source/flow/key.

## Works with every step

{{< shields_io/badge label="cross-cutting" color="b58900" >}}

These aren't steps in the flow above - they apply throughout it, from the very first call to the last.

- [Authentication and credentials]({{< relref "/features/auth-and-credentials" >}}) - Configure access to protected sources, whenever a source needs it.
- [Monitor and status]({{< relref "/features/monitor-and-status" >}}) - Check a source's health before relying on it, at any point.
- [Output and formats]({{< relref "/features/output-and-formats" >}}) - Pick the right output shape per flavor, for any of the calls above.

## Capability matrix

{{% feature-matrix %}}
