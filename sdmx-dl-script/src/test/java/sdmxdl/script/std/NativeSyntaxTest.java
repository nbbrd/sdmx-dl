package sdmxdl.script.std;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sdmxdl.web.SdmxWebManager;

/**
 * Checks the syntax of the generated scripts with the real parsers of their languages, without running them.
 * <p>
 * Each check uses a tool found on the machine and is skipped when that tool is missing,
 * unless the environment variable {@value #STRICT_ENV} is set to {@code true}.
 * Enable these tests with the {@code scriptSyntax} Maven profile.
 */
@Tag("scriptSyntax")
public class NativeSyntaxTest {

    private static final String STRICT_ENV = "SDMXDL_SCRIPT_SYNTAX_STRICT";

    private static final long TIMEOUT_IN_SECONDS = 120;

    private static final Map<String, List<Checker>> CHECKERS = Map.of(
            "bash", List.of(NativeSyntaxTest::checkBash, NativeSyntaxTest::checkShellCheck),
            "jbang", List.of(NativeSyntaxTest::checkJava),
            "jupyter", List.of(NativeSyntaxTest::checkPython),
            "powerquery", List.of(NativeSyntaxTest::checkPowerQuery),
            "powershell", List.of(NativeSyntaxTest::checkPowerShell),
            "python", List.of(NativeSyntaxTest::checkPython),
            "r", List.of(NativeSyntaxTest::checkR));

    private static final Map<String, String> BROKEN_SCRIPTS = Map.of(
            "bash", "if true; then echo a\n",
            "jbang", "void main() { String s = \"a\"b\"; }\n",
            "jupyter", "print('it's')\n",
            "powerquery", "let\n    a = 1,\nin\n    a\n",
            "powershell", "foreach ($x in @(1, 2) {\n}\n",
            "python", "print('it's')\n",
            "r", "x <- c(1, 2\n");

    private static final Map<Probe, Boolean> AVAILABLE_PROGRAMS = new ConcurrentHashMap<>();

    @TempDir
    static Path temp;

    @ParameterizedTest(name = "{0}")
    @MethodSource("samples")
    public void testSyntax(ScriptSample sample) throws IOException {
        String content = sample.getScript().getContent();
        for (Result result : checkAll(sample.getLanguage(), sample.getScript().getFileExtension(), content)) {
            assertThat(result.getExitCode())
                    .describedAs("%s%n%s", result.getOutput(), content)
                    .isZero();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("languages")
    public void testBrokenSyntax(String language) throws IOException {
        String content = BROKEN_SCRIPTS.get(language);
        for (Result result : checkAll(language, "txt", content)) {
            assertThat(result.getExitCode())
                    .describedAs("Checker should reject:%n%s", content)
                    .isNotZero();
        }
    }

    static Stream<ScriptSample> samples() {
        return ScriptSample.all().stream().filter(sample -> CHECKERS.containsKey(sample.getLanguage()));
    }

    static Stream<String> languages() {
        return CHECKERS.keySet().stream().sorted();
    }

    private static List<Result> checkAll(String language, String fileExtension, String content) throws IOException {
        Path script = Files.createTempFile(temp, "script", "." + fileExtension);
        Files.write(script, content.getBytes(UTF_8));

        List<Result> result = new ArrayList<>();
        List<String> unavailable = new ArrayList<>();
        for (Checker checker : CHECKERS.get(language)) {
            try {
                result.add(checker.check(script));
            } catch (UnavailableException ex) {
                unavailable.add(ex.getMessage());
            }
        }

        if (!unavailable.isEmpty() && Boolean.parseBoolean(System.getenv(STRICT_ENV))) {
            fail("Unavailable: " + unavailable);
        }
        assumeTrue(!result.isEmpty(), () -> "Unavailable: " + unavailable);
        return result;
    }

    private static Result checkPython(Path script) {
        String python = findProgram(
                "Python 3",
                new Probe(Arrays.asList("python3", "--version"), "Python 3"),
                new Probe(Arrays.asList("python", "--version"), "Python 3"));
        return run(Arrays.asList(
                python,
                "-c",
                "import ast,sys; ast.parse(open(sys.argv[1], encoding='utf-8').read(), sys.argv[1])",
                script.toString()));
    }

    private static Result checkR(Path script) {
        String rscript = findProgram("R", new Probe(Arrays.asList("Rscript", "--version"), "R"));
        return run(Arrays.asList(
                rscript, "-e", "invisible(parse(file = commandArgs(TRUE)[1], encoding = 'UTF-8'))", script.toString()));
    }

    private static Result checkBash(Path script) {
        // a broken WSL prints an error and exits with 0, hence the expected output
        String bash = findProgram("bash", new Probe(Arrays.asList("bash", "-c", "echo sdmx-dl"), "sdmx-dl"));
        // the script is sent to stdin so that its path doesn't have to be translated (e.g. WSL on Windows)
        return run(Arrays.asList(bash, "-n"), script);
    }

    private static Result checkShellCheck(Path script) {
        String shellcheck =
                findProgram("ShellCheck", new Probe(Arrays.asList("shellcheck", "--version"), "ShellCheck"));
        return run(Arrays.asList(shellcheck, "--shell=bash", "--severity=warning", "-"), script);
    }

    private static Result checkPowerShell(Path script) {
        String powershell = findProgram(
                "PowerShell",
                new Probe(Arrays.asList("pwsh", "-NoProfile", "-Command", "'sdmx-dl'"), "sdmx-dl"),
                new Probe(Arrays.asList("powershell", "-NoProfile", "-Command", "'sdmx-dl'"), "sdmx-dl"));
        Path checker = writeResource(
                "check-powershell.ps1",
                "param([string]$Path)\n"
                        + "$tokens = $null\n"
                        + "$errors = $null\n"
                        + "[void][System.Management.Automation.Language.Parser]::ParseFile($Path, [ref]$tokens, [ref]$errors)\n"
                        + "foreach ($e in $errors) {\n"
                        + "    '{0}:{1} {2}' -f $e.Extent.StartLineNumber, $e.Extent.StartColumnNumber, $e.Message\n"
                        + "}\n"
                        + "exit $errors.Count\n");
        return run(Arrays.asList(
                powershell,
                "-NoProfile",
                "-NonInteractive",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                checker.toString(),
                script.toString()));
    }

    private static Result checkPowerQuery(Path script) {
        String node = findProgram(
                "Node.js with @microsoft/powerquery-parser",
                new Probe(
                        Arrays.asList("node", "-e", "require('@microsoft/powerquery-parser'); console.log('sdmx-dl')"),
                        "sdmx-dl"));
        Path checker = writeResource(
                "check-powerquery.js",
                "const PQP = require('@microsoft/powerquery-parser');\n"
                        + "const text = require('fs').readFileSync(process.argv[2], 'utf8');\n"
                        + "PQP.TaskUtils.tryLexParse(PQP.DefaultSettings, text).then(task => {\n"
                        + "    if (PQP.TaskUtils.isParseStageOk(task)) process.exit(0);\n"
                        + "    console.error(task.error ? task.error.message : JSON.stringify(task));\n"
                        + "    process.exit(1);\n"
                        + "}, error => {\n"
                        + "    console.error(error);\n"
                        + "    process.exit(2);\n"
                        + "});\n");
        return run(Arrays.asList(node, checker.toString(), script.toString()));
    }

    private static Result checkJava(Path script) {
        int feature = Runtime.version().feature();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (feature < 25 || compiler == null) {
            throw new UnavailableException("JDK 25+ (current: " + feature + ")");
        }
        try {
            // implicitly declared classes are named after their file
            Path dir = Files.createTempDirectory(temp, "jbang");
            Path source = Files.copy(script, dir.resolve("Main.java"));
            StringWriter output = new StringWriter();
            try (StandardJavaFileManager files = compiler.getStandardFileManager(null, null, UTF_8)) {
                Iterable<? extends JavaFileObject> units = files.getJavaFileObjects(source.toFile());
                List<String> options = Arrays.asList(
                        "-proc:none",
                        "-Xlint:none",
                        "-d",
                        dir.toString(),
                        "-classpath",
                        getLocation(SdmxWebManager.class) + File.pathSeparator + System.getProperty("java.class.path"));
                boolean success = compiler.getTask(new PrintWriter(output), files, null, options, null, units)
                        .call();
                return new Result(success ? 0 : 1, output.toString());
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static String findProgram(String name, Probe... probes) {
        return Stream.of(probes)
                .filter(probe -> AVAILABLE_PROGRAMS.computeIfAbsent(probe, NativeSyntaxTest::isAvailable))
                .map(probe -> probe.getCommand().get(0))
                .findFirst()
                .orElseThrow(() -> new UnavailableException(name));
    }

    private static boolean isAvailable(Probe probe) {
        try {
            Result result = run(probe.getCommand());
            return result.getExitCode() == 0 && result.getOutput().contains(probe.getExpectedOutput());
        } catch (UncheckedIOException ex) {
            return false;
        }
    }

    private static Result run(List<String> command) {
        return run(command, null);
    }

    private static Result run(List<String> command, Path stdin) {
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
        if (stdin != null) {
            builder.redirectInput(stdin.toFile());
        }
        try {
            Process process = builder.start();
            if (stdin == null) {
                process.getOutputStream().close();
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Thread reader = new Thread(() -> copy(process.getInputStream(), output));
            reader.start();
            if (!process.waitFor(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return new Result(-1, "Timeout after " + TIMEOUT_IN_SECONDS + "s: " + command);
            }
            reader.join();
            return new Result(process.exitValue(), new String(output.toByteArray(), UTF_8));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    private static void copy(InputStream input, OutputStream output) {
        try (InputStream closeable = input) {
            closeable.transferTo(output);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static Path writeResource(String name, String content) {
        try {
            Path result = temp.resolve(name);
            synchronized (NativeSyntaxTest.class) {
                if (!Files.exists(result)) {
                    Files.write(result, content.getBytes(UTF_8));
                }
            }
            return result;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static String getLocation(Class<?> type) {
        try {
            return Paths.get(type.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                    .toString();
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @FunctionalInterface
    private interface Checker {
        Result check(Path script) throws UnavailableException;
    }

    @lombok.Value
    private static class Probe {
        List<String> command;
        String expectedOutput;
    }

    @lombok.Value
    private static class Result {
        int exitCode;
        String output;
    }

    private static final class UnavailableException extends RuntimeException {

        UnavailableException(String name) {
            super(name + " is not available", null, false, false);
        }
    }
}
