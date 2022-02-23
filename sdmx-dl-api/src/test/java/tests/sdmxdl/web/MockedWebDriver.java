package tests.sdmxdl.web;

import sdmxdl.SdmxConnection;
import sdmxdl.ext.SdmxException;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@lombok.Builder(toBuilder = true)
public final class MockedWebDriver implements SdmxWebDriver {

    @lombok.Getter
    @lombok.Builder.Default
    private final String name = "mockedDriver";

    @lombok.Getter
    @lombok.Builder.Default
    private final int rank = SdmxWebDriver.UNKNOWN;

    @lombok.Getter
    @lombok.Builder.Default
    private final boolean available = false;

    @lombok.Singular
    private final List<SdmxRepository> repos;

    @lombok.Getter
    @lombok.Singular
    private final List<SdmxWebSource> defaultSources;

    @lombok.Getter
    @lombok.Singular
    private final Collection<String> supportedProperties;

    @Override
    public SdmxConnection connect(SdmxWebSource source, SdmxWebContext context) throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(context);
        checkSource(source);

        return repos.stream()
                .filter(repo -> repo.getName().equals(source.getEndpoint().toString()))
                .map(SdmxRepository::asConnection)
                .findFirst()
                .orElseThrow(() -> SdmxException.missingSource(source.toString(), SdmxWebSource.class));
    }

    private void checkSource(SdmxWebSource source) throws IllegalArgumentException {
        if (!source.getDriver().equals(name)) {
            throw new IllegalArgumentException(source.getDriver());
        }
    }

    public static final class Builder {

        public Builder generateSources(String driver, SdmxRepository repo) {
            return defaultSource(
                    SdmxWebSource
                            .builder()
                            .name(repo.getName())
                            .driver(driver)
                            .endpointOf(repo.getName())
                            .build()
            );
        }
    }
}
