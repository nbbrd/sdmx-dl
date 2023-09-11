package _test;

import sdmxdl.provider.ri.drivers.FileRiDriver;
import sdmxdl.web.WebSource;
import sdmxdl.format.xml.XmlWebSource;
import tests.sdmxdl.format.xml.SdmxXmlSources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.util.Collections.singletonList;

@lombok.experimental.UtilityClass
public class FileSample {

    public List<String> readAll(File file) throws IOException {
        return Files.readAllLines(file.toPath());
    }

    public static File create(Path temp) throws IOException {
        File data = Files.createFile(temp.resolve("data.xml")).toFile();
        SdmxXmlSources.ECB_DATA.copyTo(data);

        File struct = Files.createFile(temp.resolve("struct.xml")).toFile();
        SdmxXmlSources.ECB_DATA_STRUCTURE.copyTo(struct);

        File source = Files.createFile(temp.resolve("source.xml")).toFile();
        XmlWebSource.getFormatter().formatFile(singletonList(sourceOf("sample", data, struct)), source);

        return source;
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
