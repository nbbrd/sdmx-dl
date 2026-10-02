package sdmxdl.script;

import internal.sdmxdl.script.spi.ScriptGeneratorLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import sdmxdl.Request;
import sdmxdl.script.spi.ScriptGenerator;

/**
 * Entry point to generate scripts that reproduce requests in other languages and tools.
 * <p>
 * For a target and a request type, the generator with the highest rank is used; the first one in
 * {@link #getGenerators()} wins on equal ranks.
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
        return toUnmodifiableSet(generators.stream().map(ScriptGenerator::getScriptTarget));
    }

    public @NonNull Set<Class<? extends Request>> getRequestTypes(@NonNull ScriptTarget target) {
        return toUnmodifiableSet(
                getGenerators(target).flatMap(generator -> generator.getScriptRequestTypes().stream()));
    }

    /**
     * Gets the names of the generator-specific properties supported by a target.
     *
     * @param target the target
     * @return a non-null set, empty if the target is unknown or has no property
     * @see ScriptOptions#getProperties()
     */
    public @NonNull Set<String> getPropertyNames(@NonNull ScriptTarget target) {
        return toUnmodifiableSet(
                getGenerators(target).flatMap(generator -> generator.getScriptPropertyNames().stream()));
    }

    public boolean isSupported(@NonNull ScriptTarget target, @NonNull Class<? extends Request> requestType) {
        return lookupGenerator(target, requestType).isPresent();
    }

    public @NonNull Script generate(
            @NonNull ScriptTarget target,
            @NonNull String sourceId,
            @NonNull Request request,
            @NonNull ScriptOptions options)
            throws IllegalArgumentException {
        ScriptGenerator generator = lookupGenerator(target, request.getClass())
                .orElseThrow(() -> getTargets().contains(target)
                        ? new IllegalArgumentException("Unsupported request type '"
                                + request.getClass().getSimpleName() + "' for target '" + target + "'")
                        : new IllegalArgumentException(
                                "Unknown target '" + target + "', expecting one of " + getTargets()));
        Script result = generator.generateScript(sourceId, request, options);
        return reportUnsupportedProperties(result, options, generator.getScriptPropertyNames());
    }

    private Stream<ScriptGenerator> getGenerators(ScriptTarget target) {
        return generators.stream()
                .filter(generator -> generator.getScriptTarget().equals(target));
    }

    private Optional<ScriptGenerator> lookupGenerator(ScriptTarget target, Class<? extends Request> requestType) {
        return getGenerators(target)
                .filter(generator -> generator.getScriptRequestTypes().contains(requestType))
                .max(Comparator.comparingInt(ScriptGenerator::getScriptRank));
    }

    private static Script reportUnsupportedProperties(
            Script script, ScriptOptions options, Collection<String> supportedNames) {
        List<String> unsupportedNames = options.getProperties().keySet().stream()
                .filter(name -> !supportedNames.contains(name))
                .sorted()
                .collect(Collectors.toList());
        if (unsupportedNames.isEmpty()) {
            return script;
        }
        Script.Builder result = script.toBuilder();
        unsupportedNames.forEach(name -> result.warning("Unsupported property '" + name + "' was ignored"));
        return result.build();
    }

    private static <T> Set<T> toUnmodifiableSet(Stream<T> stream) {
        return stream.collect(Collectors.collectingAndThen(
                Collectors.toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
    }
}
