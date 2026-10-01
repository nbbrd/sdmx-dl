package sdmxdl.script;

import internal.sdmxdl.script.spi.ScriptGeneratorLoader;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import sdmxdl.Request;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Entry point to generate scripts that reproduce requests in other languages and tools.
 * <p>
 * Generators are tried in order; the first one that matches the target and supports the request
 * type is used.
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class ScriptManager {

    @StaticFactoryMethod
    public static @NonNull ScriptManager ofServiceLoader() {
        return ScriptManager.builder().generators(ScriptGeneratorLoader.load()).build();
    }

    @StaticFactoryMethod
    public static @NonNull ScriptManager noOp() {
        return ScriptManager.builder().build();
    }

    @lombok.Singular
    @NonNull List<ScriptGenerator> generators;

    public @NonNull Set<ScriptTarget> getTargets() {
        return generators.stream()
                .map(ScriptGenerator::getScriptTarget)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
    }

    public @NonNull Set<Class<? extends Request>> getRequestTypes(@NonNull ScriptTarget target) {
        return generators.stream()
                .filter(generator -> generator.getScriptTarget().equals(target))
                .flatMap(generator -> generator.getScriptRequestTypes().stream())
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
    }

    public boolean isSupported(@NonNull ScriptTarget target, @NonNull Class<? extends Request> requestType) {
        return lookupGenerator(target, requestType).isPresent();
    }

    public @NonNull Script generate(
            @NonNull ScriptTarget target,
            @NonNull String source,
            @NonNull Request request,
            @NonNull ScriptOptions options)
            throws IllegalArgumentException {
        return lookupGenerator(target, request.getClass())
                .orElseThrow(() -> getTargets().contains(target)
                        ? new IllegalArgumentException("Unsupported request type '"
                                + request.getClass().getSimpleName() + "' for target '" + target + "'")
                        : new IllegalArgumentException(
                                "Unknown target '" + target + "', expecting one of " + getTargets()))
                .generateScript(source, request, options);
    }

    private Optional<ScriptGenerator> lookupGenerator(ScriptTarget target, Class<? extends Request> requestType) {
        return generators.stream()
                .filter(generator -> generator.getScriptTarget().equals(target))
                .filter(generator -> generator.getScriptRequestTypes().contains(requestType))
                .findFirst();
    }
}
