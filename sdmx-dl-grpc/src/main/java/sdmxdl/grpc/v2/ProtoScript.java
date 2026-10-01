package sdmxdl.grpc.v2;

import java.util.List;
import java.util.Locale;
import sdmxdl.Request;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptManager;
import sdmxdl.script.ScriptOptions;
import sdmxdl.script.ScriptTarget;

/**
 * Conversions between script generation objects and their protobuf counterparts, shared by gRPC and REST.
 */
@lombok.experimental.UtilityClass
class ProtoScript {

    static final String DEFAULT_TARGET = "python/cli";

    static ScriptTargetDto fromScriptTarget(ScriptManager manager, ScriptTarget target) {
        return ScriptTargetDto.newBuilder()
                .setTarget(target.toString())
                .setLanguage(target.getLanguage())
                .setTransport(target.getTransport())
                .addAllCommands(manager.getRequestTypes(target).stream()
                        .map(ProtoScript::getCommandName)
                        .toList())
                .build();
    }

    static ScriptDto fromScript(Script script) {
        return ScriptDto.newBuilder()
                .setTarget(script.getTarget().toString())
                .setFileExtension(script.getFileExtension())
                .setContent(script.getContent())
                .addAllWarnings(script.getWarnings())
                .build();
    }

    static ScriptTarget toScriptTarget(ScriptOptionsDto options) {
        return ScriptTarget.parse(options.hasTarget() ? options.getTarget() : DEFAULT_TARGET);
    }

    static ScriptOptions toScriptOptions(ScriptOptionsDto options) {
        return toScriptOptions(
                options.getCliLauncherList(),
                options.hasRestEndpoint() ? options.getRestEndpoint() : null,
                options.hasOutputFile() ? options.getOutputFile() : null);
    }

    static ScriptOptions toScriptOptions(List<String> cliLauncher, String restEndpoint, String outputFile) {
        ScriptOptions.Builder result = ScriptOptions.builder().outputFile(outputFile);
        if (cliLauncher != null && !cliLauncher.isEmpty()) {
            result.cliLauncher(List.copyOf(cliLauncher));
        }
        if (restEndpoint != null && !restEndpoint.isBlank()) {
            result.restEndpointOf(restEndpoint);
        }
        return result.build();
    }

    private static String getCommandName(Class<? extends Request> requestType) {
        String name = requestType.getSimpleName();
        return name.substring(0, name.length() - "Request".length()).toLowerCase(Locale.ROOT);
    }
}
