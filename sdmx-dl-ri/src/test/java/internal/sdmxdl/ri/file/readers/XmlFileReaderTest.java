package internal.sdmxdl.ri.file.readers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.SdmxFileContext;
import sdmxdl.samples.SdmxSource;
import sdmxdl.tck.SdmxFileReaderAssert;
import sdmxdl.xml.XmlFileSource;

import java.io.File;
import java.io.IOException;

public class XmlFileReaderTest {

    @Test
    public void testCompliance() throws IOException {
        File compact21 = temp.newFile("valid.xml");
        SdmxSource.OTHER_COMPACT21.copyTo(compact21);

        SdmxFileSource validSource = SdmxFileSource.builder().data(compact21).build();
        String validName = XmlFileSource.getFormatter().formatToString(validSource);
        SdmxFileSource invalidSource = SdmxFileSource.builder().data(temp.newFile("invalid.csv")).build();
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

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
}
