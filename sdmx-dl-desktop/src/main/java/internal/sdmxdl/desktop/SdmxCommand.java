package internal.sdmxdl.desktop;

import sdmxdl.DatabaseRef;
import sdmxdl.Key;
import sdmxdl.web.FlowRequest;
import sdmxdl.web.KeyRequest;

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

    public static String fetchData(KeyRequest request) {
        return of(request.getDatabase(), "fetch", "data", request.getSource(), request.getFlow().toString(), toCommandParameter(request.getKey()));
    }

    public static String fetchMeta(KeyRequest request) {
        return of(request.getDatabase(), "fetch", "meta", request.getSource(), request.getFlow().toString(), toCommandParameter(request.getKey()));
    }

    public static String fetchKeys(KeyRequest request) {
        return of(request.getDatabase(), "fetch", "keys", request.getSource(), request.getFlow().toString(), toCommandParameter(request.getKey()));
    }

    public static String listDimensions(FlowRequest request) {
        return of(request.getDatabase(), "list", "dimensions", request.getSource(), request.getFlow().toString());
    }

    public static String listAttributes(FlowRequest request) {
        return of(request.getDatabase(), "list", "attributes", request.getSource(), request.getFlow().toString());
    }

    private static String toCommandParameter(Key key) {
        return Key.ALL.equals(key) ? "all" : key.toString();
    }
}
