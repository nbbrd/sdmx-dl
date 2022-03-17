module sdmxdl.provider.util {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.api;
    requires transitive nbbrd.io.base;

    exports sdmxdl.util;
    exports sdmxdl.util.ext;
    exports sdmxdl.util.file;
    exports sdmxdl.util.parser;
    exports sdmxdl.util.web;
}