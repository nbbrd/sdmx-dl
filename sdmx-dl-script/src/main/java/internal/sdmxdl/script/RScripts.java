package internal.sdmxdl.script;

import static internal.sdmxdl.script.ScriptFormats.toCommandLine;
import static internal.sdmxdl.script.ScriptFormats.toUrl;

import com.github.mustachejava.Mustache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import sdmxdl.Request;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptOptions;

/**
 * Renders CLI and REST calls as R scripts; the REST ones rely on the jsonlite package.
 */
@lombok.experimental.UtilityClass
public class RScripts {

    public static final String FILE_EXTENSION = "R";

    private static final Mustache CLI_TEMPLATE = ScriptTemplates.load("r-cli.mustache");

    private static final Mustache REST_TEMPLATE = ScriptTemplates.load("r-rest.mustache");

    public static void renderCli(
            @NonNull String source,
            @NonNull Request request,
            @NonNull ScriptOptions options,
            Script.@NonNull Builder result) {
        CliCall call = CliCalls.of(source, request);
        List<String> commandLine = toCommandLine(options, call);
        Map<String, Object> scope = newScope(call.getWarnings(), options);
        scope.put("command", quote(commandLine.get(0)));
        scope.put(
                "arguments",
                commandLine.subList(1, commandLine.size()).stream()
                        .map(RScripts::quote)
                        .collect(Collectors.joining(", ")));
        scope.put("columns", toVector(call.getColumns()));
        result.content(ScriptTemplates.render(CLI_TEMPLATE, scope)).warnings(call.getWarnings());
    }

    public static void renderRest(
            @NonNull String source,
            @NonNull Request request,
            @NonNull ScriptOptions options,
            Script.@NonNull Builder result) {
        RestCall call = RestCalls.of(source, request);
        Map<String, Object> scope = newScope(call.getWarnings(), options);
        scope.put("url", quote(toUrl(options.getRestEndpoint(), call.getPath(), call.getParameters())));
        scope.put("dataSet", call.getShape() == RestCall.Shape.DATA_SET);
        scope.put("list", call.getShape() == RestCall.Shape.LIST);
        List<Map<String, Object>> fields = new ArrayList<>();
        call.getFields().forEach((column, field) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("column", column);
            item.put("field", field);
            fields.add(item);
        });
        scope.put("fields", ScriptTemplates.withLast(fields));
        result.content(ScriptTemplates.render(REST_TEMPLATE, scope)).warnings(call.getWarnings());
    }

    private static Map<String, Object> newScope(List<String> warnings, ScriptOptions options) {
        Map<String, Object> result = ScriptTemplates.newScope(warnings);
        result.put("output", options.getOutputFile() != null ? quote(options.getOutputFile()) : "stdout()");
        return result;
    }

    private static String toVector(List<String> values) {
        return values.stream().map(RScripts::quote).collect(Collectors.joining(", ", "c(", ")"));
    }

    /**
     * Quotes a text as an R string literal.
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
