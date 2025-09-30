package sdmxdl.grpc;

import io.quarkiverse.mcp.server.*;
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
import sdmxdl.format.protobuf.MetaSet;
import sdmxdl.format.protobuf.Series;
import sdmxdl.format.protobuf.web.MonitorReport;
import sdmxdl.format.protobuf.web.WebSource;
import sdmxdl.format.protobuf.web.WebSources;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static sdmxdl.DatabaseRef.NO_DATABASE_KEYWORD;
import static sdmxdl.Languages.ANY_KEYWORD;

@Path("/sdmx-dl")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@GrpcService
@RegisterForReflection
@WrapBusinessError({IOException.class, IllegalArgumentException.class})
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
                    .iterable(manager.using(request.getSource()).getDatabases(ProtoGrpc.toSourceRequest(request)))
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
    public Uni<MetaSet> getMeta(FlowRequest request) {
        try {
            return Uni.createFrom()
                    .item(manager.using(request.getSource()).getMeta(ProtoGrpc.toFlowRequest(request)))
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
    public Uni<DataSet> getData(KeyRequest request) {
        try {
            return Uni.createFrom()
                    .item(manager.using(request.getSource()).getData(ProtoGrpc.toKeyRequest(request)))
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

    private sdmxdl.web.WebSource getPublicSourceForMcp(String source) {
        sdmxdl.web.WebSource webSource = manager.getSources().get(source);
        if (webSource == null || !Confidentiality.PUBLIC.isAllowedIn(webSource)) {
            throw new IllegalArgumentException("Cannot find source: " + source);
        }
        return webSource;
    }

    @Prompt(description = "List SDMX sources IDs.", name = "listSourceIds")
    public PromptResponse mcpSourceIds() {
        return PromptResponse.withMessages(manager.getSources()
                .values()
                .stream()
                .filter(Confidentiality.PUBLIC::isAllowedIn)
                .map(sdmxdl.web.WebSource::getId)
                .map(PromptMessage::withUserRole)
                .toList()
        );
    }

    private static final String SOURCE_ARG = "SDMX source ID";
    private static final String LANGUAGES_ARG = "Language priority list";
    private static final String DATABASE_ARG = "Database ref";
    private static final String FLOW_ARG = "SDMX flow ref";
    private static final String KEY_ARG = "SDMX key";
    private static final String DETAIL_ARG = "Amount of information to retrieve (FULL, DATA_ONLY, SERIES_KEYS_ONLY, NO_DATA)";

    @Tool(description = "Get description of SDMX-DL.")
    public About mcpAbout() {
        return ProtoApi.fromAbout();
    }

    @Tool(description = "List SDMX sources.")
    public WebSources mcpSources() {
        return ProtoWeb.fromWebSources(sdmxdl.web.WebSources.builder().sources(
                manager.getSources()
                        .values()
                        .stream()
                        .filter(Confidentiality.PUBLIC::isAllowedIn)
                        .toList()
        ).build());
    }

    @Tool(description = "List SDMX databases.")
    public List<Database> mcpDatabases(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = ANY_KEYWORD) String languages
    ) throws IOException {
        return manager.using(getPublicSourceForMcp(source))
                .getDatabases(sdmxdl.SourceRequest
                        .builder()
                        .languagesOf(languages)
                        .build())
                .stream()
                .map(ProtoApi::fromDatabase)
                .toList();
    }

    @Tool(description = "List SDMX data flows.")
    public List<Flow> mcpFlows(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = ANY_KEYWORD) String languages
    ) throws IOException {
        return manager
                .using(getPublicSourceForMcp(source))
                .getFlows(sdmxdl.DatabaseRequest
                        .builder()
                        .databaseOf(database)
                        .languagesOf(languages)
                        .build())
                .stream()
                .map(ProtoApi::fromDataflow)
                .toList();
    }

    @Tool(description = "Get SDMX metadata such as flow and structure.")
    public MetaSet mcpMeta(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = FLOW_ARG) String flow,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = ANY_KEYWORD) String languages
    ) throws IOException {
        return ProtoApi.fromMetaSet(manager
                .using(getPublicSourceForMcp(source))
                .getMeta(sdmxdl.FlowRequest
                        .builder()
                        .flowOf(flow)
                        .databaseOf(database)
                        .languagesOf(languages)
                        .build())
        );
    }

    @Tool(description = "Get SDMX data series alongside their flow reference and the query used to get them.")
    public DataSet mcpData(
            @ToolArg(description = SOURCE_ARG) String source,
            @ToolArg(description = FLOW_ARG) String flow,
            @ToolArg(description = KEY_ARG) String key,
            @ToolArg(description = DETAIL_ARG) String detail,
            @ToolArg(description = DATABASE_ARG, required = false, defaultValue = NO_DATABASE_KEYWORD) String database,
            @ToolArg(description = LANGUAGES_ARG, required = false, defaultValue = ANY_KEYWORD) String languages
    ) throws IOException {
        return ProtoApi.fromDataSet(manager
                .using(getPublicSourceForMcp(source))
                .getData(sdmxdl.KeyRequest
                        .builder()
                        .flowOf(flow)
                        .keyOf(key)
                        .detailOf(detail)
                        .databaseOf(database)
                        .languagesOf(languages)
                        .build())
        );
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
                    .iterable(manager.using(request.getSource()).getFlows(ProtoGrpc.toDatabaseRequest(request)))
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
                    .iterable(manager.using(request.getSource()).getData(ProtoGrpc.toKeyRequest(request)))
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

