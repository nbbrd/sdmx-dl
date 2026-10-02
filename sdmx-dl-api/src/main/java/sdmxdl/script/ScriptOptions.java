package sdmxdl.script;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Options that tune the generated scripts without being part of the request.
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class ScriptOptions {

    public static final List<String> DEFAULT_CLI_LAUNCHER = Collections.singletonList("sdmx-dl");

    public static final URI DEFAULT_REST_ENDPOINT = URI.create("http://localhost:4559/sdmx-dl/v2");

    public static final ScriptOptions DEFAULT = ScriptOptions.builder().build();

    /**
     * Command used to launch the CLI, e.g. {@code [sdmx-dl]} or {@code [java, -jar, sdmx-dl-cli-bin.jar]}.
     */
    @lombok.Builder.Default
    @NonNull List<String> cliLauncher = DEFAULT_CLI_LAUNCHER;

    /**
     * Base URI of the web service.
     */
    @lombok.Builder.Default
    @NonNull URI restEndpoint = DEFAULT_REST_ENDPOINT;

    /**
     * File written by the script, or {@code null} to write to the standard output.
     * <p>
     * This is a path on the machine that runs the script, not on the one that generates it; it is
     * therefore kept as a string and inserted as is in the script.
     */
    @Nullable String outputFile;

    /**
     * Generator-specific options, keyed by names starting with
     * {@link sdmxdl.script.spi.ScriptGenerator#SCRIPT_PROPERTY_PREFIX}.
     * <p>
     * The names supported by a target are listed by {@link ScriptManager#getPropertyNames(ScriptTarget)};
     * unsupported names are ignored and reported by {@link ScriptManager} in {@link Script#getWarnings()}.
     */
    @lombok.Singular
    @NonNull Map<String, String> properties;

    public static final class Builder {

        public Builder cliLauncherOf(@NonNull String... cliLauncher) {
            return cliLauncher(Collections.unmodifiableList(Arrays.asList(cliLauncher.clone())));
        }

        public Builder restEndpointOf(@NonNull String restEndpoint) {
            return restEndpoint(URI.create(restEndpoint));
        }
    }
}
