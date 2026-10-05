# Feature parity map

Hand-maintained input of `FeatureParity.java`. It tells the extractor how operations and parameters of each flavor
(`api`, `cli`, `grpc`, `rest`, `mcp`) correspond to each other, and which differences are intentional.
Names are compared case-insensitively, ignoring non-alphanumeric characters (`--last-n` = `last_n` = `lastN`).

<!--
Format (only the tables under the "## Operations", "## Parameters", "## Waivers" and "## Ignore" headings are read):

Operations: one row per canonical operation.
  - empty cell        -> the flavor operation with the same (normalized) name
  - name              -> operation name or display (e.g. `list flows`, `getCodes`, `GET /{source}/flows`)
  - a + b             -> the feature is achieved by combining several operations
  - n/a[: reason]     -> intentionally absent in this flavor

Parameters: aliases from a canonical parameter name to flavor names.
  - empty cell        -> same (normalized) name
  - a, b              -> alternative names
  - n/a[: reason]     -> intentionally absent in every operation of this flavor
  - `operation` column (optional) restricts the row to one operation; scoped rows win over global ones

Waivers: acknowledged differences for one parameter of one operation (`*` = any operation / flavor).
  - status `n/a`      -> parameter intentionally absent
  - status `default`  -> default value difference intentional

Ignore: elements excluded from the comparison (`kind` = operation | parameter).
  - pattern is a glob matched against the name, the display or `@DeclaringClass`

Rule of thumb: only mark something n/a or waived when it is a deliberate design choice; otherwise leave it as a gap.
-->

## Operations

| operation           | api                      | cli               | grpc | rest              | mcp    |
|---------------------|--------------------------|-------------------|------|-------------------|--------|
| about               | n/a: constants of About  | n/a: `--version`  | GetAbout | getAbout       |        |
| listSources         |                          | list sources      |      |                   |        |
| listDatabases       |                          | list databases    |      |                   |        |
| listFlows           |                          | list flows        |      |                   |        |
| getMeta             |                          | fetch meta        |      |                   |        |
| listDimensions      |                          | list dimensions   |      |                   |        |
| listAttributes      |                          | list attributes   |      |                   |        |
| listCodes           |                          | list codes        |      | getCodes          |        |
| listAvailability    |                          | list availability |      | getAvailability   |        |
| getData             |                          | fetch data        |      |                   |        |
| fetchKeys           | n/a: getData with detail=SERIES_KEYS_ONLY | fetch keys | n/a: getData with detail=SERIES_KEYS_ONLY | n/a: getData with detail=SERIES_KEYS_ONLY | n/a: getData with detail=SERIES_KEYS_ONLY |
| getDataStream       | n/a: transport-specific streaming variant of getData | n/a: transport-specific streaming variant of getData | | | n/a: transport-specific streaming variant of getData |
| listStatuses        | getMonitorReport         | check status      |      |                   | status |
| listFeatures        | getSupportedFeatures     | list features     |      |                   |        |
| checkAccess         | testConnection           | check access      |      |                   |        |
| listPlugins         |                          | list plugins      |      |                   |        |
| listScriptTargets   | getTargets               | script targets    |      |                   |        |
| generateDataScript  | generate + getData       | script data       |      |                   |        |
| generateFlowsScript | generate + listFlows     | script flows      |      |                   |        |

## Parameters

| parameter          | operation    | api                      | cli                    | grpc        | rest        | mcp     |
|--------------------|--------------|--------------------------|------------------------|-------------|-------------|---------|
| source             |              | source, sourceId         |                        |             |             |         |
| sources            | listStatuses | name, source             | source                 |             |             | source  |
| flow               |              |                          |                        |             |             |         |
| key                |              |                          |                        |             |             |         |
| database           |              |                          |                        |             |             |         |
| languages          |              |                          |                        |             |             |         |
| query              |              |                          |                        |             |             |         |
| maxResults         |              |                          |                        |             |             |         |
| maxConfidentiality |              | confidentialityThreshold |                        |             |             | n/a: MCP only exposes public sources |
| detail             |              |                          |                        |             |             |         |
| start              |              | startPeriod              |                        |             |             |         |
| end                |              | endPeriod                |                        |             |             |         |
| firstN             |              | firstNObservations       |                        |             |             |         |
| lastN              |              | lastNObservations        |                        |             |             |         |
| target             |              |                          |                        |             |             |         |
| cliLauncher        |              |                          |                        |             |             |         |
| restEndpoint       |              |                          |                        |             |             |         |
| outputFile         |              |                          | output                 |             |             |         |
| properties         |              |                          | property               |             | property    |         |

## Waivers

| operation | parameter  | flavor | status  | reason                                             |
|-----------|------------|--------|---------|----------------------------------------------------|
| *         | languages  | mcp    | default | single language keeps LLM responses small          |
| *         | maxResults | mcp    | default | bounded results keep LLM responses small           |
| *         | detail     | mcp    | default | data-only keeps LLM responses small                |
| *         | lastN      | mcp    | default | bounded observations keep LLM responses small      |
| *         | firstN     | mcp    | default | `0` means no limit                                 |
| listDatabases | database | *    | n/a     | databases are listed for a whole source            |
| listFeatures  | database | *    | n/a     | features are defined for a whole source            |
| listFeatures  | languages | *   | n/a     | features have no localized labels                  |
| checkAccess   | languages | *   | n/a     | access check has no localized labels               |
| listStatuses  | languages | *   | n/a     | statuses have no localized labels                  |
| fetchKeys | detail     | cli    | n/a     | `fetch keys` is the SERIES_KEYS_ONLY detail itself |

## Ignore

| flavor | kind      | pattern                    | reason                                           |
|--------|-----------|----------------------------|--------------------------------------------------|
| rest   | operation | @SdmxWebManagerService*    | legacy v1 REST service                           |
| api    | operation | warmupAsync                | infrastructure                                   |
| api    | operation | getConnection              | low-level connection, wrapped by Provider        |
| api    | operation | using                      | session accessor                                 |
| api    | operation | usingName                  | obtains the Provider bound to a source           |
| api    | operation | getOnEvent                 | listener accessor                                |
| api    | operation | getOnError                 | listener accessor                                |
| api    | operation | getSource                  | Provider accessor                                |
| api    | operation | getRequestTypes            | per-target detail of listScriptTargets           |
| api    | operation | getPropertyNames           | per-target detail of listScriptTargets           |
| api    | operation | isSupported                | per-target detail of listScriptTargets           |
| api    | parameter | request                    | carried by the combined data/flows operation     |
| cli    | operation | check config               | CLI configuration diagnostics                    |
| cli    | operation | check sources              | CLI configuration diagnostics                    |
| cli    | operation | setup *                    | CLI installation tooling                         |
| cli    | parameter | @NetworkingOptions         | client-side networking setup                     |
| cli    | parameter | @WebCachingOptions         | client-side caching setup                        |
| cli    | parameter | @AuthOptions               | client-side authentication setup                 |
| cli    | parameter | @VerboseOptions            | CLI logging                                      |
| cli    | parameter | @SortOptions               | CLI output formatting                            |
| cli    | parameter | --sources                  | custom sources file (server-side configuration) |
| cli    | parameter | --no-parallel              | CLI execution strategy                           |
