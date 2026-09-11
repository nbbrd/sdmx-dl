package _demo;

import java.io.IOException;
import java.util.Locale;
import sdmxdl.DataRequest;
import sdmxdl.web.SdmxWebManager;

public class WebDemo1 {

    @nbbrd.design.Demo
    public static void main(String[] args) throws IOException {

        SdmxWebManager.ofServiceLoader()
                .usingName("ECB")
                .getData(DataRequest.builder()
                        .flowOf("EXR")
                        .keyOf("M.CHF+USD.EUR.SP00.A")
                        .build())
                .forEach(series -> System.out.printf(
                        Locale.ROOT,
                        "%s: %d obs%n",
                        series.getKey(),
                        series.getObs().size()));
    }
}
