package sdmxdl.grpc.v2;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static sdmxdl.DatabaseRef.NO_DATABASE_KEYWORD;
import static sdmxdl.HasSearch.AUTO_LIMIT;
import static sdmxdl.HasSearch.NO_QUERY;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import nbbrd.io.function.IOSupplier;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import sdmxdl.*;
import sdmxdl.format.protobuf.*;
import sdmxdl.format.protobuf.web.MonitorReportDto;
import sdmxdl.format.protobuf.web.MonitorStatusDto;
import sdmxdl.format.protobuf.web.WebSourceDto;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptOptions;
import sdmxdl.script.ScriptTarget;
import sdmxdl.web.SdmxWebManager;
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

    public record ErrorResponse(String type, String message) {
        private static ErrorResponse of(Exception x) {
            return new ErrorResponse(x.getClass().getSimpleName(), x.getMessage());
        }
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapException(IllegalArgumentException x) {
        return toErrorResponse(x);
    }

    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapException(IOException x) {
        return toErrorResponse(x);
    }

    // errors are always JSON, even if the request accepts raw scripts only
    private static RestResponse<ErrorResponse> toErrorResponse(Exception x) {
        return RestResponse.ResponseBuilder.create(Response.Status.BAD_REQUEST, ErrorResponse.of(x))
                .type(APPLICATION_JSON_TYPE)
                .build();
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
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults,
            @QueryParam("maxConfidentiality") @DefaultValue("SECRET") String maxConfidentiality) {
        List<WebSource> result = manager.listSources(WebSourcesRequest.builder()
                .languagesOf(languages)
                .query(query)
                .maxResults(maxResults)
                .confidentialityThreshold(Confidentiality.valueOf(maxConfidentiality))
                .build());

        return Multi.createFrom().iterable(result).map(ProtoWeb::fromWebSource);
    }

    @GET
    @Path("/{source}/databases")
    public Multi<DatabaseDto> listDatabases(
            @PathParam("source") String source,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults) {
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
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults) {
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
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults,
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
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults,
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
            @QueryParam("start") String start,
            @QueryParam("end") String end,
            @QueryParam("firstN") Integer firstN,
            @QueryParam("lastN") Integer lastN,
            @QueryParam("detail") @DefaultValue("FULL") Detail detail) {
        return uniOfIO(() -> manager.usingName(source)
                        .getData(DataRequest.builder()
                                .flowOf(flow)
                                .keyOf(key)
                                .databaseOf(database)
                                .languagesOf(languages)
                                .startPeriodOf(start)
                                .endPeriodOf(end)
                                .firstNObservations(firstN)
                                .lastNObservations(lastN)
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
            @QueryParam("start") String start,
            @QueryParam("end") String end,
            @QueryParam("firstN") Integer firstN,
            @QueryParam("lastN") Integer lastN,
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
                                .detail(detail)
                                .build()))
                .map(ProtoApi::fromSeries);
    }

    @GET
    @Path("/{source}/{flow}/codes/{concept}")
    public Uni<CodelistDto> getCodes(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @PathParam("concept") String concept,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages) {
        return uniOfIO(() -> manager.usingName(source)
                        .listCodes(CodesRequest.builder()
                                .flowOf(flow)
                                .concept(concept)
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

    @GET
    @Path("/script/targets")
    public Multi<ScriptTargetDto> listScriptTargets() {
        return Multi.createFrom()
                .iterable(scripts.getTargets())
                .map(target -> ProtoScript.fromScriptTarget(scripts, target));
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
    public Uni<Response> generateDataScript(
            @PathParam("source") String source,
            @PathParam("flow") String flow,
            @QueryParam("key") @DefaultValue("all") String key,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("start") String start,
            @QueryParam("end") String end,
            @QueryParam("firstN") Integer firstN,
            @QueryParam("lastN") Integer lastN,
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
    public Uni<Response> generateFlowsScript(
            @PathParam("source") String source,
            @QueryParam("database") @DefaultValue(NO_DATABASE_KEYWORD) String database,
            @QueryParam("languages") @DefaultValue(ANY_KEYWORD) String languages,
            @QueryParam("query") @DefaultValue(NO_QUERY) String query,
            @QueryParam("maxResults") @DefaultValue("" + AUTO_LIMIT) int maxResults,
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

    private Uni<Response> generateScript(
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
        return Uni.createFrom().item(() -> {
            ScriptOptions options = ProtoScript.toScriptOptions(
                    cliLauncher, endpoint, outputFile, ProtoScript.parseProperties(properties));
            Script script = scripts.generate(ScriptTarget.parse(target), source, request, options);
            List<MediaType> acceptable = headers.getAcceptableMediaTypes();
            return isRaw(format, acceptable)
                    ? toRawResponse(script, acceptable, baseName)
                    : Response.ok(ProtoScript.fromScript(script), APPLICATION_JSON_TYPE)
                            .build();
        });
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
