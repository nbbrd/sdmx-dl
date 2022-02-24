package internal.sdmxdl.ri.file.readers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.FileContext;
import tests.sdmxdl.file.FileReaderAssert;
import sdmxdl.xml.XmlFileSource;
import tests.sdmxdl.xml.SdmxXmlSources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class XmlReaderTest {

    @Test
    public void testCompliance(@TempDir Path temp) throws IOException {
        File compact21 = temp.resolve("valid.xml").toFile();
        SdmxXmlSources.OTHER_COMPACT21.copyTo(compact21);

        SdmxFileSource validSource = SdmxFileSource.builder().data(compact21).build();
        String validName = XmlFileSource.getFormatter().formatToString(validSource);
        SdmxFileSource invalidSource = SdmxFileSource.builder().data(temp.resolve("invalid.csv").toFile()).build();
        String invalidName = "invalid.csv";

        FileReaderAssert.assertCompliance(
                new XmlReader(),
                FileReaderAssert.Sample
                        .builder()
                        .context(FileContext.builder().build())
                        .validSource(validSource)
                        .validName(validName)
                        .invalidSource(invalidSource)
                        .invalidName(invalidName)
                        .build()
        );
    }
}
