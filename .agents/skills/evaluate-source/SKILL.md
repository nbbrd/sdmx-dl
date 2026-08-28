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
actually works at runtime. They were learned the hard way (see the ASTAT, INSEE and INE
case studies at the end).

1. **Trust live responses, not documentation.** Documentation and reference clients are
   frequently wrong, outdated, or describe a different endpoint mode. Every claim in the
   evaluation must be backed by an actual response you captured. When docs and live data
   disagree, the live data wins — and note the discrepancy.
2. **Verify the whole data path, not just reachability.** A source that lists flows can
   still fail to return a single observation. Always probe the full chain:
   `databases → flows → structure (DSD) → data`, and confirm at least one real
   observation comes back.
3. **Capture raw payloads as you go — including error bodies.** Save the actual JSON/XML
   responses you rely on; they become the unit-test fixtures and the evidence behind every
   mapping decision. Do **not** discard 4xx/5xx bodies: the message inside a 400 often
   states the real rule (`"Not enough key values in query, expecting 10 got 2"`) and turns
   a mysterious failure into a one-line diagnosis.
4. **Probe the requests your client will actually send.** A source can serve every URL you
   type by hand and still fail in production because sdmx-dl emits a *different shape* of
   the same request: a qualified `agency,id,version` flow ref, a percent-encoded separator,
   a partially-wildcarded key, a different `Accept` media type. Reproduce those shapes
   explicitly (Section 6b) — this is the single most common source of "it worked in my
   browser" evaluations.
5. **Prefer the smallest real example.** Identify one concrete, confirmed-working
   `database / flow / key` tuple with few series and a known observation. It drives the
   web-query test and the demo.
6. **One working example proves nothing about the catalog.** A hand-picked flow can work
   while a large fraction of the source is broken. *Always* sample several flows across the
   listing — this applies to **standard SDMX sources too**, not only to inferred models.
7. **Read the whole issue, including comments.** When the source comes from a tracker issue,
   fetch the comments as well: earlier investigations often already recorded the quirk that
   invalidates the naive reading of the issue body.
8. **A standard protocol does not mean a standard-compliant service.** A textbook SDMX 2.1
   endpoint can still need a dialect because its *content* is inconsistent (DSD ids that do
   not match the data payload, codelists missing from the DSD) or its *query layer* is
   picky (encoding, wildcards, media types). Do not conclude `RI_SDMX21` from a 200 on the
   flow listing — conclude it only after Sections 6a and 6b are green.
9. **Flag unknowns explicitly.** Accuracy beats completeness. Mark anything you could not
   confirm rather than guessing.

## 1. Gather inputs from the user

Ask the user for the following before doing anything else:

| Field                 | Required | Description                                                             |
|-----------------------|----------|-------------------------------------------------------------------------|
| **Endpoint**          | ✅        | Base URL of the API (e.g. `https://data.api.abs.gov.au/rest`)           |
| **Website**           | ✅        | URL of the public data portal (e.g. `https://explore.data.abs.gov.au`)  |
| **API documentation** | ❌        | URL of the API/developer documentation, if any                          |
| **Reference client**  | ❌        | Any existing client library (R/Python package, etc.) worth cross-checking |
| **Tracker issue**     | ❌        | Issue describing the source                                             |

If a tracker issue is given, read **the body *and* every comment** before probing:

```
GET https://api.github.com/repos/<owner>/<repo>/issues/<n>
GET https://api.github.com/repos/<owner>/<repo>/issues/<n>/comments
```

Comments frequently contain a prior investigation that contradicts the body (wrong driver,
non-standard structure query, broken endpoint). Treat the issue body as a *proposal*, not a
specification — in particular the suggested driver and endpoint must be re-verified.

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

Test it on **data** queries too — support is not necessarily the same on both resources:

```
GET {endpoint}/data/{flow}/all/all?detail=serieskeysonly&lastNObservations=1
```

### 4b. Test trailing-slash quirk

```
GET {endpoint}/dataflow/all/all/all/
```

- If this returns 200 but `{endpoint}/dataflow/all/all/all` (no slash) returns 404
  → `trailingSlash=true`

### 4c. Audit the catalog — do **not** skip this

A 200 on the flow listing only proves the *listing* works. Before trusting the endpoint,
inspect the DSD reference of **every** flow in the listing and group them:

```
//structure:Dataflow/structure:Structure/Ref/@agencyID + '/' + @id
```

Then check for these red flags:

1. **Placeholder / shared DSD.** If many flows point at the *same* reference — especially
   one named `VIRTUAL*`, `GENERIC*`, `DEFAULT*`, or owned by a different agency than the
   flows — fetch it. A DSD with 1–2 dimensions serving hundreds of flows is a **stub**, and
   those flows are unusable: `getMeta` returns garbage and `getData` typically 404s.
2. **Unresolvable references.** Resolve a few distinct refs with
   `/datastructure/{agency}/{id}/{version}?references=children`. A 404 means the flow is
   listed but not usable.
3. **Quantify the damage.** Report the split explicitly, e.g. *"260 flows listed, 122 point
   at `MDM:VIRTUALDSD` → broken"*. A source where a large fraction of flows cannot return
   data must not be registered as-is.

### 4d. Look for sibling deployments

When part of the catalog is broken, the missing structures often live on a **parallel
deployment of the same host** — a different path segment, subdomain, or service name
(`/dsm/…` vs `/esd/…`, `…/public/` vs `…/internal/`, `ws1` vs `ws2`). Probe the variants
before concluding a custom dialect is needed.

If a sibling exists, characterise the relationship precisely, because it drives the design:

| Question | How to answer |
|----------|---------------|
| Are the catalogs **disjoint, overlapping, or nested**? | Diff the two flow-id sets |
| Does data work on each side? | Sample-fetch flows from both |
| Are shared flows exposed under the **same ids**? | Compare the intersection; watch for prefixed variants like `DF_X` vs `DF_ISTAT_X` |

Outcomes:

- **Nested** (one is a superset and fully works) → register only that one, plain driver.
- **Disjoint / complementary** → either register **two sources**, or write a dialect that
  routes per flow. Prefer two sources unless the split is invisible to users.
- **Overlapping with id mismatches** → merging requires an id-mapping table; document the
  maintenance cost before choosing it.

### 4e. Select the driver

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

> A recognised platform (NSI WS, .Stat Suite, Fusion, …) is **not** a guarantee of a healthy
> catalog. The platform tells you the *protocol*; sections 4c/4d tell you whether the
> *deployment* actually serves its flows. Run both.

> `RI_SDMX21` is a *provisional* choice at this point. Confirm it only after Section 6a
> (content consistency: dimension ids, codelists, per-flow data) and Section 6b (query
> layer: encoding, wildcards, media types) come back clean. Any fix that cannot be expressed
> as a `sdmxdl.driver.*` property means a dialect — see the INSEE case study.

### 4f. Source properties

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
Also note any listing field that **distinguishes otherwise same-named entries** (frequency,
variant/base-year code, covered period, …) and map it to `Flow.description` — see 5b item 6.

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
4. **Period/frequency derivation.** Confirm where granularity comes from (an explicit
   periodicity id, or a period token like `M12`/`QI`, or the timestamp shape). Use the
   timestamp for the period start.
5. **Reachable-by-id ≠ listed.** An item may resolve by direct id yet not appear in its
   parent listing (so flow validation rejects it). Pick a test example that is actually
   returned by the listing endpoint.
6. **Display names are often not unique.** Several flows in one source can share an
   *identical* name (e.g. a quarterly and an annual table, or base-year/classification
   variants). The name alone then cannot tell them apart. Check the listing response for
   the fields that *do* distinguish them (frequency, variant/base-year code, covered
   period, publication) and surface them — map them to `Flow.description` rather than
   discarding them. Accept that a few entries may be indistinguishable by metadata alone;
   only their ref id and actual content separate those. **Do not drop fields you don't
   immediately map** — a field that looks redundant is often the only discriminator.
7. **Error semantics.** Find out how "no match"/"empty" is signalled. Some APIs return
   HTTP 500 (not an empty 200) for unknown filter values — the driver must translate that
   into an empty dataset, and/or pre-validate keys against the DSD.
8. **Language selection.** Header (`Accept-Language`) vs path/parameter vs parallel base
   URLs — confirm by comparing responses.
9. **Pagination & limits.** Check page sizes and how to cap payloads. Beware of very
   large payloads: **stream the parse** rather than buffering the whole body (huge tables
   can cause `OutOfMemoryError`), and cap slow/large fetches with a timeout.
10. **Transient "busy" responses.** Some APIs return **HTTP 200 with a status/placeholder
    body** ("request in process", empty object, …) instead of the real payload. Detect
    these and turn them into a retryable error — don't parse them as data.

### 5c. Deriving the dimension model — the hardest part (do not shortcut)

When there is no pre-built DSD, you must *infer* dimensions and codelists from series
metadata. This is where naïve implementations silently return no data. **You cannot infer
the model from one series** — you must inspect *many* series across the table. Confirm each
of the following against live data, because they are frequently all true at once:

1. **Series are heterogeneous.** Different series in the *same* table can carry different
   variable sets (different arity, different variables). Never assume a single series is
   representative.
2. **Metadata order is not stable.** The same variable may appear at different positions
   across series. **Do not key dimensions by position** — key them by identity, then order
   deterministically.
3. **Mutually-exclusive variables must be merged.** Several variables can be alternatives
   for the same conceptual dimension and never co-occur (e.g. *National totals* vs
   *Regions* vs *Municipalities* for geography). If you make each a separate dimension,
   every series leaves the others empty and **no fully-specified key can ever match**.
   Merge variables that provably never co-occur into one dimension whose codelist is the
   union of their values.
4. **The same variable name can repeat within one series.** That legitimately means two
   distinct dimensions — keying by name alone collapses them and makes keys collide.
5. **Robust approach — co-occurrence signature.** Model each metadata item as
   `(variable identity, occurrence rank within the series)`. Items that share the identical
   set of co-occurring items (⇒ provably never co-occur) merge into one dimension; repeated
   names split into distinct dimensions. This is order-independent, merges alternatives, and
   handles repeats — all at once.
6. **A wildcard/empty code is not a valid key component.** In sdmx-dl an empty string is
   the wildcard code; a *series* key must be fully specified. If a merged/absent slot has
   no natural value, fill it with an explicit "not applicable" sentinel code registered in
   the codelist — never leave it empty.

### 5d. Single-fetch consistency

Where feasible, build **both** the `Structure` and the `DataSet` from **one** response.
Sources can return *different labels for the same variable* across two separate calls
(structure call vs data call), which misaligns dimensions. One cached fetch that feeds
both `getMeta` and `getData` guarantees they can never disagree.

### 5e. Capture fixtures

Save the real responses for: the database list, the flow list, a structure-bearing
response, and a small data response. These become the unit-test resources
(`src/test/resources/.../xxx-*.json`) and the evidence for the mapping. **Capture at
least one table whose series are heterogeneous** (mixed variables / mutually-exclusive
alternatives) — it is the regression fixture that locks the dimension model.

### 5f. Plan the implementation

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
- [ ] **A random sample of other flows also returns data** (see 6a) — not just the example
- [ ] **The query shapes the client will actually emit behave** (see 6b) — qualified/encoded
      refs, partial keys, media types, error semantics
- [ ] Record the working tuple: `source / database / flow / key` + expected counts
- [ ] Record the **share of the catalog that works**, if it is not 100 %

This working tuple feeds the web-query test (CSV row) and the runnable demo.

### 6a. Don't stop at a few hand-picked examples — sample randomly and check invariants

A handful of curated examples can pass by luck while a large part of the source is broken.
This bit both INE (wrong inferred model) and ASTAT (half the flows unusable), so **sample
for every source**, standard or not.

**Minimal sample (every source, done during evaluation).** Pick ~10 flows with a fixed seed,
stratified over whatever grouping the audit in 4c revealed (e.g. per DSD, or
placeholder-DSD vs real-DSD), and for each run
`data/{flow}/all/all?lastNObservations=1`. Assert:

- HTTP 200 — record every **404 / 500 / 413**. A 404/500 means the flow is listed but not
  served; a **413 (payload too large)** means the flow cannot be fetched as a whole and
  needs a narrowed key. Both are real limitations that belong in the report.
- ≥ 1 series returned
- **key size == dimension count** of the flow's DSD
- **the dimension *names* match too**, not just the count: compare the DSD dimension ids
  with the attribute names actually present on the series (or the `Value id`s in generic
  data). A count-only check silently passes when the service publishes a DSD whose
  dimension ids differ from the data payload (`DATA_DOMAIN6` in the DSD vs `DATA_DOMAIN`
  in the data), which produces **empty keys for every series** of those flows.
- **codelist completeness**: for each dimension, check that its codelist actually carries
  codes in the structure response. A declared-but-empty codelist means the driver must
  re-fetch it separately.

Report the results as a table. A stratified sample is what turns "it works" into
"138 of 260 flows work".

**Full harness (sources with an *inferred* dimension model).** Add a seeded, random-sampling
test (tagged so it only runs on demand) that fetches many live tables and asserts the
properties that **must** hold if the model is correct — rather than checking specific values:

- key size **==** dimension count
- every series key is **fully specified** (no accidental wildcard slot)
- every code in every key is **known to the DSD** (validate the key against the structure)
- keys are **unique** across the table (no silent series collisions)
- each key **round-trips** to exactly its own series

Make it reproducible and bounded: a fixed **seed** (overridable), a cap on the number of
tables, a **per-table timeout** (report oversized tables as skipped instead of hanging),
and an overall **wall-clock budget**. On failure, print the offending flow id and the
exact invariant broken so it is directly actionable. This invariant harness is what turns
"seems to work" into evidence — it repeatedly surfaced structural bugs that no curated
example did.

### 6b. Probe the *query shapes* the client will emit — not just the ones you type

Everything up to here uses hand-written URLs. sdmx-dl does not send those. It sends
qualified refs, percent-encoded separators, partially-wildcarded keys and specific media
types — and each of those can fail on a service that answers your manual probes perfectly.
Run this matrix on **one small, known-good flow** and record the status of every row.

**1. Flow-ref forms and comma encoding.** sdmx-dl may address a flow as
`agency,id,version`. Percent-encoding of the separator is where services break:

| Request | Expected | If it differs |
|---------|----------|----------------|
| `data/{id}/all/all?lastNObservations=1` | 200 | baseline |
| `data/{agency},{id},{version}/all/all?…` (**raw** commas) | 200 | qualified refs unsupported → driver must send the short form |
| `data/{agency}%2C{id}%2C{version}/all/all?…` (**encoded** commas) | 200 | **quirk**: emit raw commas (cf. `sdmxdl.driver.noCommaEncoding`) |

INSEE returns **HTTP 500** on the encoded form and 200 on the raw one. No amount of
hand-typed probing finds this, because a human never types `%2C`.

**2. Key shapes — wildcards are not uniformly supported.** With a flow of *n* dimensions,
try all of these and tabulate:

| Key | Meaning |
|-----|---------|
| `A.B.C.D` (fully specified) | the single-series case |
| `.B.C.D` | **leading** wildcard |
| `A..C.D` | **middle** wildcard |
| `A.B.C.` (last component empty) | **trailing** wildcard |
| `...` (all empty) | everything |
| `all` | everything, keyword form |

Trailing wildcards are the classic trap: a service that trims empty trailing path segments
then rejects the key for having *"not enough key values"*. Also try the documented wildcard
tokens (`*`, `all`, `%20`) in the last position to see whether any workaround exists — and
if none does, note that keys ending in a wildcard must be expanded into a union of codes
(`CODE1+CODE2`) or widened to `all` with client-side filtering.

**3. Data media types.** The default format a client asks for is not always the one the
service serves best. Compare, on the same query:

- `application/vnd.sdmx.structurespecificdata+xml;version=2.1`
- `application/vnd.sdmx.genericdata+xml;version=2.1`
- no `Accept` header at all

Check status, `Content-Type`, size, and that the **series/observations are actually
parseable** in each. A driver often has to pin one specific media type.

**4. Error bodies.** For every non-2xx above, **read and quote the response body**. Services
routinely explain themselves there (`Not enough key values in query, expecting 10 got 2`),
and that one sentence is usually worth more than the whole rest of the probe session.

**5. Language negotiation.** Compare `Accept-Language: en` vs `fr` vs absent. If the payload
is byte-identical, the service inlines all languages and ignores the header — say so, rather
than assuming per-language requests work.

Record the whole matrix in the evaluation document; it is the specification of the dialect.

## 7. Source ID, names, confidentiality

- **ID**: short, unique `SCREAMING_SNAKE_CASE` from the org acronym (e.g. `ABS`, `ECB`).
  Check `docs/assets/sources.csv` to confirm it is not already taken, and watch for
  collisions with similarly named organisations.
- **Display names**: official name in every language confirmed from responses/website;
  always include English (`en`).
- **Confidentiality**: `PUBLIC` unless authentication is required.

## 8. Produce the output

For an **existing-driver SDMX source**, print the Forge issue summary below.
Additionally write a full evaluation document under `.agents/evaluations/<ID>.md` whenever
the source is **not** a clean drop-in, i.e. when any of these hold:

- a **new dialect** is needed → capture the Section 5 mapping, the resolved hard questions
  (with live evidence), the fixtures, the request-cost model, and the implementation work items
- **part of the catalog is broken** or a **sibling deployment** is involved → capture the
  audit numbers, the raw evidence, and a comparison of the possible designs
- **any Section 6b probe deviates from standard behaviour** (encoding, wildcards, media
  type, error semantics, size limits) → capture the full query-shape matrix with the exact
  statuses and error bodies; it is the specification the dialect will be written against
- the evaluation **contradicts the tracker issue** (different driver, different endpoint,
  different conclusion) → capture why, so the discussion has a written basis

When several designs are viable, present them as explicit options with a recommendation and
the trade-off of each — do not silently pick one.

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
| Platform | <e.g. NSI WS v9.11 / .Stat Suite / N/A> |
| `detail` parameter supported | ✅ / ❌ / N/A |
| Trailing-slash required | ✅ / ❌ / N/A |
| Authentication required | ✅ / ❌ |

### Query-shape probes (6b)

<!-- Omit only if every row was the expected/standard behaviour. -->

| Check | Result |
|-------|--------|
| Qualified flow ref `agency,id,version` | ✅ / ❌ |
| Encoded comma `%2C` in path | ✅ / ❌ HTTP <code> |
| Trailing wildcard in key | ✅ / ❌ HTTP <code> |
| Leading / middle wildcard in key | ✅ / ❌ |
| `all` keyword | ✅ / ❌ |
| Data media type served | structure-specific / generic / both |
| `Accept-Language` honoured | ✅ / ❌ (all languages inlined) |
| Unknown code on data | 404 / empty 200 / <other> |
| Oversized flows | ❌ none / ✅ <k>/<m> return 413 |

### Catalog health

<!-- Omit only if every listed flow was shown to resolve and serve data. -->

| Check | Result |
|-------|--------|
| Flows listed | <n> |
| Flows with a resolvable, non-placeholder DSD | <n> |
| Flows returning data (random stratified sample) | <k>/<m> |
| Placeholder / shared DSD detected | ✅ `<agency:id>` on <n> flows / ❌ |
| Sibling deployment found | ✅ `<url>` (<disjoint / nested / overlapping>) / ❌ |

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
- [ ] If the catalog is partly broken: which design option is chosen, and should the
      provider be asked to fix it upstream?
```
---

> Omit any table row or section for which no information is available.
> Prefer accuracy to completeness — flag unknowns explicitly.

## Common pitfalls checklist (lessons learned)

Before declaring an evaluation done, sanity-check against these recurring traps:

- [ ] Mapping is based on **live responses**, not just docs or a reference client
- [ ] The tracker issue's **comments** were read, not just its body
- [ ] The issue's proposed **driver and endpoint were re-verified**, not taken at face value
- [ ] The **full path** databases→flows→structure→data was exercised, not just listing
- [ ] At least **one real observation** was retrieved with the proposed mapping
- [ ] **All** DSD references in the flow listing were grouped and audited (4c)
- [ ] No **placeholder/shared stub DSD** silently serves a large slice of the catalog
- [ ] **Sibling deployments** (other path segment / subdomain) were probed (4d)
- [ ] The **share of usable flows** is known and stated, not assumed to be 100 %
- [ ] A **stratified random sample** of flows returns data, not just the curated example
- [ ] **413** responses were counted alongside 404/500 in the sample
- [ ] DSD **dimension ids were compared by name** with the data payload attribute names
- [ ] Every declared codelist was checked for **actually containing codes**
- [ ] The **qualified `agency,id,version` flow ref** was probed with **raw *and* encoded**
      commas (6b)
- [ ] **Partial keys** were probed — leading, middle and especially **trailing** wildcards
- [ ] **Data media types** (structure-specific vs generic vs none) were compared (6b)
- [ ] **Error bodies of 4xx/5xx were read**, not just their status codes
- [ ] **Frequency codes** were listed and checked against SDMX meanings (e.g. `T`=quarterly,
      `B`=two-monthly are *not* standard)
- [ ] The **endpoint mode/flags** that return the needed metadata are confirmed
- [ ] The **key code component is unique** (no collisions across dimensions)
- [ ] Fields used exist in **both** the structure and data responses (or are reconciled)
- [ ] The dimension model was inferred from **many series, not one** (heterogeneity checked)
- [ ] Metadata **order instability** handled — dimensions keyed by identity, not position
- [ ] **Mutually-exclusive** variables are merged into one dimension (not split)
- [ ] **Repeated variable names** within a series map to distinct dimensions
- [ ] Absent/merged slots use an explicit **"not applicable" code**, never an empty wildcard
- [ ] Structure and data are **consistent** (ideally built from a single fetch)
- [ ] **Period granularity** source is confirmed (id / token / timestamp shape)
- [ ] The test example is **listed by its parent**, not only reachable by id
- [ ] **Non-unique display names** handled — distinguishing fields kept in `description`
- [ ] **Empty/error semantics** (e.g. HTTP 500 for unknown values) are handled
- [ ] **Transient "busy"** 200 responses are detected and made retryable
- [ ] Very large payloads are **streamed** (no whole-body buffering / OOM)
- [ ] **Structure payload sizes** were measured (some DSDs are tens of MB)
- [ ] A **random-sampling invariant test** passes across many live flows (not just a few)
- [ ] Raw response **fixtures were saved** for tests (incl. a heterogeneous table)
- [ ] Discrepancies between docs and live behaviour are **documented**

### Case study: ASTAT (standard SDMX 2.1, half the catalog broken)

ASTAT (Bolzano – South Tyrol) looked like the easiest possible case: a stock **NSI Web
Service v9.11** endpoint, HTTP 200 on `dataflow/all/all/all`, `detail` supported, no auth,
three languages — identical in every respect to the already-registered `ISTAT` source. The
first evaluation concluded "plain `RI_SDMX21`, done" on the strength of one flow that
returned a clean 10-dimension DSD and 27 real observations.

It was wrong, and a **comment on the tracker issue had already said so**:

- 122 of the 260 listed flows referenced a single placeholder DSD, `MDM:VIRTUALDSD`,
  which has **1 dimension**. Those flows return **HTTP 404 on data**. The curated example
  happened to be one of the 138 healthy ones — pure luck.
- The real DSDs existed, but on a **sibling deployment**: `/esd/NSI_WS/rest` instead of
  `/dsm/NSI_WS/rest`. The *same* URL path and query string returns the stub on one and the
  real 14-dimension DSD on the other.
- Refinement of the issue comment: no exotic query (`detail=Full&references=Descendants`)
  was needed — the plain standard `datastructure/…?references=children` works, **on the
  right host path**. The comment had attributed the fix to the query, not the base path.
- The two services are **complementary, not nested**: each 404s on the other's flows. Of
  the ids, 101 match and ~21 are the same tables under prefixed variants
  (`DF_DCAT_X` vs `DF_ISTAT_DCAT_X`), so merging them would require an id-mapping table.
- A structure response on the sibling was **12.6 MB** — a streaming/OOM concern that the
  first evaluation never surfaced.

What actually caught it: grouping the `Structure/Ref` of *every* flow in the listing
(→ `122 x MDM:VIRTUALDSD`), then a seeded, **stratified** sample of 5 placeholder flows and
5 real-DSD flows (→ 5 × 404 vs 5 × OK). Two cheap steps, both now mandatory as sections
4c and 6a.

The lesson: a recognised platform and a green end-to-end example say nothing about the
health of the *catalog*. Audit every DSD reference, sample across the strata you find,
probe for sibling deployments — and read the issue comments first.

### Case study: INSEE (textbook SDMX 2.1 — and six quirks, all in plain sight)

INSEE's BDM service is the counter-example to "standard protocol ⇒ standard driver".
Everything the early sections check is green: HTTP 200 on `dataflow/all/all/all`, real
`application/xml`, no auth, two languages, **244 flows / 232 distinct DSDs, no placeholder
DSD, no sibling deployment**, and a curated example that returns a clean 8-dimension DSD
and a real observation. A Section 4-only evaluation says `RI_SDMX21`. It is wrong: the
shipped driver is a dialect with four `@SdmxFix`es, a custom time parser and a feature flag.

What the deeper sections surface — every one of them from a live response:

- **Codelists missing from the DSD.** `datastructure/FR1/{id}?references=children` declares
  10 codelists and returns 7; the three shared ones (`CL_PERIODICITE`, `CL_UNITE`,
  `CL_ZONE_GEO`) are omitted — identically for `references=descendants` and `references=all`
  — yet each resolves standalone. For the 13 IMF-DSD flows **all five** are omitted. Caught
  by the "declared codelists actually contain codes" check in 6a.
- **DSD dimension ids that don't match the data.** `IMF:ECOFIN_DSD` declares
  `DATA_DOMAIN6, REF_AREA6, …` while the data payload uses `DATA_DOMAIN, REF_AREA, …`, and
  the DSD the data message points to (`IMF:CPI`) **404s**. The key-size check passes
  numerically for FR1 flows and fails silently for IMF ones — only the *by-name* comparison
  added to 6a exposes it (0 of 5 dimensions matched).
- **Trailing wildcards rejected.** `A.IPCH.…​.BRUT.SO` → 200/1 series; the same key with the
  last component empty → **400**. Middle and leading wildcards are fine, `all` is fine, and
  an extra trailing dot is fine. The service explains it in the **error body**:
  `Not enough key values in query, expecting 10 got 2` — trailing empty segments are trimmed,
  then the key is too short. No wildcard token works in the last position.
- **Encoded comma → HTTP 500.** `data/FR1,IPCH-2005,1.0/…` returns 200;
  `data/FR1%2CIPCH-2005%2C1.0/…` returns **500**. This is exactly what
  `sdmxdl.driver.noCommaEncoding` exists for, and it is invisible unless you deliberately
  send the shape the client sends — hence 6b.
- **Non-standard frequencies.** `CL_PERIODICITE` = `A, B, M, S, T`, where `T` means
  quarterly (SDMX uses `Q`) and `B` means **two-monthly** (SDMX `B` is *business daily*).
  The driver needs a P2M reporting-period parser.
- **10 % of flows are too big to fetch.** A seeded 30-flow sample returned 27 × 200 and
  **3 × HTTP 413**, even with `lastNObservations=1`, and narrowing the key does not always
  help. Nothing is broken — the flows simply require a specific key. Worth reporting; the
  current driver does not surface it.

A dry-run of this skill against INSEE rediscovered four of the six implemented quirks on its
own, missed the comma-encoding one entirely (it had only tested commas *inside the key*,
never the qualified flow ref), and only half-noticed the media-type one. Sections 6b, the
by-name dimension check and the "read the error body" rule were all added as a direct
result.

The lesson: when a source looks like a textbook SDMX 2.1 service, the quirks are not in the
catalog — they are in the **content consistency** (ids, codelists, frequency codes) and in
the **query layer** (encoding, wildcards, media types, size limits). Neither shows up until
you reproduce the exact requests the client will make.

### Case study: INE (proprietary JSON, new dialect)

The INE source (Spain) was initially evaluated and implemented from documentation and an
R reference client. It compiled but returned **no data at runtime**, and each subsequent
"fix" revealed a deeper wrong assumption. The full sequence:

**Field/endpoint assumptions (first round):**

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

**Dimension-model assumptions (the hard part — several more rounds):**

- Series in one table are **heterogeneous**: some tables split geography into
  mutually-exclusive variables (National totals vs Autonomous Communities), so the
  union-of-variables DSD left every series with an empty slot and **no key ever matched**.
- Modelling those as separate dimensions was wrong — they had to be **merged**; an empty
  slot is a wildcard, which is illegal for a series key, so absent slots needed an explicit
  `_Z` "not applicable" code.
- A "positional" model (dimension = metadata slot index) then broke because INE returns
  metadata **in inconsistent order** across series.
- Keying purely by variable **name** broke a table that repeats the same variable name
  twice in one series (two genuine dimensions collapsed into one).
- The final model keys dimensions by **co-occurrence signature** (variable identity +
  occurrence rank), which is order-independent, merges alternatives, and splits repeats.
- The structure and data calls sometimes returned **different labels for the same
  variable**; the driver was changed to build both from a **single cached fetch**.
- Large tables caused **OOM** (whole-body buffering) and the API occasionally returned a
  **busy status object with HTTP 200** — handled by streaming the parse and a retryable
  error.

**Non-unique names (found later, in normal use):**

- Two flows with the same name and similar data turned out to be a **quarterly** and an
  **annual** table. Investigation showed *all 9* tables of that operation share the exact
  same name — the driver had parsed only id + name and **discarded** the fields that
  distinguish them (frequency, base-year/classification code, covered period, publication).
  The fix mapped those fields to `Flow.description`. A residual set of tables remained
  identical across *all* listing metadata — only their ref id and content differ, which is
  an inherent limit worth documenting rather than hiding.

**How it was finally trusted:** curated examples kept passing while the model was still
wrong, so a **seeded random-sampling invariant test** was added (key size == dim count,
fully-specified & DSD-valid keys, unique keys, round-trip), bounded by per-table timeouts
and a wall-clock budget. That harness is what actually caught each structural bug.

The lesson: drive every decision from captured live payloads, infer the dimension model
from **many** series, and prove correctness with **invariants over random samples** — not
a few hand-picked keys. This checklist exists so the next evaluation catches these at
evaluation time, not after implementation.
