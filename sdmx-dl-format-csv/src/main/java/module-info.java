module sdmxdl.format.csv {

    requires static lombok;
    requires static nbbrd.design;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.api;
    requires transitive nbbrd.io.picocsv;

    exports sdmxdl.csv;
}