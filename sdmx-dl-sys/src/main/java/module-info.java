module sdmxdl.sys {

    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires sdmxdl.api;
    requires nbbrd.picocsv;
    requires com.github.tuupertunut.powershelllibjava;
    requires nbbrd.io.base;

    exports sdmxdl.sys;
}