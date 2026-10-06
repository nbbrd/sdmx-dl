package sdmxdl.grpc.v2;

import static sdmxdl.DatabaseRef.NO_DATABASE_KEYWORD;
import static sdmxdl.HasSearch.AUTO_LIMIT;
import static sdmxdl.HasSearch.NO_QUERY;
import static sdmxdl.Languages.ANY_KEYWORD;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.WrapBusinessError;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import sdmxdl.*;
import sdmxdl.format.protobuf.*;
import sdmxdl.format.protobuf.web.HealthReportDto;
import sdmxdl.format.protobuf.web.WebSourceDto;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptTarget;
import sdmxdl.web.HealthCheck;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebHealthRequest;
import sdmxdl.web.WebSource;
import sdmxdl.web.WebSourcesRequest;

@ApplicationScoped
@RegisterForReflection
@WrapBusinessError({IOException.class, IllegalArgumentException.class})
public class SdmxdlMcpService2 {

    private static final String SOURCE_ARG =
            "Source id exactly as returned by listSources (e.g. 'ECB', 'ESTAT'). Case-sensitive. Never invent a value: call listSources first if unsure.";
    private static final String LANGUAGES_ARG =
            "Language priority list for labels, as comma-separated IETF tags (e.g. 'en', 'fr,en'). Defaults to 'en'; falls back to whatever the source provides when the language is missing.";
    private static final String DATABASE_ARG =
            "Database ref as returned by listDatabases. Leave empty (default) unless the source really exposes several databases.";
    private static final String FLOW_ARG =
            "Flow (dataset) ref exactly as returned by listFlows, e.g. 'EXR' or 'ECB:EXR(1.0)'. Never invent a value: call listFlows first if unsure.";
    private static final String KEY_ARG =
            "Positional dimension filter: one part per dimension, in the exact dimension order given by getMeta/listDimensions, separated by '.'. An empty part matches any code, '+' separates alternatives (e.g. 'M.CHF+USD.EUR.SP00.A') and omitted trailing parts match any code (e.g. 'M.CHF' is 'M.CHF...'). Use 'all' (default) to match every series. Prefer the 'dimensions' map instead, which builds this key for you.";
    private static final String DETAIL_ARG =
            "Amount of information to retrieve: FULL (observations + attributes), DATA_ONLY (observations without attributes, default), NO_DATA (series keys + attributes, no observations), SERIES_KEYS_ONLY (series keys only; cheapest way to discover which series exist).";
    private static final String QUERY_ARG =
            "Free-text search terms (words from ids or labels, e.g. 'unemployment rate'). Leave empty (default) to list everything instead of searching.";
    private static final String MAX_RESULTS_ARG =
            "Maximum number of entries to return (default 10). Use 0 for no limit; raise it when results look truncated.";
    private static final String DIMENSION_ARG =
            "Dimension to inspect: its id exactly as returned by getMeta or listDimensions (e.g. 'CURRENCY'), or its zero-based position in the key (e.g. '1'). Leave empty (default) to inspect the first dimension left as a wildcard in 'key'. The dimension must be a wildcard in 'key'.";
    private static final String CONCEPT_ARG =
            "Dimension or attribute id exactly as returned by getMeta, listDimensions or listAttributes (e.g. 'CURRENCY', 'OBS_STATUS').";
    private static final String DIMENSIONS_ARG =
            "Recommended way to filter series: a map of dimension id to code, e.g. {\"FREQ\":\"M\",\"CURRENCY\":\"CHF\"}. Dimension ids are matched case-insensitively and unlisted dimensions match any code. Several codes can be combined with '+' (e.g. \"CHF+USD\"). Takes precedence over 'key'; an unknown dimension id raises an error listing the valid ids.";
    private static final String LAST_N_ARG =
            "Keep only the N most recent observations of each series (default 20; use 0 for no limit). Applied after the period filters. Increase it or set it to 0 to get a full history.";
    private static final String FIRST_N_ARG =
            "Keep only the N oldest observations of each series (default 0 = no limit). Applied after the period filters. Combining 'firstN' and 'lastN' returns the union of both ends of each series.";
    private static final String START_ARG =
            "Inclusive lower bound on the observation period, as reduced-precision ISO-8601 (e.g. '2000', '2000-01', '2000-01-01'). Omit for no lower bound.";
    private static final String END_ARG =
            "Inclusive upper bound on the observation period, as reduced-precision ISO-8601 (e.g. '2020', '2020-12'). Omit for no upper bound.";
    private static final String SCRIPT_LANGUAGES_ARG =
            "Language priority list for labels, as comma-separated IETF tags (e.g. 'en', 'fr,en'). Defaults to any language (the source default); only set it when the user asks for a specific language.";
    private static final String SCRIPT_LAST_N_ARG =
            "Keep only the N most recent observations of each series (default 0 = no limit, i.e. full history). Applied after the period filters.";
    private static final String SCRIPT_MAX_RESULTS_ARG =
            "Maximum number of entries to return: 0 for no limit, -1 (default) for no limit without query and 10 with a query.";
    private static final String PLAIN_TEXT_ARG =
            "Strip markup (e.g. HTML tags) and collapse whitespace in flow descriptions (default true).";
    private static final String TRUNCATE_ARG =
            "Maximum length of flow descriptions, truncated with an ellipsis (default 200; use 0 for no limit).";
    private static final String SCRIPT_PLAIN_TEXT_ARG =
            "Strip markup (e.g. HTML tags) and collapse whitespace in flow descriptions (default false).";
    private static final String SCRIPT_TRUNCATE_ARG =
            "Maximum length of flow descriptions, truncated with an ellipsis (default 0 = no limit).";
    private static final String TARGET_ARG =
            "Script target as '<language>/<transport>', exactly as returned by listScriptTargets (default 'python/cli'). The 'cli' transport calls the sdmx-dl command-line tool; the 'rest' transport calls the sdmx-dl REST server.";
    private static final String CLI_LAUNCHER_ARG =
            "Command used by the script to launch the sdmx-dl CLI, as a list of arguments (e.g. [\"java\", \"-jar\", \"sdmx-dl-cli-bin.jar\"]). Defaults to [\"sdmx-dl\"]. Only used by 'cli' targets.";
    private static final String REST_ENDPOINT_ARG =
            "Base URI of the sdmx-dl REST server called by the script (default 'http://localhost:4559/sdmx-dl/v2'). Only used by 'rest' targets.";
    private static final String OUTPUT_FILE_ARG =
            "File written by the script (e.g. 'data.csv'). Omit to write to the standard output.";
    private static final String PROPERTIES_ARG =
            "Target-specific properties as a list of '<name>=<value>', where names are taken from the 'properties' returned by listScriptTargets. Omit when not needed.";

    private static final String DEFAULT_PLAIN_TEXT = "true";
    private static final String DEFAULT_TRUNCATE = "200";
    private static final String DEFAULT_SCRIPT_PLAIN_TEXT = "false";
    private static final String DEFAULT_SCRIPT_TRUNCATE = "" + HasDescription.NO_DESCRIPTION_LIMIT;
    private static final String DEFAULT_LAST_N = "20";
    private static final String DEFAULT_SCRIPT_LAST_N = "0";
    private static final String DEFAULT_FIRST_N = "0";
    private static final String DEFAULT_MAX_RESULTS = "10";
    private static final String DEFAULT_QUERY = NO_QUERY;
    private static final String DEFAULT_DETAIL = "DATA_ONLY";
    private static final String DEFAULT_KEY = "all";
    private static final Confidentiality THRESHOLD = Confidentiality.PUBLIC;

    // Default to a single language to roughly halve name-related tokens; falls back gracefully when
    // unavailable.
    private static final String DEFAULT_LANGUAGES = "en";

    @Inject
    SdmxWebManager manager;

    @Inject
    ScriptManager scripts;

    private WebSource getPublicSourceForMcp(String source) {
        WebSource webSource = manager.getSources().get(source);
        if (webSource == null || !THRESHOLD.isAllowedIn(webSource)) {
            throw new IllegalArgumentException(
                    "Cannot find source '" + source + "'. Use listSources to list available sources.");
        }
        return webSource;
    }

    @Tool(
            description =
                    "Get the name and version of the SDMX-DL server. Also describes the standard workflow for every other tool here: (1) listSources to pick a data provider id, (2) listFlows (with a 'query') to pick a dataset, (3) listDimensions/listAttributes or getMeta to see how series are identified, (4) listCodes to turn a label into a dimension code and listAvailability to check which codes really exist, (5) getData with the 'dimensions' map to fetch observations. Never guess ids: always resolve them with the list* tools first.")
    public AboutDto about() {
        return ProtoApi.fromAbout();
    }

    @Tool(
            description =
                    "List or search the available SDMX data providers (sources) such as central banks and statistical institutes. Start here when you do not know which source id to use. Empty 'query' returns sources sorted by id, truncated to 'maxResults'; a non-empty 'query' ranks them by relevance (BM25 + trigram) against id and names. Returns id, names and website for each source. Next step: pass the chosen id as 'source' to listFlows.")
    public List<WebSourceDto> listSources(
            @ToolArg(description = QUERY_ARG, required = false, defaultValue = DEFAULT_QUERY) String query,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = MAX_RESULTS_ARG, required = false, defaultValue = DEFAULT_MAX_RESULTS)
                    int maxResults) {
        WebSourcesRequest request = WebSourcesRequest.builder()
                .languagesOf(languages)
                .query(query)
                .maxResults(maxResults)
                .confidentialityThreshold(THRESHOLD)
                .build();
        return manager.listSources(request).stream()
                .map(SdmxdlMcpService2::compactSource)
                .toList();
    }

    @Tool(
            description =
                    "List or search the databases (sub-catalogs) of a source. Most sources expose a single default database, so you usually do NOT need this tool: skip it and call listFlows directly. Use it only when a source is known to have several databases, or when listFlows returns nothing. Empty 'query' returns databases sorted by ref; a non-empty 'query' ranks them by relevance. Pass the returned ref as 'database' to the other tools.")
    public List<DatabaseDto> listDatabases(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = QUERY_ARG, required = false, defaultValue = DEFAULT_QUERY) String query,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = MAX_RESULTS_ARG, required = false, defaultValue = DEFAULT_MAX_RESULTS)
                    int maxResults)
            throws IOException {
        DatabasesRequest request = DatabasesRequest.builder()
                .languagesOf(languages)
                .query(query)
                .maxResults(maxResults)
                .build();
        return manager.using(getPublicSourceForMcp(source)).listDatabases(request).stream()
                .map(ProtoApi::fromDatabase)
                .toList();
    }

    @Tool(
            description =
                    "List or search the data flows (datasets) of a source; this is how you find the dataset that holds a given indicator. A source can expose thousands of flows, so pass a topic as 'query' (e.g. 'exchange rates', 'unemployment') to rank them by relevance instead of listing them all. Empty 'query' returns flows sorted by ref.                     Each entry gives the flow ref (to use as 'flow'), its structure ref, a name and a description (plain text truncated to 200 characters by default). Next step: call getMeta or listDimensions on the chosen flow.")
    public List<FlowDto> listFlows(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = QUERY_ARG, required = false, defaultValue = DEFAULT_QUERY) String query,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = MAX_RESULTS_ARG, required = false, defaultValue = DEFAULT_MAX_RESULTS)
                    int maxResults,
            @ToolArg(description = PLAIN_TEXT_ARG, required = false, defaultValue = DEFAULT_PLAIN_TEXT)
                    boolean plainText,
            @ToolArg(description = TRUNCATE_ARG, required = false, defaultValue = DEFAULT_TRUNCATE) int truncate)
            throws IOException {
        FlowsRequest request = FlowsRequest.builder()
                .databaseOf(database)
                .languagesOf(languages)
                .query(query)
                .maxResults(maxResults)
                .plainText(plainText)
                .truncate(truncate)
                .build();
        return manager.using(getPublicSourceForMcp(source)).listFlows(request).stream()
                .map(ProtoApi::fromDataflow)
                .toList();
    }

    @Tool(
            description =
                    "List or search the dimensions of a flow: the fields that identify a series (e.g. FREQ, CURRENCY, REF_AREA). Returns each dimension's id and name in structure order, WITHOUT its codes, which keeps the answer small. Empty 'query' keeps the structure order (the same order used by the positional 'key'); a non-empty 'query' ranks dimensions by relevance. Next step: call listCodes to resolve a dimension's codes, then getData.")
    public List<DimensionDto> listDimensions(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = FLOW_ARG) String flow,
            @ToolArg(description = QUERY_ARG, required = false, defaultValue = DEFAULT_QUERY) String query,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = MAX_RESULTS_ARG, required = false, defaultValue = DEFAULT_MAX_RESULTS)
                    int maxResults)
            throws IOException {
        DimensionsRequest request = DimensionsRequest.builder()
                .flowOf(flow)
                .databaseOf(database)
                .languagesOf(languages)
                .query(query)
                .maxResults(maxResults)
                .build();
        return manager.using(getPublicSourceForMcp(source)).listDimensions(request).stream()
                .map(ProtoApi::fromDimension)
                .toList();
    }

    @Tool(
            description =
                    "List or search the attributes of a flow: the extra properties attached to series or observations (e.g. UNIT_MEASURE, OBS_STATUS, DECIMALS). Attributes cannot be used to filter data; use dimensions for that. Returns each attribute's id, name and relationship, WITHOUT its codes. Empty 'query' sorts by id; a non-empty 'query' ranks by relevance. Use listCodes with an attribute id to see its possible values.")
    public List<AttributeDto> listAttributes(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = FLOW_ARG) String flow,
            @ToolArg(description = QUERY_ARG, required = false, defaultValue = DEFAULT_QUERY) String query,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = MAX_RESULTS_ARG, required = false, defaultValue = DEFAULT_MAX_RESULTS)
                    int maxResults)
            throws IOException {
        AttributesRequest request = AttributesRequest.builder()
                .flowOf(flow)
                .databaseOf(database)
                .languagesOf(languages)
                .query(query)
                .maxResults(maxResults)
                .build();
        return manager.using(getPublicSourceForMcp(source)).listAttributes(request).stream()
                .map(ProtoApi::fromAttribute)
                .toList();
    }

    @Tool(
            description =
                    "Get the metadata skeleton of a flow in one call: the flow itself plus its structure, with all dimensions in positional order, all attributes, the time dimension and the primary measure. Codes are NOT included: each coded component only reports its codelist ref and its number of codes. Use this to learn the dimension order needed by the positional 'key'. Next step: call listCodes for the dimensions you want to filter on, then getData.")
    public MetaSetDto getMeta(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = FLOW_ARG) String flow,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages)
            throws IOException {
        return toSkeleton(ProtoApi.fromMetaSet(manager.using(getPublicSourceForMcp(source))
                .getMeta(MetaRequest.builder()
                        .flowOf(flow)
                        .databaseOf(database)
                        .languagesOf(languages)
                        .build())));
    }

    @Tool(
            description =
                    "List or search the codes of one dimension (or attribute) of a flow: this is how you turn a human label such as 'Switzerland' or 'Monthly' into the code required by getData. Empty 'query' returns codes in codelist order; a non-empty 'query' ranks them by relevance on id and label. Returns the matching codes as an id -> label map plus 'codeCount', the number of codes actually returned; if that number equals 'maxResults' the list is probably truncated, so refine 'query' or raise 'maxResults'. Codes returned here are valid for the flow but may have no data: use listAvailability to check. Next step: pass the chosen code to getData via the 'dimensions' map.")
    public CodelistDto listCodes(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = FLOW_ARG) String flow,
            @ToolArg(description = CONCEPT_ARG) String concept,
            @ToolArg(description = QUERY_ARG, required = false, defaultValue = DEFAULT_QUERY) String query,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = MAX_RESULTS_ARG, required = false, defaultValue = DEFAULT_MAX_RESULTS)
                    int maxResults)
            throws IOException {

        CodesRequest request = CodesRequest.builder()
                .flowOf(flow)
                .databaseOf(database)
                .languagesOf(languages)
                .query(query)
                .maxResults(maxResults)
                .concept(concept)
                .build();

        Map<String, String> codes = manager.using(getPublicSourceForMcp(source)).listCodes(request);

        return CodelistDto.newBuilder()
                .setRef("")
                .setCodeCount(codes.size())
                .putAllCodes(codes)
                .build();
    }

    @Tool(
            description =
                    "List the codes of one dimension that actually have data, given the other dimensions already fixed in 'key'. Unlike listCodes, which returns every code allowed by the codelist, this returns only the codes really present in the dataset for that key, sorted by code id and mapped to their label (label may be empty), together with the id of the inspected 'dimension'. Use it to narrow a key step by step and to avoid empty getData results, for example: fix FREQ in 'key', then ask which REF_AREA codes remain (or leave 'dimension' empty to inspect the next wildcard dimension). 'key' uses the same positional format as getData; use 'all' to apply no constraint. Fails when 'dimension' is unknown or is not a wildcard in 'key'.")
    public AvailabilityDto listAvailability(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = FLOW_ARG) String flow,
            @ToolArg(description = DIMENSION_ARG, required = false) String dimension,
            @ToolArg(description = KEY_ARG, required = false, defaultValue = DEFAULT_KEY) String key,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages)
            throws IOException {

        AvailabilityRequest request = AvailabilityRequest.builder()
                .flowOf(flow)
                .databaseOf(database)
                .languagesOf(languages)
                .keyOf(key)
                .dimension(dimension != null ? dimension : AvailabilityRequest.FIRST_WILDCARD_DIMENSION)
                .build();

        return ProtoApi.fromAvailability(
                manager.using(getPublicSourceForMcp(source)).listAvailability(request));
    }

    @Tool(
            description =
                    "Fetch the actual observations of a flow, as series with their key, metadata and time/value pairs, together with the query that was applied. Filter series with the 'dimensions' map (recommended, e.g. {\"FREQ\":\"M\",\"CURRENCY\":\"CHF\"}) or with the positional 'key'; 'dimensions' wins when both are given and unspecified dimensions match everything. Filter observations by period ('start'/'end') and/or by count ('firstN'/'lastN'); by default only the last 20 observations of each series are returned, so set 'lastN' to 0 for a full history. Count filters apply after period filters, and combining 'firstN' with 'lastN' returns both ends of each series. Filters are pushed to the source when it supports them and are always re-applied locally, and the returned query echoes what was applied. Tip: an unfiltered call can be huge, so first call with detail=SERIES_KEYS_ONLY to see which series exist, then call again with a narrower key and detail=DATA_ONLY.")
    public DataSetDto getData(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = FLOW_ARG) String flow,
            @ToolArg(description = KEY_ARG, required = false, defaultValue = DEFAULT_KEY) String key,
            @ToolArg(description = DETAIL_ARG, required = false, defaultValue = DEFAULT_DETAIL) String detail,
            @ToolArg(description = DIMENSIONS_ARG, required = false) Map<String, String> dimensions,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = START_ARG, required = false) String start,
            @ToolArg(description = END_ARG, required = false) String end,
            @ToolArg(description = FIRST_N_ARG, required = false, defaultValue = DEFAULT_FIRST_N) int firstN,
            @ToolArg(description = LAST_N_ARG, required = false, defaultValue = DEFAULT_LAST_N) int lastN)
            throws IOException {
        Provider<WebSource> provider = manager.using(getPublicSourceForMcp(source));
        DataRequest.Builder request = DataRequest.builder()
                .flowOf(flow)
                .keyOf(resolveKey(provider, flow, key, dimensions, database, languages))
                .detailOf(detail)
                .databaseOf(database)
                .languagesOf(languages);
        applyObsFilters(request, start, end, firstN, lastN);
        return ProtoApi.fromDataSet(provider.getData(request.build()));
    }

    @Tool(
            description =
                    "List the available script targets as '<language>/<transport>' (e.g. 'python/cli', 'python/rest') with the commands each one supports (e.g. 'data', 'flows'). Use it before generateDataScript/generateFlowsScript to pick a valid 'target'.")
    public List<ScriptTargetDto> listScriptTargets() {
        return scripts.getTargets().stream()
                .map(target -> ProtoScript.fromScriptTarget(scripts, target))
                .toList();
    }

    @Tool(
            description =
                    "Generate a ready-to-run script that fetches the observations of a flow and writes them as CSV (Series, ObsPeriod, ObsValue), so that the user can reproduce or automate a getData call in their own workflow without writing code. Takes the same filters as getData, but defaults to the full history (lastN=0). The script is NOT executed: return its 'content' to the user, mentioning any 'warnings' (request parameters the target cannot honor). Call listScriptTargets to discover the valid targets.")
    public ScriptDto generateDataScript(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = FLOW_ARG) String flow,
            @ToolArg(description = KEY_ARG, required = false, defaultValue = DEFAULT_KEY) String key,
            @ToolArg(description = DIMENSIONS_ARG, required = false) Map<String, String> dimensions,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = SCRIPT_LANGUAGES_ARG, required = false, defaultValue = ANY_KEYWORD) String languages,
            @ToolArg(description = START_ARG, required = false) String start,
            @ToolArg(description = END_ARG, required = false) String end,
            @ToolArg(description = FIRST_N_ARG, required = false, defaultValue = DEFAULT_FIRST_N) int firstN,
            @ToolArg(description = SCRIPT_LAST_N_ARG, required = false, defaultValue = DEFAULT_SCRIPT_LAST_N) int lastN,
            @ToolArg(description = TARGET_ARG, required = false, defaultValue = ProtoScript.DEFAULT_TARGET)
                    String target,
            @ToolArg(description = CLI_LAUNCHER_ARG, required = false) List<String> cliLauncher,
            @ToolArg(description = REST_ENDPOINT_ARG, required = false) String restEndpoint,
            @ToolArg(description = OUTPUT_FILE_ARG, required = false) String outputFile,
            @ToolArg(description = PROPERTIES_ARG, required = false) List<String> properties)
            throws IOException {
        Provider<WebSource> provider = manager.using(getPublicSourceForMcp(source));
        DataRequest.Builder request = DataRequest.builder()
                .flowOf(flow)
                .keyOf(resolveKey(provider, flow, key, dimensions, database, languages))
                .databaseOf(database)
                .languagesOf(languages);
        applyObsFilters(request, start, end, firstN, lastN);
        return generateScript(source, request.build(), target, cliLauncher, restEndpoint, outputFile, properties);
    }

    @Tool(
            description =
                    "Generate a ready-to-run script that lists or searches the data flows of a source and writes them as CSV (Ref, Name, Description), so that the user can reproduce or automate a listFlows call in their own workflow without writing code. The script is NOT executed: return its 'content' to the user, mentioning any 'warnings'. Call listScriptTargets to discover the valid targets.")
    public ScriptDto generateFlowsScript(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = QUERY_ARG, required = false, defaultValue = DEFAULT_QUERY) String query,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = SCRIPT_LANGUAGES_ARG, required = false, defaultValue = ANY_KEYWORD) String languages,
            @ToolArg(description = SCRIPT_MAX_RESULTS_ARG, required = false, defaultValue = "" + AUTO_LIMIT)
                    int maxResults,
            @ToolArg(description = SCRIPT_PLAIN_TEXT_ARG, required = false, defaultValue = DEFAULT_SCRIPT_PLAIN_TEXT)
                    boolean plainText,
            @ToolArg(description = SCRIPT_TRUNCATE_ARG, required = false, defaultValue = DEFAULT_SCRIPT_TRUNCATE)
                    int truncate,
            @ToolArg(description = TARGET_ARG, required = false, defaultValue = ProtoScript.DEFAULT_TARGET)
                    String target,
            @ToolArg(description = CLI_LAUNCHER_ARG, required = false) List<String> cliLauncher,
            @ToolArg(description = REST_ENDPOINT_ARG, required = false) String restEndpoint,
            @ToolArg(description = OUTPUT_FILE_ARG, required = false) String outputFile,
            @ToolArg(description = PROPERTIES_ARG, required = false) List<String> properties) {
        getPublicSourceForMcp(source);
        FlowsRequest request = FlowsRequest.builder()
                .databaseOf(database)
                .languagesOf(languages)
                .query(query)
                .maxResults(maxResults)
                .plainText(plainText)
                .truncate(truncate)
                .build();
        return generateScript(source, request, target, cliLauncher, restEndpoint, outputFile, properties);
    }

    private ScriptDto generateScript(
            String source,
            Request request,
            String target,
            List<String> cliLauncher,
            String restEndpoint,
            String outputFile,
            List<String> properties) {
        return ProtoScript.fromScript(scripts.generate(
                ScriptTarget.parse(target),
                source,
                request,
                ProtoScript.toScriptOptions(
                        cliLauncher, restEndpoint, blankToNull(outputFile), ProtoScript.parseProperties(properties))));
    }

    private static String resolveKey(
            Provider<WebSource> provider,
            String flow,
            String key,
            Map<String, String> dimensions,
            String database,
            String languages)
            throws IOException {
        if (dimensions == null || dimensions.isEmpty()) {
            return key;
        }
        Structure structure = provider.getMeta(MetaRequest.builder()
                        .flowOf(flow)
                        .databaseOf(database)
                        .languagesOf(languages)
                        .build())
                .getStructure();
        return buildKey(structure, dimensions).toString();
    }

    private static void applyObsFilters(DataRequest.Builder request, String start, String end, int firstN, int lastN) {
        if (start != null && !start.isBlank()) {
            request.startPeriodOf(start.trim());
        }
        if (end != null && !end.isBlank()) {
            request.endPeriodOf(end.trim());
        }
        if (firstN > 0) {
            request.firstNObservations(firstN);
        }
        if (lastN > 0) {
            request.lastNObservations(lastN);
        }
    }

    private static String blankToNull(String value) {
        return value != null && !value.isBlank() ? value : null;
    }

    @Tool(
            description =
                    "Diagnose the health of one source. Use it to explain failures of the other tools, not as part of the normal data workflow. Requires a valid source id from listSources. Returns a verdict: OK (works), DEGRADED (answers with errors, or monitor and live check disagree), LOCAL_ISSUE (monitor says up but the source cannot be reached from this server: proxy, firewall, SSL), REMOTE_OUTAGE (monitor says down), UNREACHABLE (cannot be reached and no monitor to tell why), UNKNOWN (not enough information). Also returns the monitor report (status, uptime ratio, average response time in ms) and, if requested, the live access check (reachable, accessible, duration in ms, status code, error message).")
    public HealthReportDto checkHealth(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(
                            description =
                                    "Comma-separated checks: 'monitor' (third-party monitor, cheap, default) and/or 'access' (live request from this server against the source; use it when the monitor is missing or says up but calls fail).",
                            required = false,
                            defaultValue = "monitor")
                    String checks)
            throws IOException {
        getPublicSourceForMcp(source);
        EnumSet<HealthCheck> set = EnumSet.noneOf(HealthCheck.class);
        for (String check : checks.split(",")) {
            if (!check.isBlank()) {
                set.add(HealthCheck.valueOf(check.trim().toUpperCase(Locale.ROOT)));
            }
        }
        return ProtoWeb.fromHealthReport(
                manager.checkHealth(source, set.isEmpty() ? WebHealthRequest.DEFAULT_CHECKS : set));
    }

    private static Key buildKey(Structure structure, Map<String, String> dimensions) {
        Map<String, String> byLowerId = new HashMap<>();
        for (Dimension dimension : structure.getDimensions()) {
            byLowerId.put(dimension.getId().toLowerCase(Locale.ROOT), dimension.getId());
        }
        Key.Builder builder = Key.builder(structure);
        for (Map.Entry<String, String> entry : dimensions.entrySet()) {
            String actualId = byLowerId.get(entry.getKey().toLowerCase(Locale.ROOT));
            if (actualId == null) {
                throw new IllegalArgumentException(
                        "Cannot find dimension '" + entry.getKey() + "'. Expected one of " + byLowerId.values());
            }
            builder.put(actualId, entry.getValue());
        }
        return builder.build();
    }

    private static MetaSetDto toSkeleton(MetaSetDto metaSet) {
        StructureDto structure = metaSet.getStructure();
        StructureDto.Builder result = structure.toBuilder().clearDimensions().clearAttributes();
        for (DimensionDto dimension : structure.getDimensionsList()) {
            result.addDimensions(
                    dimension.hasCodelist()
                            ? dimension.toBuilder()
                                    .setCodelist(skeletonCodelist(dimension.getCodelist()))
                                    .build()
                            : dimension);
        }
        for (AttributeDto attribute : structure.getAttributesList()) {
            result.addAttributes(
                    attribute.hasCodelist()
                            ? attribute.toBuilder()
                                    .setCodelist(skeletonCodelist(attribute.getCodelist()))
                                    .build()
                            : attribute);
        }
        return metaSet.toBuilder().setStructure(result.build()).build();
    }

    private static CodelistDto skeletonCodelist(CodelistDto codelist) {
        return CodelistDto.newBuilder()
                .setRef(codelist.getRef())
                .setCodeCount(codelist.getCodesMap().size())
                .build();
    }

    private static WebSourceDto compactSource(WebSource source) {
        WebSourceDto.Builder result =
                WebSourceDto.newBuilder().setId(source.getId()).putAllNames(source.getNames());
        if (source.getWebsite() != null) {
            result.setWebsite(source.getWebsite().toString());
        }
        return result.build();
    }
}
