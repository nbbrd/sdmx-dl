package sdmxdl.grpc;

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
import sdmxdl.Detail;
import sdmxdl.Query;
import sdmxdl.*;
import sdmxdl.format.protobuf.About;
import sdmxdl.format.protobuf.DataSet;
import sdmxdl.format.protobuf.Database;
import sdmxdl.format.protobuf.Flow;
import sdmxdl.format.protobuf.Series;
import sdmxdl.format.protobuf.Structure;
import sdmxdl.format.protobuf.*;
import sdmxdl.format.protobuf.web.MonitorReport;
import sdmxdl.format.protobuf.web.WebSource;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;

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
        Languages languages = request.hasLanguages() ? Languages.parse(request.getLanguages()) : Languages.ANY;
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Multi.createFrom()
                    .items(connection.getDatabases().stream())
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
        DatabaseRef databaseRef = request.hasDatabase() ? DatabaseRef.parse(request.getDatabase()) : DatabaseRef.NO_DATABASE;
        FlowRef flowRef = FlowRef.parse(request.getFlow());
        Languages languages = request.hasLanguages() ? Languages.parse(request.getLanguages()) : Languages.ANY;
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Uni.createFrom()
                    .item(connection.getFlow(databaseRef, flowRef))
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
        DatabaseRef databaseRef = request.hasDatabase() ? DatabaseRef.parse(request.getDatabase()) : DatabaseRef.NO_DATABASE;
        FlowRef flowRef = FlowRef.parse(request.getFlow());
        Languages languages = request.hasLanguages() ? Languages.parse(request.getLanguages()) : Languages.ANY;
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Uni.createFrom()
                    .item(connection.getStructure(databaseRef, flowRef))
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
        DatabaseRef databaseRef = request.hasDatabase() ? DatabaseRef.parse(request.getDatabase()) : DatabaseRef.NO_DATABASE;
        FlowRef flowRef = FlowRef.parse(request.getFlow());
        Query query = getDataQuery(request);
        Languages languages = request.hasLanguages() ? Languages.parse(request.getLanguages()) : Languages.ANY;
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Uni.createFrom()
                    .item(connection.getData(databaseRef, flowRef, query))
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
                .item(About.newBuilder().setName(sdmxdl.About.NAME).setVersion(sdmxdl.About.VERSION).build());
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
        DatabaseRef databaseRef = request.hasDatabase() ? DatabaseRef.parse(request.getDatabase()) : DatabaseRef.NO_DATABASE;
        Languages languages = request.hasLanguages() ? Languages.parse(request.getLanguages()) : Languages.ANY;
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Multi.createFrom()
                    .items(connection.getFlows(databaseRef).stream())
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
        DatabaseRef databaseRef = request.hasDatabase() ? DatabaseRef.parse(request.getDatabase()) : DatabaseRef.NO_DATABASE;
        FlowRef flowRef = FlowRef.parse(request.getFlow());
        Query query = getDataQuery(request);
        Languages languages = request.hasLanguages() ? Languages.parse(request.getLanguages()) : Languages.ANY;
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Multi.createFrom()
                    .items(connection.getData(databaseRef, flowRef, query).getData().stream())
                    .map(ProtoApi::fromSeries);
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }

    private Query getDataQuery(KeyRequest request) {
        return Query
                .builder()
                .key(Key.parse(request.getKey()))
                .detail(Detail.FULL)
                .build();
    }
}
