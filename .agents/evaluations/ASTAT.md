# ASTAT — Institute of Statistics of the Autonomous Province of Bolzano – South Tyrol

Evaluation of the source requested in [issue #1098](https://github.com/nbbrd/sdmx-dl/issues/1098),
including the follow-up
[comment](https://github.com/nbbrd/sdmx-dl/issues/1098#issuecomment-3913603708)
about the `VIRTUALDSD` structure references.

Date: 2026-08-27. All statements below are backed by live responses captured on that date.

## 1. Summary

| Field | Value |
|-------|-------|
| Proposed ID | `ASTAT` (free; `docs/assets/sources.csv` only has `ISTAT`) |
| Platform | NSI Web Service **v9.11.0.0** (same stack as `ISTAT`) |
| Protocol | SDMX 2.1 REST, `application/xml; version=2.1; charset=utf-8` |
| Languages | `de`, `en`, `it` |
| Confidentiality | `PUBLIC` (anonymous access, no 401/403) |
| Website | `https://statastat.prov.bz.it/databrowser/` (200) |
| API documentation | none found |

**The service is not a single endpoint.** It is split into two disjoint deployments that
each serve half of the catalog:

| Base path | Flows | Structures | Data |
|-----------|-------|------------|------|
| `https://astatsdmxservices.prov.bz.it/dsm/NSI_WS/rest` | 260 | 138 real DSDs, **122 `MDM:VIRTUALDSD`** | works for the 138, **HTTP 404 for the 122** |
| `https://astatsdmxservices.prov.bz.it/esd/NSI_WS/rest` | 123 | 64 distinct real DSDs, no `VIRTUALDSD` | works |

The endpoint listed in the issue is only the `dsm` one.

## 2. Evidence

### 2.1 `dsm` — flow listing

`GET /dsm/NSI_WS/rest/dataflow/all/all/all` → 200, 1 011 771 bytes, 260 dataflows,
agency `ITH1`, `xml:lang` ∈ {de, en, it}.

Structure references in that listing:

```
122 x MDM:VIRTUALDSD(1.0)
 27 x ITH1:DSD_LIVCOND(1.0)
 20 x ITH1:DSD_LIVCOND_DM(1.0)
 16 x ITH1:DSD_BUILD(1.0)
 11 x ITH1:DSD_TOUR_ACCOMODATION(1.3)
 ...
```

So **47 % of the advertised flows point at a placeholder DSD**, confirming the issue comment.

### 2.2 `MDM:VIRTUALDSD` is a stub

| Request | Result |
|---------|--------|
| `/dsm/.../datastructure/MDM/VIRTUALDSD/1.0?references=children` | 200, 4 233 bytes, **1 dimension** |
| `/esd/.../datastructure/MDM/VIRTUALDSD/1.0?references=children` | 200, 4 233 bytes, **1 dimension** |
| `/dsm/.../dataflow/ITH1/DF_ISTAT_DCCV_OCCUPATIT1_16/1.0?references=all` | 200, 43 537 bytes, DSD = `MDM:VIRTUALDSD`, 1 dimension |
| `/dsm/.../dataflow/ITH1/DF_ISTAT_DCCV_OCCUPATIT1_16/1.0/?detail=Full&references=Descendants` | 200, 7 652 bytes, DSD = `MDM:VIRTUALDSD`, 1 dimension |
| `/esd/.../dataflow/ITH1/DF_ISTAT_DCCV_OCCUPATIT1_16/1.0/?detail=Full&references=Descendants` | 200, **12 613 065 bytes**, DSD = `IT1:DCCV_OCCUPATIT1`, **14 dimensions** |

> Refinement of the issue comment: the discriminator is the **base path** (`/esd/` vs `/dsm/`),
> not the `detail`/`references` query parameters. The very same query string returns the stub
> on `/dsm/` and the real DSD on `/esd/`. On `/esd/`, the plain standard call
> `/datastructure/IT1/DCCV_OCCUPATIT1/latest?references=children` also returns the real
> 14-dimension DSD (12 609 746 bytes), so **no non-standard query is required** — only the
> right host path.

### 2.3 The two catalogs are complementary, not nested

| Request | Result |
|---------|--------|
| `/esd/.../dataflow/ITH1/DF_BUILD_FINISHED_WORK_1/1.0?references=all` | **404** |
| `/dsm/.../data/ITH1,DF_ISTAT_DCCV_OCCUPATIT1_16,1.0/all/all` | **404** |
| `/esd/.../data/ITH1,DF_ISTAT_DCCV_OCCUPATIT1_16,1.0/all/all?lastNObservations=1` | 200, 66 series, key size 14 |

Set comparison between the 123 `esd` flow ids and the 122 `VIRTUALDSD` flow ids on `dsm`:

- 101 ids identical
- 22 `esd`-only ids and 21 `dsm`-only ids — these are the *same* agriculture-census tables
  under different ids, e.g. `esd:DF_DCAT_CENSAGRIC2020_AGE` vs
  `dsm:DF_ISTAT_DCAT_CENSAGRIC2020_AGE`

So the `esd` service is the real home of the `VIRTUALDSD` flows, but **id remapping would be
needed for ~21 of them**.

### 2.4 Random sampling (seed 42 / 7)

`dsm`, 5 flows with `VIRTUALDSD` + 5 flows with a real DSD, `data/.../all/all?lastNObservations=1`:

```
virtual=True  DF_ISTAT_DF_DCSP_AGRITURISMO_COM_1        -> ERR 404
virtual=True  DF_ISTAT_DCAT_CENSAGRIC2020_ORG_CROPS     -> ERR 404
virtual=True  DF_ISTAT_DCCV_NEET1_11                    -> ERR 404
virtual=True  DF_ISTAT_DCCV_AVQ_PERSONE_16              -> ERR 404
virtual=True  DF_ISTAT_DCCV_FORZLV1_6                   -> ERR 404
virtual=False DF_LIVCOND_INDIV_2_2                      -> OK series=16    keySize=6
virtual=False DF_LIVCOND_INDIV_1_1                      -> OK series=30    keySize=6
virtual=False DF_BUSINESS_LOCAL_BSNS_UNITS_1            -> OK series=670   keySize=7
virtual=False DF_LIVCOND_DM_MOSP_5                      -> OK series=40    keySize=8
virtual=False DF_TOUR_ACCOMODATION_2                    -> OK series=84626 keySize=8
```

`esd`, 6 random flows:

```
DF_ISTAT_DCSP_FERTILIZZANTI_3            -> OK series=144  keySize=8
DF_ISTAT_DCAT_CENSAGRIC2020_AU_CATTLE_1  -> OK series=3740 keySize=6
DF_ISTAT_DCCV_AVQ_PERSONE_18             -> OK series=24   keySize=8
DF_ISTAT_DCSP_FITOSANITARI_2             -> OK series=30   keySize=7
DF_ISTAT_DCCN_TNA1_6                     -> OK series=98   keySize=10
DF_ISTAT_DCCN_TNA1_5                     -> OK series=688  keySize=10
```

Invariant `key size == dimension count` held everywhere it was checked; keys round-trip
(`.../data/ITH1,DF_BUILD_FINISHED_WORK_1,1.0/A.FINISCHED_WORK.NRS_BUILD.EXP_BUILD.TOTAL.TOTAL.TOTAL.TOTAL.M3_BUILD.021001/all`
→ exactly 1 series, 27 annual observations from `1995 = 1115`).

### 2.5 Other probes

| Check | `dsm` | `esd` |
|-------|-------|-------|
| `detail=allcompletestubs` | 200 (1 011 771 → 975 104 bytes) | 200 (282 501 → 277 800 bytes) |
| `detail=referencepartial` | 501 | not tested |
| Trailing slash | both forms 200 → **not** required | — |
| `agencyscheme/all/all/all` | 404 | not tested |
| Authentication | none | none |

## 3. Consequences for sdmx-dl

Registering a single `RI_SDMX21` source on the `dsm` endpoint — as the issue proposes —
would expose 260 flows of which **122 are unusable**: `getMeta` returns a 1-dimension
placeholder DSD and `getData` fails with HTTP 404. That is not acceptable as-is.

### Option A — two plain `RI_SDMX21` sources (recommended first step)

| Source | Endpoint | Flows |
|--------|----------|-------|
| `ASTAT` | `.../dsm/NSI_WS/rest` | 260 (122 broken) |
| `ASTAT_ESD` (name TBD) | `.../esd/NSI_WS/rest` | 123 (all working) |

Zero new code, only two `WebSource` declarations. Downside: the broken flows remain visible
under `ASTAT`, and the two catalogs partially duplicate each other.

### Option B — a small `AstatDialectDriver`

Wrap `RI_SDMX21` behaviour and:

1. On `getFlows`, **drop** the flows whose structure ref is `MDM:VIRTUALDSD` — or, better,
   substitute them with the `esd` counterparts.
2. Route `getStructure`/`getData` to `esd` for those flows, to `dsm` otherwise.
3. Handle the ~21 id mismatches (`DF_ISTAT_DCAT_*` ↔ `DF_DCAT_*`) if the flows are merged.

This yields one coherent 260-flow source. Cost: a new driver class, plus the id-mapping
maintenance burden.

### Option C — `ASTAT` on `dsm` only, with `VIRTUALDSD` flows filtered out

Middle ground: 138 native ASTAT flows, all working, no `esd` mirror. The ISTAT-derived flows
are arguably already reachable through the existing `ISTAT` source.

## 4. Practical warnings for implementation

- **Very large structure payloads**: `/esd/.../datastructure/IT1/DCCV_OCCUPATIT1/latest?references=children`
  is **12.6 MB**. Parsing must stay streaming; do not buffer whole bodies.
- **No `agencyscheme`** (404) — agency display names must come from the website, not the service.
- Data queries on `dsm` without a key can be large too (2 MB for
  `DF_BUILD_FINISHED_WORK_1` with `lastNObservations=2`, 84 626 series for
  `DF_TOUR_ACCOMODATION_2`).
- `detail=referencepartial` returns 501; only `allcompletestubs` was confirmed working, which is
  what `sdmxdl.driver.detailSupported=true` relies on (same as `ISTAT`).

## 5. Proposed metadata (whatever option is chosen)

```java
.source(WebSource
        .builder()
        .id("ASTAT")
        .name("en", "Institute of Statistics of the Autonomous Province of Bolzano - South Tyrol")
        .name("de", "Landesinstitut für Statistik ASTAT der Autonomen Provinz Bozen – Südtirol")
        .name("it", "Istituto provinciale di statistica ASTAT della Provincia autonoma di Bolzano - Alto Adige")
        .driver(RI_SDMX_21)
        .confidentiality(PUBLIC)
        .endpointOf("https://astatsdmxservices.prov.bz.it/dsm/NSI_WS/rest")
        .propertyOf(DETAIL_SUPPORTED_PROPERTY, true)
        .websiteOf("https://statastat.prov.bz.it/databrowser/")
        .build())
```

Working example for the web-query test:
`ASTAT / DF_BUILD_FINISHED_WORK_1 / A.FINISCHED_WORK.NRS_BUILD.EXP_BUILD.TOTAL.TOTAL.TOTAL.TOTAL.M3_BUILD.021001`
(1 series, 27 annual observations, first `1995 = 1115`).

For an `esd`-based source:
`DF_ISTAT_DCCV_OCCUPATIT1_16 / A.IT.1.Y15-24.99.TOTAL.0010.0010.9.99.99.99.9.9` (14 dimensions).

## 6. Open questions

- [ ] Which option (A / B / C) should be implemented?
- [ ] Is the `dsm` / `esd` split intentional and stable, or is `VIRTUALDSD` on `dsm` a
      configuration bug that ASTAT could fix upstream? Worth asking the provider.
- [ ] Confirm the official German name wording (the issue used the genitive
      "Landesinstituts").
- [ ] Register an upptime monitor (`upptime:/nbbrd/sdmx-upptime/ASTAT`).

