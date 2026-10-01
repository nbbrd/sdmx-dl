package sdmxdl.grpc;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import sdmxdl.script.ScriptManager;

@Singleton
public class ScriptManagerProducer {

    // Note: ScriptManager is final (@lombok.Value), so it cannot be proxied.
    // @Unremovable because it is also looked up programmatically (see SdmxdlRestService2).
    @Produces
    @Singleton
    @Unremovable
    public ScriptManager scriptManager() {
        return ScriptManager.ofServiceLoader();
    }
}
