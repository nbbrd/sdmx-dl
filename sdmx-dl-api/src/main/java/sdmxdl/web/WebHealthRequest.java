package sdmxdl.web;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;

/**
 * Request of {@link SdmxWebManager#checkHealth(WebHealthRequest)}.
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class WebHealthRequest {

    public static final Set<HealthCheck> DEFAULT_CHECKS = Collections.unmodifiableSet(EnumSet.of(HealthCheck.MONITOR));

    public static final WebHealthRequest DEFAULT = WebHealthRequest.builder().build();

    /**
     * Sources to check; empty means every known source (aliases excluded).
     */
    @lombok.Singular
    @NonNull List<String> sources;

    /**
     * Checks to perform; defaults to {@link HealthCheck#MONITOR} only since access checks hit the sources.
     */
    @lombok.Builder.Default
    @NonNull Set<HealthCheck> checks = DEFAULT_CHECKS;

    /**
     * Whether sources are checked in parallel.
     */
    @lombok.Builder.Default
    boolean parallel = true;
}
