package internal.sdmxdl.cli.ext;

import org.jspecify.annotations.Nullable;
import picocli.CommandLine;
import sdmxdl.web.WebSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@lombok.Getter
@lombok.Setter
public class VerboseOptions {

    private static final Set<String> EXPLAIN_MARKERS = new HashSet<>(Arrays.asList(
            "DRIVER", "QUERY", "NETWORK", "RI_CACHING", "HTTP"
    ));

    @CommandLine.Option(
            names = {"-v", "--verbose"},
            defaultValue = "false",
            descriptionKey = "cli.verbose"
    )
    private boolean verbose;

    @CommandLine.Option(
            names = {"--explain"},
            defaultValue = "false",
            descriptionKey = "cli.explain"
    )
    private boolean explain;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    public void reportToErrorStream(@Nullable WebSource source, @Nullable String marker, CharSequence message) {
        reportToErrorStream(source, marker, message, 0);
    }

    public void reportToErrorStream(@Nullable WebSource source, @Nullable String marker, CharSequence message, int depth) {
        if (verbose) {
            printToErrorStream(source, marker, message, null, depth);
        } else if (explain && isExplainMarker(marker)) {
            printToErrorStream(source, marker, message, null, depth);
        }
    }

    public void reportToErrorStream(@Nullable WebSource source, @Nullable String marker, CharSequence message, Exception ex) {
        reportToErrorStream(source, marker, message, ex, 0);
    }

    public void reportToErrorStream(@Nullable WebSource source, @Nullable String marker, CharSequence message, Exception ex, int depth) {
        if (verbose) {
            printToErrorStream(source, marker, message, ex, depth);
        } else if (explain && isExplainMarker(marker)) {
            printToErrorStream(source, marker, message, ex, depth);
        }
    }

    private static boolean isExplainMarker(@Nullable String marker) {
        return marker != null && EXPLAIN_MARKERS.contains(marker);
    }

    private void printToErrorStream(@Nullable WebSource source, @Nullable String marker, CharSequence message, @Nullable Exception ex, int depth) {
        CommandLine.Help.ColorScheme colorScheme = spec.commandLine().getColorScheme();

        String indent = depth > 0 ? repeat("  ", depth) : "";

        CommandLine.Help.Ansi.Text result = colorScheme
                .text("[")
                .concat(colorScheme.commandText(source != null ? source.getId() : "-"))
                .concat(colorScheme.text("] " + indent + "("))
                .concat(colorScheme.optionText(marker))
                .concat(colorScheme.text(") "))
                .concat(colorScheme.text(message.toString()));

        if (ex != null) {
            String details = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();
            result = result.concat(" ").concat(colorScheme.stackTraceText(details));
        }

        spec.commandLine().getErr().println(result);
    }

    private static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }
}
