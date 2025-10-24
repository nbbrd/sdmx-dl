import sdmxdl.format.protobuf.JsonPersistence;
import sdmxdl.format.protobuf.ProtobufPersistence;
import sdmxdl.ext.Persistence;

module sdmxdl.format.protobuf {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive sdmxdl.format.base;
    requires transitive com.google.protobuf;
    requires transitive com.google.protobuf.util;

    exports sdmxdl.format.protobuf;
    exports sdmxdl.format.protobuf.web;

    provides Persistence with
            JsonPersistence,
            ProtobufPersistence;
}