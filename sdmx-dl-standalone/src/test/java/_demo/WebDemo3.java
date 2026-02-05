package _demo;

import sdmxdl.DatabaseRequest;
import sdmxdl.Provider;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;

import java.io.IOException;
import java.util.stream.IntStream;

public class WebDemo3 {

    @nbbrd.design.Demo
    public static void main(String[] args) throws IOException {

        Provider<WebSource> salsa = SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .onEvent(SdmxWebManager::printEvent)
                .onError(SdmxWebManager::printError)
                .caching(WebCaching.noOp())
                .build()
                .usingName("ECB");

        IntStream.range(0, 10)
                .parallel()
                .forEach(index -> {
                    long start = System.currentTimeMillis();
                    try {
                        System.out.println(salsa.getFlows(DatabaseRequest.builder().build()).size());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    long end = System.currentTimeMillis();
                    System.out.println(index + " took " + (end - start) + " ms");
                });
    }
}
