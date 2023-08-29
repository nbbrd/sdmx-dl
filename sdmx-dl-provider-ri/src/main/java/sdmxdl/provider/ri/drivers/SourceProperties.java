package sdmxdl.provider.ri.drivers;

import nbbrd.design.ReturnNew;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import sdmxdl.format.xml.XmlWebSource;
import sdmxdl.provider.PropertiesSupport;
import sdmxdl.web.SdmxWebSource;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

@lombok.experimental.UtilityClass
public class SourceProperties {

    public static final Property<File> SOURCES =
            Property.of("sdmxdl.sources", null, Parser.onFile(), Formatter.onFile());

    @ReturnNew
    public static List<SdmxWebSource> loadCustomSources() throws IOException {
        File sourcesFile = SourceProperties.SOURCES.get(key -> PropertiesSupport.getProperty(emptyMap(), key));
        return sourcesFile != null ? XmlWebSource.getParser().parseFile(sourcesFile) : emptyList();
    }
}
