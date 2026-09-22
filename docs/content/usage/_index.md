---
title: "Usage"
weight: 1
# Note: sdmx-dl also ships a desktop GUI (sdmx-dl-desktop/sdmx-dl-swing) which is still in
# beta and intentionally not documented here yet; revisit once it stabilizes.
# The MCP server (sdmx-dl-grpc) has a short overview in /ws under "MCP endpoint" rather than
# per-feature examples, since its tools map 1-to-1 onto the WS operations described here.
---

**sdmx-dl** features are available in all flavors. Pick the flavor that best fits your workflow:

- [Java library]({{< relref "/api" >}}) - Use as a dependency in your Java project.
- [Command-line tool]({{< relref "/cli" >}}) - Run from a terminal or script, without writing any code.
- [Web service]({{< relref "/ws" >}}) - Use from any language or application that can make HTTP or gRPC calls, including AI assistants and agents.

Most tasks follow the same three steps, regardless of the flavor you use:  
[discovery](#discovery) → [browsing](#browsing) → [retrieval](#retrieval).

## Discovery

Find the source and flow you want to query.

- [Discover sources]({{< relref "/usage/discover-sources" >}}) - See which data providers are available, or find one by name or topic.
- [Discover databases]({{< relref "/usage/discover-databases" >}}) - Find a database namespace within a source.
- [Discover flows]({{< relref "/usage/discover-flows" >}}) - List the datasets published by a source, or find one by topic.

## Browsing

Explore the flow's structure and narrow down valid keys.

- [Browse dimensions and attributes]({{< relref "/usage/browse-structure" >}}) - List the components that make up a flow's structure.
- [Browse codes]({{< relref "/usage/browse-codes" >}}) - Look up the labels behind a dimension's codes.
- [Browse availability]({{< relref "/usage/browse-availability" >}}) - Narrow codes down to what's actually available under a key.

## Retrieval

Fetch metadata and observations for the key you've settled on.

- [Inspect metadata]({{< relref "/usage/inspect-metadata" >}}) - Look up descriptive metadata for a series or flow.
- [Narrow your request]({{< relref "/usage/narrow-your-request" >}}) - Restrict data to a date range and/or a limited number of observations.
- [Retrieve data]({{< relref "/usage/retrieve-data" >}}) - Download observations for a source/flow/key.

## Troubleshooting

Check the health of sources and flows, and get help when things go wrong.

- [Monitor and status]({{< relref "/usage/monitor-and-status" >}}) - Check a source's health before relying on it, at any point.
