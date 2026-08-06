# PxWeb driver improvement report

**Date:** 2026-07-31
**Module:** `sdmx-dl-provider-px`
**Driver:** `PX_PXWEB`
**Method:** live end-to-end probing of all registered PxWeb sources with
`_demo.PxWebExplorer`, following the `evaluate-source` skill principles
(*trust live responses, verify the whole data path, capture evidence*).

## 1. Problem statement

The PxWeb driver was incomplete: many sources failed to list flows, fetch
metadata, or return data. Running `PxWebExplorer` against the ~40 registered
sources exposed the concrete failures below.

## 2. Root causes (all live-verified)

1. **`NullPointerException` while parsing metadata.**
   `TableVariable.deserialize` called `getAsJsonArray("values")` /
   `getAsJsonArray("valueTexts")` unconditionally and crashed whenever a
   variable omitted those arrays.

   ```
   java.lang.NullPointerException: Cannot invoke "com.google.gson.JsonArray.spliterator()" because "array" is null
       at sdmxdl.provider.px.drivers.GsonUtil.asStream(GsonUtil.java:46)
       at sdmxdl.provider.px.drivers.PxWebDriver$TableVariable.deserialize(PxWebDriver.java:817)
   ```

2. **Flow listing relied on the flat `?query=*&filter=*` search endpoint, which
   is fundamentally unreliable.**
   - Returns **HTTP 400** on several servers (SCB, LINKOPING, STATICE,
     SWITZERLAND, VASTERAS).
   - Returns a **stale index** on others (IRENA, LIECHTENSTEIN): the listed
     `id`/`path` no longer resolve.

     ```
     # IRENA search still lists a table that no longer exists:
     GET /api/v1/en/IRENASTAT?query=*&filter=*
     -> [{"id":"CAP_EN_FR_ES.px","path":"/Power Capacity and Generation", ...}]
     GET /api/v1/en/IRENASTAT/Power%20Capacity%20and%20Generation/CAP_EN_FR_ES.px
     -> HTTP 404
     ```

3. **The `path` returned by the search was discarded.**
   `Table.toDataflow()` built the flow ref from the table `id` only, so
   metadata/data requests hit `db/id` and 404'd for every nested table.

4. **PxWeb is a folder tree, not a flat catalog.**
   Each database exposes a tree of nodes typed `"l"` (level/folder) or `"t"`
   (table). Navigating the tree is the only listing that always yields
   reachable tables *with their correct paths*.

   ```
   GET /OV0104/v1/doris/en/ssd          -> [{"id":"BE","type":"l", ...}, ...]
   GET /OV0104/v1/doris/en/ssd/BE/BE0101/BE0101A
                                        -> [{"id":"BefolkManad","type":"t", ...}, ...]
   ```

## 3. Changes

All changes are in `sdmx-dl-provider-px`.

### `PxWebDriver.java`
- Added a **configurable table-listing strategy** (`sdmxdl.driver.px.tableListing`,
  enum `AUTO` | `FLAT` | `TREE`, default `AUTO`) that combines the fast flat
  search with the reliable tree navigation:
  - **`FLAT`** — single `?query=*&filter=*` query; tables addressed by id only
    (the original, fast behavior; the search `path` field is decorative and
    inconsistent across servers, so it is not used).
  - **`TREE`** — breadth-first folder-tree navigation (`collectTables` + `Node`
    DTO); each table keeps the full folder path needed for metadata/data.
  - **`AUTO`** — try `FLAT`, fall back to `TREE` when the search endpoint is
    unsupported (throws, e.g. HTTP 400) **or returns nothing**.
- Made metadata/data **path-aware**: the flow/structure ref encodes the relative
  table path (a single segment for flat/id-only tables, multiple segments for
  tree tables); requests rebuild it as individual URL-encoded path segments.
- Made tree navigation **resilient**: a failing *sub-folder* is skipped so it
  cannot abort the whole catalog, while a failing *root* listing propagates.
  Added a defensive `MAX_FOLDER_REQUESTS` bound against runaway/cyclic trees.
- `AUTO` cannot detect a *stale* search index (HTTP 200 with outdated entries);
  such sources are **pinned to `TREE`** in `api.json`.

### `api.json` + `PxWebSourcesFormat.java`
- Added an optional per-source `"listing"` field mapped to
  `TABLE_LISTING_PROPERTY`.
- Pinned the sources with a **known stale flat index** to `TREE`:
  `IRENA`, `LIECHTENSTEIN`, `GEOSTAT`.

### `GsonUtil.java`
- Added null-safe `getAsStringList(...)` (returns an empty list when the array
  is absent or JSON null) and used it in `TableVariable.deserialize` — fixes the
  NPE.

### Tests (`PxWebDriverTest.java` + fixtures)
- `testNodeDto` — node (level/table) parsing.
- `testSearchTableDto` — flat-search table parsing (id-only refs).
- `testSelectTables` — strategy dispatch: `FLAT`, `TREE`, and `AUTO`
  (flat-success, flat-empty→tree, flat-error→tree).
- `testTablePathConverter` — flow/structure ref path round-trips (incl. spaces).
- `testCollectTables` — tree navigation collects root and nested tables with
  their full path.
- `testCollectTablesIsBounded` — the defensive request bound is honored.
- `testCollectTablesSkipsUnreachableSubFolder` — a broken sub-folder is skipped.
- `testCollectTablesPropagatesRootFailure` — a broken root propagates.
- `testTableMetaWithMissingValues` — metadata with missing `values`/`valueTexts`
  parses instead of crashing.
- `testTableListingInBuiltInSources` — every source resolves a listing strategy;
  the stale-index sources are pinned to `TREE`.
- New fixtures: `statfin-nodes.json`, `statfin-search.json`,
  `statfin-table-meta-missing-values.json`.

## 4. Before / after (live `PxWebExplorer`)

| Source        | Before                          | After                                         |
|---------------|---------------------------------|-----------------------------------------------|
| SWITZERLAND   | FLOW_FAILURE (400)              | ✅ SUCCESS (AUTO → tree fallback)             |
| STATICE       | FLOW_FAILURE (400)              | ✅ SUCCESS (AUTO → tree fallback)             |
| IRENA         | META_FAILURE (404, stale index) | ✅ SUCCESS (pinned `TREE`)                    |
| GEOSTAT       | META_FAILURE (0 flows)          | pinned `TREE`: 1037 flows + structures (data 404 remains) |
| LINKOPING     | FLOW_FAILURE (0 flows)          | 135 flows + 5/5 structures (data 404 remains) |
| VASTERAS      | FLOW_FAILURE (0 flows)          | 3 flows + structures (data 404 remains)       |
| *all sources* | intermittent NPE in `getMeta`   | eliminated                                    |

Previously-working sources stayed working via the fast `FLAT` path (id-only refs
unchanged), and several are **markedly faster** than pure tree navigation
(e.g. `STAT_EE` ~7.5 s vs ~42 s, `ENERGIMYNDIGHETEN`/`ETK` ~2 s). Net result:
~29 fully green → **32 fully green**, plus 3 more that now list flows and
structures.

### Listing-strategy rationale
- The flat search **fails hard (HTTP 400)** on some servers → `AUTO` detects
  this and falls back to tree navigation.
- The flat search returns a **stale HTTP-200 index** on a few servers
  (`IRENA`, `LIECHTENSTEIN`, `GEOSTAT`) — undetectable at listing time, so those
  are pinned to `TREE` in `api.json`.
- Everywhere else, `AUTO` keeps the fast single-request flat listing.

## 5. Known remaining items (outside the scope of these bugs)

- **SCB / LIECHTENSTEIN**: first-run `TIMEOUT` — tree navigation is heavier on
  very large, rate-limited catalogs (results are cached afterwards). They failed
  immediately before anyway.
- **Data POST 404** on LINKOPING / VASTERAS / GEOSTAT and **400** on
  ASKDATA / STATFI / VGREGION — source-specific data-endpoint / key quirks.
- **GRANDE_REGION / UNECE / NHWSTAT / PODERJUDICIAL / SUNDSVALL / TELA** —
  endpoint/config issues (wrong base URL, auth, non-array root), unrelated to
  listing/metadata.
- Additional servers may also carry a stale flat index; they can be pinned to
  `TREE` via `"listing": "TREE"` in `api.json` as they are discovered.

## 6. Database-addressing investigation (single-database hypothesis)

Hypothesis: some remaining failures are single-database sources whose root does
not enumerate databases, so the database should be addressed directly (its id is
often the website URL segment after the language code, e.g.
`.../pxweb/en/H2/` → db `H2`).

Findings from live probing:

- The pattern is **real** and the website-URL trick is valid — but it is already
  handled: for single-db servers the driver's root listing returns that one db
  and everything works (`HAGSTOVAN`=`H2`, `ETK`=`ETK`, `GEOSTAT`=`Database`,
  `TAI`=`Andmebaas`, `STAT_HEL_FI`=`Nordstat`, …).
- It does **not** explain any of the remaining failures:

| Source        | `?config` | Root db-list      | Direct db path         | Diagnosis                                   |
|---------------|-----------|-------------------|------------------------|---------------------------------------------|
| SUNDSVALL     | —         | ✅ 200 (single db) | —                      | Works now; earlier failure was transient    |
| NHWSTAT       | ✅ 200     | ❌ 400 (empty)     | ❌ 400 (empty)          | Server rejects all GET navigation (lockdown) |
| PODERJUDICIAL | ✅ 200     | ❌ 400 (empty)     | ❌ 400 (empty)          | Server rejects all GET navigation (lockdown) |
| TELA          | ❌ 500/404 | ❌                 | ❌ 500 (`Tela`)         | API base dead/relocated                      |
| GRANDE_REGION | —         | ❌ connection drop | —                      | Connectivity/blocking                        |
| UNECE         | —         | ❌ 403             | —                      | Blocked / auth                               |

Conclusion: addressing the database directly was **verified not to help**
(NHWSTAT/PODERJUDICIAL db paths also return 400; TELA returns 500). These are
server-side lockdown / dead-endpoint / connectivity issues, so **no driver
change is warranted** for them. If a future single-db source is found whose root
genuinely 404s but whose db resolves directly, the cleanest fix would be an
optional per-source `database` property (mirroring the `listing` property) —
deferred until there is a source it actually fixes.

## 7. How to reproduce

```shell
# Fast unit tests
mvn test -pl sdmx-dl-provider-px -am -Pyolo

# Live end-to-end overview (network; beware of the on-disk cache)
#   run _demo.PxWebExplorer from sdmx-dl-standalone test classpath
```

