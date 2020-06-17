module sdmxdl.api {

    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires java.logging;

    exports be.nbb.sdmx.facade;
    exports be.nbb.sdmx.facade.repo;
    exports be.nbb.sdmx.facade.web;
    exports be.nbb.sdmx.facade.web.spi;

    uses be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
}