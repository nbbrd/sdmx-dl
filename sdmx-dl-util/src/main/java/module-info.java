module sdmxdl.util {

    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires sdmxdl.api;
    requires nbbrd.io.base;

    exports be.nbb.sdmx.facade.parser;
    exports be.nbb.sdmx.facade.parser.spi;
    exports be.nbb.sdmx.facade.util;

    uses be.nbb.sdmx.facade.parser.spi.SdmxDialect;
}