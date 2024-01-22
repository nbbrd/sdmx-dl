package _test;

import sdmxdl.web.WebSources;
import sdmxdl.provider.ri.drivers.FileRiDriver;
import sdmxdl.provider.ri.drivers.SourceProperties;
import sdmxdl.web.WebSource;
import tests.sdmxdl.format.xml.SdmxXmlSources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@lombok.experimental.UtilityClass
public class FileSample {

    public List<String> readAll(File file) throws IOException {
        return Files.readAllLines(file.toPath());
    }

    public static File create(Path temp) throws IOException {
        Path data = Files.createFile(temp.resolve("data.xml"));
        SdmxXmlSources.ECB_DATA.copyTo(data);

        Path struct = Files.createFile(temp.resolve("struct.xml"));
        SdmxXmlSources.ECB_DATA_STRUCTURE.copyTo(struct);

        Path source = Files.createFile(temp.resolve("source.xml"));
        SourceProperties
                .getFileFormat(source.toFile())
                .orElseThrow(IOException::new)
                .formatPath(WebSources.builder().source(sourceOf("sample", data.toFile(), struct.toFile())).build(), source);

        return source.toFile();
    }

    private static WebSource sourceOf(String name, File data, File struct) {
        return WebSource
                .builder()
                .id(name)
                .driver("ri:file")
                .endpoint(data.toURI())
                .propertyOf(FileRiDriver.STRUCTURE_URI_PROPERTY, struct.toURI())
                .build();
    }
}
