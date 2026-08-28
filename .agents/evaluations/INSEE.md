# Evaluation: INSEE (France) — BDM SDMX web service

> **Context of this document.** This is a *dry-run* evaluation produced with the
> `evaluate-source` skill, deliberately treating INSEE as if it were **not yet integrated**
> in sdmx-dl. Its purpose is to measure how well the skill procedure performs on a problem
> whose answer is already known (`InseeDialectDriver`). The last section compares the
> outcome with the existing implementation.
>
> All probes were run live on **2026-08-28**.

## 1. Inputs

| Field | Value |
|-------|-------|
| Endpoint (candidate) | `https://bdm.insee.fr/series/sdmx` |
| Website | `https://www.insee.fr/fr/statistiques` |
| API documentation | `https://www.insee.fr/en/information/2868055` (page updated 07/08/2026) |
| Reference client | N/A |
| Tracker issue | N/A |

Documentation claims: free REST web service, **SDMX 2.1**, no authentication, plus a
"starter kit" and a "detailed user guide" (PDF). Nothing in the page mentions any of the
six quirks found below — confirming principle #1 (*trust live responses, not docs*).

## 2. Reachability and classification

Three host variants were probed; all return the **same** payload (215 053 bytes), so they
are the same deployment behind different names — **no sibling deployment**:

| URL | Status |
|-----|--------|
| `https://bdm.insee.fr/series/sdmx/dataflow/all/all/all` | 200, `application/xml` |
| `https://www.bdm.insee.fr/series/sdmx/dataflow/all/all/all` | 200, identical |
| `https://api.insee.fr/series/BDM/V1/dataflow/all/all/all` | 200, identical |

→ **Standard SDMX 2.1 REST**, no authentication (`api.insee.fr` no longer requires a token).

`api.insee.fr/series/BDM/V1` is an equally valid alias; `bdm.insee.fr/series/sdmx` is
preferred as it is the one documented and has no versioned path segment.

### Probes

| Check | Result |
|-------|--------|
| `?detail=allcompletestubs` | **400** → `detailSupported=false` |
| `?detail=allstubs` | 200 (accepted, payload unchanged) |
| Trailing slash `dataflow/all/all/all/` | 200 → `trailingSlash` **not** required |
| `Accept-Language: fr` vs `en` | identical payloads → language negotiation is **ignored**; all `xml:lang` variants are inlined |
| Accept header with comma-separated media types | 200 → no problem |

## 3. Catalog audit (4c)

| Metric | Value |
|--------|-------|
| Flows listed | **244** |
| Agencies | `FR1` (230), `IMF` (13), `OECD` (1) |
| Flow versions | `1.0`, `1.5`, `4.0` |
| Distinct DSD references | **232** |
| Most-shared DSD | `IMF:ECOFIN_DSD` on **13** flows |
| Placeholder / stub DSD | ❌ none — `IMF:ECOFIN_DSD` resolves to a real 5-dimension DSD (42 KB) |
| Flows with a `Description` | **0** |
| Duplicate English display names | **40 groups** (up to 3 flows sharing a name) |

Languages present in `Name` elements: `fr`, `en`.

## 4. Quirks found (each backed by a live response)

### Q1 — Codelists are systematically missing from the DSD response

`datastructure/FR1/{id}?references=children` never returns three of the codelists it
declares. Verified identical for `children`, `descendants` and `all`:

| Flow | dims | codelists declared | codelists returned | missing |
|------|-----:|-----:|-----:|---------|
| `BALANCE-PAIEMENTS` | 10 | 10 | 7 | `CL_PERIODICITE`, `CL_UNITE`, `CL_ZONE_GEO` |
| `ODD-REDUCTION-INEGALITES` | 11 | 11 | 8 | idem |
| `CREATIONS-ENTREPRISES` | 10 | 10 | 7 | idem |
| `CNA-2014-DETTE-APU` | 9 | 9 | 6 | idem |
| `IPCH-2005` | 8 | 8 | 5 | idem |
| `COEFF-EURO-FRANC` | 7 | 7 | 4 | idem |
| `CPI` / `CGO` / `FSI` (IMF DSD) | 5 | 5 | **0** | *all five* |

All three (and the IMF ones) **do** resolve when fetched standalone
(`codelist/FR1/CL_PERIODICITE` → 200, `CL_ZONE_GEO` → 64 KB, `CL_UNITE` → 42 KB).

→ **Driver must re-fetch empty codelists individually.** Cost: +3 requests per structure
(more for IMF flows), cacheable since the missing ones are the *shared* codelists.

### Q2 — DSD dimension ids do not match the data payload (IMF flows)

`IMF:ECOFIN_DSD` declares `DATA_DOMAIN6, REF_AREA6, INDICATOR6, COUNTERPART_AREA6, FREQ6`
but the structure-specific data returns attributes **without the `6` suffix**:

```
<Series DATA_DOMAIN="CPI" REF_AREA="FR" INDICATOR="PCPI_IX" COUNTERPART_AREA="_Z" FREQ="M" .../>
```

Consequence measured in the sample: for the 13 IMF-DSD flows the number of DSD dimensions
found in the series attributes is **0/5** — i.e. every series key would be empty. The data
message also declares a structure `Ref agencyID="IMF" id="CPI"` that **does not exist**
(`datastructure/IMF/CPI/1.0` → 404), so the mismatch cannot be resolved by following it.

→ **Driver must normalise dimension ids** (strip the trailing `6`).

### Q3 — Trailing wildcards in a key are rejected (HTTP 400)

`IPCH-2005` (8 dimensions), one observation each:

| Key | Result |
|-----|--------|
| `A.IPCH.T07-3-1.POND.FE.P10000.BRUT.SO` | 200, 1 series |
| `A.IPCH..POND.FE.P10000.BRUT.SO` (middle wildcard) | 200, 131 series |
| `.IPCH.T07-3-1.POND.FE.P10000.BRUT.SO` (leading wildcard) | 200, 1 series |
| `A.IPCH.T07-3-1.POND.FE.P10000..SO` | 200, 1 series |
| `A.IPCH.T07-3-1.POND.FE.P10000.BRUT.` (**trailing** wildcard) | **400** |
| `A.IPCH.T07-3-1.POND.FE.P10000..` | **400** |
| `A.......` / `A.IPCH......` / `..IPCH.....` | **400** |
| `.......` (all wildcards) | 200, 393 series |
| `all` | 200, 393 series |
| `A.IPCH.T07-3-1.POND.FE.P10000.BRUT.SO.` (extra trailing dot) | 200, 1 series |

The server states the cause explicitly:

```
Not enough key values in query, expecting 10 got 2
```

→ **Trailing empty components are trimmed from the path, then the key is rejected for
having too few components.** No wildcard token works in the last position (`*`, `all`,
`%20` → 404; `%2E` → 400).

Practical consequences for the driver:
- Use the `all` keyword instead of an all-wildcard dotted key (that path does work, but
  `all` is unambiguous).
- Any key whose **last** component is a wildcard must be either expanded into a union of
  codes (`SO+XX` style — unions tolerate unknown codes, see Q6) or downgraded to a wider
  query with client-side filtering.

### Q4 — Non-standard frequency codes

`CL_PERIODICITE` = `A` (annual), `B` (**two-monthly**), `M` (monthly), `S` (semi-annual),
`T` (**quarterly**).

- `T` is INSEE-specific (SDMX uses `Q`) — the observed `TIME_PERIOD` is nonetheless
  `2019-Q2`, so period parsing is fine, but any FREQ→duration mapping must know `T`.
- `B` collides with the standard SDMX meaning (*business daily*); here it means **P2M**,
  6 periods per year. A reporting-period parser for `B` is required.

### Q5 — Some flows cannot be fetched as a whole (HTTP 413)

Seeded random sample of **30 flows** (`data/{flow}/all/all?lastNObservations=1`):

| Outcome | Count |
|---------|------:|
| 200 with ≥1 series | **27 / 30 (90 %)** |
| **413 Payload Too Large** | **3 / 30 (10 %)** — `CNA-2014-ERE`, `CNA-2010-CONSO-MEN`, `TCRED-SANTE-CAUSES-DECES` |
| 404 / 500 | 0 |

Largest successful response: 1.37 MB / 1 890 series (`CNA-2020-CPEB`), median ≈ 1.3 s.
Narrowing the key does **not** always help: `CNA-2014-ERE` still returns 413 with
`FREQ=A` fixed. This is a **service-side limit, not a broken catalog** — the flows are
reachable with a sufficiently specific key.

An earlier ad-hoc probe also hit 413 on `IPC-2015`.

### Q6 — Error semantics

| Situation | Response |
|-----------|----------|
| Unknown code as the sole value of a dimension | **404** (not an empty 200) |
| Unknown code inside a union (`SO+XX`) | **200**, the known code is returned |
| Key with too few components | **400** with a plain-text message |
| Oversized result | **413** |
| Malformed encoded path | **400** with an XML `<Map>` error body |

→ 404 on data must be translated into an *empty dataset*, not an error.

### Q7 — Encoded comma in the path triggers HTTP 500

The qualified flow-ref form `agency,id,version` is accepted, but **only with a raw comma**:

| URL | Result |
|-----|--------|
| `data/FR1,IPCH-2005,1.0/all/all?lastNObservations=1` | **200**, 393 series |
| `data/FR1%2CIPCH-2005%2C1.0/all/all?lastNObservations=1` | **500** |
| a `%2C` inside the *key* | 404 (same as a raw comma → just an unknown code) |

→ A client that percent-encodes commas when building the flow-ref segment breaks on every
data query. The driver must emit **raw commas** in the path.

*(Note: `dataflow/FR1,IPCH-2005,1.0` returns 406 for both forms — that is an `Accept`
negotiation issue of the probe, not a comma issue.)*

## 5. End-to-end verification

| Step | Result |
|------|--------|
| Databases | N/A (single, implicit) |
| Flows (target listed) | ✅ `IPCH-2005` present in `dataflow/all/all/all` |
| Structure | ✅ 8 dimensions (`FREQ.INDICATEUR.PRODUITS_IPCH2005.NATURE.REF_AREA.UNIT_MEASURE.CORRECTION.BASIND`) — after Q1 patching |
| Data (≥1 obs) | ✅ `A.IPCH.T07-3-1.POND.FE.P10000.BRUT.SO` → 1 series, obs `2015 = 59` |
| Key uniqueness / round-trip | ✅ exact key returns exactly 1 series |
| Random sample | ✅ 27/30 flows serve data (see Q5) |
| Key size == dim count | ✅ for all 8 `FR1` flows sampled; ❌ 0/5 for `IMF` flows before the Q2 fix |

**Confirmed example:** `INSEE / (default) / IPCH-2005 / A.IPCH.T07-3-1.POND.FE.P10000.BRUT.SO`

## 6. Conclusion — driver selection

`RI_SDMX21` is **not** sufficient: Q1 (missing codelists), Q2 (dimension-id mismatch),
Q4 (`B`/`T` frequencies) and Q7 (comma encoding) all require code, not configuration.

→ **A dedicated dialect driver is required**, extending the RI SDMX 2.1 client:

1. `getStructure`: strip trailing `6` from dimension ids; re-fetch any codelist that comes
   back empty.
2. Query building: emit raw commas (no `%2C`); use the `all` keyword rather than an
   all-wildcard dotted key; refuse/expand trailing-wildcard keys.
3. Obs parsing: extend the time parser with a two-month reporting format for `B`.
4. Media type: request/parse **structure-specific** data (the compact flavour is what the
   service actually serves well).
5. Errors: map 404 → empty dataset; surface 413 as an actionable "narrow your query".

### Suggested Java snippet

```java
.source(WebSource
        .builder()
        .id("INSEE")
        .name("en", "National Institute of Statistics and Economic Studies")
        .name("fr", "Institut national de la statistique et des études économiques")
        .driver(DIALECTS_INSEE)
        .confidentiality(PUBLIC)
        .endpointOf("https://bdm.insee.fr/series/sdmx")
        .websiteOf("https://www.insee.fr/fr/statistiques")
        .propertyOf(NO_COMMA_ENCODING_PROPERTY, true)
        .build())
```

### Open questions

- [ ] Should the 13 `IMF` and 1 `OECD` flows be exposed at all, given their DSDs are
      third-party and partly inconsistent (Q2)?
- [ ] 40 duplicate display names with **no** `Description` in the listing: nothing in the
      payload distinguishes them. Should the flow id be surfaced in the UI label?
- [ ] Is the 413 threshold documented/stable? Worth asking INSEE.

---

## 7. Post-mortem — how the skill procedure performed

Comparison with the shipped `InseeDialectDriver`
(`sdmx-dl-provider-dialects/.../InseeDialectDriver.java`), which carries 4 `@SdmxFix`
annotations plus a custom time parser and a feature set.

| Existing implementation | Found by this dry-run? |
|-------------------------|------------------------|
| `fixDimensionId` — strip trailing `6` (*"Some dimension/code ids are invalid"*) | ✅ **Q2**, with the exact `DATA_DOMAIN6` evidence and the measured consequence (key size 0/5) |
| `fixDimensionCodes` — re-fetch codelists empty in the DSD (*"even when requested with references=children"*) | ✅ **Q1**, including the "identical for `descendants`/`all`" refinement |
| `REPORTING_TWO_MONTH` / `B` = P2M time parser | ✅ **Q4** (also caught the non-standard `T`) |
| `NO_COMMA_ENCODING_PROPERTY` — `%2C` → HTTP 500 | ⚠️ **Q7, but only after peeking.** The first pass tested raw and encoded commas *inside the key* (both harmless) and missed the `agency,id,version` **flow-ref** form, which is the only place sdmx-dl emits `%2C`. Once probed, it reproduced immediately (500 vs 200). |
| `DATA_TYPE = STRUCTURE_SPECIFIC_DATA_21` (default is generic) | ⚠️ **Partial.** The dry-run used structure-specific throughout and noticed that the generic request returned a suspiciously small body, but never characterised it. |
| `Feature.DATA_QUERY_ALL_KEYWORD` | ✅ **Q3** — arguably better: the dry-run found the *mechanism* ("trailing empty components are trimmed → not enough key values") and the exact boundary cases, which the code only implies. |
| `Feature.DATA_QUERY_DETAIL` | ➖ Not assessed — the skill only tests `detail` on **structure** queries, not on data queries. |
| — | ➕ **Bonus findings not encoded in the driver:** 10 % of flows return **413** even with `lastNObservations=1`; 404-on-unknown-code semantics; 40 duplicate display names with zero descriptions; `detail=allcompletestubs` → 400. |

**Score: 4 of 6 implemented quirks rediscovered independently, 1 after a hint, 1 partial —
plus 4 issues the current driver does not address.**

### Gaps in the skill worth fixing

> **Status: all seven applied** to `.agents/skills/evaluate-source/SKILL.md` — as a new
> Section 6b ("Probe the *query shapes* the client will emit"), reinforced 6a assertions,
> two new guiding principles, extra checklist items, a query-shape table in the issue
> template, and a dedicated INSEE case study.

1. **Probe the qualified flow-ref form.** Section 4 only exercises `data/{flow}/...`.
   Add a check of `data/{agency},{id},{version}/...` with **both raw and percent-encoded
   commas** — this is the shape sdmx-dl actually emits, and it is what hid Q7.
2. **Probe partial keys, not just `all`.** Section 6a checks `all/all` only; the
   trailing-wildcard 400 (Q3) is invisible from that. Recommend adding: full key,
   leading-wildcard, middle-wildcard, **trailing-wildcard**, and all-wildcard.
3. **Compare data formats.** Add a step contrasting `genericdata` vs
   `structurespecificdata` responses; the default-media-type quirk lives there.
4. **Read the response body of 4xx/5xx.** The single most valuable clue of this evaluation
   (`Not enough key values in query, expecting 10 got 2`) came from the error body, which
   the procedure never asks to capture.
5. **Cross-check DSD dimension ids against data attribute names.** Section 6a checks
   "key size == dimension count" only via the DSD; making the comparison *nominal*
   (attribute names vs dimension ids) is what turned Q2 from a number into a diagnosis.
6. **Add HTTP 413 to the expected failure modes** in 6a alongside 404/500.
7. **Test the `detail` parameter on data queries too**, not only on structure queries.

