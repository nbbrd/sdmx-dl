module sdmxdl.tck {

    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires transitive sdmxdl.api;
    requires transitive org.assertj.core;
    requires transitive nbbrd.io.base;

    exports sdmxdl.tck;
    exports sdmxdl.samples;
}