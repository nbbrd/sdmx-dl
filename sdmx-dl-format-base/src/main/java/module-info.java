import sdmxdl.ext.Persistence;

module sdmxdl.format.base {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive sdmxdl.api;
    requires transitive nbbrd.io.base;

    exports sdmxdl.format;
    exports sdmxdl.format.design;
    exports sdmxdl.format.time;

    uses Persistence;
}