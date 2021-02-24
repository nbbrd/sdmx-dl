package _test;

import org.junit.rules.TemporaryFolder;
import sdmxdl.samples.SdmxSource;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.xml.XmlWebSource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.List;

import static java.util.Collections.singletonList;

@lombok.experimental.UtilityClass
public class FileSample {

    public List<String> readAll(File file) throws IOException {
        return Files.readAllLines(file.toPath());
    }

    public static File create(TemporaryFolder temp) throws IOException {
        File data = temp.newFile("data.xml");
        SdmxSource.ECB_DATA.copyTo(data);

        File struct = temp.newFile("struct.xml");
        SdmxSource.ECB_DATA_STRUCTURE.copyTo(struct);

        File source = temp.newFile("source.xml");
        XmlWebSource.getFormatter().formatFile(singletonList(sourceOf("sample", data, struct)), source);

        return source;
    }

    private static SdmxWebSource sourceOf(String name, File data, File struct) throws MalformedURLException {
        return SdmxWebSource
                .builder()
                .name(name)
                .driver("ri:file")
                .endpoint(data.toURI().toURL())
                .property("structureURL", struct.toURI().toURL().toString())
                .build();
    }
}
