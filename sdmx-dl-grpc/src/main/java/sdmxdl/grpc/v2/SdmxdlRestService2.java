package sdmxdl.grpc.v2;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static sdmxdl.DatabaseRef.NO_DATABASE_KEYWORD;
import static sdmxdl.HasLimit.NO_LIMIT;
import static sdmxdl.HasSearchQuery.NO_QUERY;
import static sdmxdl.Languages.ANY_KEYWORD;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nbbrd.io.function.IOSupplier;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import sdmxdl.*;
import sdmxdl.format.protobuf.*;
import sdmxdl.format.protobuf.web.MonitorReportDto;
import sdmxdl.format.protobuf.web.MonitorStatusDto;
import sdmxdl.format.protobuf.web.WebSourceDto;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.WebSourcesRequest;

/**
 * REST counterpart of {@link SdmxdlGrpcService2}, exposing the same features
 * through plain HTTP GET endpoints instead of gRPC calls.
 */
@Path("/sdmx-dl/v2")
@Produces(APPLICATION_JSON)
@RegisterForReflection
public class SdmxdlRestService2 {

    // This class is a JAX-RS resource instantiated by RESTEasy Reactive via a no-arg
    // constructor (not through CDI), so the shared SdmxWebManager singleton is resolved
    // programmatically to guarantee a single instance across gRPC, REST and MCP.
    private final SdmxWebManager manager =
            Arc.container().select(SdmxWebManager.class).get();

    public record ErrorResponse(String type, String message) {
        private static ErrorResponse of(Exception x) {
            return new ErrorResponse(x.getClass().getSimpleName(), x.getMessage());
        }
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapException(IllegalArgumentException x) {
        return RestResponse.status(Response.Status.BAD_REQUEST, ErrorResponse.of(x));
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapException(IOException x) {
        return RestResponse.status(Response.Status.BAD_REQUEST, ErrorResponse.of(x));
    }

    @GET
    @Path("/about")
    public Uni<AboutDto> getAbout() {
        return Uni.createFrom().item(ProtoApi.fromAbout());
    }

    @GET
    @Path("/sources")
    public Multi<WebSourceDto> listSources(
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + NO_LIMIT) int maxResults,
            @QueryParam("confidentialityThreshold") @DefaultValue("SECRET") String confidentialityThreshold) {
        List<WebSource> result = manager.listSources(WebSourcesRequest.builder()
                .languagesOf(languages)
                .query(query)
                .maxResults(maxResults)
                .confidentialityThreshold(Confidentiality.valueOf(confidentialityThreshold))
                .build());

        return Multi.createFrom().iterable(result).map(ProtoWeb::fromWebSource);
    }

    @GET
    @Path("/{source}/databases")
    public Multi<DatabaseDto> listDatabases(
            @PathParam("source") String source,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + NO_LIMIT) int maxResults) {
        return multiOfIO(() -> manager.usingName(source)
                        .listDatabases(DatabasesRequest.builder()
                                .languagesOf(languages)
                                .query(query)
                                .maxResults(maxResults)
                                .build()))
                .map(ProtoApi::fromDatabase);
    }

    @GET
    @Path("/{source}/flows")
    public Multi<FlowDto> listFlows(
            @PathParam("source") String source,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + NO_LIMIT) int maxResults) {
        return multiOfIO(() -> manager.usingName(source)
                        .listFlows(FlowsRequest.builder()
                                .databaseOf(database)
                                .languagesOf(languages)
                                .query(query)
                                .maxResults(maxResults)
                                .build()))
                .map(ProtoApi::fromDataflow);
    }

    @GET
    @Path("/{source}/{flow}/meta")
    public Uni<MetaSetDto> getMeta(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages) {
        return uniOfIO(() -> manager.usingName(source)
                        .getMeta(MetaRequest.builder()
                                .flowOf(flow)
                                .databaseOf(database)
                                .languagesOf(languages)
                                .build()))
                .map(ProtoApi::fromMetaSet);
    }

    @GET
    @Path("/{source}/{flow}/dimensions")
    public Multi<DimensionDto> listDimensions(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + NO_LIMIT) int maxResults,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages) {
        return multiOfIO(() -> manager.usingName(source)
                        .listDimensions(DimensionsRequest.builder()
                                .flowOf(flow)
                                .databaseOf(database)
                                .languagesOf(languages)
                                .query(query)
                                .maxResults(maxResults)
                                .build()))
                .map(ProtoApi::fromDimension);
    }

    @GET
    @Path("/{source}/{flow}/attributes")
    public Multi<AttributeDto> listAttributes(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + NO_LIMIT) int maxResults,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages) {
        return multiOfIO(() -> manager.usingName(source)
                        .listAttributes(AttributesRequest.builder()
                                .flowOf(flow)
                                .databaseOf(database)
                                .languagesOf(languages)
                                .query(query)
                                .maxResults(maxResults)
                                .build()))
                .map(ProtoApi::fromAttribute);
    }

    @GET
    @Path("/{source}/{flow}/data")
    public Uni<DataSetDto> getData(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @QueryParam("key") @DefaultValue("all") String key,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("startPeriod") String startPeriod,
            @QueryParam("endPeriod") String endPeriod,
            @QueryParam("firstNObservations") Integer firstNObservations,
            @QueryParam("lastNObservations") Integer lastNObservations,
            @QueryParam("detail") @DefaultValue("FULL") Detail detail) {
        return uniOfIO(() -> manager.usingName(source)
                        .getData(DataRequest.builder()
                                .flowOf(flow)
                                .keyOf(key)
                                .databaseOf(database)
                                .languagesOf(languages)
                                .startPeriodOf(startPeriod)
                                .endPeriodOf(endPeriod)
                                .firstNObservations(firstNObservations)
                                .lastNObservations(lastNObservations)
                                .detail(detail)
                                .build()))
                .map(ProtoApi::fromDataSet);
    }

    @GET
    @Path("/{source}/{flow}/data:stream")
    public Multi<SeriesDto> getDataStream(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @QueryParam("key") @DefaultValue("all") String key,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("startPeriod") String startPeriod,
            @QueryParam("endPeriod") String endPeriod,
            @QueryParam("firstNObservations") Integer firstNObservations,
            @QueryParam("lastNObservations") Integer lastNObservations,
            @QueryParam("detail") @DefaultValue("FULL") Detail detail) {
        return multiOfIO(() -> manager.usingName(source)
                        .getData(DataRequest.builder()
                                .flowOf(flow)
                                .keyOf(key)
                                .databaseOf(database)
                                .languagesOf(languages)
                                .startPeriodOf(startPeriod)
                                .endPeriodOf(endPeriod)
                                .firstNObservations(firstNObservations)
                                .lastNObservations(lastNObservations)
                                .detail(detail)
                                .build()))
                .map(ProtoApi::fromSeries);
    }

    @GET
    @Path("/{source}/{flow}/codes/{dimension}")
    public Uni<CodelistDto> getCodes(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @PathParam("dimension") String dimension,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + NO_LIMIT) int maxResults,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages) {
        return uniOfIO(() -> manager.usingName(source)
                        .listCodes(CodesRequest.builder()
                                .flowOf(flow)
                                .concept(dimension)
                                .databaseOf(database)
                                .languagesOf(languages)
                                .query(query)
                                .maxResults(maxResults)
                                .build()))
                .map(codes -> CodelistDto.newBuilder()
                        .setRef("")
                        .setCodeCount(codes.size())
                        .putAllCodes(codes)
                        .build());
    }

    @GET
    @Path("/{source}/{flow}/availability/{dimension}")
    public Uni<CodelistDto> getAvailability(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @PathParam("dimension") String dimension,
            @QueryParam("key") @DefaultValue("all") String key,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages) {
        return uniOfIO(() -> manager.usingName(source)
                        .listAvailability(AvailabilityRequest.builder()
                                .languagesOf(languages)
                                .databaseOf(database)
                                .flowOf(flow)
                                .keyOf(key)
                                .dimension(dimension)
                                .build()))
                .map(codes -> CodelistDto.newBuilder()
                        .setRef("")
                        .setCodeCount(codes.size())
                        .putAllCodes(codes)
                        .build());
    }

    @GET
    @Path("/statuses")
    public Multi<MonitorReportDto> listStatuses(@QueryParam("sources") @DefaultValue("all") String sources) {
        List<String> resolved = resolveSources(sources);
        List<MonitorReportDto> reports = new ArrayList<>(resolved.size());
        for (String name : resolved) {
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

    private List<String> resolveSources(String sources) {
        if (sources == null || sources.isBlank() || sources.equalsIgnoreCase("all")) {
            return manager.getSources().values().stream()
                    .filter(source -> !source.isAlias())
                    .map(WebSource::getId)
                    .toList();
        }
        return Arrays.stream(sources.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .toList();
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
