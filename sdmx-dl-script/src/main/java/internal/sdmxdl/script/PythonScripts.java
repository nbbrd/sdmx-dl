package internal.sdmxdl.script;

import static internal.sdmxdl.script.ScriptFormats.toCommandLine;
import static internal.sdmxdl.script.ScriptFormats.toUrl;

import com.github.mustachejava.Mustache;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import sdmxdl.Request;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptOptions;

/**
 * Renders CLI and REST calls as Python 3.7+ scripts, either with the standard library only or with pandas for Jupyter
 * notebooks.
 * <p>
 * The layout of the scripts is defined in Mustache templates while the Python literals are built here.
 */
@lombok.experimental.UtilityClass
public class PythonScripts {

    public static final String FILE_EXTENSION = "py";

    public static final String MEDIA_TYPE = "text/x-python";

    private static final Mustache CLI_TEMPLATE = ScriptTemplates.load("python-cli.mustache");

    private static final Mustache REST_TEMPLATE = ScriptTemplates.load("python-rest.mustache");

    private static final Mustache JUPYTER_CLI_TEMPLATE = ScriptTemplates.load("jupyter-cli.mustache");

    private static final Mustache JUPYTER_REST_TEMPLATE = ScriptTemplates.load("jupyter-rest.mustache");

    public static void renderCli(
            @NonNull String source,
            @NonNull Request request,
            @NonNull ScriptOptions options,
            Script.@NonNull Builder result) {
        CliCall call = CliCalls.of(source, request);
        Map<String, Object> scope = newScope(call.getWarnings(), options);
        scope.put("arguments", toList(toCommandLine(options, call)));
        scope.put("header", toList(call.getColumns()));
        scope.put("row", toList(call.getColumns(), column -> "row[" + quote(column) + "]"));
        result.content(ScriptTemplates.render(CLI_TEMPLATE, scope)).warnings(call.getWarnings());
    }

    public static void renderRest(
            @NonNull String source,
            @NonNull Request request,
            @NonNull ScriptOptions options,
            Script.@NonNull Builder result) {
        RestCall call = RestCalls.of(source, request);
        String url = toUrl(options.getRestEndpoint(), call.getPath());
        Map<String, Object> scope = newRestScope(call, options);
        scope.put("url", quote(url));
        scope.put("urlWithParameters", quote(url + "?{params}"));
        result.content(ScriptTemplates.render(REST_TEMPLATE, scope)).warnings(call.getWarnings());
    }

    public static void renderJupyterCli(
            @NonNull String source,
            @NonNull Request request,
            @NonNull ScriptOptions options,
            Script.@NonNull Builder result) {
        CliCall call = CliCalls.of(source, request);
        List<String> arguments = toCommandLine(options, call).stream()
                .map(PythonScripts::quote)
                .collect(Collectors.toCollection(ArrayList::new));
        arguments.add(quote("-o"));
        arguments.add("outfile");
        Map<String, Object> scope = newScope(call.getWarnings(), options);
        scope.put("arguments", arguments.stream().collect(Collectors.joining(", ", "[", "]")));
        scope.put("columns", toList(call.getColumns()));
        result.content(ScriptTemplates.render(JUPYTER_CLI_TEMPLATE, scope)).warnings(call.getWarnings());
    }

    public static void renderJupyterRest(
            @NonNull String source,
            @NonNull Request request,
            @NonNull ScriptOptions options,
            Script.@NonNull Builder result) {
        RestCall call = RestCalls.of(source, request);
        Map<String, Object> scope = newRestScope(call, options);
        scope.put("url", quote(toUrl(options.getRestEndpoint(), call.getPath())));
        result.content(ScriptTemplates.render(JUPYTER_REST_TEMPLATE, scope)).warnings(call.getWarnings());
    }

    private static Map<String, Object> newScope(List<String> warnings, ScriptOptions options) {
        String outputFile = options.getOutputFile();
        Map<String, Object> result = ScriptTemplates.newScope(warnings);
        result.put("stdout", outputFile == null);
        result.put("hasOutputFile", outputFile != null);
        result.put("outputFile", outputFile != null ? quote(outputFile) : "");
        result.put(
                "output",
                outputFile == null
                        ? "contextlib.nullcontext(sys.stdout)"
                        : "open(" + quote(outputFile) + ", \"w\", newline=\"\", encoding=\"utf-8\")");
        return result;
    }

    private static Map<String, Object> newRestScope(RestCall call, ScriptOptions options) {
        Map<String, Object> result = newScope(call.getWarnings(), options);
        result.put("hasParameters", !call.getParameters().isEmpty());
        result.put("parameters", toDict(call.getParameters()));
        result.put("dataSet", call.getShape() == RestCall.Shape.DATA_SET);
        result.put("list", call.getShape() == RestCall.Shape.LIST);
        result.put("header", toList(new ArrayList<>(call.getFields().keySet())));
        result.put(
                "row",
                toList(new ArrayList<>(call.getFields().values()), field -> "item.get(" + quote(field) + ", \"\")"));
        return result;
    }

    private static String toList(List<String> values) {
        return toList(values, PythonScripts::quote);
    }

    private static String toList(List<String> values, Function<String, String> formatter) {
        return values.stream().map(formatter).collect(Collectors.joining(", ", "[", "]"));
    }

    private static String toDict(Map<String, Object> values) {
        return values.entrySet().stream()
                .map(entry -> quote(entry.getKey()) + ": " + toLiteral(entry.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static String toLiteral(Object value) {
        return value instanceof Integer ? value.toString() : quote(value.toString());
    }

    /**
     * Quotes a text as a Python string literal.
     */
    public static @NonNull String quote(@NonNull String text) {
        StringBuilder result = new StringBuilder(text.length() + 2).append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\':
                    result.append("\\\\");
                    break;
                case '"':
                    result.append("\\\"");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                default:
                    if (c < 0x20 || c == 0x7F) {
                        result.append("\\x")
                                .append(Character.forDigit(c >> 4, 16))
                                .append(Character.forDigit(c & 0xF, 16));
                    } else {
                        result.append(c);
                    }
            }
        }
        return result.append('"').toString();
    }
}
