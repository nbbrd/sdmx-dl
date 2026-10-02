package sdmxdl.script.std;

import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TSParser;
import org.treesitter.TreeSitterBash;
import org.treesitter.TreeSitterJava;
import org.treesitter.TreeSitterPython;
import org.treesitter.TreeSitterR;

/**
 * Checks the syntax of the generated scripts with <a href="https://tree-sitter.github.io">tree-sitter</a> grammars.
 * <p>
 * These grammars are portable (bundled native libraries) but more lenient than the real parsers;
 * see {@link NativeSyntaxTest} for stricter checks that rely on the tools installed on the machine.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class TreeSitterSyntaxTest {

    @BeforeAll
    public static void checkPlatform() {
        assumeFalse(
                System.getProperty("os.name").startsWith("Windows") && "aarch64".equals(System.getProperty("os.arch")),
                "tree-sitter-ng has no native library for Windows on ARM");
    }

    private static final Map<String, Supplier<TSLanguage>> GRAMMARS = Map.of(
            "bash", TreeSitterBash::new,
            "jbang", TreeSitterJava::new,
            "jupyter", TreeSitterPython::new,
            "python", TreeSitterPython::new,
            "r", TreeSitterR::new);

    @ParameterizedTest(name = "{0}")
    @MethodSource("samples")
    public void testSyntax(ScriptSample sample) {
        String content = sample.getScript().getContent();
        assertThat(parseErrors(sample.getLanguage(), content)).as(content).isEmpty();
    }

    @Test
    public void testCoverage() {
        Set<String> uncovered = ScriptSample.all().stream()
                .map(ScriptSample::getLanguage)
                .filter(language -> !GRAMMARS.containsKey(language))
                .collect(toCollection(TreeSet::new));
        assertThat(uncovered)
                .describedAs("Languages without tree-sitter grammar")
                .containsExactly("batch", "powerquery", "powershell");
    }

    @Test
    public void testParseErrors() {
        assertThat(parseErrors("bash", "if true; then echo a\n")).containsExactly("2:1 MISSING fi");
        assertThat(parseErrors("jbang", "void main() { String s = \"a\"b\"; }\n"))
                .isNotEmpty();
        assertThat(parseErrors("python", "print('it's')\n")).isNotEmpty();
        assertThat(parseErrors("r", "x <- c(1, 2\n")).containsExactly("1:12 MISSING )");
    }

    static Stream<ScriptSample> samples() {
        return ScriptSample.all().stream().filter(sample -> GRAMMARS.containsKey(sample.getLanguage()));
    }

    private static List<String> parseErrors(String language, String content) {
        try (TSParser parser = new TSParser()) {
            parser.setLanguage(GRAMMARS.get(language).get());
            List<String> result = new ArrayList<>();
            collectErrors(parser.parseString(null, content).getRootNode(), result);
            return result;
        }
    }

    private static void collectErrors(TSNode node, List<String> result) {
        if (node.isError() || node.isMissing()) {
            result.add((node.getStartPoint().getRow() + 1) + ":"
                    + (node.getStartPoint().getColumn() + 1) + " "
                    + (node.isMissing() ? "MISSING " + node.getType() : "ERROR"));
            return;
        }
        if (node.hasError()) {
            for (int i = 0; i < node.getChildCount(); i++) {
                collectErrors(node.getChild(i), result);
            }
        }
    }
}
