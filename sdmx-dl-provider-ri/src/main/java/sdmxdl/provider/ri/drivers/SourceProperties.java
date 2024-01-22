package sdmxdl.provider.ri.drivers;

import nbbrd.design.ReturnNew;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import sdmxdl.format.WebSources;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.format.spi.FileFormat;
import sdmxdl.format.spi.Persistence;
import sdmxdl.format.spi.PersistenceLoader;
import sdmxdl.provider.PropertiesSupport;
import sdmxdl.web.WebSource;

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
                .map(Persistence::getSourcesFormat)
                .filter(FileFormat::isParsingSupported)
                .filter(format -> sourcesFile.toString().endsWith(format.getFileExtension()))
                .findFirst();
    }
}
