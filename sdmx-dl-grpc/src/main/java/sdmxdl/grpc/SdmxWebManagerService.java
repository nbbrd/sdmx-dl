package sdmxdl.grpc;

import io.quarkiverse.mcp.server.WrapBusinessError;
import io.quarkus.arc.Arc;
import io.quarkus.grpc.GrpcService;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import sdmxdl.*;
import sdmxdl.format.protobuf.*;
import sdmxdl.format.protobuf.web.MonitorReportDto;
import sdmxdl.format.protobuf.web.WebSourceDto;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.Search;
import sdmxdl.web.WebSource;

import java.io.IOException;
import java.util.Collection;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/sdmx-dl")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@GrpcService
@RegisterForReflection
@WrapBusinessError({IOException.class, IllegalArgumentException.class})
public class SdmxWebManagerService implements sdmxdl.grpc.SdmxWebManager {

    // This class is both a @GrpcService bean and a JAX-RS resource. RESTEasy Reactive instantiates
    // the resource via a no-arg constructor (not through CDI), so the shared SdmxWebManager singleton
    // is resolved programmatically to guarantee a single instance across gRPC, REST and MCP.
    private final SdmxWebManager manager = Arc.container().select(SdmxWebManager.class).get();

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

    @RequestBody(
            content = @Content(
                    examples = @ExampleObject(
                            name = "ECB example",
                            value = """
                                    {
                                      "source": "ECB"
                                    }
                                    """
                    )
            )
    )
    @POST
    @Path("/monitorReport")
    @Override
    public Uni<MonitorReportDto> getMonitorReport(SourceRequestDto request) {
        try {
            return Uni.createFrom()
                    .item(manager.getMonitorReport(request.getSource()))
                    .map(ProtoWeb::fromMonitorReport);
        } catch (IOException ex) {
            return Uni.createFrom().failure(ex);
        }
    }

    @RequestBody(
            content = @Content(
                    examples = @ExampleObject(
                            name = "ECB example",
                            value = """
                                    {
                                      "source": "ECB"
                                    }
                                    """
                    )
            )
    )
    @POST
    @Path("/databases")
    @Override
    public Multi<DatabaseDto> getDatabases(SourceRequestDto request) {
        try {
            return Multi.createFrom()
                    .iterable(manager.usingName(request.getSource()).getDatabases(ProtoGrpc.toSourceRequest(request)))
                    .map(ProtoApi::fromDatabase);
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }

    @RequestBody(
            content = @Content(
                    examples = @ExampleObject(
                            name = "ECB example",
                            value = """
                                    {
                                      "source": "ECB",
                                      "flow": "EXR"
                                    }
                                    """
                    )
            )
    )
    @POST
    @Path("/meta")
    @Override
    public Uni<MetaSetDto> getMeta(FlowRequestDto request) {
        try {
            return Uni.createFrom()
                    .item(manager.usingName(request.getSource()).getMeta(ProtoGrpc.toFlowRequest(request)))
                    .map(ProtoApi::fromMetaSet);
        } catch (IOException ex) {
            return Uni.createFrom().failure(ex);
        }
    }

    @RequestBody(
            content = @Content(
                    examples = @ExampleObject(
                            name = "ECB example",
                            value = """
                                    {
                                      "source": "ECB",
                                      "flow": "EXR",
                                      "key": "M.USD+CHF.EUR.SP00.A"
                                    }
                                    """
                    )
            )
    )
    @POST
    @Path("/data")
    @Override
    public Uni<DataSetDto> getData(KeyRequestDto request) {
        try {
            return Uni.createFrom()
                    .item(manager.usingName(request.getSource()).getData(ProtoGrpc.toKeyRequest(request)))
                    .map(ProtoApi::fromDataSet);
        } catch (IOException ex) {
            return Uni.createFrom().failure(ex);
        }
    }

    @POST
    @Path("/about")
    @Override
    public Uni<AboutDto> getAbout(EmptyDto request) {
        return Uni.createFrom()
                .item(ProtoApi.fromAbout());
    }


    @POST
    @Path("/sources")
    @Override
    public Multi<WebSourceDto> getSources(EmptyDto request) {
        return Multi.createFrom().items(manager.getSources()
                .values()
                .stream()
                .filter(source -> !source.isAlias())
                .map(ProtoWeb::fromWebSource));
    }

    @RequestBody(
            content = @Content(
                    examples = @ExampleObject(
                            name = "ECB example",
                            value = """
                                    {
                                      "source": "ECB"
                                    }
                                    """
                    )
            )
    )
    @POST
    @Path("/flows")
    @Override
    public Multi<FlowDto> getFlows(DatabaseRequestDto request) {
        try {
            return Multi.createFrom()
                    .iterable(manager.usingName(request.getSource()).getFlows(ProtoGrpc.toDatabaseRequest(request)))
                    .map(ProtoApi::fromDataflow);
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }

    @RequestBody(
            content = @Content(
                    examples = @ExampleObject(
                            name = "ECB example",
                            value = """
                                    {
                                      "source": "ECB",
                                      "flow": "EXR",
                                      "key": "M.USD+CHF.EUR.SP00.A"
                                    }
                                    """
                    )
            )
    )
    @POST
    @Path("/dataStream")
    @Override
    public Multi<SeriesDto> getDataStream(KeyRequestDto request) {
        try {
            return Multi.createFrom()
                    .iterable(manager.usingName(request.getSource()).getData(ProtoGrpc.toKeyRequest(request)))
                    .map(ProtoApi::fromSeries);
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }

    @RequestBody(
            content = @Content(
                    examples = @ExampleObject(
                            name = "ECB example",
                            value = """
                                    {
                                      "source": "ECB",
                                      "flow": "EXR",
                                      "key": "M..EUR.SP00.A",
                                      "dimension": 1
                                    }
                                    """
                    )
            )
    )
    @POST
    @Path("/availability")
    @Override
    public Multi<DimensionCodesDto> getAvailability(KeyDimensionRequestDto request) {
        DatabaseRef databaseRef = request.hasDatabase() ? DatabaseRef.parse(request.getDatabase()) : DatabaseRef.NO_DATABASE;
        FlowRef flowRef = FlowRef.parse(request.getFlow());
        Key key = Key.parse(request.getKey());
        Languages languages = request.hasLanguages() ? Languages.parse(request.getLanguages()) : Languages.ANY;
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Multi.createFrom()
                    .items(connection.getAvailableDimensionCodes(databaseRef, flowRef, key, request.getDimension()))
                    .map(codes -> DimensionCodesDto.newBuilder().addAllCodes(codes).build());
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }

    @RequestBody(
            content = @Content(
                    examples = @ExampleObject(
                            name = "ECB example",
                            value = """
                                    {
                                      "query": "european central",
                                      "maxResults": 5
                                    }
                                    """
                    )
            )
    )
    @POST
    @Path("/searchSources")
    @Override
    public Multi<WebSourceDto> searchSources(SearchSourcesRequestDto request) {
        Languages languages = request.hasLanguages() ? Languages.parse(request.getLanguages()) : Languages.ANY;
        int maxResults = request.hasMaxResults() ? request.getMaxResults() : 20;
        Collection<WebSource> sources = manager.getSources().values();
        return Multi.createFrom()
                .iterable(Search.ofSources(sources, languages).search(request.getQuery(), maxResults))
                .map(Search.Result::getItem)
                .map(ProtoWeb::fromWebSource);
    }

    @POST
    @Path("/searchDatabases")
    @Override
    public Multi<DatabaseDto> searchDatabases(SearchDatabaseRequestDto request) {
        Languages languages = request.hasLanguages() ? Languages.parse(request.getLanguages()) : Languages.ANY;
        int maxResults = request.hasMaxResults() ? request.getMaxResults() : 20;
        try {
            Collection<Database> databases = manager
                    .usingName(request.getSource())
                    .getDatabases(SourceRequest
                            .builder()
                            .languages(languages)
                            .build());
            return Multi.createFrom()
                    .iterable(Search.ofDatabases(databases).search(request.getQuery(), maxResults))
                    .map(Search.Result::getItem)
                    .map(ProtoApi::fromDatabase);
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }

    @RequestBody(
            content = @Content(
                    examples = @ExampleObject(
                            name = "ECB example",
                            value = """
                                    {
                                      "source": "ECB",
                                      "query": "exchange rates",
                                      "maxResults": 5
                                    }
                                    """
                    )
            )
    )
    @POST
    @Path("/searchFlows")
    @Override
    public Multi<FlowDto> searchFlows(SearchFlowsRequestDto request) {
        DatabaseRef databaseRef = request.hasDatabase() ? DatabaseRef.parse(request.getDatabase()) : DatabaseRef.NO_DATABASE;
        Languages languages = request.hasLanguages() ? Languages.parse(request.getLanguages()) : Languages.ANY;
        int maxResults = request.hasMaxResults() ? request.getMaxResults() : 20;
        try {
            Collection<Flow> flows = manager
                    .usingName(request.getSource())
                    .getFlows(DatabaseRequest
                            .builder()
                            .database(databaseRef)
                            .languages(languages)
                            .build());
            return Multi.createFrom()
                    .iterable(Search.ofFlows(flows).search(request.getQuery(), maxResults))
                    .map(Search.Result::getItem)
                    .map(ProtoApi::fromDataflow);
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }
}
