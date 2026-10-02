package internal.sdmxdl.script;

import static internal.sdmxdl.script.ScriptFormats.toCommandLine;

import com.github.mustachejava.Mustache;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.NonNull;
import sdmxdl.Request;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptOptions;

/**
 * Renders CLI calls as Windows batch files.
 * <p>
 * Batch files cannot parse CSV reliably, so the output of the CLI is written as is.
 */
@lombok.experimental.UtilityClass
public class BatchScripts {

    public static final String FILE_EXTENSION = "bat";

    public static final String MEDIA_TYPE = "application/x-bat";

    private static final Mustache CLI_TEMPLATE = ScriptTemplates.load("batch-cli.mustache");

    private static final Pattern SAFE_WORD = Pattern.compile("[A-Za-z0-9_./:\\\\@+-]+");

    public static void renderCli(
            @NonNull String source,
            @NonNull Request request,
            @NonNull ScriptOptions options,
            Script.@NonNull Builder result) {
        CliCall call = CliCalls.of(source, request);
        List<String> commandLine = new ArrayList<>(toCommandLine(options, call));
        if (options.getOutputFile() != null) {
            commandLine.add("-o");
            commandLine.add(options.getOutputFile());
        }
        Map<String, Object> scope = ScriptTemplates.newScope(call.getWarnings());
        scope.put("command", commandLine.stream().map(BatchScripts::quote).collect(Collectors.joining(" ")));
        String content = ScriptTemplates.render(CLI_TEMPLATE, scope).replace("\n", "\r\n");
        result.content(content).warnings(call.getWarnings());
    }

    /**
     * Quotes a text as an argument of a command called with {@code call} in a batch file.
     */
    public static @NonNull String quote(@NonNull String text) {
        // percent signs are expanded twice: once when the line is parsed and once by call
        String escaped = text.replace("%", "%%%%");
        return SAFE_WORD.matcher(text).matches() ? escaped : "\"" + escaped.replace("\"", "\\\"") + "\"";
    }
}
