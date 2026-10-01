package internal.sdmxdl.script;

import static internal.sdmxdl.script.ScriptFormats.formatPeriod;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.NonNull;
import sdmxdl.*;

/**
 * Maps requests to CLI calls.
 */
@lombok.RequiredArgsConstructor
public final class CliCalls implements RequestVisitor<CliCall> {

    public static final Set<Class<? extends Request>> SUPPORTED_TYPES =
            Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(DataRequest.class, FlowsRequest.class)));

    public static @NonNull CliCall of(@NonNull String source, @NonNull Request request) {
        return request.accept(new CliCalls(source));
    }

    private final @NonNull String source;

    @Override
    public CliCall visitData(@NonNull DataRequest request) {
        CliCall.Builder result = CliCall.builder()
                .argument("fetch")
                .argument("data")
                .argument(source)
                .argument(request.getFlow().toShortString())
                .argument(request.getKey().toString());
        appendDatabase(result, request.getDatabase());
        appendLanguages(result, request.getLanguages());
        if (request.getStartPeriod() != null) {
            result.argument("--start").argument(formatPeriod(request.getStartPeriod()));
        }
        if (request.getEndPeriod() != null) {
            result.argument("--end").argument(formatPeriod(request.getEndPeriod()));
        }
        if (request.getFirstNObservations() != null) {
            result.argument("--first-n")
                    .argument(request.getFirstNObservations().toString());
        }
        if (request.getLastNObservations() != null) {
            result.argument("--last-n").argument(request.getLastNObservations().toString());
        }
        if (request.getDetail() != Detail.FULL) {
            result.warning("Detail '" + request.getDetail() + "' is not supported by the CLI; FULL is used instead");
        }
        return result.column("Series").column("ObsPeriod").column("ObsValue").build();
    }

    @Override
    public CliCall visitFlows(@NonNull FlowsRequest request) {
        CliCall.Builder result =
                CliCall.builder().argument("list").argument("flows").argument(source);
        appendDatabase(result, request.getDatabase());
        appendLanguages(result, request.getLanguages());
        appendSearch(result, request);
        if (request.isPlainDescription()) {
            result.argument("--plain-description");
        }
        if (request.getMaxDescriptionLength() != HasDescription.NO_DESCRIPTION_LIMIT) {
            result.argument("--max-description-length").argument(String.valueOf(request.getMaxDescriptionLength()));
        }
        return result.column("Ref").column("Name").column("Description").build();
    }

    @Override
    public CliCall visitDatabases(@NonNull DatabasesRequest request) {
        throw unsupported(request);
    }

    @Override
    public CliCall visitMeta(@NonNull MetaRequest request) {
        throw unsupported(request);
    }

    @Override
    public CliCall visitDimensions(@NonNull DimensionsRequest request) {
        throw unsupported(request);
    }

    @Override
    public CliCall visitAttributes(@NonNull AttributesRequest request) {
        throw unsupported(request);
    }

    @Override
    public CliCall visitCodes(@NonNull CodesRequest request) {
        throw unsupported(request);
    }

    @Override
    public CliCall visitAvailability(@NonNull AvailabilityRequest request) {
        throw unsupported(request);
    }

    private static void appendDatabase(CliCall.Builder result, DatabaseRef database) {
        if (!database.equals(DatabaseRef.NO_DATABASE)) {
            result.argument("-d").argument(database.toString());
        }
    }

    private static void appendLanguages(CliCall.Builder result, Languages languages) {
        if (!languages.equals(Languages.ANY)) {
            result.argument("-l").argument(languages.toString());
        }
    }

    private static void appendSearch(CliCall.Builder result, HasSearch search) {
        if (!search.getQuery().equals(HasSearch.NO_QUERY)) {
            result.argument("-q").argument(search.getQuery());
        }
        if (search.getMaxResults() != HasSearch.AUTO_LIMIT) {
            result.argument("-m").argument(String.valueOf(search.getMaxResults()));
        }
    }

    static IllegalArgumentException unsupported(Request request) {
        return new IllegalArgumentException(
                "Unsupported request type: " + request.getClass().getSimpleName());
    }
}
