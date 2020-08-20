package internal.sdmxdl.web;

import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebSource;

import java.util.logging.Level;

@lombok.extern.java.Log
public enum DefaultSdmxWebListener implements SdmxWebListener {

    INSTANCE;

    @Override
    public boolean isEnabled() {
        return log.isLoggable(Level.INFO);
    }

    @Override
    public void onSourceEvent(SdmxWebSource source, String message) {
        log.log(Level.INFO, message);
    }
}
