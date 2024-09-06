import sdmxdl.format.protobuf.JsonPersistence;
import sdmxdl.format.protobuf.ProtobufPersistence;
import sdmxdl.ext.Persistence;

module sdmxdl.format.protobuf {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.format.base;
    requires transitive protobuf.java;
    requires transitive protobuf.java.util;

    exports sdmxdl.format.protobuf;
    exports sdmxdl.format.protobuf.web;

    provides Persistence with
            JsonPersistence,
            ProtobufPersistence;
}