package internal.sdmxdl.desktop;

import sdmxdl.CatalogRef;
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

    public static Builder builderOf(CatalogRef ref) {
        return builder().option("c", !ref.equals(CatalogRef.NO_CATALOG) ? ref.toString() : null);
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

    public static String of(CatalogRef catalog, String... parameters) {
        return builderOf(catalog).parameters(asList(parameters)).build().toText();
    }

    public static String fetchData(CatalogRef catalog, String source, String flow, Key key) {
        return of(catalog, "fetch", "data", source, flow, toCommandParameter(key));
    }

    public static String fetchMeta(CatalogRef catalog, String source, String flow, Key key) {
        return of(catalog, "fetch", "meta", source, flow, toCommandParameter(key));
    }

    public static String fetchKeys(CatalogRef catalog, String source, String flow, Key key) {
        return of(catalog, "fetch", "keys", source, flow, toCommandParameter(key));
    }

    public static String listDimensions(CatalogRef catalog, String source, FlowRef flow) {
        return of(catalog, "list", "dimensions", source, flow.toString());
    }

    public static String listAttributes(CatalogRef catalog, String source, FlowRef flow) {
        return of(catalog, "list", "attributes", source, flow.toString());
    }

    private static String toCommandParameter(Key key) {
        return Key.ALL.equals(key) ? "all" : key.toString();
    }
}
