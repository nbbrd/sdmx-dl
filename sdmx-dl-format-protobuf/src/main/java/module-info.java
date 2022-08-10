module sdmxdl.format.protobuf {

    requires static lombok;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.format.util;
    requires transitive com.google.protobuf;

    exports sdmxdl.format.protobuf;
}