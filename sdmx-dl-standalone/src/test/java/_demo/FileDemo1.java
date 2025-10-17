package _demo;

import sdmxdl.KeyRequest;
import sdmxdl.file.SdmxFileManager;
import tests.sdmxdl.format.xml.SdmxXmlSources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

public class FileDemo1 {

    @nbbrd.design.Demo
    public static void main(String[] args) throws IOException {

        SdmxFileManager.ofServiceLoader()
                .usingFile(dataFile())
                .getData(KeyRequest
                        .builder()
                        .flowOf("data")
                        .keyOf("A.DEU.1.0.319.0.UBLGE")
                        .build())
                .forEach(series -> System.out.printf(Locale.ROOT, "%s: %d obs%n", series.getKey(), series.getObs().size()));

    }

    private static File dataFile() throws IOException {
        File result = Files.createTempFile("ecb", ".xml").toFile();
        result.deleteOnExit();
        SdmxXmlSources.ECB_DATA.copyTo(result);
        return result;
    }
}
