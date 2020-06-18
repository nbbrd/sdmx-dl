module sdmxdl.tck {

    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires nbbrd.io.base;

    requires transitive sdmxdl.api;
    requires transitive org.assertj.core;

    exports sdmxdl.tck;
    exports sdmxdl.samples;
}