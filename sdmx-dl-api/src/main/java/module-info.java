import sdmxdl.file.spi.Reader;
import sdmxdl.web.spi.Authenticator;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.Monitor;

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
    uses Reader;
    uses sdmxdl.web.spi.Networking;
    uses Authenticator;
    uses sdmxdl.web.spi.WebCaching;
    uses Driver;
    uses Monitor;
}