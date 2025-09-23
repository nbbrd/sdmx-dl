package _demo;

import sdmxdl.web.KeyRequest;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.Locale;

public class Demo1 {

    @nbbrd.design.Demo
    public static void main(String[] args) throws IOException {

        SdmxWebManager.ofServiceLoader()
                .getData(KeyRequest
                        .builder()
                        .source("ECB")
                        .flowOf("EXR")
                        .keyOf("M.CHF+USD.EUR.SP00.A")
                        .build())
                .forEach(series -> System.out.printf(Locale.ROOT, "%s: %d obs%n", series.getKey(), series.getObs().size()));

    }
}
