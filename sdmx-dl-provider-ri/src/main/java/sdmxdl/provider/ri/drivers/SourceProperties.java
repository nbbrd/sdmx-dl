package sdmxdl.provider.ri.drivers;

import nbbrd.design.ReturnNew;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import sdmxdl.ext.FileFormat;
import sdmxdl.ext.PersistenceLoader;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.provider.PropertiesSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.WebSources;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

@lombok.experimental.UtilityClass
public class SourceProperties {

    @PropertyDefinition
    public static final Property<File> SOURCES_PROPERTY =
            Property.of("sdmxdl.sources", null, Parser.onFile(), Formatter.onFile());

    @ReturnNew
    public static List<WebSource> loadCustomSources() throws IOException {
        File sourcesFile = SourceProperties.SOURCES_PROPERTY.get(key -> PropertiesSupport.getProperty(emptyMap(), key));
        if (sourcesFile == null) return emptyList();
        return getFileFormat(sourcesFile)
                .orElseThrow(() -> new IOException("Cannot read source file '" + sourcesFile + "'"))
                .parsePath(sourcesFile.toPath())
                .getSources();
    }

    public static Optional<FileFormat<WebSources>> getFileFormat(File sourcesFile) {
        return PersistenceLoader.load()
                .stream()
                .filter(persistence -> persistence.isFormatSupported(WebSources.class))
                .map(persistence -> persistence.getFormat(WebSources.class))
                .filter(format -> sourcesFile.toString().endsWith(format.getFileExtension()))
                .findFirst();
    }
}
