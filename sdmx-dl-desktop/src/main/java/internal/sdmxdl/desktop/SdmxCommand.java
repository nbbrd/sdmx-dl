package internal.sdmxdl.desktop;

import sdmxdl.DatabaseRef;
import sdmxdl.FlowRequest;
import sdmxdl.Key;
import sdmxdl.KeyRequest;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

@lombok.Value
@lombok.Builder
public class SdmxCommand {

    @lombok.Singular
    List<String> parameters;

    @lombok.Singular
    Map<String, String> options;

    public String toText() {
        return ("sdmx-dl "
                + String.join(" ", parameters)
                + options.entrySet().stream().map(SdmxCommand::toOptionText).collect(joining(" ", " ", ""))
        ).trim();
    }

    public static Builder builderOf(DatabaseRef ref) {
        return builder().option("d", !ref.equals(DatabaseRef.NO_DATABASE) ? ref.toString() : null);
    }

    private static String toOptionText(Map.Entry<String, String> e) {
        switch (e.getKey().length()) {
            case 0:
                return "";
            case 1:
                return e.getValue() != null ? ("-" + e.getKey() + " " + e.getValue()) : "";
            default:
                return e.getValue() != null ? ("--" + e.getKey() + " " + e.getValue()) : "";
        }
    }

    public static String of(DatabaseRef database, String... parameters) {
        return builderOf(database).parameters(asList(parameters)).build().toText();
    }

    public static String fetchData(String source, KeyRequest request) {
        return of(request.getDatabase(), "fetch", "data", source, request.getFlow().toString(), toCommandParameter(request.getKey()));
    }

    public static String fetchMeta(String source, KeyRequest request) {
        return of(request.getDatabase(), "fetch", "meta", source, request.getFlow().toString(), toCommandParameter(request.getKey()));
    }

    public static String fetchKeys(String source, KeyRequest request) {
        return of(request.getDatabase(), "fetch", "keys", source, request.getFlow().toString(), toCommandParameter(request.getKey()));
    }

    public static String listDimensions(String source, FlowRequest request) {
        return of(request.getDatabase(), "list", "dimensions", source, request.getFlow().toString());
    }

    public static String listAttributes(String source, FlowRequest request) {
        return of(request.getDatabase(), "list", "attributes", source, request.getFlow().toString());
    }

    private static String toCommandParameter(Key key) {
        return Key.ALL.equals(key) ? "all" : key.toString();
    }
}
