package sdmxdl.tck.web;

import sdmxdl.repo.SdmxRepository;
import sdmxdl.web.SdmxWebConnection;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@lombok.RequiredArgsConstructor
@lombok.Builder(toBuilder = true)
public class MockedWebDriver implements SdmxWebDriver {

    @lombok.Getter
    private final String name;

    @lombok.Getter
    private final int rank;

    @lombok.Singular
    private final Map<URL, SdmxRepository> repos;

    @lombok.Singular
    private final List<SdmxWebSource> sources;

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
        return Collections.emptyList();
    }

    private SdmxWebConnection connect(URL endpoint) throws IOException {
        SdmxRepository result = repos.get(endpoint);
        if (result != null) {
            return new MockedWebConnection(name, result.asConnection());
        }
        throw new IOException(endpoint.toString());
    }
}
