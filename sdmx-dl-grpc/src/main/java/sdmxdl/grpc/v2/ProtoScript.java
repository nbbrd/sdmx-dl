package sdmxdl.grpc.v2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
                .addAllProperties(manager.getPropertyNames(target))
                .build();
    }

    static ScriptDto fromScript(Script script) {
        return ScriptDto.newBuilder()
                .setTarget(script.getTarget().toString())
                .setFileExtension(script.getFileExtension())
                .setMediaType(script.getMediaType())
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
                options.hasOutputFile() ? options.getOutputFile() : null,
                options.getPropertiesMap());
    }

    static ScriptOptions toScriptOptions(
            List<String> cliLauncher, String restEndpoint, String outputFile, Map<String, String> properties) {
        ScriptOptions.Builder result = ScriptOptions.builder().outputFile(outputFile);
        if (cliLauncher != null && !cliLauncher.isEmpty()) {
            result.cliLauncher(List.copyOf(cliLauncher));
        }
        if (restEndpoint != null && !restEndpoint.isBlank()) {
            result.restEndpointOf(restEndpoint);
        }
        if (properties != null) {
            result.properties(properties);
        }
        return result.build();
    }

    /**
     * Parses properties formatted as {@code name=value}.
     */
    static Map<String, String> parseProperties(List<String> properties) throws IllegalArgumentException {
        Map<String, String> result = new LinkedHashMap<>();
        if (properties != null) {
            for (String property : properties) {
                int index = property.indexOf('=');
                if (index <= 0) {
                    throw new IllegalArgumentException(
                            "Invalid script property: '" + property + "', expecting <name>=<value>");
                }
                result.put(property.substring(0, index), property.substring(index + 1));
            }
        }
        return result;
    }

    private static String getCommandName(Class<? extends Request> requestType) {
        String name = requestType.getSimpleName();
        return name.substring(0, name.length() - "Request".length()).toLowerCase(Locale.ROOT);
    }
}
