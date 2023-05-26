module sdmxdl.format.protobuf {

    requires static lombok;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.format.base;
    requires transitive com.google.protobuf;

    exports sdmxdl.format.protobuf;
    exports sdmxdl.format.protobuf.web;

    provides sdmxdl.format.spi.FileFormatProvider with sdmxdl.format.protobuf.ProtobufProvider;
}