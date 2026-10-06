package internal.sdmxdl.script;

import static internal.sdmxdl.script.CliCalls.unsupported;
import static internal.sdmxdl.script.ScriptFormats.formatPeriod;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.NonNull;
import sdmxdl.*;

/**
 * Maps requests to Java library calls.
 */
public final class ApiCalls implements RequestVisitor<ApiCall> {

    public static final Set<Class<? extends Request>> SUPPORTED_TYPES =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(DataRequest.class, FlowsRequest.class)));

    private static final ApiCalls INSTANCE = new ApiCalls();

    public static @NonNull ApiCall of(@NonNull Request request) {
        return request.accept(INSTANCE);
    }

    @Override
    public ApiCall visitData(@NonNull DataRequest request) {
        ApiCall.Builder result = ApiCall.builder()
                .requestType(DataRequest.class.getSimpleName())
                .providerMethod("getData")
                .shape(ApiCall.Shape.DATA_SET);
        appendDatabase(result, request.getDatabase());
        result.argument(ApiCall.Argument.of("flowOf", request.getFlow().toShortString()));
        if (!request.getKey().equals(Key.ALL)) {
            result.argument(ApiCall.Argument.of("keyOf", request.getKey().toString()));
        }
        appendLanguages(result, request.getLanguages());
        if (request.getStartPeriod() != null) {
            result.argument(ApiCall.Argument.of("startPeriodOf", formatPeriod(request.getStartPeriod())));
        }
        if (request.getEndPeriod() != null) {
            result.argument(ApiCall.Argument.of("endPeriodOf", formatPeriod(request.getEndPeriod())));
        }
        if (request.getFirstNObservations() != null) {
            result.argument(ApiCall.Argument.of("firstNObservations", request.getFirstNObservations()));
        }
        if (request.getLastNObservations() != null) {
            result.argument(ApiCall.Argument.of("lastNObservations", request.getLastNObservations()));
        }
        if (request.getDetail() != Detail.FULL) {
            result.argument(ApiCall.Argument.of("detailOf", request.getDetail().name()));
        }
        return result.build();
    }

    @Override
    public ApiCall visitFlows(@NonNull FlowsRequest request) {
        ApiCall.Builder result = ApiCall.builder()
                .requestType(FlowsRequest.class.getSimpleName())
                .providerMethod("listFlows")
                .shape(ApiCall.Shape.LIST);
        appendDatabase(result, request.getDatabase());
        appendLanguages(result, request.getLanguages());
        if (!request.getQuery().equals(HasSearch.NO_QUERY)) {
            result.argument(ApiCall.Argument.of("query", request.getQuery()));
        }
        if (request.getMaxResults() != HasSearch.AUTO_LIMIT) {
            result.argument(ApiCall.Argument.of("maxResults", request.getMaxResults()));
        }
        if (request.isPlainText()) {
            result.argument(ApiCall.Argument.of("plainText", true));
        }
        if (request.getTruncate() != HasDescription.NO_DESCRIPTION_LIMIT) {
            result.argument(ApiCall.Argument.of("truncate", request.getTruncate()));
        }
        return result.getter("Ref", "getRef")
                .getter("Name", "getName")
                .getter("Description", "getDescription")
                .build();
    }

    @Override
    public ApiCall visitDatabases(@NonNull DatabasesRequest request) {
        throw unsupported(request);
    }

    @Override
    public ApiCall visitMeta(@NonNull MetaRequest request) {
        throw unsupported(request);
    }

    @Override
    public ApiCall visitDimensions(@NonNull DimensionsRequest request) {
        throw unsupported(request);
    }

    @Override
    public ApiCall visitAttributes(@NonNull AttributesRequest request) {
        throw unsupported(request);
    }

    @Override
    public ApiCall visitCodes(@NonNull CodesRequest request) {
        throw unsupported(request);
    }

    @Override
    public ApiCall visitAvailability(@NonNull AvailabilityRequest request) {
        throw unsupported(request);
    }

    private static void appendDatabase(ApiCall.Builder result, DatabaseRef database) {
        if (!database.equals(DatabaseRef.NO_DATABASE)) {
            result.argument(ApiCall.Argument.of("databaseOf", database.toString()));
        }
    }

    private static void appendLanguages(ApiCall.Builder result, Languages languages) {
        if (!languages.equals(Languages.ANY)) {
            result.argument(ApiCall.Argument.of("languagesOf", languages.toString()));
        }
    }
}
