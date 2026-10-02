package internal.sdmxdl.script;

import java.util.Collection;
import java.util.Set;
import lombok.NonNull;
import sdmxdl.Request;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptOptions;
import sdmxdl.script.ScriptTarget;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Generic script generator that delegates the rendering of the content to a function.
 */
@lombok.Builder(toBuilder = true)
public final class ScriptGeneratorSupport implements ScriptGenerator {

    @FunctionalInterface
    public interface Renderer {

        /**
         * Renders a script by setting its content and warnings in the specified builder.
         */
        void render(
                @NonNull String sourceId,
                @NonNull Request request,
                @NonNull ScriptOptions options,
                Script.@NonNull Builder result)
                throws IllegalArgumentException;
    }

    private final @NonNull ScriptTarget scriptTarget;

    @lombok.Builder.Default
    private final int scriptRank = BUILTIN_SCRIPT_RANK;

    private final @NonNull String scriptFileExtension;

    @lombok.Builder.Default
    private final @NonNull String scriptMediaType = DEFAULT_SCRIPT_MEDIA_TYPE;

    private final @NonNull Set<Class<? extends Request>> scriptRequestTypes;

    @lombok.Singular
    private final @NonNull Set<String> scriptPropertyNames;

    private final @NonNull Renderer renderer;

    @Override
    public @NonNull ScriptTarget getScriptTarget() {
        return scriptTarget;
    }

    @Override
    public int getScriptRank() {
        return scriptRank;
    }

    @Override
    public @NonNull String getScriptFileExtension() {
        return scriptFileExtension;
    }

    @Override
    public @NonNull Set<Class<? extends Request>> getScriptRequestTypes() {
        return scriptRequestTypes;
    }

    @Override
    public @NonNull String getScriptMediaType() {
        return scriptMediaType;
    }

    @Override
    public @NonNull Collection<String> getScriptPropertyNames() {
        return scriptPropertyNames;
    }

    @Override
    public @NonNull Script generateScript(
            @NonNull String sourceId, @NonNull Request request, @NonNull ScriptOptions options)
            throws IllegalArgumentException {
        if (!scriptRequestTypes.contains(request.getClass())) {
            throw new IllegalArgumentException(
                    "Unsupported request type: " + request.getClass().getSimpleName());
        }
        Script.Builder result = Script.builder()
                .target(scriptTarget)
                .fileExtension(scriptFileExtension)
                .mediaType(scriptMediaType);
        renderer.render(sourceId, request, options, result);
        return result.build();
    }
}
