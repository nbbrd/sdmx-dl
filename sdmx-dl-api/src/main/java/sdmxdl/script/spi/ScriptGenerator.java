package sdmxdl.script.spi;

import java.util.Collection;
import java.util.Collections;
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
 * A generator declares the request types it supports. When several generators support the same
 * target and request type, the one with the highest rank wins (the first one on equal ranks).
 * Built-in generators use {@link #BUILTIN_SCRIPT_RANK} so that external ones can override them with
 * {@link #EXTERNAL_SCRIPT_RANK}. Since the selection is done per request type, an external generator
 * may override a target for some request types only.
 */
@ServiceDefinition(quantifier = Quantifier.MULTIPLE, loaderName = "internal.{{canonicalName}}Loader")
@ThreadSafe
public interface ScriptGenerator {

    @NonNull ScriptTarget getScriptTarget();

    @ServiceSorter(reverse = true)
    int getScriptRank();

    @NonNull String getScriptFileExtension();

    /**
     * Gets the media type of the generated scripts, without parameters (e.g. {@code text/x-python}).
     *
     * @return a non-null media type, {@link #DEFAULT_SCRIPT_MEDIA_TYPE} by default
     */
    default @NonNull String getScriptMediaType() {
        return DEFAULT_SCRIPT_MEDIA_TYPE;
    }

    @NonNull Set<Class<? extends Request>> getScriptRequestTypes();

    /**
     * Gets the names of the generator-specific properties supported in {@link ScriptOptions#getProperties()}.
     * These names should start with {@link #SCRIPT_PROPERTY_PREFIX}.
     *
     * @return a non-null collection, empty by default
     */
    default @NonNull Collection<String> getScriptPropertyNames() {
        return Collections.emptyList();
    }

    /**
     * Generates a script that sends a request to a source.
     *
     * @param sourceId the id of the web source
     * @param request  a request whose type is part of {@link #getScriptRequestTypes()}
     * @param options  the generation options
     * @return a non-null script
     * @throws IllegalArgumentException if the request type is not supported
     */
    @NonNull Script generateScript(@NonNull String sourceId, @NonNull Request request, @NonNull ScriptOptions options)
            throws IllegalArgumentException;

    int EXTERNAL_SCRIPT_RANK = Byte.MAX_VALUE;

    int BUILTIN_SCRIPT_RANK = 0;

    int UNKNOWN_SCRIPT_RANK = -1;

    String SCRIPT_PROPERTY_PREFIX = "sdmxdl.script";

    String DEFAULT_SCRIPT_MEDIA_TYPE = "text/plain";
}
