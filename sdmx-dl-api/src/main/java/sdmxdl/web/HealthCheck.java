package sdmxdl.web;

/**
 * Kind of check performed by {@link SdmxWebManager#checkHealth(WebHealthRequest)}.
 */
public enum HealthCheck {

    /**
     * Asks the third-party monitor of the source (Upptime, UptimeRobot, ...) for its status.
     * Cheap and cached; does not contact the source itself.
     */
    MONITOR,

    /**
     * Performs a live request against the source, using the local network configuration.
     */
    ACCESS
}
