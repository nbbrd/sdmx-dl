package _demo;

import sdmxdl.provider.Explorer;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import java.util.List;

public class PxWebExplorer {

    @nbbrd.design.Demo
    public static void main(String[] args) {

        System.setProperty("enablePxWebDriver", "true");

        // NOTE: beware of cache!
        SdmxWebManager manager = SdmxWebManager
                .ofServiceLoader()
                .toBuilder()
//                .onEvent(SdmxWebManager::printEvent)
//                .onError(SdmxWebManager::printError)
                .build();

        Explorer.explore(manager, PxWebExplorer::isPxWebSource).forEach(PxWebExplorer::print);
    }

    private static boolean isPxWebSource(WebSource source) {
        return !source.isAlias() && source.getDriver().equals("PX_PXWEB");
    }

    private static void print(Explorer.Status status, List<Explorer.Report> reports) {
        System.out.println("==== " + status + " ====");
        reports.forEach(r -> System.out.println("[" + r.getSource() + "] error=" + r.getError() + " message=" + r.getMessage() + " request=" + r.getRequest()));
        System.out.println();
    }
}
