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
 * Renders CLI and REST calls as PowerShell scripts.
 */
@lombok.experimental.UtilityClass
public class PowerShellScripts {

    public static final String FILE_EXTENSION = "ps1";

    private static final Mustache CLI_TEMPLATE = ScriptTemplates.load("powershell-cli.mustache");

    private static final Mustache REST_TEMPLATE = ScriptTemplates.load("powershell-rest.mustache");

    private static final Pattern SAFE_WORD = Pattern.compile("[A-Za-z0-9_./:\\\\=+-]+");

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    public static void renderCli(
            @NonNull String source,
            @NonNull Request request,
            @NonNull ScriptOptions options,
            Script.@NonNull Builder result) {
        CliCall call = CliCalls.of(source, request);
        Map<String, Object> scope = newScope(call.getWarnings(), options);
        scope.put(
                "command",
                toCommandLine(options, call).stream()
                        .map(PowerShellScripts::quoteWord)
                        .collect(Collectors.joining(" ")));
        scope.put(
                "columns",
                call.getColumns().stream().map(PowerShellScripts::quoteWord).collect(Collectors.joining(", ")));
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
        scope.put("hasParameters", !call.getParameters().isEmpty());
        scope.put(
                "parameters",
                call.getParameters().entrySet().stream()
                        .map(entry -> toKey(entry.getKey()) + " = " + toLiteral(entry.getValue()))
                        .collect(Collectors.joining("; ", "@{ ", " }")));
        scope.put("dataSet", call.getShape() == RestCall.Shape.DATA_SET);
        scope.put("list", call.getShape() == RestCall.Shape.LIST);
        scope.put(
                "fields",
                call.getFields().entrySet().stream()
                        .map(entry -> "@{ Name = " + quote(entry.getKey()) + "; Expression = { $_."
                                + toKey(entry.getValue()) + " } }")
                        .collect(Collectors.joining(", ")));
        result.content(ScriptTemplates.render(REST_TEMPLATE, scope)).warnings(call.getWarnings());
    }

    private static Map<String, Object> newScope(List<String> warnings, ScriptOptions options) {
        Map<String, Object> result = ScriptTemplates.newScope(warnings);
        result.put(
                "sink",
                options.getOutputFile() != null
                        ? "Export-Csv -NoTypeInformation -Encoding utf8 -Path " + quote(options.getOutputFile())
                        : "ConvertTo-Csv -NoTypeInformation");
        return result;
    }

    private static String toKey(String name) {
        return IDENTIFIER.matcher(name).matches() ? name : quote(name);
    }

    private static String toLiteral(Object value) {
        return value instanceof Integer ? value.toString() : quote(value.toString());
    }

    private static String quoteWord(String text) {
        return SAFE_WORD.matcher(text).matches() ? text : quote(text);
    }

    /**
     * Quotes a text as a verbatim PowerShell string literal.
     */
    public static @NonNull String quote(@NonNull String text) {
        return "'" + text.replace("'", "''") + "'";
    }
}
