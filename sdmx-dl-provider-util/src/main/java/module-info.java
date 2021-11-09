module sdmxdl.util {

    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires transitive sdmxdl.api;
    requires transitive nbbrd.io.base;
    requires nbbrd.io.xml;

    exports sdmxdl.util;
    exports sdmxdl.util.ext;
    exports sdmxdl.util.file;
    exports sdmxdl.util.parser;
    exports sdmxdl.util.web;

    provides sdmxdl.ext.spi.SdmxDialect with
            internal.sdmxdl.util.ext.EcbDialect,
            internal.sdmxdl.util.ext.InseeDialect,
            internal.sdmxdl.util.ext.Sdmx20Dialect,
            internal.sdmxdl.util.ext.Sdmx21Dialect;

    uses sdmxdl.ext.spi.SdmxDialect;
}