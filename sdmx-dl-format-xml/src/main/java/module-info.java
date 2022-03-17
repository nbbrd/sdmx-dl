module sdmxdl.format.xml {

    requires static lombok;
    requires static nbbrd.design;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.api;
    requires transitive nbbrd.io.xml;

    exports sdmxdl.xml;
    exports sdmxdl.xml.stream;
}