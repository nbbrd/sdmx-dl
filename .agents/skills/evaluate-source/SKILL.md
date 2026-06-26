---
name: evaluate-source

description: >-
  Evaluate a candidate data source for inclusion in sdmx-dl, whether it is a standard
  SDMX REST service or a proprietary (non-SDMX) API that needs a custom dialect driver.
  Use when the user wants to assess, onboard, or create a Forge issue for a new web source.
---

# Evaluate a new data source

Collect information about a candidate web source, probe its endpoint **end-to-end**,
determine the best driver (existing or new), and produce a summary that can be used
directly to add the source to sdmx-dl.

## Guiding principles (read first)

These rules are the difference between an evaluation that *looks* done and one that
actually works at runtime. They were learned the hard way (see the INE case study at
the end).

1. **Trust live responses, not documentation.** Documentation and reference clients are
   frequently wrong, outdated, or describe a different endpoint mode. Every claim in the
   evaluation must be backed by an actual response you captured. When docs and live data
   disagree, the live data wins — and note the discrepancy.
2. **Verify the whole data path, not just reachability.** A source that lists flows can
   still fail to return a single observation. Always probe the full chain:
   `databases → flows → structure (DSD) → data`, and confirm at least one real
   observation comes back.
3. **Capture raw payloads as you go.** Save the actual JSON/XML responses you rely on;
   they become the unit-test fixtures and the evidence behind every mapping decision.
4. **Prefer the smallest real example.** Identify one concrete, confirmed-working
   `database / flow / key` tuple with few series and a known observation. It drives the
   web-query test and the demo.
5. **Flag unknowns explicitly.** Accuracy beats completeness. Mark anything you could not
   confirm rather than guessing.

## 1. Gather inputs from the user

Ask the user for the following before doing anything else:

| Field                 | Required | Description                                                             |
|-----------------------|----------|-------------------------------------------------------------------------|
| **Endpoint**          | ✅        | Base URL of the API (e.g. `https://data.api.abs.gov.au/rest`)           |
| **Website**           | ✅        | URL of the public data portal (e.g. `https://explore.data.abs.gov.au`)  |
| **API documentation** | ❌        | URL of the API/developer documentation, if any                          |
| **Reference client**  | ❌        | Any existing client library (R/Python package, etc.) worth cross-checking |

## 2. Read the API documentation

If an API documentation URL was provided, fetch and read it **before** issuing
any requests to the endpoint. Look for:

- Whether it is SDMX at all, and which version (2.0, 2.1, 3.0)
- The exact base URL of the REST endpoint (may differ from what the user provided)
- Authentication scheme, custom headers, rate-limiting or quota information
- Whether the service is based on a known platform
  (.Stat Suite, Fusion Metadata Registry, SDMX Global Registry, etc.)
- For proprietary APIs: the response format, how series are identified, how language is
  selected, and any pagination scheme

> Treat documentation as a hypothesis to verify, not a fact. Note its version/date so you
> can later record where it diverged from live behaviour.

If no documentation URL was provided, proceed directly to probing.

## 3. Probe reachability and classify the source

Issue requests with HTTP tools. If HTTP tools are unavailable, ask the user to run them
(e.g. with `curl` or a browser) and paste the responses. **Save each response body** you
will rely on.

### 3a. SDMX baseline — list all dataflows

```
GET {endpoint}/dataflow/all/all/all
Accept: application/xml
```

Observe:
- **HTTP status** — 200 means reachable and likely SDMX 2.1 REST. A 404, an HTML error
  page, or a JSON body means it is *not* a standard SDMX 2.1 endpoint.
- **Response `Content-Type`** — `application/xml` or `application/vnd.sdmx.structure+xml`
  for SDMX; anything else (`application/json`, …) signals a proprietary API.
- **Languages** — collect the `xml:lang` attributes present in `Name` elements.
- **Authentication** — a 401/403 means credentials are required.

### 3b. Classify

- **SDMX 2.1 REST** (XML structure response, 200) → continue with **Section 4 (SDMX path)**.
- **Proprietary / non-SDMX** (JSON, HTML, non-standard paths, 404 on the SDMX path) →
  continue with **Section 5 (Custom dialect path)**.
- **SDMX 2.0 or legacy** → use a `CONNECTORS_*` driver (see Section 4 table) and verify
  end-to-end as in Section 6.

---

## 4. SDMX path — confirm quirks and select a driver

### 4a. Test `detail` parameter support

```
GET {endpoint}/dataflow/all/all/all?detail=allcompletestubs
```

- **200** → `detailSupported=true`
- **400 / 501 / ignored** → `detailSupported=false`

### 4b. Test trailing-slash quirk

```
GET {endpoint}/dataflow/all/all/all/
```

- If this returns 200 but `{endpoint}/dataflow/all/all/all` (no slash) returns 404
  → `trailingSlash=true`

### 4c. Select the driver

Choose **exactly one** driver. Prefer `RI_SDMX21` whenever the endpoint is a standard
SDMX 2.1 REST service.

| Driver              | When to use                                                            |
|---------------------|------------------------------------------------------------------------|
| `RI_SDMX21`         | Standard SDMX 2.1 REST, no known quirks — **default choice**           |
| `DIALECTS_ESTAT`    | Eurostat Dissemination API or EC DG portals built on the same platform |
| `DIALECTS_BBK`      | Deutsche Bundesbank API (specific pagination and error handling)       |
| `DIALECTS_IMF`      | IMF Data API (`api.imf.org`)                                           |
| `DIALECTS_INSEE`    | INSEE BDM API (requires `noCommaEncoding`)                             |
| `DIALECTS_STATCAN`  | Statistics Canada WDS REST API                                         |
| `DIALECTS_UIS`      | UNESCO UIS API                                                         |
| `DIALECTS_DOTSTAT`  | Any .Stat Suite deployment                                             |
| `CONNECTORS_SDMX21` | Legacy SDMX 2.1 endpoint that does not conform to the RI client        |
| `CONNECTORS_SDMX20` | SDMX 2.0 endpoint                                                      |

> If docs or probes reveal a well-known platform listed above, prefer the matching dialect.

### 4d. Source properties

| Property                        | Value  | When to set                                  |
|---------------------------------|--------|----------------------------------------------|
| `sdmxdl.driver.detailSupported` | `true` | Probe 4a returned 200                        |
| `sdmxdl.driver.trailingSlash`   | `true` | Probe 4b revealed trailing-slash requirement |

Leave properties out entirely if not needed — do not set them to `false`.

Then go to **Section 6 (end-to-end verification)** and **Section 7 (output)**.

---

## 5. Custom dialect path — proprietary / non-SDMX sources

When no existing driver fits, the source needs a **new dialect driver** in
`sdmx-dl-provider-dialects` (e.g. `sdmxdl.provider.dialects.drivers.XxxDialectDriver`).
This is a larger effort; the evaluation must de-risk it by mapping and **verifying every
operation against live responses**.

### 5a. Map the proprietary model onto the sdmx-dl model

Produce an explicit mapping table. The sdmx-dl `Connection` operations you must satisfy:

| sdmx-dl operation                | What it needs from the source                          |
|----------------------------------|--------------------------------------------------------|
| `getDatabases()`                 | The top-level grouping (optional; may be a single one) |
| `getFlows(DatabaseRef)`          | The list of addressable datasets (flows)               |
| `getMeta(...)` → `Structure`     | The DSD: ordered dimensions + codelists                |
| `getData(...)` → `Series`/`Obs`  | Series keys + observations (period + value)            |

For each, record the exact endpoint, parameters, and the response fields used.

### 5b. Resolve the hard questions — each must be answered from live data

These are the questions that, if guessed, break the driver at runtime:

1. **Endpoint mode matters.** The same endpoint may have several modes/flags
   (e.g. "friendly" vs "with-metadata"). Confirm *which mode returns the fields you need*.
   A response that omits the metadata array makes series keys impossible to build.
2. **Series-key identity.** Determine exactly which field identifies each dimension and
   each code. **Verify the code component is unique** within its dimension — labels or
   "official codes" often collide and cannot be used as keys; an internal numeric id may
   be the only safe choice.
3. **Field presence differs per endpoint.** A field available in the structure endpoint
   may be absent (or renamed) in the data endpoint. Pick fields available in *both*, or
   reconcile them, so structure and data stay consistent.
4. **Dimension ordering must be deterministic.** Choose an explicit, reproducible order
   (e.g. natural order of an id/name) and use the *same* order when building the DSD and
   the keys.
5. **Period/frequency derivation.** Confirm where granularity comes from (an explicit
   periodicity id, or a period token like `M12`/`QI`, or the timestamp shape). Use the
   timestamp for the period start.
6. **Reachable-by-id ≠ listed.** An item may resolve by direct id yet not appear in its
   parent listing (so flow validation rejects it). Pick a test example that is actually
   returned by the listing endpoint.
7. **Error semantics.** Find out how "no match"/"empty" is signalled. Some APIs return
   HTTP 500 (not an empty 200) for unknown filter values — the driver must translate that
   into an empty dataset, and/or pre-validate keys against the DSD.
8. **Language selection.** Header (`Accept-Language`) vs path/parameter vs parallel base
   URLs — confirm by comparing responses.
9. **Pagination & limits.** Check page sizes and how to cap payloads for structure-only
   calls (fetch metadata without all observations).

### 5c. Capture fixtures

Save the real responses for: the database list, the flow list, a structure-bearing
response, and a small data response. These become the unit-test resources
(`src/test/resources/.../xxx-*.json`) and the evidence for the mapping.

### 5d. Plan the implementation

List the classes/changes required, mirroring an existing dialect driver:
the `Driver` SPI class (declaring the `WebSource` via `DriverSupport`), an HTTP client
wrapping the endpoints, JSON/XML parsing, period parsing, plus updating
`module-info.java` and `META-INF/services/sdmxdl.web.spi.Driver`.

> `docs/assets/sources.csv` is **generated** from the `WebSource` declarations in the
> driver classes — never edit it by hand.

---

## 6. End-to-end verification (mandatory for every source)

Before writing the summary, confirm the **entire** data path against the live service and
record one concrete working example:

- [ ] `getDatabases()` returns at least the expected grouping (or N/A)
- [ ] `getFlows(...)` returns the target flow **in its listing**
- [ ] `getMeta(...)` yields a structure with the expected dimension count
- [ ] `getData(...)` returns ≥ 1 series with ≥ 1 real observation (period + value)
- [ ] The chosen key components are unique and reproduce the same key on re-fetch
- [ ] Record the working tuple: `source / database / flow / key` + expected counts

This working tuple feeds the web-query test (CSV row) and the runnable demo.

## 7. Source ID, names, confidentiality

- **ID**: short, unique `SCREAMING_SNAKE_CASE` from the org acronym (e.g. `ABS`, `ECB`).
  Check `docs/assets/sources.csv` to confirm it is not already taken, and watch for
  collisions with similarly named organisations.
- **Display names**: official name in every language confirmed from responses/website;
  always include English (`en`).
- **Confidentiality**: `PUBLIC` unless authentication is required.

## 8. Produce the output

For an **existing-driver SDMX source**, print the Forge issue summary below.
For a **new dialect**, additionally write a full evaluation document under
`.agents/evaluations/<ID>.md` capturing the Section 5 mapping, the resolved hard
questions (with live evidence), the fixtures, the request-cost model, and the
implementation work items.

---

```markdown
## New source: <Display name in English>

### Source metadata

| Field | Value |
|-------|-------|
| **ID** | `<SCREAMING_SNAKE_CASE>` |
| **Driver** | `<DRIVER_ID or "new dialect: XxxDialectDriver">` |
| **Endpoint** | `<endpoint URL>` |
| **Website** | `<website URL>` |
| **Languages** | `<comma-separated BCP 47 language tags>` |
| **Confidentiality** | `PUBLIC` |
| **API documentation** | `<URL or "N/A">` |

### Probe results

| Check | Result |
|-------|--------|
| Reachability | ✅ / ❌ |
| Protocol | SDMX 2.1 / SDMX 2.0 / proprietary (<format>) |
| `detail` parameter supported | ✅ / ❌ / N/A |
| Trailing-slash required | ✅ / ❌ / N/A |
| Authentication required | ✅ / ❌ |

### End-to-end verification

| Step | Result |
|------|--------|
| Databases | ✅ / ❌ / N/A |
| Flows (target listed) | ✅ / ❌ |
| Structure (dimension count) | ✅ <n> / ❌ |
| Data (≥1 obs) | ✅ / ❌ |
| Confirmed example | `<source> / <database> / <flow> / <key>` |

### Properties

<!-- List only the properties that need to be set, one per line. Leave empty if none. -->
- `sdmxdl.driver.detailSupported=true`

### Display names

| Language | Name |
|----------|------|
| `en` | <English name> |
| `fr` | <French name, if available> |

### Suggested Java snippet

To be added in the appropriate driver class
(e.g. `sdmx-dl-provider-ri/src/main/java/sdmxdl/provider/ri/drivers/Sdmx21RiDriver.java`):

​```java
.source(WebSource
        .builder()
        .id("<ID>")
        .name("en", "<English name>")
        // add further .name() calls for each additional language
        .driver(<DRIVER_CONSTANT>)
        .confidentiality(PUBLIC)
        .endpointOf("<endpoint URL>")
        // add .propertyOf(...) calls for each property
        .websiteOf("<website URL>")
        .build())
​```

### Target file

<!-- State which Java file should be edited to register this source. -->
- For standard SDMX 2.1: `sdmx-dl-provider-ri/src/main/java/sdmxdl/provider/ri/drivers/Sdmx21RiDriver.java`
- For an existing dialect: the matching class in `sdmx-dl-provider-dialects/`
- For a new dialect: a new class under `sdmx-dl-provider-dialects/` plus a
  `.agents/evaluations/<ID>.md` design document

### Open questions

<!-- List any uncertainties that need follow-up before the source can be merged. -->
- [ ] Confirm endpoint stability / official support
- [ ] Verify all supported languages
```
---

> Omit any table row or section for which no information is available.
> Prefer accuracy to completeness — flag unknowns explicitly.

## Common pitfalls checklist (lessons learned)

Before declaring an evaluation done, sanity-check against these recurring traps:

- [ ] Mapping is based on **live responses**, not just docs or a reference client
- [ ] The **full path** databases→flows→structure→data was exercised, not just listing
- [ ] At least **one real observation** was retrieved with the proposed mapping
- [ ] The **endpoint mode/flags** that return the needed metadata are confirmed
- [ ] The **key code component is unique** (no collisions across dimensions)
- [ ] Fields used exist in **both** the structure and data responses (or are reconciled)
- [ ] **Dimension order** is deterministic and identical for DSD and keys
- [ ] **Period granularity** source is confirmed (id / token / timestamp shape)
- [ ] The test example is **listed by its parent**, not only reachable by id
- [ ] **Empty/error semantics** (e.g. HTTP 500 for unknown values) are handled
- [ ] Raw response **fixtures were saved** for tests
- [ ] Discrepancies between docs and live behaviour are **documented**

### Case study: INE (proprietary JSON, new dialect)

The INE source (Spain) was initially evaluated and implemented from documentation and an
R reference client. It compiled but returned **no data at runtime** because several
documented assumptions were wrong against the live API:

- The structure endpoint suggested by docs (`SERIES_TABLA`, `det=1` nested `Variable`)
  did not return usable fields; data had to be built from the **data endpoint in
  metadata mode** (`DATOS_TABLA?tip=AM`) — the friendly mode omitted the metadata array.
- The variable was a flat **name string** in the data endpoint, not a nested id object.
- The "official code" collided across variables (`"00"` reused); the **value id** was the
  only safe key component.
- Periodicity was **not** in an id field; it had to be derived from the period token.
- A table was reachable by id but **not listed** under its operation, so flow validation
  rejected the documented example; a different, listed table was required.
- Unknown filter values returned **HTTP 500**, not an empty 200.

The fix was to drive every decision from captured live payloads and to verify the full
databases→flows→structure→data path end-to-end. This checklist exists so the next
evaluation catches these at evaluation time, not after implementation.
