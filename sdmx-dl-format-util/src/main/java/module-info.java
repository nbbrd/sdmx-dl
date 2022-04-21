module sdmxdl.format.util {

    requires static lombok;
    requires static nbbrd.design;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.api;
    requires transitive nbbrd.io.base;

    exports sdmxdl.format;
}