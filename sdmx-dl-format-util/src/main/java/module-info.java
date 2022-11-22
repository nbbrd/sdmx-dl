module sdmxdl.format.util {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.api;
    requires transitive nbbrd.io.base;

    exports sdmxdl.format;
    exports sdmxdl.format.time;
    exports sdmxdl.format.spi;

    uses sdmxdl.format.spi.FileFormatProvider;
}