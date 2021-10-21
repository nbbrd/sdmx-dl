package internal.sdmxdl.ri.file.readers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.SdmxFileContext;
import sdmxdl.samples.SdmxSource;
import sdmxdl.tck.file.SdmxFileReaderAssert;
import sdmxdl.xml.XmlFileSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class XmlFileReaderTest {

    @Test
    public void testCompliance(@TempDir Path temp) throws IOException {
        File compact21 = temp.resolve("valid.xml").toFile();
        SdmxSource.OTHER_COMPACT21.copyTo(compact21);

        SdmxFileSource validSource = SdmxFileSource.builder().data(compact21).build();
        String validName = XmlFileSource.getFormatter().formatToString(validSource);
        SdmxFileSource invalidSource = SdmxFileSource.builder().data(temp.resolve("invalid.csv").toFile()).build();
        String invalidName = "invalid.csv";

        SdmxFileReaderAssert.assertCompliance(
                new XmlFileReader(),
                SdmxFileReaderAssert.Sample
                        .builder()
                        .context(SdmxFileContext.builder().build())
                        .validSource(validSource)
                        .validName(validName)
                        .invalidSource(invalidSource)
                        .invalidName(invalidName)
                        .build()
        );
    }
}
