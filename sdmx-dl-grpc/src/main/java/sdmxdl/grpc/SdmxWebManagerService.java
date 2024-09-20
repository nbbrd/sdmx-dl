package sdmxdl.grpc;

import io.quarkus.grpc.GrpcService;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import sdmxdl.*;
import sdmxdl.format.protobuf.DataSet;
import sdmxdl.format.protobuf.Series;
import sdmxdl.format.protobuf.*;
import sdmxdl.format.protobuf.web.MonitorReport;
import sdmxdl.format.protobuf.web.SdmxWebSource;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/sdmx-dl")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@GrpcService
@RegisterForReflection
public class SdmxWebManagerService implements sdmxdl.grpc.SdmxWebManager {

    private final SdmxWebManager manager = SdmxWebManager.ofServiceLoader();
    private final Languages languages = Languages.ANY;

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
                    .map(ProtobufMonitors::fromMonitorReport);
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
    @Path("/flow")
    @Override
    public Uni<Dataflow> getFlow(FlowRequest request) {
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Uni.createFrom()
                    .item(connection.getFlow(CatalogRef.NO_CATALOG, FlowRef.parse(request.getFlow())))
                    .map(ProtobufRepositories::fromDataflow);
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
    public Uni<DataStructure> getStructure(FlowRequest request) {
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Uni.createFrom()
                    .item(connection.getStructure(CatalogRef.NO_CATALOG, FlowRef.parse(request.getFlow())))
                    .map(ProtobufRepositories::fromDataStructure);
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
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Uni.createFrom()
                    .item(connection.getData(CatalogRef.NO_CATALOG, getFlowRef(request), getDataQuery(request)))
                    .map(ProtobufRepositories::fromDataSet);
        } catch (IOException ex) {
            return Uni.createFrom().failure(ex);
        }
    }

    @POST
    @Path("/sources")
    @Override
    public Multi<SdmxWebSource> getSources(Empty request) {
        return Multi.createFrom().items(manager.getSources()
                .values()
                .stream()
                .map(ProtobufSources::fromWebSource));
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
    public Multi<Dataflow> getFlows(SourceRequest request) {
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Multi.createFrom()
                    .items(connection.getFlows(CatalogRef.NO_CATALOG).stream())
                    .map(ProtobufRepositories::fromDataflow);
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
                                      "flow": "EXR";
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
        try (Connection connection = manager.getConnection(request.getSource(), languages)) {
            return Multi.createFrom()
                    .items(connection.getData(CatalogRef.NO_CATALOG, getFlowRef(request), getDataQuery(request)).getData().stream())
                    .map(ProtobufRepositories::fromSeries);
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }

    private FlowRef getFlowRef(KeyRequest request) {
        return FlowRef.parse(request.getFlow());
    }

    private Query getDataQuery(KeyRequest request) {
        return Query
                .builder()
                .key(Key.parse(request.getKey()))
                .detail(Detail.FULL)
                .build();
    }
}
