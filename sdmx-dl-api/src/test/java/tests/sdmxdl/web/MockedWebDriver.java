package tests.sdmxdl.web;

import sdmxdl.repo.SdmxRepository;
import sdmxdl.web.SdmxWebConnection;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@lombok.RequiredArgsConstructor
@lombok.Builder(toBuilder = true)
public final class MockedWebDriver implements SdmxWebDriver {

    @lombok.Getter
    private final String name;

    @lombok.Getter
    private final int rank;

    @lombok.Getter
    private final boolean available;

    @lombok.Singular
    private final Map<URI, SdmxRepository> repos;

    @lombok.Singular
    private final List<SdmxWebSource> sources;

    @lombok.Singular
    private final List<String> supportedProperties;

    @Override
    public SdmxWebConnection connect(SdmxWebSource source, SdmxWebContext context) throws IOException {
        return connect(source.getEndpoint());
    }

    @Override
    public Collection<SdmxWebSource> getDefaultSources() {
        return sources;
    }

    @Override
    public Collection<String> getSupportedProperties() {
        return supportedProperties;
    }

    private SdmxWebConnection connect(URI endpoint) throws IOException {
        SdmxRepository result = repos.get(endpoint);
        if (result != null) {
            return new MockedWebConnection(name, result.asConnection());
        }
        throw new IOException(endpoint.toString());
    }
}
