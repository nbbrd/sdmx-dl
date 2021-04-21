module sdmxdl.util.csv {

    requires static org.checkerframework.checker.qual;
    requires static lombok;
    requires static nbbrd.design;

    requires transitive sdmxdl.api;
    requires transitive nbbrd.io.base;
    requires transitive nbbrd.picocsv;

    exports sdmxdl.csv;
}