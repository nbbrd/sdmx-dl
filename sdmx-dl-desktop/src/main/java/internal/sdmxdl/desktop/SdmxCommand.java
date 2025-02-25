package internal.sdmxdl.desktop;

import sdmxdl.DatabaseRef;
import sdmxdl.FlowRef;
import sdmxdl.Key;

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

    public static String fetchData(DatabaseRef database, String source, String flow, Key key) {
        return of(database, "fetch", "data", source, flow, toCommandParameter(key));
    }

    public static String fetchMeta(DatabaseRef database, String source, String flow, Key key) {
        return of(database, "fetch", "meta", source, flow, toCommandParameter(key));
    }

    public static String fetchKeys(DatabaseRef database, String source, String flow, Key key) {
        return of(database, "fetch", "keys", source, flow, toCommandParameter(key));
    }

    public static String listDimensions(DatabaseRef database, String source, FlowRef flow) {
        return of(database, "list", "dimensions", source, flow.toString());
    }

    public static String listAttributes(DatabaseRef database, String source, FlowRef flow) {
        return of(database, "list", "attributes", source, flow.toString());
    }

    private static String toCommandParameter(Key key) {
        return Key.ALL.equals(key) ? "all" : key.toString();
    }
}
