package internal.sdmxdl.script;

import static internal.sdmxdl.script.CliCalls.unsupported;
import static internal.sdmxdl.script.ScriptFormats.encodePathSegment;
import static internal.sdmxdl.script.ScriptFormats.formatPeriod;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.NonNull;
import sdmxdl.*;

/**
 * Maps requests to REST calls.
 */
@lombok.RequiredArgsConstructor
public final class RestCalls implements RequestVisitor<RestCall> {

    public static final Set<Class<? extends Request>> SUPPORTED_TYPES =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(DataRequest.class, FlowsRequest.class)));

    public static @NonNull RestCall of(@NonNull String source, @NonNull Request request) {
        return request.accept(new RestCalls(source));
    }

    private final @NonNull String source;

    @Override
    public RestCall visitData(@NonNull DataRequest request) {
        RestCall.Builder result = RestCall.builder()
                .path("/" + encodePathSegment(source) + "/"
                        + encodePathSegment(request.getFlow().toShortString()) + "/data")
                .shape(RestCall.Shape.DATA_SET);
        if (!request.getKey().equals(Key.ALL)) {
            result.parameter("key", request.getKey().toString());
        }
        appendDatabase(result, request.getDatabase());
        appendLanguages(result, request.getLanguages());
        if (request.getStartPeriod() != null) {
            result.parameter("start", formatPeriod(request.getStartPeriod()));
        }
        if (request.getEndPeriod() != null) {
            result.parameter("end", formatPeriod(request.getEndPeriod()));
        }
        if (request.getFirstNObservations() != null) {
            result.parameter("firstN", request.getFirstNObservations());
        }
        if (request.getLastNObservations() != null) {
            result.parameter("lastN", request.getLastNObservations());
        }
        if (request.getDetail() != Detail.FULL) {
            result.parameter("detail", request.getDetail().name());
        }
        return result.build();
    }

    @Override
    public RestCall visitFlows(@NonNull FlowsRequest request) {
        RestCall.Builder result = RestCall.builder()
                .path("/" + encodePathSegment(source) + "/flows")
                .shape(RestCall.Shape.LIST);
        appendDatabase(result, request.getDatabase());
        appendLanguages(result, request.getLanguages());
        appendSearch(result, request);
        if (request.isPlainText()) {
            result.parameter("plainText", "true");
        }
        if (request.getTruncate() != HasDescription.NO_DESCRIPTION_LIMIT) {
            result.parameter("truncate", request.getTruncate());
        }
        return result.field("Ref", "ref")
                .field("Name", "name")
                .field("Description", "description")
                .build();
    }

    @Override
    public RestCall visitDatabases(@NonNull DatabasesRequest request) {
        throw unsupported(request);
    }

    @Override
    public RestCall visitMeta(@NonNull MetaRequest request) {
        throw unsupported(request);
    }

    @Override
    public RestCall visitDimensions(@NonNull DimensionsRequest request) {
        throw unsupported(request);
    }

    @Override
    public RestCall visitAttributes(@NonNull AttributesRequest request) {
        throw unsupported(request);
    }

    @Override
    public RestCall visitCodes(@NonNull CodesRequest request) {
        throw unsupported(request);
    }

    @Override
    public RestCall visitAvailability(@NonNull AvailabilityRequest request) {
        throw unsupported(request);
    }

    private static void appendDatabase(RestCall.Builder result, DatabaseRef database) {
        if (!database.equals(DatabaseRef.NO_DATABASE)) {
            result.parameter("database", database.toString());
        }
    }

    private static void appendLanguages(RestCall.Builder result, Languages languages) {
        if (!languages.equals(Languages.ANY)) {
            result.parameter("languages", languages.toString());
        }
    }

    private static void appendSearch(RestCall.Builder result, HasSearch search) {
        if (!search.getQuery().equals(HasSearch.NO_QUERY)) {
            result.parameter("query", search.getQuery());
        }
        if (search.getMaxResults() != HasSearch.AUTO_LIMIT) {
            result.parameter("maxResults", search.getMaxResults());
        }
    }
}
