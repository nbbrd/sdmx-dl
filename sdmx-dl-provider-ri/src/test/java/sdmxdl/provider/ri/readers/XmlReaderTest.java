package sdmxdl.provider.ri.readers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sdmxdl.file.FileSource;
import sdmxdl.format.xml.XmlFileSource;
import tests.sdmxdl.file.spi.ReaderAssert;
import tests.sdmxdl.format.xml.SdmxXmlSources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class XmlReaderTest {

    @Test
    public void testCompliance(@TempDir Path temp) throws IOException {
        File compact21 = temp.resolve("valid.xml").toFile();
        SdmxXmlSources.OTHER_COMPACT21.copyTo(compact21);

        FileSource validSource = FileSource.builder().data(compact21).build();
        String validName = XmlFileSource.getFormatter().formatToString(validSource);
        FileSource invalidSource = FileSource.builder().data(temp.resolve("invalid.csv").toFile()).build();
        String invalidName = "invalid.csv";

        ReaderAssert.assertCompliance(
                new XmlReader(),
                ReaderAssert.Sample
                        .builder()
                        .context(ReaderAssert.noOpFileContext())
                        .validSource(validSource)
                        .validName(validName)
                        .invalidSource(invalidSource)
                        .invalidName(invalidName)
                        .build()
        );
    }
}
