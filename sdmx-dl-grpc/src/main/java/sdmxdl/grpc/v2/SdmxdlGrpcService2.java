package sdmxdl.grpc.v2;

import static sdmxdl.DatabaseRef.NO_DATABASE_KEYWORD;
import static sdmxdl.HasDescription.NO_DESCRIPTION_LIMIT;
import static sdmxdl.HasSearch.AUTO_LIMIT;
import static sdmxdl.HasSearch.NO_OFFSET;
import static sdmxdl.HasSearch.NO_QUERY;
import static sdmxdl.Languages.ANY_KEYWORD;

import io.quarkus.arc.Arc;
import io.quarkus.grpc.GrpcService;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import nbbrd.io.function.IOSupplier;
import org.jspecify.annotations.Nullable;
import sdmxdl.*;
import sdmxdl.format.protobuf.*;
import sdmxdl.script.ScriptManager;
import sdmxdl.web.FlowEntry;
import sdmxdl.web.HealthCheck;
import sdmxdl.web.SourceFailure;
import sdmxdl.web.WebFlowsRequest;
import sdmxdl.web.WebFlowsResult;
import sdmxdl.web.WebHealthRequest;
import sdmxdl.web.WebSourcesRequest;

@GrpcService
@RegisterForReflection
@Blocking
public class SdmxdlGrpcService2 implements SdmxWebManager {

    // This class is both a @GrpcService bean and a JAX-RS resource. RESTEasy Reactive instantiates
    // the resource via a no-arg constructor (not through CDI), so the shared SdmxWebManager singleton
    // is resolved programmatically to guarantee a single instance across gRPC, REST and MCP.
    private final sdmxdl.web.SdmxWebManager manager =
            Arc.container().select(sdmxdl.web.SdmxWebManager.class).get();

    private final ScriptManager scripts =
            Arc.container().select(ScriptManager.class).get();

    @Override
    public Uni<AboutDto> getAbout(EmptyDto request) {
        return Uni.createFrom().item(ProtoApi.fromAbout());
    }

    @Override
    public Uni<ListSourcesResponseDto> listSources(WebSourcesRequestDto request) {
        return uniOfIO(null, () -> {
            var result = manager.listSources(WebSourcesRequest.builder()
                    .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                    .query(request.hasQuery() ? request.getQuery() : NO_QUERY)
                    .maxResults(request.hasMaxResults() ? request.getMaxResults() : AUTO_LIMIT)
                    .offset(request.hasOffset() ? request.getOffset() : NO_OFFSET)
                    .confidentialityThreshold(
                            request.hasMaxConfidentiality()
                                    ? ProtoApi.toConfidentiality(request.getMaxConfidentiality())
                                    : Confidentiality.SECRET)
                    .build());
            return ListSourcesResponseDto.newBuilder()
                    .addAllItems(result.getItems().stream()
                            .map(ProtoWeb::fromWebSource)
                            .toList())
                    .setTotal(result.getTotal())
                    .addAllScores(result.getScores())
                    .build();
        });
    }

    @Override
    public Uni<ListDatabasesResponseDto> listDatabases(WebDatabasesRequestDto request) {
        return uniOfIO(request.getSource(), () -> {
            var result = manager.usingName(request.getSource())
                    .listDatabases(DatabasesRequest.builder()
                            .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                            .query(request.hasQuery() ? request.getQuery() : NO_QUERY)
                            .maxResults(request.hasMaxResults() ? request.getMaxResults() : AUTO_LIMIT)
                            .offset(request.hasOffset() ? request.getOffset() : NO_OFFSET)
                            .build());
            return ListDatabasesResponseDto.newBuilder()
                    .addAllItems(result.getItems().stream()
                            .map(ProtoApi::fromDatabase)
                            .toList())
                    .setTotal(result.getTotal())
                    .addAllScores(result.getScores())
                    .build();
        });
    }

    @Override
    public Uni<ListFlowsResponseDto> listFlows(WebFlowsRequestDto request) {
        return uniOfIO(request.getSource(), () -> {
            var result = manager.usingName(request.getSource()).listFlows(toFlowsRequest(request));
            return ListFlowsResponseDto.newBuilder()
                    .addAllItems(result.getItems().stream()
                            .map(ProtoApi::fromDataflow)
                            .toList())
                    .setTotal(result.getTotal())
                    .addAllScores(result.getScores())
                    .build();
        });
    }

    @Override
    public Uni<SearchFlowsResponseDto> searchFlows(WebSearchFlowsRequestDto request) {
        return uniOfIO(
                null,
                () -> toSearchFlowsResponse(manager.searchFlows(WebFlowsRequest.builder()
                        .sources(toSourceIds(request.getSourcesList()))
                        .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                        .query(request.hasQuery() ? request.getQuery() : NO_QUERY)
                        .maxResults(request.hasMaxResults() ? request.getMaxResults() : AUTO_LIMIT)
                        .offset(request.hasOffset() ? request.getOffset() : NO_OFFSET)
                        .plainText(request.hasPlainText() && request.getPlainText())
                        .truncate(request.hasTruncate() ? request.getTruncate() : NO_DESCRIPTION_LIMIT)
                        .confidentialityThreshold(
                                request.hasMaxConfidentiality()
                                        ? ProtoApi.toConfidentiality(request.getMaxConfidentiality())
                                        : null)
                        .timeout(toTimeout(request.hasTimeout() ? request.getTimeout() : 0))
                        .build())));
    }

    @Override
    public Uni<MetaSetDto> getMeta(WebMetaRequestDto request) {
        return uniOfIO(
                request.getSource(),
                () -> ProtoApi.fromMetaSet(manager.usingName(request.getSource())
                        .getMeta(MetaRequest.builder()
                                .flowOf(request.getFlow())
                                .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                                .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                                .build())));
    }

    @Override
    public Uni<ListDimensionsResponseDto> listDimensions(WebDimensionsRequestDto request) {
        return uniOfIO(request.getSource(), () -> {
            var result = manager.usingName(request.getSource())
                    .listDimensions(DimensionsRequest.builder()
                            .flowOf(request.getFlow())
                            .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                            .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                            .query(request.hasQuery() ? request.getQuery() : NO_QUERY)
                            .maxResults(request.hasMaxResults() ? request.getMaxResults() : AUTO_LIMIT)
                            .offset(request.hasOffset() ? request.getOffset() : NO_OFFSET)
                            .build());
            return ListDimensionsResponseDto.newBuilder()
                    .addAllItems(result.getItems().stream()
                            .map(ProtoApi::fromDimension)
                            .toList())
                    .setTotal(result.getTotal())
                    .addAllScores(result.getScores())
                    .build();
        });
    }

    @Override
    public Uni<ListAttributesResponseDto> listAttributes(WebAttributesRequestDto request) {
        return uniOfIO(request.getSource(), () -> {
            var result = manager.usingName(request.getSource())
                    .listAttributes(AttributesRequest.builder()
                            .flowOf(request.getFlow())
                            .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                            .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                            .query(request.hasQuery() ? request.getQuery() : NO_QUERY)
                            .maxResults(request.hasMaxResults() ? request.getMaxResults() : AUTO_LIMIT)
                            .offset(request.hasOffset() ? request.getOffset() : NO_OFFSET)
                            .build());
            return ListAttributesResponseDto.newBuilder()
                    .addAllItems(result.getItems().stream()
                            .map(ProtoApi::fromAttribute)
                            .toList())
                    .setTotal(result.getTotal())
                    .addAllScores(result.getScores())
                    .build();
        });
    }

    @Override
    public Uni<DataSetDto> getData(WebDataRequestDto request) {
        return uniOfIO(
                request.getSource(),
                () -> ProtoApi.fromDataSet(
                        manager.usingName(request.getSource()).getData(toDataRequest(request))));
    }

    @Override
    public Multi<SeriesDto> getDataStream(WebDataRequestDto request) {
        return multiOfIO(
                        request.getSource(),
                        () -> manager.usingName(request.getSource()).getData(toDataRequest(request)))
                .map(ProtoApi::fromSeries);
    }

    @Override
    public Uni<ListCodesResponseDto> listCodes(WebCodesRequestDto request) {
        return uniOfIO(request.getSource(), () -> {
            var result = manager.usingName(request.getSource())
                    .listCodes(CodesRequest.builder()
                            .flowOf(request.getFlow())
                            .concept(request.getConcept())
                            .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                            .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                            .query(request.hasQuery() ? request.getQuery() : NO_QUERY)
                            .maxResults(request.hasMaxResults() ? request.getMaxResults() : AUTO_LIMIT)
                            .offset(request.hasOffset() ? request.getOffset() : NO_OFFSET)
                            .build());
            return toCodesResponse(result);
        });
    }

    @Override
    public Uni<ListAvailabilityResponseDto> listAvailability(WebAvailabilityRequestDto request) {
        return uniOfIO(
                request.getSource(),
                () -> toAvailabilityResponse(manager.usingName(request.getSource())
                        .listAvailability(AvailabilityRequest.builder()
                                .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                                .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                                .flowOf(request.getFlow())
                                .keyOf(request.getKey())
                                .dimension(request.getDimension())
                                .build())));
    }

    @Override
    public Uni<CheckHealthResponseDto> checkHealth(WebHealthRequestDto request) {
        return uniOfIO(null, () -> {
            var result = manager.checkHealth(toHealthRequest(
                    request.getSourcesList(),
                    request.getChecksList().stream()
                            .map(ProtoWeb::toHealthCheck)
                            .toList()));
            return CheckHealthResponseDto.newBuilder()
                    .addAllItems(result.stream().map(ProtoWeb::fromHealthReport).toList())
                    .setTotal(result.size())
                    .build();
        });
    }

    @Override
    public Uni<ListScriptTargetsResponseDto> listScriptTargets(EmptyDto request) {
        return Uni.createFrom().item(() -> toScriptTargetsResponse(scripts));
    }

    @Override
    public Uni<ScriptDto> generateDataScript(WebDataScriptRequestDto request) {
        return generateScript(
                request.getRequest().getSource(), () -> toDataRequest(request.getRequest()), request.getOptions());
    }

    @Override
    public Uni<ScriptDto> generateFlowsScript(WebFlowsScriptRequestDto request) {
        WebFlowsRequestDto flows = request.getRequest();
        return generateScript(flows.getSource(), () -> toFlowsRequest(flows), request.getOptions());
    }

    private Uni<ScriptDto> generateScript(String source, IOSupplier<Request> request, ScriptOptionsDto options) {
        return uniOfIO(
                source,
                () -> ProtoScript.fromScript(scripts.generate(
                        ProtoScript.toScriptTarget(options),
                        source,
                        request.getWithIO(),
                        ProtoScript.toScriptOptions(options))));
    }

    static ListCodesResponseDto toCodesResponse(SearchResult<Code> codes) {
        return ListCodesResponseDto.newBuilder()
                .addAllItems(codes.getItems().stream()
                        .map(code -> ProtoApi.fromCode(code.getId(), code.getName()))
                        .toList())
                .setTotal(codes.getTotal())
                .addAllScores(codes.getScores())
                .build();
    }

    static ListAvailabilityResponseDto toAvailabilityResponse(List<Availability> availabilities) {
        return ListAvailabilityResponseDto.newBuilder()
                .addAllItems(availabilities.stream()
                        .map(availability -> AvailabilityDto.newBuilder()
                                .setDimension(availability.getDimension())
                                .addAllCodes(ProtoApi.fromCodes(availability.getCodes()))
                                .build())
                        .toList())
                .setTotal(availabilities.size())
                .build();
    }

    static ListScriptTargetsResponseDto toScriptTargetsResponse(ScriptManager scripts) {
        return ListScriptTargetsResponseDto.newBuilder()
                .addAllItems(scripts.getTargets().stream()
                        .map(target -> ProtoScript.fromScriptTarget(scripts, target))
                        .toList())
                .setTotal(scripts.getTargets().size())
                .build();
    }

    static SearchFlowsResponseDto toSearchFlowsResponse(WebFlowsResult result) {
        return SearchFlowsResponseDto.newBuilder()
                .addAllItems(result.getItems().stream()
                        .map(SdmxdlGrpcService2::fromFlowEntry)
                        .toList())
                .setTotal(result.getTotal())
                .addAllScores(result.getScores())
                .addAllFailures(result.getFailures().stream()
                        .map(SdmxdlGrpcService2::fromSourceFailure)
                        .toList())
                .build();
    }

    private static FlowEntryDto fromFlowEntry(FlowEntry entry) {
        FlowEntryDto.Builder result = FlowEntryDto.newBuilder()
                .setSource(entry.getSource().getId())
                .setFlow(ProtoApi.fromDataflow(entry.getFlow()));
        if (!DatabaseRef.NO_DATABASE.equals(entry.getDatabase())) {
            result.setDatabase(entry.getDatabase().getId());
        }
        return result.build();
    }

    private static SourceFailureDto fromSourceFailure(SourceFailure failure) {
        ApiError error = ApiError.of(failure.getCause());
        SourceFailureDto.Builder result = SourceFailureDto.newBuilder()
                .setSource(failure.getSource())
                .setReason(error.reason())
                .setMessage(error.detail());
        if (!DatabaseRef.NO_DATABASE.equals(failure.getDatabase())) {
            result.setDatabase(failure.getDatabase().getId());
        }
        if (error.upstreamStatus() != null) {
            result.setUpstreamStatus(error.upstreamStatus());
        }
        return result.build();
    }

    /**
     * Converts a timeout in milliseconds; zero means no timeout.
     */
    static @Nullable Duration toTimeout(int millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Invalid timeout: " + millis);
        }
        return millis > 0 ? Duration.ofMillis(millis) : null;
    }

    /**
     * Resolves requested source ids; a single "all" entry (case-insensitive) means every source,
     * like an empty list.
     */
    static List<String> toSourceIds(List<String> sources) {
        return sources.size() == 1 && "all".equalsIgnoreCase(sources.get(0)) ? List.of() : sources;
    }

    private static FlowsRequest toFlowsRequest(WebFlowsRequestDto request) {
        return FlowsRequest.builder()
                .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                .query(request.hasQuery() ? request.getQuery() : NO_QUERY)
                .maxResults(request.hasMaxResults() ? request.getMaxResults() : AUTO_LIMIT)
                .offset(request.hasOffset() ? request.getOffset() : NO_OFFSET)
                .plainText(request.hasPlainText() && request.getPlainText())
                .truncate(request.hasTruncate() ? request.getTruncate() : NO_DESCRIPTION_LIMIT)
                .build();
    }

    private static DataRequest toDataRequest(WebDataRequestDto request) {
        return DataRequest.builder()
                .flowOf(request.getFlow())
                .keyOf(request.getKey())
                .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                .startPeriodOf(request.hasStart() ? request.getStart() : null)
                .endPeriodOf(request.hasEnd() ? request.getEnd() : null)
                .firstNObservations(request.hasFirstN() ? request.getFirstN() : null)
                .lastNObservations(request.hasLastN() ? request.getLastN() : null)
                .seriesOffset(request.hasSeriesOffset() ? request.getSeriesOffset() : 0)
                .seriesLimit(request.hasSeriesLimit() ? request.getSeriesLimit() : null)
                .detail(ProtoApi.toDataDetail(request.getDetail()))
                .build();
    }

    /**
     * Builds a health request; empty sources or a single "all" entry (case-insensitive) means every
     * source, and empty checks means the default (monitor only).
     */
    static WebHealthRequest toHealthRequest(List<String> sources, Collection<HealthCheck> checks) {
        WebHealthRequest.Builder result = WebHealthRequest.builder().sources(toSourceIds(sources));
        if (!checks.isEmpty()) {
            result.checks(EnumSet.copyOf(checks));
        }
        return result.build();
    }

    private static <T> Uni<T> uniOfIO(String source, IOSupplier<T> supplier) {
        Uni<T> result;
        try {
            result = Uni.createFrom().item(supplier.getWithIO());
        } catch (IOException | RuntimeException ex) {
            result = Uni.createFrom().failure(ex);
        }
        return result.onFailure().transform(ex -> ApiError.of(ex).toGrpc(source));
    }

    private static <T> Multi<T> multiOfIO(String source, IOSupplier<? extends Iterable<T>> supplier) {
        Multi<T> result;
        try {
            result = Multi.createFrom().iterable(supplier.getWithIO());
        } catch (IOException | RuntimeException ex) {
            result = Multi.createFrom().failure(ex);
        }
        return result.onFailure().transform(ex -> ApiError.of(ex).toGrpc(source));
    }
}
