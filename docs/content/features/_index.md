---
title: "Features"
weight: 6
# Note: sdmx-dl also ships a desktop GUI (sdmx-dl-desktop/sdmx-dl-swing) which is still in
# beta and intentionally not documented here yet; revisit once it stabilizes.
# The MCP server (sdmx-dl-grpc) has a short overview in /ws under "MCP endpoint" rather than
# per-feature examples, since its tools map 1-to-1 onto the WS operations described here.
---

**sdmx-dl Features** documents one capability per page, with examples for each flavor:

- Java library (`API`)
- Command-line tool (`CLI`)
- Web service (`WS`: REST + gRPC + [MCP]({{< relref "/ws#mcp-endpoint" >}}))

New here? Follow the steps below in order.

## How it works

Most tasks follow the same three steps, regardless of the flavor you use.

### Discover

Find the source and flow you want to query.

- [Discover sources]({{< relref "/features/discover-sources" >}}) - See which data providers are available, or find one by name or topic.
- [Discover databases]({{< relref "/features/discover-databases" >}}) - Find a database namespace within a source.
- [Discover flows]({{< relref "/features/discover-flows" >}}) - List the datasets published by a source, or find one by topic.

### Browse

Explore the flow's structure and narrow down valid keys.

- [Browse dimensions and attributes]({{< relref "/features/browse-structure" >}}) - List the components that make up a flow's structure.
- [Browse codes]({{< relref "/features/browse-codes" >}}) - Look up the labels behind a dimension's codes.
- [Browse availability]({{< relref "/features/browse-availability" >}}) - Narrow codes down to what's actually available under a key.

### Retrieve

Fetch metadata and observations for the key you've settled on.

- [Inspect metadata]({{< relref "/features/inspect-metadata" >}}) - Look up descriptive metadata for a series or flow.
- [Narrow your request]({{< relref "/features/narrow-your-request" >}}) - Restrict data to a date range and/or a limited number of observations.
- [Retrieve data]({{< relref "/features/retrieve-data" >}}) - Download observations for a source/flow/key.

## Operate

These aren't steps in the flow above - they apply throughout it, from the very first call to the last, whenever you run sdmx-dl reliably (interactively or unattended).

- [Authentication and credentials]({{< relref "/features/auth-and-credentials" >}}) - Configure access to protected sources, whenever a source needs it.
- [Monitor and status]({{< relref "/features/monitor-and-status" >}}) - Check a source's health before relying on it, at any point.
- [Output and formats]({{< relref "/features/output-and-formats" >}}) - Pick the right output shape per flavor, for any of the calls above.
