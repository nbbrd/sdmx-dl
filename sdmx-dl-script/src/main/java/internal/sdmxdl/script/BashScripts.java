package internal.sdmxdl.script;

import static internal.sdmxdl.script.ScriptFormats.toCommandLine;
import static internal.sdmxdl.script.ScriptFormats.toUrl;

import com.github.mustachejava.Mustache;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.NonNull;
import sdmxdl.Request;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptOptions;

/**
 * Renders CLI and REST calls as bash scripts; the CLI ones rely on Miller and the REST ones on curl and jq.
 */
@lombok.experimental.UtilityClass
public class BashScripts {

    public static final String FILE_EXTENSION = "sh";

    public static final String MEDIA_TYPE = "application/x-sh";

    private static final Mustache CLI_TEMPLATE = ScriptTemplates.load("bash-cli.mustache");

    private static final Mustache REST_TEMPLATE = ScriptTemplates.load("bash-rest.mustache");

    private static final Pattern SAFE_WORD = Pattern.compile("[A-Za-z0-9_./:,=@%+-]+");

    public static void renderCli(
            @NonNull String source,
            @NonNull Request request,
            @NonNull ScriptOptions options,
            Script.@NonNull Builder result) {
        CliCall call = CliCalls.of(source, request);
        Map<String, Object> scope = newScope(call.getWarnings(), options);
        scope.put("command", toWords(toCommandLine(options, call)));
        scope.put("columns", quote(String.join(",", call.getColumns())));
        result.content(ScriptTemplates.render(CLI_TEMPLATE, scope)).warnings(call.getWarnings());
    }

    public static void renderRest(
            @NonNull String source,
            @NonNull Request request,
            @NonNull ScriptOptions options,
            Script.@NonNull Builder result) {
        RestCall call = RestCalls.of(source, request);
        Map<String, Object> scope = newScope(call.getWarnings(), options);
        scope.put("url", quote(toUrl(options.getRestEndpoint(), call.getPath())));
        scope.put(
                "parameters",
                call.getParameters().entrySet().stream()
                        .map(entry -> quote(entry.getKey() + "=" + entry.getValue()))
                        .collect(Collectors.toList()));
        switch (call.getShape()) {
            case DATA_SET:
                scope.put("header", quote("Series,ObsPeriod,ObsValue"));
                scope.put(
                        "filter",
                        quote("(.data // [])[] | .key as $key | (.obs // [])[]"
                                + " | [$key, (.period | split(\"/\")[0]), (.value // 0)] | @csv"));
                break;
            case LIST:
                scope.put("header", quote(String.join(",", call.getFields().keySet())));
                scope.put(
                        "filter",
                        quote(call.getFields().values().stream()
                                .map(field -> "." + field + " // \"\"")
                                .collect(Collectors.joining(", ", ".[] | [", "] | @csv"))));
                break;
        }
        result.content(ScriptTemplates.render(REST_TEMPLATE, scope)).warnings(call.getWarnings());
    }

    private static Map<String, Object> newScope(List<String> warnings, ScriptOptions options) {
        Map<String, Object> result = ScriptTemplates.newScope(warnings);
        result.put("hasOutputFile", options.getOutputFile() != null);
        result.put("outputFile", options.getOutputFile() != null ? quote(options.getOutputFile()) : "");
        return result;
    }

    private static String toWords(List<String> values) {
        return values.stream().map(BashScripts::quote).collect(Collectors.joining(" "));
    }

    /**
     * Quotes a text as a bash word, leaving it as is when it only contains safe characters.
     */
    public static @NonNull String quote(@NonNull String text) {
        return SAFE_WORD.matcher(text).matches() ? text : "'" + text.replace("'", "'\\''") + "'";
    }
}
