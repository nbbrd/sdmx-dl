---
name: feature-parity

description: >-
  Document the current feature parity (operations and parameters) between the sdmx-dl flavors
  API, CLI, gRPC, REST and MCP, without changing any code. Use when the user asks about feature
  parity, missing operations or parameters, or inconsistencies between flavors.
---

# Feature parity report

A deterministic extractor (`FeatureParity.java`) parses the sources, matches operations and parameters
with the hand-maintained [parity map](parity-map.md) and writes a Markdown report.
Your job is to run it and summarize the result; **do not read the flavor sources yourself** unless the user
asks you to investigate a specific finding.

Scope: `sdmx-dl-api` (`SdmxWebManager`, `Provider`, `ScriptManager`), `sdmx-dl-cli` (picocli commands),
`sdmx-dl-grpc` (`sdmxdl_grpc_v2.proto`, JAX-RS REST resources, MCP `@Tool` methods). The desktop app is out of scope.

## Rules

- Never modify source code. The report is written to the **session files folder only**, never in the repository.
- Only edit `parity-map.md` when the user explicitly agrees, and only mark something `n/a` or waived when it is
  a deliberate design choice confirmed by the user; otherwise it stays a gap.

## Steps

1. Find a JDK 17+ (`java -version`; otherwise `JAVA_HOME` or a JDK under `~/.jdks`). No Maven build is needed.
2. Run from the repository root:

   ```shell
   <jdk17+>/bin/java .agents/skills/feature-parity/FeatureParity.java --out <session-files>/feature-parity-<yyyy-MM-dd>.md
   ```

   Options: `--repo <dir>` (default `.`), `--map <file>` (default: the map next to the script),
   `--inventory <file>` (raw extraction per flavor, useful to debug the extractor or the map).
3. Read only the report sections down to `## Parity map maintenance`; open the matrices (`## Operations`,
   `## Parameters by operation`) only for details on a specific finding.
4. Reply briefly:
   - one line per flavor from `## Summary`;
   - the gaps (missing operations, missing parameters, default value differences), grouped and deduplicated;
   - single-flavor parameters that look like naming mismatches, unmapped operations and map maintenance items,
     each with a proposed `parity-map.md` change (alias, row, waiver or ignore);
   - the path of the report file.

## Map maintenance

The format is documented in a comment at the top of [parity-map.md](parity-map.md). Typical fixes:

| Report item                                     | Map change                                                               |
|-------------------------------------------------|--------------------------------------------------------------------------|
| Unmapped operation (new feature)                | add a row in `## Operations`                                             |
| Same parameter with different names             | add an alias in `## Parameters` (use the `operation` column to scope it) |
| Intentional absence or default difference       | add a row in `## Waivers`                                                |
| Infrastructure option/operation (not a feature) | add a row in `## Ignore`                                                 |
| Stale entry in `## Parity map maintenance`      | rename or remove the entry                                               |

If the extractor itself fails (e.g., a new annotation style), report the error and the `--inventory` output
instead of guessing; fixing `FeatureParity.java` is a separate task.
