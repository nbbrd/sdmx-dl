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
import sdmxdl.provider.web.SdmxValidators;
import sdmxdl.provider.web.Validator;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@ServiceProvider
public final class FileDriver implements WebDriver {

    private static final String RI_FILE = "ri:file";

    private static final BooleanProperty ENABLE =
            BooleanProperty.of("enableFileDriver", false);

    private final SdmxFileManager fileManager = SdmxFileManager.ofServiceLoader();

    private final Validator<SdmxWebSource> sourceValidator = SdmxValidators.onDriverName(RI_FILE);

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
    public @NonNull Connection connect(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IOException, IllegalArgumentException {
        sourceValidator.checkValidity(source);

        return fileManager
                .toBuilder()
                .languages(context.getLanguages())
                .eventListener((fileSource, message) -> context.getEventListener().accept(source, message))
                .cache(context.getCache())
                .build()
                .getConnection(toFileSource(source));
    }

    @Override
    public @NonNull Collection<SdmxWebSource> getDefaultSources() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull Collection<String> getSupportedProperties() {
        return Collections.singletonList(STRUCTURE_PROPERTY.getKey());
    }

    @Override
    public @NonNull String getDefaultDialect() {
        return NO_DEFAULT_DIALECT;
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
}
