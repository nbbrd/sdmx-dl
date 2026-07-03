package _demo;

import sdmxdl.*;
import sdmxdl.provider.ri.http.CachingDecoration;
import sdmxdl.provider.ri.http.HttpManager;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;

import java.io.IOException;

public class HttpCacheExplorer {

    @nbbrd.design.Demo
    public static void main(String[] args) {

        HttpManager.setHttpFactory(new CachingDecoration().decorate(HttpManager.getHttpFactory()));

        SdmxWebManager manager = SdmxWebManager
                .ofServiceLoader()
                .toBuilder()
                .caching(WebCaching.noOp())
                .onEvent(ignore -> (marker, message) -> {
                    if (marker.equals("HTTP_CACHE")) System.out.println("  " + message);
                })
                .build();

        manager.getNetworking().warmupNetwork();

        for (WebSource source : manager.getDefaultSources()) {
            Provider<WebSource> provider = manager.using(source);
            try {
                DatabaseRef db = provider.getDatabases(SourceRequest.builder().build()).stream().map(Database::getRef).findFirst().orElse(DatabaseRef.NO_DATABASE);
                DatabaseRequest request = DatabaseRequest.builder().database(db).build();
                run(source, " 1/2 ", provider, request);
                run(source, " 2/2 ", provider, request);
            } catch (IOException e) {
                System.err.println(source.getId() + ": error: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private static void run(WebSource source, String x, Provider<WebSource> provider, DatabaseRequest request) throws IOException {
        long start = System.currentTimeMillis();
        System.out.println(source.getId() + x);
        provider.getFlows(request);
        System.out.println("  -> " + (System.currentTimeMillis() - start) + " ms");
    }
}
