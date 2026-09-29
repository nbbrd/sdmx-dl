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

Most tasks follow the same three steps, regardless of the flavor you use (new here? follow the [Getting started]({{< relref "/usage/getting-started" >}}) walkthrough):

1. [**Discover**]({{< relref "/usage/discover" >}}) - Find the source, database, and flow you want to query.
2. [**Browse**]({{< relref "/usage/browse" >}}) - Explore the flow's dimensions and codes to build a valid [key]({{< relref "/usage/browse#keys" >}}).
3. [**Retrieve**]({{< relref "/usage/retrieve" >}}) - Fetch observations and metadata for that key, optionally narrowed by period or observation count.

At any step, [monitor]({{< relref "/usage/monitor-and-status" >}}) a source's health before relying on it.