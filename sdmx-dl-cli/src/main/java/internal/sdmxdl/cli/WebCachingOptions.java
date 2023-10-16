package internal.sdmxdl.cli;

import picocli.CommandLine;
import sdmxdl.provider.ri.caching.RiCaching;
import sdmxdl.web.spi.WebCaching;

import java.io.File;

@lombok.Getter
@lombok.Setter
public class WebCachingOptions {

    @CommandLine.Option(
            names = {"--no-cache"},
            negatable = true,
            defaultValue = "${env:SDMXDL_CACHING_NOCACHE:-false}",
            fallbackValue = "true",
            descriptionKey = "cli.noCache"
    )
    private boolean noCache;

    @CommandLine.Option(
            names = {"--cache-folder"},
            paramLabel = "<folder>",
            descriptionKey = "cli.cacheFolder",
            hidden = true
    )
    private File cacheFolder;

    public WebCaching getWebCaching() {
        System.setProperty(RiCaching.NO_CACHE_PROPERTY.getKey(), Boolean.toString(isNoCache()));
        File cacheFolder = getCacheFolder();
        if (cacheFolder == null) {
            System.clearProperty(RiCaching.CACHE_FOLDER.getKey());
        } else {
            System.setProperty(RiCaching.CACHE_FOLDER.getKey(), cacheFolder.toString());
        }
        return new RiCaching();
    }
}
