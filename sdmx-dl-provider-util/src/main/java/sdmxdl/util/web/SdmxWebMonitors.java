package sdmxdl.util.web;

import java.net.URI;

@lombok.experimental.UtilityClass
public class SdmxWebMonitors {
    
    public static void checkMonitor(URI monitor, String uriScheme) {
        if (monitor == null) {
            throw new IllegalArgumentException("Expecting monitor not to be null");
        }
        if (!monitor.getScheme().equals(uriScheme)) {
            throw new IllegalArgumentException(monitor.toString());
        }
    }
}
