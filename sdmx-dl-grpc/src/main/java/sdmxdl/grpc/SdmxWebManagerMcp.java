package sdmxdl.grpc;

import static sdmxdl.DatabaseRef.NO_DATABASE_KEYWORD;
import static sdmxdl.HasSearchQuery.NO_QUERY;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.WrapBusinessError;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import sdmxdl.*;
import sdmxdl.format.protobuf.*;
import sdmxdl.format.protobuf.web.WebSourceDto;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.WebSourcesRequest;

@ApplicationScoped
@RegisterForReflection
@WrapBusinessError({IOException.class, IllegalArgumentException.class})
public class SdmxWebManagerMcp {

    private static final String SOURCE_ARG = "SDMX source ID";
    private static final String LANGUAGES_ARG = "Language priority list";
    private static final String DATABASE_ARG = "Database ref";
    private static final String FLOW_ARG = "SDMX flow ref";
    private static final String KEY_ARG =
            "SDMX key (positional, dot-separated; empty part = wildcard; e.g. 'M.CHF.EUR.SP00.A'). Ignored when 'dimensions' is provided.";
    private static final String DETAIL_ARG =
            "Amount of information to retrieve (FULL, DATA_ONLY, SERIES_KEYS_ONLY, NO_DATA)";
    private static final String QUERY_ARG = "Search query (free text)";
    private static final String MAX_RESULTS_ARG = "Maximum number of results to return";
    private static final String DIMENSION_ARG = "Dimension id (as returned by getMeta or listDimensions)";
    private static final String DIMENSIONS_ARG =
            "Optional structured filter mapping dimension id to code (e.g. {\"CURRENCY\":\"CHF\"}); the server builds the positional key from the flow structure and treats unspecified dimensions as wildcards. Takes precedence over 'key'.";
    private static final String LAST_N_ARG =
            "Maximum number of most recent observations to return per series (<= 0 means no limit). Applied server-side when the source supports it, otherwise client-side.";
    private static final String FIRST_N_ARG =
            "Maximum number of oldest observations to return per series (<= 0 means no limit). Combine with 'lastN' to fetch both ends of a series.";
    private static final String START_PERIOD_ARG =
            "Inclusive lower bound of the observation period as reduced-precision ISO-8601 (e.g. '2000', '2000-01', '2000-01-01'). Omit for no lower bound.";
    private static final String END_PERIOD_ARG =
            "Inclusive upper bound of the observation period as reduced-precision ISO-8601 (e.g. '2020', '2020-12'). Omit for no upper bound.";

    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final String DEFAULT_LAST_N = "20";
    private static final String DEFAULT_FIRST_N = "0";
    private static final String DEFAULT_MAX_RESULTS = "0";
    private static final String DEFAULT_QUERY = NO_QUERY;
    private static final String DEFAULT_DETAIL = "DATA_ONLY";
    private static final String DEFAULT_KEY = "all";
    private static final Confidentiality THRESHOLD = Confidentiality.PUBLIC;

    // Default to a single language to roughly halve name-related tokens; falls back gracefully when
    // unavailable.
    private static final String DEFAULT_LANGUAGES = "en";

    @Inject
    SdmxWebManager manager;

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
                    "Get name and version of SDMX-DL. Typical workflow: (1) find a source with listSources, (2) find a flow with listFlows, (3) inspect dimensions/attributes with getMeta or listDimensions/listAttributes, (4) resolve dimension codes with listCodes, (5) fetch data with getData (prefer the structured 'dimensions' map over a positional 'key').")
    public AboutDto about() {
        return ProtoApi.fromAbout();
    }

    @Tool(
            description =
                    "List or search SDMX sources. When 'query' is empty, entries are sorted by id and truncated to 'maxResults' (0 = no limit). When 'query' is non-empty, entries are ranked by relevance (BM25 + trigram) and limited to 'maxResults'. Next step: pick a source id and call listFlows.")
    public List<WebSourceDto> listSources(
            @ToolArg(description = QUERY_ARG, required = false, defaultValue = DEFAULT_QUERY) String query,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = MAX_RESULTS_ARG, required = false, defaultValue = DEFAULT_MAX_RESULTS)
                    int maxResults) {
        WebSourcesRequest request = WebSourcesRequest.builder()
                .languagesOf(languages)
                .query(query)
                .maxResults(maxResults)
                .threshold(THRESHOLD)
                .build();
        return manager.listSources(request).stream()
                .map(SdmxWebManagerMcp::compactSource)
                .toList();
    }

    @Tool(
            description =
                    "List or search SDMX databases of a source. When 'query' is empty, entries are sorted by ref and truncated to 'maxResults' (0 = no limit). When 'query' is non-empty, entries are ranked by relevance (BM25 + trigram) and limited to 'maxResults'. Most sources expose a single default database; databases are only needed for multi-database sources.")
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
                    "List or search SDMX data flows (datasets) of a source. When 'query' is empty, entries are sorted by ref and truncated to 'maxResults' (0 = no limit). When 'query' is non-empty, entries are ranked by relevance (BM25 + trigram) and limited to 'maxResults'. Next step: call getMeta on the chosen flow to see its dimensions.")
    public List<FlowDto> listFlows(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = QUERY_ARG, required = false, defaultValue = DEFAULT_QUERY) String query,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = MAX_RESULTS_ARG, required = false, defaultValue = DEFAULT_MAX_RESULTS)
                    int maxResults)
            throws IOException {
        FlowsRequest request = FlowsRequest.builder()
                .databaseOf(database)
                .languagesOf(languages)
                .query(query)
                .maxResults(maxResults)
                .build();
        return manager.using(getPublicSourceForMcp(source)).listFlows(request).stream()
                .map(ProtoApi::fromDataflow)
                .map(SdmxWebManagerMcp::cleanDescription)
                .toList();
    }

    private static FlowDto cleanDescription(FlowDto flowDto) {
        if (!flowDto.hasDescription()) {
            return flowDto;
        }
        String cleaned = flowDto.getDescription()
                .replaceAll("<[^>]*>", " ") // strip HTML tags
                .replaceAll("\\s+", " ") // collapse whitespace
                .trim();
        if (cleaned.length() > MAX_DESCRIPTION_LENGTH) {
            cleaned = cleaned.substring(0, MAX_DESCRIPTION_LENGTH - 1).trim() + "…";
        }
        return flowDto.toBuilder().setDescription(cleaned).build();
    }

    @Tool(
            description =
                    "List or search dimensions of a flow's structure. When 'query' is empty, entries are returned in structure order and truncated to 'maxResults' (0 = no limit). When 'query' is non-empty, entries are ranked by relevance (BM25 + trigram) and limited to 'maxResults'. Next step: use listCodes to resolve a dimension's values.")
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
                    "List or search attributes of a flow's structure. When 'query' is empty, entries are sorted by id and truncated to 'maxResults' (0 = no limit). When 'query' is non-empty, entries are ranked by relevance (BM25 + trigram) and limited to 'maxResults'.")
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
                    "Get SDMX metadata: flow and structure skeleton. Returns dimensions in order with their id, name, codelist ref and code count, but NOT the codes themselves. Next step: use listCodes to resolve a dimension's codes, then getData.")
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
                    "List or search the codes of a single dimension of an SDMX flow. Use after getMeta or listDimensions to resolve a label (e.g. a product name) to its dimension code. Returns the codelist ref, the total code count and the matching codes (id -> label); when 'codeCount' exceeds the number of returned codes, refine 'query' or raise 'maxResults'. Next step: pass the chosen code(s) to getData via the 'dimensions' map.")
    public CodelistDto listCodes(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = FLOW_ARG) String flow,
            @ToolArg(description = DIMENSION_ARG) String dimension,
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
                .concept(dimension)
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
                    "Get SDMX data series alongside their flow reference and the query used to get them. Provide either the positional 'key' or the structured 'dimensions' map (recommended: it avoids positional-key mistakes). Observations can be filtered by period ('startPeriod'/'endPeriod') and/or count ('firstN'/'lastN'); by default only the most recent observations of each series are returned (lastN=20), so increase or disable 'lastN' to get more. Filters are applied server-side when the source supports it and always enforced client-side, and the returned query echoes the filters that were applied. Tip: call once with detail=SERIES_KEYS_ONLY to list the available series keys without data, then call again with a chosen key (or dimensions) and detail=DATA_ONLY to fetch observations.")
    public DataSetDto getData(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = FLOW_ARG) String flow,
            @ToolArg(description = KEY_ARG, required = false, defaultValue = DEFAULT_KEY) String key,
            @ToolArg(description = DETAIL_ARG, required = false, defaultValue = DEFAULT_DETAIL) String detail,
            @ToolArg(description = DIMENSIONS_ARG, required = false) Map<String, String> dimensions,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = START_PERIOD_ARG, required = false) String startPeriod,
            @ToolArg(description = END_PERIOD_ARG, required = false) String endPeriod,
            @ToolArg(description = FIRST_N_ARG, required = false, defaultValue = DEFAULT_FIRST_N) int firstN,
            @ToolArg(description = LAST_N_ARG, required = false, defaultValue = DEFAULT_LAST_N) int lastN)
            throws IOException {
        Provider<WebSource> provider = manager.using(getPublicSourceForMcp(source));
        String effectiveKey = key;
        if (dimensions != null && !dimensions.isEmpty()) {
            Structure structure = provider.getMeta(MetaRequest.builder()
                            .flowOf(flow)
                            .databaseOf(database)
                            .languagesOf(languages)
                            .build())
                    .getStructure();
            effectiveKey = buildKey(structure, dimensions).toString();
        }
        DataRequest.Builder request = DataRequest.builder()
                .flowOf(flow)
                .keyOf(effectiveKey)
                .detailOf(detail)
                .databaseOf(database)
                .languagesOf(languages);
        if (startPeriod != null && !startPeriod.isBlank()) {
            request.startPeriodOf(startPeriod.trim());
        }
        if (endPeriod != null && !endPeriod.isBlank()) {
            request.endPeriodOf(endPeriod.trim());
        }
        if (firstN > 0) {
            request.firstNObservations(firstN);
        }
        if (lastN > 0) {
            request.lastNObservations(lastN);
        }
        return ProtoApi.fromDataSet(provider.getData(request.build()));
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
