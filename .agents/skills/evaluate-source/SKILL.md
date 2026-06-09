---
name: evaluate-source

description: >-
  Evaluate a candidate SDMX data source for inclusion in sdmx-dl.
  Use when the user wants to assess, onboard, or create a Forge issue for a new SDMX web source.
---

# Evaluate a new SDMX data source

Collect information about a candidate SDMX web source, probe its REST endpoint,
determine the best driver, and produce a Forge-issue-ready summary that can be
used directly to add the source to sdmx-dl.

## 1. Gather inputs from the user

Ask the user for the following before doing anything else:

| Field                 | Required | Description                                                             |
|-----------------------|----------|-------------------------------------------------------------------------|
| **Endpoint**          | ✅        | Base URL of the SDMX REST API (e.g. `https://data.api.abs.gov.au/rest`) |
| **Website**           | ✅        | URL of the public data portal (e.g. `https://explore.data.abs.gov.au`)  |
| **API documentation** | ❌        | URL of the API/developer documentation, if any                          |

## 2. Read the API documentation

If an API documentation URL was provided, fetch and read it **before** issuing
any requests to the endpoint. Look for:

- SDMX version (2.0, 2.1, 3.0)
- Any non-standard behavior, custom headers, authentication scheme
- Rate-limiting or quota information
- Whether the service is based on a known platform
  (.Stat Suite, Fusion Metadata Registry, SDMX Global Registry, etc.)
- The exact base URL of the REST endpoint (may differ from what the user provided)

If no documentation URL was provided, proceed directly to probing.

## 3. Probe the endpoint

Issue the following HTTP GET requests in order.
If HTTP tools are unavailable, ask the user to run them manually (e.g. with `curl` or a browser) and paste the responses.

### 3a. List all dataflows (baseline reachability)

```
GET {endpoint}/dataflow/all/all/all
Accept: application/xml
```

Observe:
- **HTTP status** — 200 means the endpoint is reachable and speaks SDMX 2.1 REST.
  A 404 or HTML error page may indicate an SDMX 2.0 endpoint or a non-standard path.
- **Response `Content-Type`** — should be `application/xml` or
  `application/vnd.sdmx.structure+xml`.
- **Languages** — collect the `xml:lang` attributes present in `Name` elements
  across the response (e.g. `en`, `fr`, `de`).
- **Authentication** — a 401/403 means credentials are required.

### 3b. Test `detail` parameter support

```
GET {endpoint}/dataflow/all/all/all?detail=allcompletestubs
```

- **200** → `detailSupported=true`
- **400 / 501 / ignored** → `detailSupported=false`

### 3c. Test trailing-slash quirk

```
GET {endpoint}/dataflow/all/all/all/
```

- If this returns 200 but `{endpoint}/dataflow/all/all/all` (no slash) returns 404
  → `trailingSlash=true`

## 4. Select the driver

Choose **exactly one** driver based on the table below.
Prefer `RI_SDMX21` whenever the endpoint is a standard SDMX 2.1 REST service.

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

> If the API documentation or probe reveals that the service is built on a
> well-known platform listed above, prefer the matching dialect driver.

## 5. Determine source properties

Based on the probe results, decide which optional properties to include:

| Property                        | Value  | When to set                                  |
|---------------------------------|--------|----------------------------------------------|
| `sdmxdl.driver.detailSupported` | `true` | Probe 3b returned 200                        |
| `sdmxdl.driver.trailingSlash`   | `true` | Probe 3c revealed trailing-slash requirement |

Leave properties out entirely if not needed — do not set them to `false`.

## 6. Suggest a source ID

Derive a short, unique ID in `SCREAMING_SNAKE_CASE` from the organization acronym
or a well-known abbreviation (e.g. `ABS`, `ECB`, `SWISS_STAT`).

Check `docs/assets/sources.csv` in the sdmx-dl repository to confirm the ID is
not already taken.

## 7. Collect display names

From the probe responses and the website, collect the official name of the
organization in every language whose `xml:lang` appeared in the dataflow response.
Always include English (`en`).

## 8. Produce the Forge issue summary

Print a Markdown document with the following structure, filling every section
with the information gathered above.

---

```markdown
## New source: <Display name in English>

### Source metadata

| Field | Value |
|-------|-------|
| **ID** | `<SCREAMING_SNAKE_CASE>` |
| **Driver** | `<DRIVER_ID>` |
| **Endpoint** | `<endpoint URL>` |
| **Website** | `<website URL>` |
| **Languages** | `<comma-separated BCP 47 language tags>` |
| **Confidentiality** | `PUBLIC` |
| **API documentation** | `<URL or "N/A">` |

### Probe results

| Check | Result |
|-------|--------|
| Reachability | ✅ / ❌ |
| SDMX version | 2.1 / 2.0 / unknown |
| `detail` parameter supported | ✅ / ❌ |
| Trailing-slash required | ✅ / ❌ |
| Authentication required | ✅ / ❌ |

### Properties

<!-- List only the properties that need to be set, one per line.
     Leave this section empty if no properties are needed. -->
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
- For a dialect: the matching class in `sdmx-dl-provider-dialects/`
- For a new dialect: a new class must be created under `sdmx-dl-provider-dialects/`

### Open questions

<!-- List any uncertainties that need follow-up before the source can be merged. -->
- [ ] Confirm endpoint stability / official support
- [ ] Verify all supported languages

```
---

> Omit any table row or section for which no information is available.
> Prefer accuracy to completeness — flag unknowns explicitly.

