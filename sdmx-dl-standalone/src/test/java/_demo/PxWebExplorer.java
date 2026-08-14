package _demo;

import sdmxdl.provider.Explorer;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;

import java.io.IOException;

public class PxWebExplorer {

    @nbbrd.design.Demo
    public static void main(String[] args) throws IOException {

        System.setProperty("enablePxWebDriver", "true");

        // NOTE: beware of cache!
        SdmxWebManager manager = SdmxWebManager
                .ofServiceLoader()
                .toBuilder()
                .onEvent(SdmxWebManager::printEvent)
                .onError(SdmxWebManager::printError)
                .build();

        Explorer.printStylish(System.out, Explorer.explore(manager, PxWebExplorer::isPxWebSource, Explorer.Options.DEFAULT), true);
    }

    private static boolean isPxWebSource(WebSource source) {
        return !source.isAlias() && source.getDriver().equals("PX_PXWEB");
    }
}
