package sdmxdl.grpc;

import io.quarkiverse.mcp.server.*;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import sdmxdl.*;
import sdmxdl.format.protobuf.*;
import sdmxdl.format.protobuf.web.WebSourceDto;
import sdmxdl.format.protobuf.web.WebSourcesDto;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.Search;
import sdmxdl.web.WebSource;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static sdmxdl.DatabaseRef.NO_DATABASE_KEYWORD;

@ApplicationScoped
@RegisterForReflection
@WrapBusinessError({IOException.class, IllegalArgumentException.class})
public class SdmxWebManagerMcp {

    private static final String SOURCE_ARG = "SDMX source ID";
    private static final String LANGUAGES_ARG = "Language priority list";
    private static final String DATABASE_ARG = "Database ref";
    private static final String FLOW_ARG = "SDMX flow ref";
    private static final String KEY_ARG = "SDMX key (positional, dot-separated; empty part = wildcard; e.g. 'M.CHF.EUR.SP00.A'). Ignored when 'dimensions' is provided.";
    private static final String DETAIL_ARG = "Amount of information to retrieve (FULL, DATA_ONLY, SERIES_KEYS_ONLY, NO_DATA)";
    private static final String QUERY_ARG = "Search query (free text)";
    private static final String MAX_RESULTS_ARG = "Maximum number of results to return";
    private static final String DIMENSION_ARG = "Dimension id (as returned by mcpMeta)";
    private static final String DIMENSIONS_ARG = "Optional structured filter mapping dimension id to code (e.g. {\"CURRENCY\":\"CHF\"}); the server builds the positional key from the flow structure and treats unspecified dimensions as wildcards. Takes precedence over 'key'.";
    private static final String LAST_N_ARG = "Maximum number of most recent observations to return per series (<= 0 means no limit)";

    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final String DEFAULT_LAST_N = "20";
    private static final String DEFAULT_MAX_RESULTS = "5";
    private static final String DEFAULT_DETAIL = "DATA_ONLY";
    private static final String DEFAULT_KEY = "all";

    // Default to a single language to roughly halve name-related tokens; falls back gracefully when unavailable.
    private static final String DEFAULT_LANGUAGES = "en";

    @Inject
    SdmxWebManager manager;

    private WebSource getPublicSourceForMcp(String source) {
        WebSource webSource = manager.getSources().get(source);
        if (webSource == null || !Confidentiality.PUBLIC.isAllowedIn(webSource)) {
            throw new IllegalArgumentException("Cannot find source '" + source + "'." + suggestSources(source));
        }
        return webSource;
    }

    private List<WebSource> getPublicSources() {
        return manager.getSources()
                .values()
                .stream()
                .filter(source -> !source.isAlias())
                .filter(Confidentiality.PUBLIC::isAllowedIn)
                .toList();
    }

    private String suggestSources(String source) {
        List<String> suggestions = Search.ofSources(getPublicSources(), Languages.parse(DEFAULT_LANGUAGES))
                .search(source == null ? "" : source, 3)
                .stream()
                .map(result -> result.getItem().getId())
                .toList();
        return suggestions.isEmpty()
                ? " Use mcpSources to list available sources."
                : " Did you mean " + suggestions + "? Use mcpSources to list all sources.";
    }

    @Prompt(description = "List SDMX sources IDs.", name = "listSourceIds")
    public PromptResponse mcpSourceIds() {
        return PromptResponse.withMessages(getPublicSources()
                .stream()
                .map(WebSource::getId)
                .map(PromptMessage::withUserRole)
                .toList()
        );
    }

    @Tool(description = "Get name and version of SDMX-DL. Typical workflow: (1) find a source with mcpSources/mcpSearchSources, (2) find a flow with mcpFlows/mcpSearchFlows, (3) inspect dimensions with mcpMeta, (4) resolve dimension codes with mcpCodes, (5) fetch data with mcpData (prefer the structured 'dimensions' map over a positional 'key').")
    public AboutDto mcpAbout() {
        return ProtoApi.fromAbout();
    }

    @Tool(description = "List SDMX sources. Next step: pick a source id and call mcpFlows or mcpSearchFlows.")
    public WebSourcesDto mcpSources() {
        return WebSourcesDto.newBuilder().addAllWebSources(
                getPublicSources()
                        .stream()
                        .map(SdmxWebManagerMcp::compactSource)
                        .toList()
        ).build();
    }

    @Tool(description = "List SDMX databases. Most sources expose a single default database; databases are only needed for multi-database sources.")
    public List<DatabaseDto> mcpDatabases(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages
    ) throws IOException {
        return manager.using(getPublicSourceForMcp(source))
                .getDatabases(SourceRequest
                        .builder()
                        .languagesOf(languages)
                        .build())
                .stream()
                .map(ProtoApi::fromDatabase)
                .toList();
    }

    @Tool(description = "List SDMX data flows (datasets) of a source. Next step: call mcpMeta on the chosen flow to see its dimensions.")
    public List<FlowDto> mcpFlows(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages
    ) throws IOException {
        return manager
                .using(getPublicSourceForMcp(source))
                .getFlows(DatabaseRequest
                        .builder()
                        .databaseOf(database)
                        .languagesOf(languages)
                        .build())
                .stream()
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
                .replaceAll("\\s+", " ")     // collapse whitespace
                .trim();
        if (cleaned.length() > MAX_DESCRIPTION_LENGTH) {
            cleaned = cleaned.substring(0, MAX_DESCRIPTION_LENGTH - 1).trim() + "…";
        }
        return flowDto.toBuilder().setDescription(cleaned).build();
    }

    @Tool(description = "Search SDMX data flows of a source by relevance using hybrid search (BM25 + trigram). Increase maxResults to widen the search. Next step: call mcpMeta on the chosen flow.")
    public List<FlowDto> mcpSearchFlows(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = QUERY_ARG) String query,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = MAX_RESULTS_ARG, required = false, defaultValue = DEFAULT_MAX_RESULTS) int maxResults
    ) throws IOException {
        Collection<Flow> flows = manager
                .using(getPublicSourceForMcp(source))
                .getFlows(DatabaseRequest
                        .builder()
                        .databaseOf(database)
                        .languagesOf(languages)
                        .build());
        return Search.ofFlows(flows).search(query, maxResults)
                .stream()
                .map(result -> ProtoApi.fromDataflow(result.getItem()))
                .map(SdmxWebManagerMcp::cleanDescription)
                .toList();
    }

    @Tool(description = "Search SDMX sources by relevance using hybrid search (BM25 + trigram). Increase maxResults to widen the search. Next step: use the returned source id with mcpFlows or mcpSearchFlows.")
    public List<WebSourceDto> mcpSearchSources(
            @ToolArg(description = QUERY_ARG) String query,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = MAX_RESULTS_ARG, required = false, defaultValue = DEFAULT_MAX_RESULTS) int maxResults
    ) {
        return Search.ofSources(getPublicSources(), Languages.parse(languages)).search(query, maxResults)
                .stream()
                .map(result -> compactSource(result.getItem()))
                .toList();
    }

    @Tool(description = "Search SDMX databases of a source by relevance using hybrid search (BM25 + trigram). Increase maxResults to widen the search.")
    public List<DatabaseDto> mcpSearchDatabases(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = QUERY_ARG) String query,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = MAX_RESULTS_ARG, required = false, defaultValue = DEFAULT_MAX_RESULTS) int maxResults
    ) throws IOException {
        Collection<Database> databases = manager
                .using(getPublicSourceForMcp(source))
                .getDatabases(SourceRequest
                        .builder()
                        .languagesOf(languages)
                        .build());
        return Search.ofDatabases(databases).search(query, maxResults)
                .stream()
                .map(result -> ProtoApi.fromDatabase(result.getItem()))
                .toList();
    }

    @Tool(description = "Get SDMX metadata: flow and structure skeleton. Returns dimensions in order with their id, name, codelist ref and code count, but NOT the codes themselves. Next step: use mcpCodes to resolve a dimension's codes, then mcpData.")
    public MetaSetDto mcpMeta(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = FLOW_ARG) String flow,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages
    ) throws IOException {
        return toSkeleton(ProtoApi.fromMetaSet(manager
                .using(getPublicSourceForMcp(source))
                .getMeta(FlowRequest
                        .builder()
                        .flowOf(flow)
                        .databaseOf(database)
                        .languagesOf(languages)
                        .build())
        ));
    }

    @Tool(description = "List or search the codes of a single dimension of an SDMX flow. Use after mcpMeta to resolve a label (e.g. a product name) to its dimension code. Returns the codelist ref, the total code count and the matching codes (id -> label); when 'codeCount' exceeds the number of returned codes, refine 'query' or raise 'maxResults'. Next step: pass the chosen code(s) to mcpData via the 'dimensions' map.")
    public CodelistDto mcpCodes(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = FLOW_ARG) String flow,
            @ToolArg(description = DIMENSION_ARG) String dimension,
            @ToolArg(description = QUERY_ARG, required = false, defaultValue = "") String query,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = MAX_RESULTS_ARG, required = false, defaultValue = DEFAULT_MAX_RESULTS) int maxResults
    ) throws IOException {
        MetaSet meta = manager
                .using(getPublicSourceForMcp(source))
                .getMeta(FlowRequest
                        .builder()
                        .flowOf(flow)
                        .databaseOf(database)
                        .languagesOf(languages)
                        .build());
        Codelist codelist = meta.getStructure().getDimensions().stream()
                .filter(d -> d.getId().equalsIgnoreCase(dimension))
                .map(Dimension::getCodelist)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find coded dimension '" + dimension
                        + "'. Expected one of " + codedDimensionIds(meta.getStructure())));
        return CodelistDto.newBuilder()
                .setRef(codelist.getRef().toString())
                .setCodeCount(codelist.getCodes().size())
                .putAllCodes(filterCodes(codelist.getCodes(), query, maxResults))
                .build();
    }

    @Tool(description = "Get SDMX data series alongside their flow reference and the query used to get them. Provide either the positional 'key' or the structured 'dimensions' map (recommended: it avoids positional-key mistakes). By default only the most recent observations of each series are returned; increase or disable 'lastN' to get more. Truncated series carry 'sdmxdl.obs.total'/'sdmxdl.obs.returned'/'sdmxdl.obs.truncated' in their meta. Tip: call once with detail=SERIES_KEYS_ONLY to list the available series keys without data, then call again with a chosen key (or dimensions) and detail=DATA_ONLY to fetch observations.")
    public DataSetDto mcpData(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = FLOW_ARG) String flow,
            @ToolArg(description = KEY_ARG, required = false, defaultValue = DEFAULT_KEY) String key,
            @ToolArg(description = DETAIL_ARG, required = false, defaultValue = DEFAULT_DETAIL) String detail,
            @ToolArg(description = DIMENSIONS_ARG, required = false) Map<String, String> dimensions,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = DEFAULT_LANGUAGES) String languages,
            @ToolArg(description = LAST_N_ARG, required = false, defaultValue = DEFAULT_LAST_N) int lastN
    ) throws IOException {
        Provider<WebSource> provider = manager.using(getPublicSourceForMcp(source));
        String effectiveKey = key;
        if (dimensions != null && !dimensions.isEmpty()) {
            Structure structure = provider
                    .getMeta(FlowRequest
                            .builder()
                            .flowOf(flow)
                            .databaseOf(database)
                            .languagesOf(languages)
                            .build())
                    .getStructure();
            effectiveKey = buildKey(structure, dimensions).toString();
        }
        return trimObs(ProtoApi.fromDataSet(provider
                .getData(KeyRequest
                        .builder()
                        .flowOf(flow)
                        .keyOf(effectiveKey)
                        .detailOf(detail)
                        .databaseOf(database)
                        .languagesOf(languages)
                        .build())
        ), lastN);
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
                throw new IllegalArgumentException("Cannot find dimension '" + entry.getKey()
                        + "'. Expected one of " + byLowerId.values());
            }
            builder.put(actualId, entry.getValue());
        }
        return builder.build();
    }

    private static List<String> codedDimensionIds(Structure structure) {
        return structure.getDimensions().stream()
                .filter(Dimension::isCoded)
                .map(Dimension::getId)
                .toList();
    }

    private static MetaSetDto toSkeleton(MetaSetDto metaSet) {
        StructureDto structure = metaSet.getStructure();
        StructureDto.Builder result = structure.toBuilder()
                .clearDimensions()
                .clearAttributes();
        for (DimensionDto dimension : structure.getDimensionsList()) {
            result.addDimensions(dimension.hasCodelist()
                    ? dimension.toBuilder().setCodelist(skeletonCodelist(dimension.getCodelist())).build()
                    : dimension);
        }
        for (AttributeDto attribute : structure.getAttributesList()) {
            result.addAttributes(attribute.hasCodelist()
                    ? attribute.toBuilder().setCodelist(skeletonCodelist(attribute.getCodelist())).build()
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

    private static Map<String, String> filterCodes(Map<String, String> codes, String query, int maxResults) {
        if (maxResults <= 0) {
            return Map.of();
        }
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> code : codes.entrySet()) {
            if (needle.isEmpty()
                    || code.getKey().toLowerCase(Locale.ROOT).contains(needle)
                    || code.getValue().toLowerCase(Locale.ROOT).contains(needle)) {
                result.put(code.getKey(), code.getValue());
                if (result.size() >= maxResults) {
                    break;
                }
            }
        }
        return result;
    }

    private static DataSetDto trimObs(DataSetDto dataSet, int lastN) {
        if (lastN <= 0) {
            return dataSet;
        }
        List<SeriesDto> trimmed = dataSet.getDataList().stream()
                .map(series -> {
                    int size = series.getObsCount();
                    return size <= lastN
                            ? series
                            : series.toBuilder()
                            .clearObs()
                            .addAllObs(series.getObsList().subList(size - lastN, size))
                            .putMeta("sdmxdl.obs.total", Integer.toString(size))
                            .putMeta("sdmxdl.obs.returned", Integer.toString(lastN))
                            .putMeta("sdmxdl.obs.truncated", "true")
                            .build();
                })
                .toList();
        return dataSet.toBuilder().clearData().addAllData(trimmed).build();
    }

    private static WebSourceDto compactSource(WebSource source) {
        WebSourceDto.Builder result = WebSourceDto.newBuilder()
                .setId(source.getId())
                .putAllNames(source.getNames());
        if (source.getWebsite() != null) {
            result.setWebsite(source.getWebsite().toString());
        }
        return result.build();
    }
}

