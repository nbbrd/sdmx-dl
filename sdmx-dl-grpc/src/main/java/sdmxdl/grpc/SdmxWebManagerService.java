package sdmxdl.grpc;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
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
import sdmxdl.Confidentiality;
import sdmxdl.format.protobuf.*;
import sdmxdl.format.protobuf.About;
import sdmxdl.format.protobuf.DataSet;
import sdmxdl.format.protobuf.Database;
import sdmxdl.format.protobuf.Flow;
import sdmxdl.format.protobuf.Series;
import sdmxdl.format.protobuf.Structure;
import sdmxdl.format.protobuf.web.*;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/sdmx-dl")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@GrpcService
@RegisterForReflection
public class SdmxWebManagerService implements sdmxdl.grpc.SdmxWebManager {

    private final SdmxWebManager manager = SdmxWebManager.ofServiceLoader().warmupAsync();

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
    public Uni<MonitorReport> getMonitorReport(SourceRequest request) {
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
    public Multi<Database> getDatabases(SourceRequest request) {
        try {
            return Multi.createFrom()
                    .items(manager.getDatabases(ProtoWeb.toSourceRequest(request)).stream())
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
    @Path("/flow")
    @Override
    public Uni<Flow> getFlow(FlowRequest request) {
        try {
            return Uni.createFrom()
                    .item(manager.getFlow(ProtoWeb.toFlowRequest(request)))
                    .map(ProtoApi::fromDataflow);
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
                                      "flow": "EXR"
                                    }
                                    """
                    )
            )
    )
    @POST
    @Path("/structure")
    @Override
    public Uni<Structure> getStructure(FlowRequest request) {
        try {
            return Uni.createFrom()
                    .item(manager.getStructure(ProtoWeb.toFlowRequest(request)))
                    .map(ProtoApi::fromDataStructure);
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
    public Uni<DataSet> getData(KeyRequest request) {
        try {
            return Uni.createFrom()
                    .item(manager.getData(ProtoWeb.toKeyRequest(request)))
                    .map(ProtoApi::fromDataSet);
        } catch (IOException ex) {
            return Uni.createFrom().failure(ex);
        }
    }

    @POST
    @Path("/about")
    @Override
    public Uni<About> getAbout(Empty request) {
        return Uni.createFrom()
                .item(ProtoApi.fromAbout());
    }

    @Tool(description = "Get description of SDMX-DL.")
    public About mcpGetAbout() {
        return ProtoApi.fromAbout();
    }

    @Tool(description = "List SDMX sources.")
    public WebSources mcpGetSources() {
        return ProtoWeb.fromWebSources(sdmxdl.web.WebSources.builder().sources(
                manager.getSources()
                        .values()
                        .stream()
                        .filter(Confidentiality.PUBLIC::isAllowedIn)
                        .toList()
        ).build());
    }

    @Tool(description = "List SDMX data flows.")
    public List<Flow> mcpGetFlows(@ToolArg(description = "SDMX source ID") String source) {
        sdmxdl.web.WebSource webSource = manager.getSources().get(source);
        if (webSource == null || !Confidentiality.PUBLIC.isAllowedIn(webSource)) {
            throw new RuntimeException("Cannot access flows for source: " + source);
        }
        try {
            return manager.getFlows(sdmxdl.web.DatabaseRequest.builder().build()).stream()
                    .map(ProtoApi::fromDataflow)
                    .toList();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @POST
    @Path("/sources")
    @Override
    public Multi<WebSource> getSources(Empty request) {
        return Multi.createFrom().items(manager.getSources()
                .values()
                .stream()
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
    public Multi<Flow> getFlows(DatabaseRequest request) {
        try {
            return Multi.createFrom()
                    .items(manager.getFlows(ProtoWeb.toDatabaseRequest(request)).stream())
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
    public Multi<Series> getDataStream(KeyRequest request) {
        try {
            return Multi.createFrom()
                    .items(manager.getData(ProtoWeb.toKeyRequest(request)).getData().stream())
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
    public Multi<DimensionCodes> getAvailability(KeyDimensionRequest request) {
        DatabaseRef databaseRef = request.hasDatabase() ? DatabaseRef.parse(request.getDatabase()) : DatabaseRef.NO_DATABASE;
        FlowRef flowRef = FlowRef.parse(request.getFlow());
        Key key = Key.parse(request.getKey());
        Languages languages = request.hasLanguages() ? Languages.parse(request.getLanguages()) : Languages.ANY;
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Multi.createFrom()
                    .items(connection.getAvailableDimensionCodes(databaseRef, flowRef, key, request.getDimension()))
                    .map(codes -> DimensionCodes.newBuilder().addAllCodes(codes).build());
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }
}
