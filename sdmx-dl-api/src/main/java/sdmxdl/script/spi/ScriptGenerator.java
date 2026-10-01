package sdmxdl.script.spi;

import java.util.Set;
import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceSorter;
import sdmxdl.Request;
import sdmxdl.script.Script;
import sdmxdl.script.ScriptOptions;
import sdmxdl.script.ScriptTarget;

/**
 * SPI that generates scripts for a single {@link ScriptTarget} (language and transport).
 * <p>
 * A generator declares the request types it supports. When several generators share a target,
 * the one with the highest rank wins; built-in generators use {@link #BUILTIN_SCRIPT_RANK} so that
 * external ones can override them with {@link #EXTERNAL_SCRIPT_RANK}.
 */
@ServiceDefinition(quantifier = Quantifier.MULTIPLE, loaderName = "internal.{{canonicalName}}Loader")
@ThreadSafe
public interface ScriptGenerator {

    @NonNull ScriptTarget getScriptTarget();

    @ServiceSorter(reverse = true)
    int getScriptRank();

    @NonNull String getScriptFileExtension();

    @NonNull Set<Class<? extends Request>> getScriptRequestTypes();

    /**
     * Generates a script that sends a request to a source.
     *
     * @param source  the id of the web source
     * @param request a request whose type is part of {@link #getScriptRequestTypes()}
     * @param options the generation options
     * @return a non-null script
     * @throws IllegalArgumentException if the request type is not supported
     */
    @NonNull Script generateScript(@NonNull String source, @NonNull Request request, @NonNull ScriptOptions options)
            throws IllegalArgumentException;

    int BUILTIN_SCRIPT_RANK = 0;

    int EXTERNAL_SCRIPT_RANK = Byte.MAX_VALUE;
}
