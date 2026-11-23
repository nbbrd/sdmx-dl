package sdmxdl.grpc.v2;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static sdmxdl.DatabaseRef.NO_DATABASE_KEYWORD;
import static sdmxdl.HasDescription.NO_DESCRIPTION_LIMIT;
import static sdmxdl.HasSearch.AUTO_LIMIT;
import static sdmxdl.HasSearch.NO_QUERY;
import static sdmxdl.Languages.ANY_KEYWORD;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import nbbrd.io.function.IOSupplier;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import sdmxdl.*;
import sdmxdl.format.protobuf.*;
import sdmxdl.format.protobuf.web.HealthReportDto;
import sdmxdl.grpc.ProtoGrpc;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptOptions;
import sdmxdl.script.ScriptTarget;
import sdmxdl.web.HealthCheck;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SourceIcon;
import sdmxdl.web.WebFlowsRequest;
import sdmxdl.web.WebHealthRequest;
import sdmxdl.web.WebSource;
import sdmxdl.web.WebSourcesRequest;

/**
 * REST counterpart of {@link SdmxdlGrpcService2}, exposing the same features
 * through plain HTTP GET endpoints instead of gRPC calls.
 */
@Tag(name = "sdmx-dl v2", description = "sdmx-dl REST API version 2")
@Path("/sdmx-dl/v2")
@Produces(APPLICATION_JSON)
@RegisterForReflection
public class SdmxdlRestService2 {

    // This class is a JAX-RS resource instantiated by RESTEasy Reactive via a no-arg
    // constructor (not through CDI), so the shared SdmxWebManager singleton is resolved
    // programmatically to guarantee a single instance across gRPC, REST and MCP.
    private final SdmxWebManager manager =
            Arc.container().select(SdmxWebManager.class).get();

    private final ScriptManager scripts =
            Arc.container().select(ScriptManager.class).get();

    private static final String PROBLEM_JSON = "application/problem+json";

    @ServerExceptionMapper
    public RestResponse<ApiError.Problem> mapException(IllegalArgumentException x, UriInfo uriInfo) {
        return toProblemResponse(x, uriInfo);
    }

    @ServerExceptionMapper
    public RestResponse<ApiError.Problem> mapException(IOException x, UriInfo uriInfo) {
        return toProblemResponse(x, uriInfo);
    }

    @ServerExceptionMapper
    public RestResponse<ApiError.Problem> mapException(UncheckedIOException x, UriInfo uriInfo) {
        return toProblemResponse(x, uriInfo);
    }

    // errors are always JSON problem details, even if the request accepts raw scripts only
    private static RestResponse<ApiError.Problem> toProblemResponse(Exception x, UriInfo uriInfo) {
        ApiError error = ApiError.of(x);
        return RestResponse.ResponseBuilder.<ApiError.Problem>create(error.httpStatus())
                .entity(error.toProblem(uriInfo.getPathParameters().getFirst("source")))
                .type(PROBLEM_JSON)
                .build();
    }

    @GET
    @Path("/about")
    public AboutDto getAbout() {
        return ProtoApi.fromAbout();
    }

    @GET
    @Path("/sources")
    public ListSourcesResponseDto listSources(
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults,
            @QueryParam("offset") @DefaultValue("0") @Parameter(description = OFFSET_DESCRIPTION) int offset,
            @QueryParam("maxConfidentiality") @DefaultValue("SECRET") String maxConfidentiality) {
        var result = manager.listSources(WebSourcesRequest.builder()
                .languagesOf(languages)
                .query(query)
                .maxResults(maxResults)
                .offset(offset)
                .confidentialityThreshold(Confidentiality.valueOf(maxConfidentiality))
                .build());
        return ListSourcesResponseDto.newBuilder()
                .addAllItems(result.getItems().stream()
                        .map(ProtoGrpc::fromListedWebSource)
                        .toList())
                .setTotal(result.getTotal())
                .addAllScores(result.getScores())
                .build();
    }

    @GET
    @Path("/{source}/icon")
    @Produces({ICON_PNG, ICON_SVG})
    @APIResponse(
            responseCode = "200",
            description = ICON_RESPONSE_DESCRIPTION,
            content = {
                @Content(mediaType = ICON_PNG, schema = @Schema(type = SchemaType.STRING, format = "binary")),
                @Content(mediaType = ICON_SVG, schema = @Schema(type = SchemaType.STRING))
            })
    @APIResponse(responseCode = "304", description = "The icon has not changed (If-None-Match)")
    public Response getSourceIcon(
            @PathParam("source") String source,
            @QueryParam("size")
                    @DefaultValue("" + ProtoIcon.DEFAULT_SIZE)
                    @Parameter(description = ProtoIcon.SIZE_DESCRIPTION)
                    int size,
            @QueryParam("format") @Parameter(description = ProtoIcon.FORMAT_DESCRIPTION) String format,
            @Context jakarta.ws.rs.core.Request request)
            throws IOException {
        WebSource webSource = ProtoIcon.getSource(manager, source);
        SourceIcon icon = ProtoIcon.getIcon(manager, webSource, size, format);
        boolean isPublic = webSource.getConfidentiality() == Confidentiality.PUBLIC;
        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(ICON_MAX_AGE);
        cacheControl.setPrivate(!isPublic);
        EntityTag tag = new EntityTag(ProtoIcon.getEntityTag(icon));
        Response.ResponseBuilder notModified = request.evaluatePreconditions(tag);
        Response.ResponseBuilder result =
                notModified != null ? notModified : Response.ok(icon.getContent(), icon.getMediaType());
        return result.tag(tag)
                .cacheControl(cacheControl)
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; sandbox")
                .build();
    }

    private static final String ICON_PNG = "image/png";

    private static final String ICON_SVG = "image/svg+xml";

    private static final int ICON_MAX_AGE = 24 * 60 * 60;

    private static final String ICON_RESPONSE_DESCRIPTION =
            "The icon of the source that best matches the size: a PNG of the exact size, then an SVG, then the smallest larger PNG, then the largest PNG; the SDMX logo if the source has no icon";

    @GET
    @Path("/{source}/databases")
    public ListDatabasesResponseDto listDatabases(
            @PathParam("source") String source,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults,
            @QueryParam("offset") @DefaultValue("0") @Parameter(description = OFFSET_DESCRIPTION) int offset)
            throws IOException {
        var result = manager.usingName(source)
                .listDatabases(DatabasesRequest.builder()
                        .languagesOf(languages)
                        .query(query)
                        .maxResults(maxResults)
                        .offset(offset)
                        .build());
        return ListDatabasesResponseDto.newBuilder()
                .addAllItems(
                        result.getItems().stream().map(ProtoApi::fromDatabase).toList())
                .setTotal(result.getTotal())
                .addAllScores(result.getScores())
                .build();
    }

    @GET
    @Path("/{source}/flows")
    public ListFlowsResponseDto listFlows(
            @PathParam("source") String source,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults,
            @QueryParam("offset") @DefaultValue("0") @Parameter(description = OFFSET_DESCRIPTION) int offset,
            @QueryParam("plainText") @DefaultValue("false") boolean plainText,
            @QueryParam("truncate") @DefaultValue("" + NO_DESCRIPTION_LIMIT) int truncate)
            throws IOException {
        var result = manager.usingName(source)
                .listFlows(FlowsRequest.builder()
                        .databaseOf(database)
                        .languagesOf(languages)
                        .query(query)
                        .maxResults(maxResults)
                        .offset(offset)
                        .plainText(plainText)
                        .truncate(truncate)
                        .build());
        return ListFlowsResponseDto.newBuilder()
                .addAllItems(
                        result.getItems().stream().map(ProtoApi::fromDataflow).toList())
                .setTotal(result.getTotal())
                .addAllScores(result.getScores())
                .build();
    }

    @GET
    @Path("/flows")
    public SearchFlowsResponseDto searchFlows(
            @QueryParam("sources") @DefaultValue("all") @Parameter(description = SOURCES_DESCRIPTION) String sources,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults,
            @QueryParam("offset") @DefaultValue("0") @Parameter(description = OFFSET_DESCRIPTION) int offset,
            @QueryParam("plainText") @DefaultValue("false") boolean plainText,
            @QueryParam("truncate") @DefaultValue("" + NO_DESCRIPTION_LIMIT) int truncate,
            @QueryParam("maxConfidentiality") @Parameter(description = SEARCH_CONFIDENTIALITY_DESCRIPTION)
                    String maxConfidentiality,
            @QueryParam("timeout") @DefaultValue("0") @Parameter(description = TIMEOUT_DESCRIPTION) int timeout)
            throws IOException {
        return SdmxdlGrpcService2.toSearchFlowsResponse(manager.searchFlows(WebFlowsRequest.builder()
                .sources(SdmxdlGrpcService2.toSourceIds(splitList(sources)))
                .languagesOf(languages)
                .query(query)
                .maxResults(maxResults)
                .offset(offset)
                .plainText(plainText)
                .truncate(truncate)
                .confidentialityThreshold(
                        maxConfidentiality == null || maxConfidentiality.isEmpty()
                                ? null
                                : Confidentiality.valueOf(maxConfidentiality))
                .timeout(SdmxdlGrpcService2.toTimeout(timeout))
                .build()));
    }

    @GET
    @Path("/{source}/{flow}/meta")
    public MetaSetDto getMeta(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages)
            throws IOException {
        return ProtoApi.fromMetaSet(manager.usingName(source)
                .getMeta(MetaRequest.builder()
                        .flowOf(flow)
                        .databaseOf(database)
                        .languagesOf(languages)
                        .build()));
    }

    @GET
    @Path("/{source}/{flow}/dimensions")
    public ListDimensionsResponseDto listDimensions(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults,
            @QueryParam("offset") @DefaultValue("0") @Parameter(description = OFFSET_DESCRIPTION) int offset,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages)
            throws IOException {
        var result = manager.usingName(source)
                .listDimensions(DimensionsRequest.builder()
                        .flowOf(flow)
                        .databaseOf(database)
                        .languagesOf(languages)
                        .query(query)
                        .maxResults(maxResults)
                        .offset(offset)
                        .build());
        return ListDimensionsResponseDto.newBuilder()
                .addAllItems(
                        result.getItems().stream().map(ProtoApi::fromDimension).toList())
                .setTotal(result.getTotal())
                .addAllScores(result.getScores())
                .build();
    }

    @GET
    @Path("/{source}/{flow}/attributes")
    public ListAttributesResponseDto listAttributes(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults,
            @QueryParam("offset") @DefaultValue("0") @Parameter(description = OFFSET_DESCRIPTION) int offset,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages)
            throws IOException {
        var result = manager.usingName(source)
                .listAttributes(AttributesRequest.builder()
                        .flowOf(flow)
                        .databaseOf(database)
                        .languagesOf(languages)
                        .query(query)
                        .maxResults(maxResults)
                        .offset(offset)
                        .build());
        return ListAttributesResponseDto.newBuilder()
                .addAllItems(
                        result.getItems().stream().map(ProtoApi::fromAttribute).toList())
                .setTotal(result.getTotal())
                .addAllScores(result.getScores())
                .build();
    }

    @GET
    @Path("/{source}/{flow}/data")
    public DataSetDto getData(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @QueryParam("key") @DefaultValue("all") String key,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("start") String start,
            @QueryParam("end") String end,
            @QueryParam("firstN") Integer firstN,
            @QueryParam("lastN") Integer lastN,
            @QueryParam("seriesOffset") Integer seriesOffset,
            @QueryParam("seriesLimit") Integer seriesLimit,
            @QueryParam("detail") @DefaultValue("FULL") Detail detail)
            throws IOException {
        return ProtoApi.fromDataSet(manager.usingName(source)
                .getData(DataRequest.builder()
                        .flowOf(flow)
                        .keyOf(key)
                        .databaseOf(database)
                        .languagesOf(languages)
                        .startPeriodOf(start)
                        .endPeriodOf(end)
                        .firstNObservations(firstN)
                        .lastNObservations(lastN)
                        .seriesOffset(seriesOffset != null ? seriesOffset : 0)
                        .seriesLimit(seriesLimit)
                        .detail(detail)
                        .build()));
    }

    @GET
    @Path("/{source}/{flow}/data:stream")
    @Blocking
    public Multi<SeriesDto> getDataStream(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @QueryParam("key") @DefaultValue("all") String key,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("start") String start,
            @QueryParam("end") String end,
            @QueryParam("firstN") Integer firstN,
            @QueryParam("lastN") Integer lastN,
            @QueryParam("seriesOffset") Integer seriesOffset,
            @QueryParam("seriesLimit") Integer seriesLimit,
            @QueryParam("detail") @DefaultValue("FULL") Detail detail) {
        return multiOfIO(() -> manager.usingName(source)
                        .getData(DataRequest.builder()
                                .flowOf(flow)
                                .keyOf(key)
                                .databaseOf(database)
                                .languagesOf(languages)
                                .startPeriodOf(start)
                                .endPeriodOf(end)
                                .firstNObservations(firstN)
                                .lastNObservations(lastN)
                                .seriesOffset(seriesOffset != null ? seriesOffset : 0)
                                .seriesLimit(seriesLimit)
                                .detail(detail)
                                .build()))
                .map(ProtoApi::fromSeries);
    }

    @GET
    @Path("/{source}/{flow}/codes/{concept}")
    public ListCodesResponseDto getCodes(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @PathParam("concept") String concept,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults,
            @QueryParam("offset") @DefaultValue("0") @Parameter(description = OFFSET_DESCRIPTION) int offset,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages)
            throws IOException {
        return SdmxdlGrpcService2.toCodesResponse(manager.usingName(source)
                .listCodes(CodesRequest.builder()
                        .flowOf(flow)
                        .concept(concept)
                        .databaseOf(database)
                        .languagesOf(languages)
                        .query(query)
                        .maxResults(maxResults)
                        .offset(offset)
                        .build()));
    }

    @GET
    @Path("/{source}/{flow}/availability")
    public ListAvailabilityResponseDto getAvailability(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @QueryParam("key") @DefaultValue("all") String key,
            @QueryParam("dimension") @DefaultValue(AvailabilityRequest.FIRST_WILDCARD_DIMENSION) String dimension,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages)
            throws IOException {
        return SdmxdlGrpcService2.toAvailabilityResponse(manager.usingName(source)
                .listAvailability(AvailabilityRequest.builder()
                        .languagesOf(languages)
                        .databaseOf(database)
                        .flowOf(flow)
                        .keyOf(key)
                        .dimension(dimension)
                        .build()));
    }

    private static final String OFFSET_DESCRIPTION =
            "Number of matching items to skip before returning 'maxResults' items (default 0)";

    private static final String SOURCES_DESCRIPTION =
            "Comma-separated source ids, or 'all' (default) for every source allowed by 'maxConfidentiality'";

    private static final String SEARCH_CONFIDENTIALITY_DESCRIPTION =
            "Maximum confidentiality of the searched sources; defaults to PUBLIC for 'all' sources and to SECRET (any) for named sources";

    private static final String TIMEOUT_DESCRIPTION =
            "Maximum time to wait for the sources, in milliseconds (default 0 = no limit); late sources are reported as failures";

    private static final String CHECKS_DESCRIPTION =
            "Comma-separated checks: 'monitor' (third-party monitor, default) and/or 'access' (live request against the source)";

    @GET
    @Path("/health")
    public CheckHealthResponseDto checkHealth(
            @QueryParam("sources") @DefaultValue("all") String sources,
            @QueryParam("checks") @DefaultValue("monitor") @Parameter(description = CHECKS_DESCRIPTION) String checks)
            throws IOException {
        List<HealthCheck> list = parseChecks(checks);
        var result = manager.checkHealth(SdmxdlGrpcService2.toHealthRequest(splitList(sources), list));
        return CheckHealthResponseDto.newBuilder()
                .addAllItems(result.stream().map(ProtoWeb::fromHealthReport).toList())
                .setTotal(result.size())
                .build();
    }

    @GET
    @Path("/{source}/health")
    public HealthReportDto checkSourceHealth(
            @PathParam("source") String source,
            @QueryParam("checks") @DefaultValue("monitor") @Parameter(description = CHECKS_DESCRIPTION) String checks)
            throws IOException {
        List<HealthCheck> list = parseChecks(checks);
        return ProtoWeb.fromHealthReport(
                manager.checkHealth(source, list.isEmpty() ? WebHealthRequest.DEFAULT_CHECKS : EnumSet.copyOf(list)));
    }

    @GET
    @Path("/script/targets")
    public ListScriptTargetsResponseDto listScriptTargets() {
        return SdmxdlGrpcService2.toScriptTargetsResponse(scripts);
    }

    @GET
    @Path("/{source}/{flow}/data:script")
    @Produces({APPLICATION_JSON, TEXT_PLAIN, APPLICATION_OCTET_STREAM})
    @APIResponse(
            responseCode = "200",
            description = SCRIPT_RESPONSE_DESCRIPTION,
            content = {
                @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ScriptDto.class)),
                @Content(mediaType = TEXT_PLAIN, schema = @Schema(type = SchemaType.STRING)),
                @Content(mediaType = APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING))
            })
    public Response generateDataScript(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @QueryParam("key") @DefaultValue("all") String key,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("start") String start,
            @QueryParam("end") String end,
            @QueryParam("firstN") Integer firstN,
            @QueryParam("lastN") Integer lastN,
            @QueryParam("seriesOffset") Integer seriesOffset,
            @QueryParam("seriesLimit") Integer seriesLimit,
            @QueryParam("detail") @DefaultValue("FULL") Detail detail,
            @QueryParam("target") @DefaultValue(ProtoScript.DEFAULT_TARGET) String target,
            @QueryParam("cliLauncher") List<String> cliLauncher,
            @QueryParam("restEndpoint") String restEndpoint,
            @QueryParam("outputFile") String outputFile,
            @QueryParam("property") List<String> properties,
            @QueryParam("format") ScriptFormat format,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        return generateScript(
                source,
                DataRequest.builder()
                        .flowOf(flow)
                        .keyOf(key)
                        .databaseOf(database)
                        .languagesOf(languages)
                        .startPeriodOf(start)
                        .endPeriodOf(end)
                        .firstNObservations(firstN)
                        .lastNObservations(lastN)
                        .seriesOffset(seriesOffset != null ? seriesOffset : 0)
                        .seriesLimit(seriesLimit)
                        .detail(detail)
                        .build(),
                target,
                cliLauncher,
                restEndpoint,
                outputFile,
                properties,
                uriInfo,
                format,
                headers,
                source + "_" + flow + "_data");
    }

    @GET
    @Path("/{source}/flows:script")
    @Produces({APPLICATION_JSON, TEXT_PLAIN, APPLICATION_OCTET_STREAM})
    @APIResponse(
            responseCode = "200",
            description = SCRIPT_RESPONSE_DESCRIPTION,
            content = {
                @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ScriptDto.class)),
                @Content(mediaType = TEXT_PLAIN, schema = @Schema(type = SchemaType.STRING)),
                @Content(mediaType = APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING))
            })
    public Response generateFlowsScript(
            @PathParam("source") String source,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults,
            @QueryParam("offset") @DefaultValue("0") @Parameter(description = OFFSET_DESCRIPTION) int offset,
            @QueryParam("plainText") @DefaultValue("false") boolean plainText,
            @QueryParam("truncate") @DefaultValue("" + NO_DESCRIPTION_LIMIT) int truncate,
            @QueryParam("target") @DefaultValue(ProtoScript.DEFAULT_TARGET) String target,
            @QueryParam("cliLauncher") List<String> cliLauncher,
            @QueryParam("restEndpoint") String restEndpoint,
            @QueryParam("outputFile") String outputFile,
            @QueryParam("property") List<String> properties,
            @QueryParam("format") ScriptFormat format,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        return generateScript(
                source,
                FlowsRequest.builder()
                        .databaseOf(database)
                        .languagesOf(languages)
                        .query(query)
                        .maxResults(maxResults)
                        .offset(offset)
                        .plainText(plainText)
                        .truncate(truncate)
                        .build(),
                target,
                cliLauncher,
                restEndpoint,
                outputFile,
                properties,
                uriInfo,
                format,
                headers,
                source + "_flows");
    }

    /**
     * Representation of a generated script in a REST response.
     */
    public enum ScriptFormat {
        /**
         * JSON object with the script content and its metadata.
         */
        JSON,
        /**
         * Script content only, downloadable as a file.
         */
        RAW;

        public static ScriptFormat fromString(String text) {
            return valueOf(text.toUpperCase(Locale.ROOT));
        }
    }

    private static final String SCRIPT_RESPONSE_DESCRIPTION =
            "The generated script, as a JSON object by default or as a downloadable file if format=raw or if the Accept header prefers text/plain or application/octet-stream";

    private static final String SCRIPT_WARNING_HEADER = "Sdmxdl-Script-Warning";

    private Response generateScript(
            String source,
            Request request,
            String target,
            List<String> cliLauncher,
            String restEndpoint,
            String outputFile,
            List<String> properties,
            UriInfo uriInfo,
            ScriptFormat format,
            HttpHeaders headers,
            String baseName) {
        // Scripts generated by this server target this server by default
        String endpoint = restEndpoint != null
                ? restEndpoint
                : uriInfo.getBaseUri().resolve("sdmx-dl/v2").toString();
        ScriptOptions options =
                ProtoScript.toScriptOptions(cliLauncher, endpoint, outputFile, ProtoScript.parseProperties(properties));
        Script script = scripts.generate(ScriptTarget.parse(target), source, request, options);
        List<MediaType> acceptable = headers.getAcceptableMediaTypes();
        return isRaw(format, acceptable)
                ? toRawResponse(script, acceptable, baseName)
                : Response.ok(ProtoScript.fromScript(script), APPLICATION_JSON_TYPE)
                        .build();
    }

    private static boolean isRaw(ScriptFormat format, List<MediaType> acceptable) {
        if (format != null) {
            return format == ScriptFormat.RAW;
        }
        // acceptable media types are sorted by preference; wildcards keep the JSON default
        return acceptable.stream()
                .filter(type -> !type.isWildcardType() && !type.isWildcardSubtype())
                .findFirst()
                .map(type -> !type.isCompatible(APPLICATION_JSON_TYPE))
                .orElse(false);
    }

    private static Response toRawResponse(Script script, List<MediaType> acceptable, String baseName) {
        MediaType scriptType = MediaType.valueOf(script.getMediaType());
        MediaType contentType = acceptable.isEmpty() || acceptable.stream().anyMatch(scriptType::isCompatible)
                ? scriptType
                : acceptable.stream()
                        .filter(type -> type.isCompatible(TEXT_PLAIN_TYPE))
                        .findFirst()
                        .map(ignore -> TEXT_PLAIN_TYPE)
                        .orElse(APPLICATION_OCTET_STREAM_TYPE);
        Response.ResponseBuilder result = Response.ok(
                        script.getContent(), contentType.withCharset(StandardCharsets.UTF_8.name()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + toFileName(baseName, script.getFileExtension()) + "\"");
        script.getWarnings().forEach(warning -> result.header(SCRIPT_WARNING_HEADER, warning));
        return result.build();
    }

    private static String toFileName(String baseName, String fileExtension) {
        return (baseName + "." + fileExtension).replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static List<String> splitList(String text) {
        return text == null
                ? List.of()
                : Arrays.stream(text.split(","))
                        .map(String::trim)
                        .filter(item -> !item.isEmpty())
                        .toList();
    }

    private static List<HealthCheck> parseChecks(String checks) {
        return splitList(checks).stream()
                .map(check -> HealthCheck.valueOf(check.toUpperCase(Locale.ROOT)))
                .toList();
    }

    private static <T> Multi<T> multiOfIO(IOSupplier<? extends Iterable<T>> supplier) {
        try {
            return Multi.createFrom().iterable(supplier.getWithIO());
        } catch (IOException ex) {
            return Multi.createFrom().failure(ex);
        }
    }
}
