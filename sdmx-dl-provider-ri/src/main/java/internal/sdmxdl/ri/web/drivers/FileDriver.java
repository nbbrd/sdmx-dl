package internal.sdmxdl.ri.web.drivers;

import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.SdmxConnection;
import sdmxdl.file.SdmxFileConnection;
import sdmxdl.file.SdmxFileListener;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.util.web.SdmxRestDriverSupport;
import sdmxdl.web.SdmxWebConnection;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@ServiceProvider
public final class FileDriver implements SdmxWebDriver {

    private static final String RI_FILE = "ri:file";

    private static final BooleanProperty ENABLE =
            BooleanProperty.of("enableFileDriver", false);

    private final SdmxFileManager fileManager = SdmxFileManager.ofServiceLoader();

    @Override
    public @NonNull String getName() {
        return RI_FILE;
    }

    @Override
    public int getRank() {
        return NATIVE_RANK;
    }

    @Override
    public boolean isAvailable() {
        return ENABLE.get(System.getProperties());
    }

    @Override
    public @NonNull SdmxWebConnection connect(@NonNull SdmxWebSource source, @NonNull SdmxWebContext context) throws IOException, IllegalArgumentException {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(context, "context");
        SdmxRestDriverSupport.checkSource(source, getName());

        return new WebOverFileConnection(open(source, context), getName());
    }

    @Override
    public @NonNull Collection<SdmxWebSource> getDefaultSources() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull Collection<String> getSupportedProperties() {
        return Collections.singletonList(STRUCTURE_PROPERTY.getKey());
    }

    private SdmxFileConnection open(SdmxWebSource source, SdmxWebContext context) throws IOException {
        return fileManager
                .toBuilder()
                .languages(context.getLanguages())
                .eventListener(new FileOverWebListener(source, context.getEventListener()))
                .cache(context.getCache())
                .dialects(context.getDialects())
                .build()
                .getConnection(toFileSource(source));
    }

    private static SdmxFileSource toFileSource(SdmxWebSource source) throws IOException {
        return SdmxFileSource
                .builder()
                .data(toFile(source.getEndpoint()))
                .structure(toFile(STRUCTURE_PROPERTY.get(source.getProperties())))
                .dialect(source.getDialect())
                .build();
    }

    @VisibleForTesting
    static File toFile(URI endpoint) throws IOException {
        if (endpoint != null) {
            try {
                return new File(endpoint);
            } catch (IllegalArgumentException ex) {
                throw new IOException("Invalid file name: '" + endpoint + "'", ex);
            }
        }
        return null;
    }

    private static final Property<URI> STRUCTURE_PROPERTY =
            Property.of("structureURL", null, Parser.of(text -> URI.create(text.toString())), Formatter.of(Objects::toString));

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

    @lombok.RequiredArgsConstructor
    static final class FileOverWebListener implements SdmxFileListener {

        @lombok.NonNull
        private final SdmxWebSource webSource;

        @lombok.NonNull
        private final SdmxWebListener webListener;

        @Override
        public boolean isEnabled() {
            return webListener.isEnabled();
        }

        @Override
        public void onFileSourceEvent(@NonNull SdmxFileSource source, @NonNull String message) {
            webListener.onWebSourceEvent(webSource, message);
        }
    }
}
