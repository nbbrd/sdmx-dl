module sdmxdl.util.xml {

    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires sdmxdl.api;
    requires sdmxdl.util;
    requires java.logging;
    requires transitive nbbrd.io.xml;

    exports be.nbb.sdmx.facade.xml;
    exports be.nbb.sdmx.facade.xml.stream;
}