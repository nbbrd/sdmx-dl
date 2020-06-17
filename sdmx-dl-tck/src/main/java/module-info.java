module sdmxdl.tck {

    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires sdmxdl.api;
    requires org.assertj.core;
    requires nbbrd.io.base;

    exports be.nbb.sdmx.facade.tck;
    exports be.nbb.sdmx.facade.samples;
}