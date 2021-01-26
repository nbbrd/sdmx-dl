package internal.sdmxdl.cli;

import sdmxdl.web.SdmxWebAuthenticator;
import sdmxdl.web.SdmxWebSource;

import java.net.PasswordAuthentication;
import java.util.concurrent.ConcurrentHashMap;

@lombok.AllArgsConstructor
final class CachedAuthenticator implements SdmxWebAuthenticator {

    @lombok.NonNull
    private final SdmxWebAuthenticator delegate;

    @lombok.NonNull
    private final ConcurrentHashMap<SdmxWebSource, PasswordAuthentication> cache;

    @Override
    public PasswordAuthentication getPasswordAuthentication(SdmxWebSource source) {
        return cache.computeIfAbsent(source, delegate::getPasswordAuthentication);
    }

    @Override
    public void invalidate(SdmxWebSource source) {
        cache.remove(source);
        delegate.invalidate(source);
    }
}
