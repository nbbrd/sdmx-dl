module sdmxdl.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires java.logging;

    exports sdmxdl;
    exports sdmxdl.ext;
    exports sdmxdl.file;
    exports sdmxdl.file.spi;
    exports sdmxdl.web;
    exports sdmxdl.web.spi;

    uses sdmxdl.file.spi.FileCaching;
    uses sdmxdl.file.spi.FileReader;
    uses sdmxdl.web.spi.Networking;
    uses sdmxdl.web.spi.WebAuthenticator;
    uses sdmxdl.web.spi.WebCaching;
    uses sdmxdl.web.spi.WebDriver;
    uses sdmxdl.web.spi.WebMonitoring;
}