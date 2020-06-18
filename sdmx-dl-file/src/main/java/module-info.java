module sdmxdl.file {

    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires sdmxdl.util;
    requires sdmxdl.util.xml;

    requires transitive sdmxdl.api;
}