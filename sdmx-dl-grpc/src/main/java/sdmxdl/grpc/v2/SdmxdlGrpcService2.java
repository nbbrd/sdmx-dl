package sdmxdl.grpc.v2;

import static sdmxdl.DatabaseRef.NO_DATABASE_KEYWORD;
import static sdmxdl.HasLimit.NO_LIMIT;
import static sdmxdl.HasSearchQuery.NO_QUERY;
import static sdmxdl.Languages.ANY_KEYWORD;

import io.quarkus.arc.Arc;
import io.quarkus.grpc.GrpcService;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nbbrd.io.function.IOSupplier;
import sdmxdl.*;
import sdmxdl.format.protobuf.*;
import sdmxdl.format.protobuf.web.MonitorReportDto;
import sdmxdl.format.protobuf.web.MonitorStatusDto;
import sdmxdl.format.protobuf.web.WebSourceDto;
import sdmxdl.web.WebSource;
import sdmxdl.web.WebSourcesRequest;

@GrpcService
@RegisterForReflection
public class SdmxdlGrpcService2 implements SdmxWebManager {

    // This class is both a @GrpcService bean and a JAX-RS resource. RESTEasy Reactive instantiates
    // the resource via a no-arg constructor (not through CDI), so the shared SdmxWebManager singleton
    // is resolved programmatically to guarantee a single instance across gRPC, REST and MCP.
    private final sdmxdl.web.SdmxWebManager manager =
            Arc.container().select(sdmxdl.web.SdmxWebManager.class).get();

    @Override
    public Uni<AboutDto> getAbout(EmptyDto request) {
        return Uni.createFrom().item(ProtoApi.fromAbout());
    }

    @Override
    public Multi<WebSourceDto> listSources(WebSourcesRequestDto request) {
        List<WebSource> result = manager.listSources(WebSourcesRequest.builder()
                .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                .query(request.hasQuery() ? request.getQuery() : HasSearchQuery.NO_QUERY)
                .maxResults(request.hasMaxResults() ? request.getMaxResults() : HasLimit.NO_LIMIT)
                .confidentialityThreshold(
                        request.hasConfidentialityThreshold()
                                ? ProtoApi.toConfidentiality(request.getConfidentialityThreshold())
                                : Confidentiality.SECRET)
                .build());

        return Multi.createFrom().iterable(result).map(ProtoWeb::fromWebSource);
    }

    @Override
    public Multi<DatabaseDto> listDatabases(WebDatabasesRequestDto request) {
        return multiOfIO(() -> manager.usingName(request.getSource())
                        .listDatabases(DatabasesRequest.builder()
                                .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                                .query(request.hasQuery() ? request.getQuery() : HasSearchQuery.NO_QUERY)
                                .maxResults(request.hasMaxResults() ? request.getMaxResults() : HasLimit.NO_LIMIT)
                                .build()))
                .map(ProtoApi::fromDatabase);
    }

    @Override
    public Multi<FlowDto> listFlows(WebFlowsRequestDto request) {
        return multiOfIO(() -> manager.usingName(request.getSource())
                        .listFlows(FlowsRequest.builder()
                                .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                                .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                                .query(request.hasQuery() ? request.getQuery() : HasSearchQuery.NO_QUERY)
                                .maxResults(request.hasMaxResults() ? request.getMaxResults() : HasLimit.NO_LIMIT)
                                .build()))
                .map(ProtoApi::fromDataflow);
    }

    @Override
    public Uni<MetaSetDto> getMeta(WebMetaRequestDto request) {
        return uniOfIO(() -> manager.usingName(request.getSource())
                        .getMeta(MetaRequest.builder()
                                .flowOf(request.getFlow())
                                .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                                .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                                .build()))
                .map(ProtoApi::fromMetaSet);
    }

    @Override
    public Multi<DimensionDto> listDimensions(WebDimensionsRequestDto request) {
        return multiOfIO(() -> manager.usingName(request.getSource())
                        .listDimensions(DimensionsRequest.builder()
                                .flowOf(request.getFlow())
                                .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                                .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                                .query(request.hasQuery() ? request.getQuery() : NO_QUERY)
                                .maxResults(request.hasMaxResults() ? request.getMaxResults() : NO_LIMIT)
                                .build()))
                .map(ProtoApi::fromDimension);
    }

    @Override
    public Multi<AttributeDto> listAttributes(WebAttributesRequestDto request) {
        return multiOfIO(() -> manager.usingName(request.getSource())
                        .listAttributes(AttributesRequest.builder()
                                .flowOf(request.getFlow())
                                .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                                .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                                .query(request.hasQuery() ? request.getQuery() : NO_QUERY)
                                .maxResults(request.hasMaxResults() ? request.getMaxResults() : NO_LIMIT)
                                .build()))
                .map(ProtoApi::fromAttribute);
    }

    @Override
    public Uni<DataSetDto> getData(WebDataRequestDto request) {
        return uniOfIO(() -> manager.usingName(request.getSource())
                        .getData(DataRequest.builder()
                                .flowOf(request.getFlow())
                                .keyOf(request.getKey())
                                .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                                .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                                .startPeriodOf(request.hasStartPeriod() ? request.getStartPeriod() : null)
                                .endPeriodOf(request.hasEndPeriod() ? request.getEndPeriod() : null)
                                .firstNObservations(
                                        request.hasFirstNObservations() ? request.getFirstNObservations() : null)
                                .lastNObservations(
                                        request.hasLastNObservations() ? request.getLastNObservations() : null)
                                .detail(ProtoApi.toDataDetail(request.getDetail()))
                                .build()))
                .map(ProtoApi::fromDataSet);
    }

    @Override
    public Multi<SeriesDto> getDataStream(WebDataRequestDto request) {
        return multiOfIO(() -> manager.usingName(request.getSource())
                        .getData(DataRequest.builder()
                                .flowOf(request.getFlow())
                                .keyOf(request.getKey())
                                .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                                .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                                .startPeriodOf(request.hasStartPeriod() ? request.getStartPeriod() : null)
                                .endPeriodOf(request.hasEndPeriod() ? request.getEndPeriod() : null)
                                .firstNObservations(
                                        request.hasFirstNObservations() ? request.getFirstNObservations() : null)
                                .lastNObservations(
                                        request.hasLastNObservations() ? request.getLastNObservations() : null)
                                .detail(ProtoApi.toDataDetail(request.getDetail()))
                                .build()))
                .map(ProtoApi::fromSeries);
    }

    @Override
    public Uni<CodelistDto> listCodes(WebCodesRequestDto request) {
        return uniOfIO(() -> manager.usingName(request.getSource())
                        .listCodes(CodesRequest.builder()
                                .flowOf(request.getFlow())
                                .concept(request.getConcept())
                                .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                                .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                                .query(request.hasQuery() ? request.getQuery() : NO_QUERY)
                                .maxResults(request.hasMaxResults() ? request.getMaxResults() : NO_LIMIT)
                                .build()))
                .map(codes -> CodelistDto.newBuilder()
                        .setRef("")
                        .setCodeCount(codes.size())
                        .putAllCodes(codes)
                        .build());
    }

    @Override
    public Uni<CodelistDto> listAvailability(WebAvailabilityRequestDto request) {
        return uniOfIO(() -> manager.usingName(request.getSource())
                        .listAvailability(AvailabilityRequest.builder()
                                .languagesOf(request.hasLanguages() ? request.getLanguages() : ANY_KEYWORD)
                                .databaseOf(request.hasDatabase() ? request.getDatabase() : NO_DATABASE_KEYWORD)
                                .flowOf(request.getFlow())
                                .keyOf(request.getKey())
                                .dimension(request.getDimension())
                                .build()))
                .map(codes -> CodelistDto.newBuilder()
                        .setRef("")
                        .setCodeCount(codes.size())
                        .putAllCodes(codes)
                        .build());
    }

    @Override
    public Multi<MonitorReportDto> listStatuses(WebStatusesRequestDto request) {
        List<String> sources = resolveSources(request.getSourcesList());
        List<MonitorReportDto> reports = new ArrayList<>(sources.size());
        for (String name : sources) {
            try {
                reports.add(ProtoWeb.fromMonitorReport(manager.getMonitorReport(name)));
            } catch (IOException ex) {
                reports.add(MonitorReportDto.newBuilder()
                        .setSource(name)
                        .setStatus(MonitorStatusDto.UNKNOWN)
                        .build());
            }
        }
        return Multi.createFrom().iterable(reports);
    }

    private List<String> resolveSources(List<String> requested) {
        if (requested.isEmpty() || (requested.size() == 1 && "all".equalsIgnoreCase(requested.get(0)))) {
            return manager.getSources().values().stream()
                    .filter(source -> !source.isAlias())
                    .map(WebSource::getId)
                    .toList();
        }
        return requested;
    }

    private static <T> Uni<T> uniOfIO(IOSupplier<T> supplier) {
        try {
            return Uni.createFrom().item(supplier.getWithIO());
        } catch (IOException ex) {
            return Uni.createFrom().failure(ex);
        }
    }

    private static <T> Multi<T> multiOfIO(IOSupplier<? extends Iterable<T>> supplier) {
        try {
            return Multi.createFrom().iterable(supplier.getWithIO());
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }
}
