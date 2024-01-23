import sdmxdl.ext.Persistence;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.file.spi.Reader;
import sdmxdl.web.spi.*;

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

    uses FileCaching;
    uses Reader;
    uses Networking;
    uses Authenticator;
    uses WebCaching;
    uses Driver;
    uses Monitor;
    uses Persistence;
    uses Registry;
}