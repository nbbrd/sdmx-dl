module sdmxdl.util.xml {

    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires transitive sdmxdl.api;
    requires transitive nbbrd.io.xml;

    exports sdmxdl.xml;
    exports sdmxdl.xml.stream;
}