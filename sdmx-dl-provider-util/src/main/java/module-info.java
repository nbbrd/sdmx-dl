module sdmxdl.provider.util {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.format.util;

    exports sdmxdl.provider;
    exports sdmxdl.provider.ext;
    exports sdmxdl.provider.file;
    exports sdmxdl.provider.web;

    exports internal.http.curl to sdmxdl.provider.ri;
}