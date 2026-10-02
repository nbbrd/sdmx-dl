package internal.sdmxdl.script;

import com.github.mustachejava.Mustache;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import sdmxdl.About;
import sdmxdl.Request;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptOptions;

/**
 * Renders Java library calls as single-file Java programs run by jbang.
 */
@lombok.experimental.UtilityClass
public class JBangScripts {

    public static final String FILE_EXTENSION = "java";

    public static final String MEDIA_TYPE = "text/x-java-source";

    private static final Mustache JAVA_TEMPLATE = ScriptTemplates.load("jbang-java.mustache");

    public static void renderJava(
            @NonNull String source,
            @NonNull Request request,
            @NonNull ScriptOptions options,
            Script.@NonNull Builder result) {
        ApiCall call = ApiCalls.of(request);
        String outputFile = options.getOutputFile();
        Map<String, Object> scope = ScriptTemplates.newScope(call.getWarnings());
        scope.put("version", About.VERSION);
        scope.put("source", quote(source));
        scope.put("requestType", call.getRequestType());
        scope.put("providerMethod", call.getProviderMethod());
        scope.put(
                "arguments",
                call.getArguments().stream()
                        .map(argument -> argument.getMethod() + "(" + toLiteral(argument.getValue()) + ")")
                        .collect(Collectors.toList()));
        scope.put("hasOutputFile", outputFile != null);
        scope.put(
                "output",
                outputFile == null
                        ? "new PrintWriter(System.out)"
                        : "new PrintWriter(Files.newBufferedWriter(Path.of(" + quote(outputFile) + ")))");
        scope.put("dataSet", call.getShape() == ApiCall.Shape.DATA_SET);
        scope.put("list", call.getShape() == ApiCall.Shape.LIST);
        scope.put("header", quote(String.join(",", call.getGetters().keySet())));
        scope.put(
                "row",
                call.getGetters().values().stream()
                        .map(getter -> "csv(item." + getter + "())")
                        .collect(Collectors.joining(" + \",\" + ")));
        result.content(ScriptTemplates.render(JAVA_TEMPLATE, scope)).warnings(call.getWarnings());
    }

    private static String toLiteral(Object value) {
        return value instanceof String ? quote((String) value) : value.toString();
    }

    /**
     * Quotes a text as a Java string literal.
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
                        result.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
            }
        }
        return result.append('"').toString();
    }
}
