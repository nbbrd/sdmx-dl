package internal.sdmxdl.script;

import static internal.sdmxdl.script.ScriptFormats.toUrl;

import com.github.mustachejava.Mustache;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.NonNull;
import sdmxdl.Request;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptOptions;

/**
 * Renders REST calls as Power Query M scripts, to be used in Excel or Power BI.
 */
@lombok.experimental.UtilityClass
public class PowerQueryScripts {

    public static final String FILE_EXTENSION = "pq";

    public static final String MEDIA_TYPE = "text/plain";

    private static final Mustache REST_TEMPLATE = ScriptTemplates.load("powerquery-rest.mustache");

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private static final Set<String> KEYWORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "and",
            "as",
            "each",
            "else",
            "error",
            "false",
            "if",
            "in",
            "is",
            "let",
            "meta",
            "not",
            "null",
            "or",
            "otherwise",
            "section",
            "shared",
            "then",
            "true",
            "try",
            "type")));

    public static void renderRest(
            @NonNull String source,
            @NonNull Request request,
            @NonNull ScriptOptions options,
            Script.@NonNull Builder result) {
        RestCall call = RestCalls.of(source, request);
        List<String> warnings = new ArrayList<>(call.getWarnings());
        if (options.getOutputFile() != null) {
            warnings.add("Output file is not supported by Power Query; the result is returned as a table");
        }

        URI endpoint = options.getRestEndpoint();
        String url = toUrl(endpoint, call.getPath());
        String base = endpoint.getScheme() + "://" + endpoint.getRawAuthority();
        String relativePath = url.substring(Math.min(base.length() + 1, url.length()));

        Map<String, Object> scope = ScriptTemplates.newScope(warnings);
        scope.put("base", quote(base));
        scope.put("relativePath", quote(relativePath));
        scope.put("hasParameters", !call.getParameters().isEmpty());
        scope.put(
                "parameters",
                call.getParameters().entrySet().stream()
                        .map(entry -> toIdentifier(entry.getKey()) + " = "
                                + quote(entry.getValue().toString()))
                        .collect(Collectors.joining(", ", "[", "]")));
        scope.put("dataSet", call.getShape() == RestCall.Shape.DATA_SET);
        scope.put("list", call.getShape() == RestCall.Shape.LIST);
        scope.put(
                "fieldNames",
                call.getFields().values().stream()
                        .map(PowerQueryScripts::quote)
                        .collect(Collectors.joining(", ", "{", "}")));
        scope.put(
                "renames",
                call.getFields().entrySet().stream()
                        .map(entry -> "{" + quote(entry.getValue()) + ", " + quote(entry.getKey()) + "}")
                        .collect(Collectors.joining(", ", "{", "}")));
        result.content(ScriptTemplates.render(REST_TEMPLATE, scope)).warnings(warnings);
    }

    private static String toIdentifier(String name) {
        return IDENTIFIER.matcher(name).matches() && !KEYWORDS.contains(name) ? name : "#" + quote(name);
    }

    /**
     * Quotes a text as a Power Query M text literal.
     */
    public static @NonNull String quote(@NonNull String text) {
        StringBuilder result = new StringBuilder(text.length() + 2).append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"':
                    result.append("\"\"");
                    break;
                case '\n':
                    result.append("#(lf)");
                    break;
                case '\r':
                    result.append("#(cr)");
                    break;
                case '\t':
                    result.append("#(tab)");
                    break;
                case '#':
                    // avoids an accidental escape sequence such as "#(lf)" in the original text
                    result.append(i + 1 < text.length() && text.charAt(i + 1) == '(' ? "#(#)" : "#");
                    break;
                default:
                    result.append(c);
            }
        }
        return result.append('"').toString();
    }
}
