package internal.sdmxdl.provider.ri.web.drivers;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import nbbrd.service.ServiceProvider;
import sdmxdl.Connection;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.provider.web.WebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URI;

@ServiceProvider
public final class FileDriver implements WebDriver {

    private static final String RI_FILE = "ri:file";

    private static final BooleanProperty ENABLE =
            BooleanProperty.of("enableFileDriver", false);

    @lombok.experimental.Delegate
    private final WebDriverSupport support = WebDriverSupport
            .builder()
            .id(RI_FILE)
            .rank(NATIVE_RANK)
            .availability(ENABLE::get)
            .connector(this::newConnection)
            .supportedPropertyOf(STRUCTURE_PROPERTY)
            .build();

    private final SdmxFileManager fileManager = SdmxFileManager.ofServiceLoader();

    private @NonNull Connection newConnection(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IOException, IllegalArgumentException {
        return fileManager
                .toBuilder()
                .languages(context.getLanguages())
                .eventListener((fileSource, message) -> context.getEventListener().accept(source, message))
                .cache(context.getCache())
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
            Property.of("structureURL", null, Parser.onURI(), Formatter.onURI());
}
