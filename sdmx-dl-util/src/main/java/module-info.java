import sdmxdl.ext.spi.SdmxDialect;

module sdmxdl.util {

    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires transitive sdmxdl.api;
    requires transitive nbbrd.io.base;

    exports sdmxdl.util;
    exports sdmxdl.util.ext;
    exports sdmxdl.util.parser;
    exports sdmxdl.util.web;

    provides SdmxDialect with
            internal.sdmxdl.util.parser.EcbDialect,
            internal.sdmxdl.util.parser.InseeDialect,
            internal.sdmxdl.util.parser.Sdmx20Dialect,
            internal.sdmxdl.util.parser.Sdmx21Dialect;

    uses SdmxDialect;
}