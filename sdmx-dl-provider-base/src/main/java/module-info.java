module sdmxdl.provider.base {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.format.base;

    exports sdmxdl.provider;
    exports sdmxdl.provider.ext;
    exports sdmxdl.provider.file;
    exports sdmxdl.provider.web;

    exports internal.http.curl to sdmxdl.provider.ri;
}