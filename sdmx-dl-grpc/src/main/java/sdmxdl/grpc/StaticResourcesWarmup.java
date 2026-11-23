package sdmxdl.grpc;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

/**
 * Warms up the Vert.x file cache in the background at startup so that the first request of the web UI is not slow.
 * <p>
 * Static resources are served from the classpath by Vert.x, which unpacks the whole parent tree of a resource to its
 * file cache on first access. In the uber-jar, this means thousands of files (mostly webjars) under {@code META-INF},
 * which can take tens of seconds on some file systems.
 */
@ApplicationScoped
public class StaticResourcesWarmup {

    private static final Logger LOGGER = Logger.getLogger(StaticResourcesWarmup.class);

    private static final String RESOURCE = "META-INF/resources/index.html";

    void onStart(@Observes StartupEvent event, Vertx vertx) {
        long start = System.currentTimeMillis();
        vertx.fileSystem()
                .exists(RESOURCE)
                .onSuccess(ignore ->
                        LOGGER.debugf("Static resources warmed up in %d ms", System.currentTimeMillis() - start))
                .onFailure(ex -> LOGGER.debug("Failed to warm up static resources", ex));
    }
}
