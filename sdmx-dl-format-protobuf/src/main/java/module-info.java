module sdmxdl.format.protobuf {

    requires static lombok;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.format.base;
    requires transitive protobuf.java;
    requires transitive protobuf.java.util;

    exports sdmxdl.format.protobuf;
    exports sdmxdl.format.protobuf.web;

    provides sdmxdl.format.spi.FileFormatProvider with
            sdmxdl.format.protobuf.JsonProvider,
            sdmxdl.format.protobuf.ProtobufProvider;
}