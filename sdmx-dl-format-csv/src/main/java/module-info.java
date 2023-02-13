module sdmxdl.format.csv {

    requires static lombok;
    requires static nbbrd.design;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.format.base;
    requires transitive nbbrd.io.picocsv;

    exports sdmxdl.format.csv;
}