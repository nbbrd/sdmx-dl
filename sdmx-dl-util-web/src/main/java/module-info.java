module sdmxdl.util.web {

    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires sdmxdl.api;
    requires sdmxdl.util;
    requires nbbrd.io.base;

    exports internal.web;
    exports internal.util.drivers;

    provides be.nbb.sdmx.facade.parser.spi.SdmxDialect with internal.util.drivers.InseeDialect;
}