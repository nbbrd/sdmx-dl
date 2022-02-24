package tests.sdmxdl.web;

import sdmxdl.Connection;
import sdmxdl.ext.SdmxException;
import sdmxdl.DataRepository;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@lombok.Builder(toBuilder = true)
public final class MockedDriver implements WebDriver {

    @lombok.Getter
    @lombok.Builder.Default
    private final String name = "mockedDriver";

    @lombok.Getter
    @lombok.Builder.Default
    private final int rank = WebDriver.UNKNOWN;

    @lombok.Getter
    @lombok.Builder.Default
    private final boolean available = false;

    @lombok.Singular
    private final List<DataRepository> repos;

    @lombok.Getter
    @lombok.Singular
    private final List<SdmxWebSource> defaultSources;

    @lombok.Getter
    @lombok.Singular
    private final Collection<String> supportedProperties;

    @Override
    public Connection connect(SdmxWebSource source, WebContext context) throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(context);
        checkSource(source);

        return repos.stream()
                .filter(repo -> repo.getName().equals(source.getEndpoint().toString()))
                .map(DataRepository::asConnection)
                .findFirst()
                .orElseThrow(() -> SdmxException.missingSource(source.toString(), SdmxWebSource.class));
    }

    private void checkSource(SdmxWebSource source) throws IllegalArgumentException {
        if (!source.getDriver().equals(name)) {
            throw new IllegalArgumentException(source.getDriver());
        }
    }

    public static final class Builder {

        public Builder generateSources(String driver, DataRepository repo) {
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
