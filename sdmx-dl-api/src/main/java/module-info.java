module sdmxdl.api {

    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires java.logging;

    exports sdmxdl;
    exports sdmxdl.repo;
    exports sdmxdl.web;
    exports sdmxdl.web.spi;

    uses sdmxdl.web.spi.SdmxWebDriver;
}