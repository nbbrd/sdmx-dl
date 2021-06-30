
module sdmxdl.api {

    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires java.logging;

    exports sdmxdl;
    exports sdmxdl.ext;
    exports sdmxdl.ext.spi;
    exports sdmxdl.file;
    exports sdmxdl.file.spi;
    exports sdmxdl.repo;
    exports sdmxdl.web;
    exports sdmxdl.web.spi;

    uses sdmxdl.ext.spi.SdmxDialect;
    uses sdmxdl.file.spi.SdmxFileReader;
    uses sdmxdl.web.spi.SdmxWebAuthenticator;
    uses sdmxdl.web.spi.SdmxWebDriver;
    uses sdmxdl.web.spi.SdmxWebMonitoring;
}