# Evaluation: Instituto Nacional de Estadística (INE)

> **Status**: Implemented — `IneDialectDriver` in `sdmx-dl-provider-dialects`  
> **Date**: 2026-06-24 (evaluated), 2026-06-26 (implementation corrections), 2026-07-30 (co-occurrence dimension model + random-sampling validation), 2026-07-31 (flow description for same-name tables)  
> **Scope**: Tempus3 tables only (v1); PC-Axis and tpx excluded  
> **Flow granularity**: Tables (not Operations)

---

> ## ⚠️ Corrections after live implementation (2026-06-26)
>
> Several assumptions in the original evaluation turned out to be **wrong** when verified against
> the live API. The driver was implemented against the corrected behaviour described below.
>
> 1. **Variable identity is exposed differently per endpoint.**
>    - `SERIES_TABLA?tip=M&det=1` exposes the variable as a flat integer field **`FK_Variable`**
>      (it is *populated*, not empty), with **no nested `Variable` object**.
>    - `DATOS_TABLA?tip=AM` exposes the variable as a string field **`T3_Variable`** (its name),
>      with **no** `FK_Variable` and no id.
>    The original claim that `det=1` returns a nested `Variable` sub-object and that `FK_Variable`
>    is empty is incorrect.
> 2. **Both structure and data are built from `DATOS_TABLA?tip=AM`.** Using a single endpoint keeps
>    `getMeta` and `getData` consistent: dimensions are discriminated by the `T3_Variable` name and
>    ordered deterministically (natural order of the name). `getMeta` adds `nult=1` to cap the
>    payload to one observation per series.
> 3. **`tip=A` (friendly, no metadata) cannot be used for data**: it omits the `MetaData` array, so
>    series keys cannot be reconstructed. `tip=AM` is required.
> 4. **Codes are the value `Id`, not `Codigo`.** The value `Codigo` collides across variables
>    (e.g. `"00"` is reused), so it cannot be used as a key component.
> 5. **`T3_Periodo` IS parsed.** `DATOS_TABLA` does not carry a periodicity id, so the observation
>    granularity is derived from the `T3_Periodo` token (`M12`, `QI`, `SII`, …); `Fecha` provides the
>    period start.
> 6. **A table can be reachable by id but not listed under its operation.** Example: `DATOS_TABLA/50902`
>    works, yet `TABLAS_OPERACION/IPC` does not list `50902`. Since `getMeta` validates the flow against
>    `getFlows(database)`, only tables actually returned by `TABLAS_OPERACION` resolve. Table `24077`
>    is a confirmed working IPC example.
> 7. **`tv=` server-side filtering is not implemented in v1.** `getData` fetches the whole table and
>    filters in memory; this avoids the HTTP 500 returned for unknown value ids.
> 8. **Dimensions are derived by CO-OCCURRENCE, not by name and not by position.** This is the single
>    most important correction; it went through three iterations, each disproved by random sampling of
>    live tables (see the `testRandomTablesInvariants` harness):
>    - *Name-based grouping* (one dimension per distinct variable name) over-splits tables that use
>      several **mutually-exclusive** variables for one concept (e.g. `76136` geography =
>      `Regional totals` **or** `Autonomous Communities and Cities`; `30656` territory = `Districts`
>      **or** `Municipalities` **or** `Sections`). The resulting DSD has phantom dimensions and **no
>      real series key can exist** — the reported failures for `76136` and `30656`.
>    - *Positional grouping* (one dimension per `MetaData` slot index) is also wrong: INE does **not**
>      return `MetaData` in a consistent order across series (confirmed on `36641`, `73035`, `71109`).
>    - **Final model — co-occurrence signature.** Each `MetaData` entry is an *item* = (variable name,
>      occurrence rank within the series). Two items that share the exact same set of co-occurring items
>      provably never co-occur, so they are alternative breakdowns of one dimension and are merged; the
>      dimension codelist is the union of their value Ids. This is order-independent and handles all
>      observed cases. Dimensions are ordered by name so `getMeta`/`getData` always agree.
>    - The **occurrence rank** matters because INE reuses the same variable name several times in one
>      series for different roles (e.g. `20252` has `Type of marriage dissolution` twice). Keying by
>      name alone overwrote one value and collapsed distinct series onto one key; the rank keeps them
>      apart.
>    - A `_Z` ("not applicable") code is kept as a defensive fallback for a dimension a series does not
>      carry; an empty component must never be emitted because `Key` treats it as a wildcard.
> 9. **Structure and data MUST come from one fetch.** INE can return different variable **labels** for
>    the same table between two calls (observed on `70712`: `Salary/Labour Line Items` vs
>    `Wage/labour concepts`, and between `nult=1` and full responses). Different labels change the
>    grouping/order, so a separate `nult=1` structure fetch could misalign with the full data fetch.
>    The driver now builds both the `Structure` and the `DataSet` from a **single** `DATOS_TABLA?tip=AM`
>    response cached as one entry (`getTable`). The `nult=1` optimization was dropped; since `getData`
>    needs the full response anyway, this is one fetch instead of two for the common case.
> 10. **`DATOS_TABLA` can answer HTTP 200 with a status object while it computes a table**, e.g.
> 	`{"status":"Peticion en proceso. Actualice pagina pasados unos minutos."}`. The driver detects a
> 	non-array body and raises a clean, retryable `IOException` instead of an opaque
> 	`JsonSyntaxException`.
> 11. **Table `Nombre` is NOT unique within an operation, so flows need a `description`.** Many operations
> 	return several tables sharing an identical name — e.g. **all 9 `IPS` tables** are
> 	`"Services sector price index by sectors"`. `TABLAS_OPERACION` exposes the discriminating fields
> 	(`T3_Periodicidad` = `Quarterly`/`Annual`, `Codigo` = base-year/classification variant such as
> 	`2015_NAC` vs `2015_NAC_M` vs `2021-CNAE2009_NAC`, `T3_Publicacion`, and the covered period
> 	`T3_Periodo_ini`/`Anyo_Periodo_ini`…`T3_Periodo_fin`/`Anyo_Periodo_fin`). The driver originally
> 	dropped all of these (parsing only `Id` + `Nombre`), leaving `28481` and `28482` indistinguishable.
> 	`toFlow` now sets `Flow.description` to a `·`-joined summary (frequency · variant code · covered
> 	period · publication when it differs from the name). **Limitation**: some tables remain identical
> 	even across *all* table-level metadata (e.g. `79673`, `59973`, `67159`: same name, `Codigo`,
> 	periodicity and start period); only the FlowRef id and their actual DSD/content separate those.
>
> **Validation approach.** Confidence does not come from a few hand-picked tables (they hid all of the
> above). `IneDialectDriverTest#testRandomTablesInvariants` randomly samples many live tables and, for
> each, asserts the invariants that MUST hold if the model is correct: key size == dimension count,
> every key fully specified, every code known to the DSD (`Key.validateOn`), keys unique across the
> table, and each key round-trips to exactly its own series. Each iteration of the model above was
> rejected by this harness before the co-occurrence model passed it.
>
> **Cost control.** Whole tables are downloaded and their size is unknown before the fetch, so the sweep
> is bounded by two time budgets: `-Dine.perTableSeconds` cancels a table that is too slow to
> fetch/process (a proxy for "too large") and reports it as `SKIP-oversized`; `-Dine.budgetSeconds`
> caps the overall wall-clock time. Sampling is also bounded by `-Dine.maxTables` / `-Dine.tablesPerDb`
> and is reproducible via `-Dine.seed`. Example:
> `mvn test -pl sdmx-dl-provider-dialects -Pyolo,webQueries -Dtest=IneDialectDriverTest#testRandomTablesInvariants -Dine.maxTables=40 -Dine.perTableSeconds=15 -Dine.budgetSeconds=180`.

---

## Source metadata

| Field | Value |
|-------|-------|
| **ID** | `INE` |
| **Driver** | *New custom driver — `IneDriver` in `sdmx-dl-provider-dialects` (see design below)* |
| **Base endpoint** | `https://servicios.ine.es/wstempus/js/EN` |
| **Website** | `https://www.ine.es` |
| **API documentation** | `https://ine.es/OpenAPI/en/index.html` |
| **OpenAPI spec** | `https://ine.es/OpenAPI/includes/files/en/wstempus.yaml` (v1.6.23-5, OpenAPI 3.1.0) |
| **Reference client** | `ineapir` R package v0.2.5 — `https://es-ine.github.io/ineapir/` |
| **Languages** | `es`, `en` (two parallel server base URLs) |
| **Confidentiality** | `PUBLIC` |
| **License** | CC BY 4.0 |
| **Authentication** | None |

> **Note on ID**: `INE` is unambiguous in the context of sdmx-dl. `INEGI` (already registered) is
> Mexico's statistics institute; Spain's INE is a different organisation.

---

## Background

INE (Instituto Nacional de Estadística) is Spain's National Statistics Institute. Its data portal
[INEbase](https://www.ine.es/inebmenu/queesinebase.htm) publishes official statistics through a
proprietary JSON REST API called **WSTempus**, which is **not SDMX**.

INEbase has three distinct back-ends:

| Back-end | Identifier form | Scope |
|---|---|---|
| **Tempus3** | Numeric integer (e.g., `50902`) | Structured time series — **v1 scope** |
| **PC-Axis** | Path string (e.g., `t20/e245/p08/l0/01001.px`) | Static tabular files — excluded v1 |
| **tpx** | Numeric integer (different namespace) | Extended PC-Axis — excluded v1 |

---

## Why a new driver is required

The INE API is completely proprietary. None of the existing drivers can be reused:

- **No SDMX protocol** — responses are JSON, not SDMX-ML/XML
- **No pre-built DSD** — data structures must be assembled at query time from variable/value metadata
- **No structured series key** — series are identified by an internal numeric `Id` and an alphanumeric
  `COD` (e.g., `IPC251856`), not by a positional SDMX key
- **Language selection by server path** — `/EN/` vs `/ES/` base URL, not `Accept-Language`
- **Pagination** — list endpoints return up to 500 items per page

---

## Probe results (live, confirmed 2026-06-24)

| Check | Result |
|-------|--------|
| Reachability | ✅ HTTP 200 |
| Protocol | ❌ Not SDMX — proprietary JSON REST API |
| `ver=3` query parameter (used by `ineapir`) | ⚠️ **Not required** — identical results without it |
| Total operations | 112, fit in one page (pagination not needed in practice) |
| Tables per operation (sampled) | EI=12, ICLA=12, ETDP=17, CNTR2010=11, IPC=59 |
| Estimated total Tempus3 tables | ~1 000–2 000 |
| Response format | `application/json;charset=UTF-8` |
| Authentication required | ✅ None |

---

## API overview

### Servers

| Language | Base URL |
|---|---|
| English | `https://servicios.ine.es/wstempus/js/EN` |
| Spanish | `https://servicios.ine.es/wstempus/js/ES` |

### Core endpoints used by the driver

| Endpoint | Purpose | sdmx-dl operation |
|---|---|---|
| `GET /OPERACIONES_DISPONIBLES` | List all 112 operations | `getDatabases()` |
| `GET /TABLAS_OPERACION/{IdOPERACION}?tip=A` | List tables for one operation | `getFlows(DatabaseRef)` |
| `GET /DATOS_TABLA/{IdTABLA}?tip=AM&nult=1` | Series list with full metadata, one obs each | `getMeta()` — DSD assembly |
| `GET /DATOS_TABLA/{IdTABLA}?tip=AM` | All series with observations | `getData()` |

> **Note**: `tip=AM` = friendly (`A`) + metadata (`M`). The `M` flag is what makes the per-series
> `MetaData` array (`T3_Variable` + value `Id`) available; without it the keys cannot be built.
> `SERIES_TABLA` is **not** used by the driver (it exposes the variable as `FK_Variable` id without
> the name, which would not be consistent with the `T3_Variable` name returned by `DATOS_TABLA`).


### Endpoints not used in v1

| Endpoint | Reason |
|---|---|
| `GET /DATOS_SERIE/{IdSERIE}` | Per-series fetch — less efficient than `/DATOS_TABLA` |
| `GET /DATOS_METADATAOPERACION/{IdOPERACION}` | Operation-level filter — not needed when tables are flows |
| `GET /PUBLICACIONES*` | Publication calendar — out of scope |
| `GET /PERIODICIDADES` | Periodicity lookup — `Fecha` ISO 8601 field used directly instead |

---

## Conceptual mapping to sdmx-dl model

| INE concept | sdmx-dl concept | Notes |
|---|---|---|
| **Operation** (`OPERACION.Codigo`, e.g. `IPC`) | `Database` / `DatabaseRef` | Natural two-level grouping; maps directly onto the `Connection.getDatabases()` / `getFlows(DatabaseRef)` split |
| **Operation name** (`OPERACION.Nombre`) | `Database.name` | English when using `/EN/` base URL |
| **Table** (`TABLA.Id`, e.g. `50902`) | `Flow` | The primary addressable unit, scoped to a `DatabaseRef` |
| **Table name** (`Nombre`) | `Flow.name` | English when using `/EN/` base URL. **Not unique** within an operation (see correction #11) |
| **Table metadata** (`T3_Periodicidad`, `Codigo`, `T3_Publicacion`, period range) | `Flow.description` | `·`-joined summary that distinguishes same-name tables (frequency / variant / covered period / publication) |
| **Variable** (`VARIABLE.Id`) | `Dimension` | Dimensions are derived by **co-occurrence signature** (see correction #8): each `MetaData` item (variable name + occurrence rank) is grouped with items it never co-occurs with. Mutually-exclusive variables (e.g. Districts/Municipalities/Sections) merge into one dimension; a variable reused within a series yields several dimensions. Codes are the union of value Ids |
| **Value** (`VALOR.Id`) | `Code` in a codelist | Has `Id` (int), `Nombre` (label), `Codigo` (official code, may be null) |
| **Series** (`COD`, e.g. `IPC251856`) | `Series` | Identified by key (variable value combination) |
| **Series name** (`Nombre`) | `Series.name` | Dot-separated concatenation of dimension value labels |
| **Observation** (`Data[]` entry) | `Obs` | One row per time point |
| **`Fecha`** (ISO 8601 timestamp) | `Obs.period` | Used directly — no period-token parsing needed |
| **`Valor`** (double) | `Obs.value` | |
| **`T3_TipoDato`** (string, e.g. `Definitivo`) | `Obs` attribute | Observation status |

---

## FlowRef and DatabaseRef format

```
DatabaseRef : operation code  e.g.  IPC
FlowRef     : INE:{tableId}(latest)   e.g.  INE:50902(latest)
```

The `DatabaseRef` (operation code) scopes all `Connection` calls: `getFlows(ref)`, `getMeta(ref, flowRef)`, `getData(ref, flowRef, query)`.  
`DatabaseRef.NO_DATABASE` is **rejected with `IllegalArgumentException`** — an operation code is always required.

---

## DSD construction (virtual, assembled at query time)

There is no pre-built DSD. The structure for a table is assembled by calling
`GET /SERIES_TABLA/{tableId}?tip=M&det=1&nult=1`:

1. Each series in the response has a `Metadata` array of value objects.
2. With `det=1`, each value object carries a nested **`Variable`** sub-object that provides
   `Variable.Id` (the dimension ID) and `Variable.Nombre` (the dimension name).
3. Take the union of all `(Variable.Id, value.Id)` pairs across all series.
4. Each distinct `Variable.Id` becomes one **Dimension**, ordered by ascending variable ID.
5. The set of `value.Id` entries for a given `Variable.Id` becomes its **Codelist**.

> **Correction (see #1, #2, #8, #9 above)**: the implemented driver builds both the structure and the
> data from a **single** `DATOS_TABLA?tip=AM` response (cached as one entry), not from `SERIES_TABLA`
> and not from a separate `nult=1` call. Dimensions are formed by **co-occurrence signature** over
> `MetaData` items (variable name + occurrence rank), which merges mutually-exclusive variables (e.g.
> Districts/Municipalities/Sections) into one dimension and splits a reused variable name into several.
> Series keys are fully specified; `_Z` ("not applicable") is only a defensive fallback for a dimension
> a series does not carry.

> **Note**: The top-level `FK_Variable` field in each Metadata entry is always empty (confirmed
> live). The variable ID is only available through the nested `Variable` sub-object exposed by
> `det=1`. Using `det=0` is insufficient for DSD construction.
>
> `nult=1` caps the observation payload to zero rows (confirmed: 0 Data entries when combined with
> `tip=M`) while still returning the full `Metadata` array — making it the correct parameter to
> use for structure-only calls.

**Confirmed payload sizes** (live, provincial IPC table 24081, 53 series):

| Parameters | Response size |
|---|---|
| `tip=M&nult=1` (det=0) | 29 KB |
| `tip=M&det=1&nult=1` | 54 KB |

Both are well within acceptable limits even for wide tables.

### Confirmed DSD for table 50902 (IPC National Indices)

| Slot | Variable ID | Variable name | Values |
|---|---|---|---|
| 0 | `3` | Type of data | `74` Annual change, `83` Index, `84` Monthly change, `87` YTD change |
| 1 | `762` | ECOICOP Groups | `304092` Overall index, `304093` Food, … `304104` Other goods (13 total) |

Key `74.304092` → Annual change of Overall index → series `IPC251856`.

---

## Data fetch

```
GET /DATOS_TABLA/{tableId}
  ?tv={varId1}:{valId1}        ← one parameter per non-wildcard key slot
  &tv={varId2}:{valId2}
  &tip=A                       ← "user-friendly" mode (flat JSON, no nested objects)
  &date={yyyymmdd}:{yyyymmdd}  ← date range; open end: "20250101:"
  &nult={n}                    ← last N periods (alternative to date range)
```

### Confirmed live example

```
GET /DATOS_TABLA/50902?tv=762:304092&tv=3:74&nult=3&tip=A
→ COD=IPC251856 | National. Overall index. Annual change.
  2025-12-01T00:00:00.000+01:00 | M12 2025 | 2.9
  2025-11-01T00:00:00.000+01:00 | M11 2025 | 3.0
  2025-10-01T00:00:00.000+02:00 | M10 2025 | 3.1
```

### Empty / no-match result (confirmed live)

When the `tv` filter references a value ID that does not exist in the table, the API returns
**HTTP 500** (not a 200 with an empty array). The driver must catch HTTP 500 from `DATOS_TABLA`
and treat it as an empty dataset rather than a hard error. Pre-validating key values against the
DSD (assembled via `getMeta`) can avoid this case entirely when the key contains unknown codes.

---

## Observation period

The `Fecha` field is always a valid ISO 8601 timestamp and should be used as the authoritative period.
`T3_Periodo` and `Anyo` are display labels and do not need to be parsed.

| Periodicity | `T3_Periodo` (display only) | `Fecha` example | ISO 8601 result |
|---|---|---|---|
| Monthly | `M01` … `M12` | `2025-08-01T00:00:00.000+02:00` | `2025-08` |
| Quarterly | `QI`, `QII`, `QIII`, `QIV` | `2026-01-01T00:00:00.000+01:00` | `2026-Q1` |
| Semi-annual (named date) | `January 1st,`, `July 1st,` | `2025-01-01T00:00:00.000+01:00` | `2025-01-01` |
| Annual | (year name) | `2024-01-01T00:00:00.000+01:00` | `2024` |

Quarter mapping from `Fecha` month: Jan=Q1, Apr=Q2, Jul=Q3, Oct=Q4.

### Observation status values (`T3_TipoDato`) — confirmed live with `/EN/` base URL

| `T3_TipoDato` value | Meaning | Tables observed |
|---|---|---|
| `Final value` | Definitive / confirmed observation | IPC (50902), Population (56934) |
| `Provisional value` | Preliminary / not yet revised | GDP (67196) |

These are the English labels returned by the `/EN/` endpoint with `tip=A`. Other labels may exist
but were not encountered in the sampled tables. The driver should map any unrecognised value to a
generic "unknown status" attribute rather than fail.

---

## Periodicity codes (confirmed live via `GET /PERIODICIDADES`)

| Id | Code | Name |
|---|---|---|
| `0` | `N` | Minutely |
| `1` | `M` | Monthly |
| `3` | `Q` | Quarterly |
| `6` | `S` | Semi-annual |
| `7` | `W` | Weekly |
| `12` | `A` | Annual |
| `13` | `ABI` | Biannual Odd |
| `14` | `ABP` | Biannual Even |
| `30` | `D` | Daily |
| `31` | `B` | Daily-business week |
| `100` | `SP` | Without periodicity |

---

## Implementation design

### Module: `sdmx-dl-provider-dialects`

`IneDriver` is added to the existing `sdmx-dl-provider-dialects` module alongside the other
dialect drivers (`EstatDriver`, `BbkDriver`, `ImfDriver`, …). No new Maven module is introduced.

New classes under the existing package `sdmxdl.provider.dialects.drivers`:

```
sdmx-dl-provider-dialects/src/main/java/sdmxdl/provider/dialects/drivers/
  IneDriver.java        ← Driver SPI implementation (declares the WebSource)
  IneClient.java        ← HTTP client wrapping the WSTempus endpoints
  IneJsonParser.java    ← JSON deserialization
  InePeriodParser.java  ← Fecha → TimeInterval conversion
```

The existing `module-info.java` and `META-INF/services/sdmxdl.web.spi.Driver` registration file
in `sdmx-dl-provider-dialects` must be updated to declare `IneDriver`.

> **Note**: `sources.csv` is a **generated** file derived from the `WebSource` declarations inside
> each driver class. It must **not** be edited manually. The source registration belongs in
> `IneDriver.java` via `DriverSupport.builder().source(...)`, following the same pattern as
> `InseeDialectDriver`, `BbkDialectDriver`, etc.

### `IneDriver` — key responsibilities

```java
// Registered via ServiceLoader as sdmxdl.web.spi.Driver

// connect() returns an IneConnection implementing Connection:

// getDatabases():
//   GET /OPERACIONES_DISPONIBLES (paginated until empty page)
//   map each OperacionesJSON → Database(DatabaseRef.parse(Codigo), Nombre)

// getFlows(DatabaseRef operationCode):
//   GET /TABLAS_OPERACION/{operationCode}?tip=A
//   map each TablasJSON → Flow(FlowRef.parse("INE:" + Id), Nombre)

// getMeta(DatabaseRef operationCode, FlowRef tableRef):
//   GET /SERIES_TABLA/{tableId}?tip=M&det=1&nult=1
//   union all Metadata[i].Variable.Id → Dimension list ordered by Variable.Id asc
//   build virtual Structure

// getData(DatabaseRef operationCode, FlowRef tableRef, Query query):
//   translate non-wildcard key slots → ?tv={varId}:{valId} params
//   GET /DATOS_TABLA/{tableId}?tv=...&tip=A&date=...
//   for each DatosSerieJSON entry: parse Fecha as ISO 8601, emit Obs
```

### Suggested Java snippet for `IneDriver.java`

Following the same pattern as the other dialect drivers (e.g., `InseeDialectDriver`):

```java
@DirectImpl
@ServiceProvider
public final class IneDriver implements Driver {

    private static final String DIALECTS_INE = "DIALECTS_INE";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(DIALECTS_INE)
            .rank(NATIVE_DRIVER_RANK)
            .connector(/* IneConnector */)
            .source(WebSource
                    .builder()
                    .id("INE")
                    .name("en", "National Statistics Institute of Spain")
                    .name("es", "Instituto Nacional de Estadística")
                    .driver(DIALECTS_INE)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://servicios.ine.es/wstempus/js")
                    .websiteOf("https://www.ine.es")
                    .build())
            .build();
}
```

---

## Engineering work items

| Item | Complexity | Notes |
|---|---|---|
| `IneDriver` SPI implementation in `sdmx-dl-provider-dialects` | Medium | `getDatabases`, `getFlows`, `getMeta`, `getData`; declares the `INE` `WebSource` via `DriverSupport` |
| HTTP client layer (`IneClient`) | Small | Thin wrapper over existing `nbbrd.io.http` already used by the dialects module |
| JSON deserialization (`IneJsonParser`) | Medium | `OperacionesJSON`, `TablasJSON`, `SeriesJSON`, `DatosSerieJSON` schemas |
| Two-level flow enumeration (ops → tables) | Small | Two paginated GET calls; cache result |
| DSD assembly from `SERIES_TABLA?tip=M` | Medium | Group/deduplicate `(FK_Variable, Id)` from all series `Metadata` arrays |
| Key → `tv=` query parameter translation | Small | Ordered variable slots → repeated `tv` params |
| `Fecha` ISO 8601 → `TimeInterval` (`InePeriodParser`) | Small | Parse `2025-12-01T00:00:00.000+01:00`, determine granularity from periodicity |
| `T3_TipoDato` → observation attribute | Small | Map "Definitivo", "Provisional", … |
| Update `module-info.java` in `sdmx-dl-provider-dialects` | Small | Declare `IneDriver` |
| Update `META-INF/services/sdmxdl.web.spi.Driver` | Small | Register `IneDriver` via ServiceLoader |
| `sources.csv` row + docs update | — | **Generated** — no manual edit needed; derived from the `WebSource` in `IneDriver` |

---

## Request cost model

The use of `Database` (Operations) as the first-level selection makes flow enumeration **lazy and
scoped**: a user first selects an operation, then browses only that operation's tables. The ~113-call
upfront cost is entirely avoided in normal usage.

### `getDatabases()` — cold (no cache)

| Call | Count | Note |
|---|---|---|
| `GET /OPERACIONES_DISPONIBLES` | 1 | All 112 operations in one page |
| **Total** | **1** | Cache for 24 h+ (operations rarely change) |

### `getFlows(DatabaseRef)` — per operation

| Call | Count | Note |
|---|---|---|
| `GET /TABLAS_OPERACION/{operationCode}?tip=A` | 1 | Tables for one operation only |
| **Total** | **1** | Cache per operation (24 h+) |

### `getMeta(DatabaseRef, FlowRef)` — per table

| Call | Count | Note |
|---|---|---|
| `GET /SERIES_TABLA/{tableId}?tip=M&nult=1` | 1 | May return a large JSON for wide tables |
| **Total** | **1** | Cache per table (structure is stable) |

### `getData(DatabaseRef, FlowRef, Query)` — per query

| Call | Count | Note |
|---|---|---|
| `GET /DATOS_TABLA/{tableId}?tv=...` | 1 | Returns all matching series with observations |
| **Total** | **1** | |

---

## Resolved decisions

All open questions have been resolved (confirmed live 2026-06-24):

- **DSD size for wide tables**: `SERIES_TABLA?tip=M&det=1&nult=1` is confirmed sufficient.
  `nult=1` with `tip=M` returns 0 `Data` entries, giving metadata-only payloads of 30–54 KB even
  for 53-series provincial tables. `det=1` is **required** to obtain variable IDs via the nested
  `Variable` sub-object.
- **Empty result / HTTP 500**: A `tv` filter referencing a non-existent value ID returns **HTTP 500**
  (not a graceful empty array). The driver must catch HTTP 500 on `DATOS_TABLA` and treat it as an
  empty dataset. Pre-validating key codes against the DSD avoids this case for well-formed queries.
- **Observation status values**: Two labels confirmed with the `/EN/` endpoint and `tip=A`:
  `"Final value"` and `"Provisional value"`. Unknown labels should map to a generic attribute value.
- **`ver=3` parameter**: Confirmed **not required** across all driver endpoints
  (`OPERACIONES_DISPONIBLES`, `TABLAS_OPERACION`, `SERIES_TABLA`, `DATOS_TABLA`). The driver omits it.
- **`DatabaseRef.NO_DATABASE`**: Rejected with `IllegalArgumentException` — an operation code is
  always required.
- **`sdmx-dl-standalone` bundling**: `sdmx-dl-provider-dialects` is already included in the
  fat-jar distribution; `IneDriver` will be bundled automatically.

---

## References

- INE OpenAPI spec: `https://ine.es/OpenAPI/includes/files/en/wstempus.yaml`
- INE OpenAPI docs: `https://ine.es/OpenAPI/en/index.html`
- `ineapir` R package docs: `https://es-ine.github.io/ineapir/`
- INEbase search engine: `https://www.ine.es/dyngs/INEbase/listaoperaciones.htm`
- INE table/series identifier guide: `https://www.ine.es/dyngs/DAB/index.htm?cid=1104`
- License: `https://creativecommons.org/licenses/by/4.0/deed.es`

