module sdmxdl.util {

    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires transitive sdmxdl.api;
    requires transitive nbbrd.io.base;

    exports sdmxdl.util;
    exports sdmxdl.util.parser;
    exports sdmxdl.util.parser.spi;
    exports sdmxdl.util.web;

    provides sdmxdl.util.parser.spi.SdmxDialect with internal.sdmxdl.util.parser.InseeDialect;

    uses sdmxdl.util.parser.spi.SdmxDialect;
}