package sdmxdl.provider.ri.registry;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Persistence;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.provider.PropertiesSupport;
import sdmxdl.web.WebSources;
import sdmxdl.web.spi.Registry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static nbbrd.io.text.BaseProperty.keysOf;

@DirectImpl
@ServiceProvider
public final class RiRegistry implements Registry {

    public static final File NO_SOURCES_FILE = Paths.get("").toFile();

    // Set data source definitions file
    @PropertyDefinition
    public static final Property<File> SOURCES_FILE_PROPERTY =
            Property.of("sdmxdl.registry.sourcesFile", NO_SOURCES_FILE, Parser.onFile(), Formatter.onFile());

    @Override
    public @NonNull String getRegistryId() {
        return "RI_REGISTRY";
    }

    @Override
    public int getRegistryRank() {
        return 400;
    }

    @Override
    public @NonNull WebSources getSources(
            @NonNull List<Persistence> persistences,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError) {

        Function<? super String, ? extends CharSequence> properties = key -> PropertiesSupport.getProperty(emptyMap(), key);

        File sourcesFile = SOURCES_FILE_PROPERTY.get(properties);
        if (sourcesFile == null || sourcesFile.equals(NO_SOURCES_FILE)) {
            if (onEvent != null) onEvent.accept(getRegistryId(), "Using default sources");
            return WebSources.EMPTY;
        }

        try {
            WebSources result = loadCustomSources(sourcesFile, persistences);
            if (onEvent != null)
                onEvent.accept(getRegistryId(), "Using " + result.getSources().size() + " custom sources from file '" + sourcesFile + "'");
            return result;
        } catch (IOException ex) {
            if (onError != null)
                onError.accept(getRegistryId(), "Failed to load source file '" + sourcesFile + "'", ex);
            return WebSources.EMPTY;
        }
    }

    @Override
    public @NonNull Collection<String> getRegistryProperties() {
        return keysOf(SOURCES_FILE_PROPERTY);
    }

    private static WebSources loadCustomSources(File sourcesFile, List<Persistence> persistences) throws IOException {
        return persistences
                .stream()
                .filter(persistence -> persistence.getFormatSupportedTypes().contains(WebSources.class))
                .map(persistence -> persistence.getFormat(WebSources.class))
                .filter(format -> sourcesFile.toString().endsWith(format.getFileExtension()))
                .findFirst()
                .orElseThrow(() -> new IOException("Cannot handle source file '" + sourcesFile + "'"))
                .parsePath(sourcesFile.toPath());
    }
}
