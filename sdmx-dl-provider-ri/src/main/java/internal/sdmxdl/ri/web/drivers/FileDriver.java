package internal.sdmxdl.ri.web.drivers;

import nbbrd.design.VisibleForTesting;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
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
    static File toFile(URL url) throws IOException {
        if (url != null) {
            try {
                return new File(url.toURI());
            } catch (URISyntaxException | IllegalArgumentException ex) {
                throw new IOException("Invalid file name: '" + url + "'", ex);
            }
        }
        return null;
    }

    private static final Property<URL> STRUCTURE_PROPERTY =
            Property.of("structureURL", null, Parser.onURL(), Formatter.onURL());

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
