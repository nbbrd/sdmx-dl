package internal.sdmxdl.ri.web.drivers;

import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.SdmxConnection;
import sdmxdl.file.SdmxFileConnection;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.util.Property;
import sdmxdl.util.web.SdmxWebDriverSupport;
import sdmxdl.web.SdmxWebConnection;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@ServiceProvider(SdmxWebDriver.class)
public final class FileDriver implements SdmxWebDriver {

    private final SdmxFileManager fileManager = SdmxFileManager.ofServiceLoader();

    @Override
    public @NonNull String getName() {
        return "ri:file";
    }

    @Override
    public int getRank() {
        return NATIVE_RANK;
    }

    @Override
    public @NonNull SdmxWebConnection connect(@NonNull SdmxWebSource source, @NonNull SdmxWebContext context) throws IOException, IllegalArgumentException {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(context, "context");
        SdmxWebDriverSupport.checkSource(source, getName());

        return new WebOverFileConnection(fileManager.getConnection(toFileSource(source)), getName());
    }

    @Override
    public @NonNull Collection<SdmxWebSource> getDefaultSources() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull Collection<String> getSupportedProperties() {
        return Collections.singletonList(STRUCTURE_PROPERTY);
    }

    private static SdmxFileSource toFileSource(SdmxWebSource source) throws IOException {
        return SdmxFileSource
                .builder()
                .data(toFile(source.getEndpoint()))
                .structure(getStructure(source.getProperties()))
                .dialect(source.getDialect())
                .build();
    }

    @VisibleForTesting
    static File toFile(URL endpoint) throws IOException {
        try {
            return new File(endpoint.toURI());
        } catch (URISyntaxException | IllegalArgumentException ex) {
            throw new IOException("Invalid file name: '" + endpoint + "'", ex);
        }
    }

    private static final String STRUCTURE_PROPERTY = "structure";
    private static final File DEFAULT_STRUCTURE = null;

    private static File getStructure(Map<?, ?> o) {
        return Property.get(STRUCTURE_PROPERTY, DEFAULT_STRUCTURE, o, Parser.onFile());
    }

    @VisibleForTesting
    @lombok.RequiredArgsConstructor
    static final class WebOverFileConnection implements SdmxWebConnection {

        @lombok.experimental.Delegate(types = SdmxConnection.class)
        @lombok.NonNull
        private final SdmxFileConnection delegate;

        @lombok.NonNull
        private final String driver;

        @Override
        public @NonNull Duration ping() {
            return Duration.ZERO;
        }

        @Override
        public @NonNull String getDriver() {
            return driver;
        }
    }
}
