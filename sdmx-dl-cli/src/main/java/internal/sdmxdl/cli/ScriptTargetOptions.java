package internal.sdmxdl.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import picocli.CommandLine;
import sdmxdl.Request;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptOptions;
import sdmxdl.script.ScriptTarget;

/**
 * Options of the commands that generate scripts instead of executing requests.
 */
@lombok.Getter
@lombok.Setter
public class ScriptTargetOptions {

    @CommandLine.Option(
            names = {"-t", "--target"},
            paramLabel = "<language/transport>",
            defaultValue = "python/cli",
            converter = ScriptTargetConverter.class,
            completionCandidates = ScriptTargetCandidates.class,
            descriptionKey = "cli.script.target")
    private ScriptTarget target;

    @CommandLine.Option(
            names = {"--cli-launcher"},
            paramLabel = "<command>",
            split = ",",
            descriptionKey = "cli.script.cliLauncher")
    private List<String> cliLauncher;

    @CommandLine.Option(
            names = {"--rest-endpoint"},
            paramLabel = "<uri>",
            descriptionKey = "cli.script.restEndpoint")
    private URI restEndpoint;

    @CommandLine.Option(
            names = {"-o", "--output"},
            paramLabel = "<file>",
            descriptionKey = "cli.script.output")
    private String output;

    @CommandLine.Option(
            names = {"--script-file"},
            paramLabel = "<file>",
            descriptionKey = "cli.script.scriptFile")
    private Path scriptFile;

    @CommandLine.Option(
            names = {"-P", "--property"},
            paramLabel = "<name=value>",
            descriptionKey = "cli.script.property")
    private Map<String, String> properties;

    public ScriptOptions toScriptOptions() {
        ScriptOptions.Builder result = ScriptOptions.builder().outputFile(output);
        if (cliLauncher != null && !cliLauncher.isEmpty()) {
            result.cliLauncherOf(cliLauncher.toArray(new String[0]));
        }
        if (restEndpoint != null) {
            result.restEndpoint(restEndpoint);
        }
        if (properties != null) {
            result.properties(properties);
        }
        return result.build();
    }

    public void generate(CommandLine.Model.CommandSpec spec, String source, Request request) throws IOException {
        Script script = ScriptManager.ofServiceLoader().generate(target, source, request, toScriptOptions());
        if (!script.getWarnings().isEmpty()) {
            PrintWriter err = spec.commandLine().getErr();
            script.getWarnings().forEach(warning -> err.println("Warning: " + warning));
            err.flush();
        }
        if (scriptFile != null) {
            Files.write(scriptFile, script.getContent().getBytes(StandardCharsets.UTF_8));
        } else {
            PrintWriter out = spec.commandLine().getOut();
            out.print(script.getContent());
            out.flush();
        }
    }
}
