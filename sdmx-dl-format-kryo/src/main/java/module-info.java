import sdmxdl.format.kryo.KryoPersistence;
import sdmxdl.ext.Persistence;

module sdmxdl.format.kryo {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive sdmxdl.format.base;
    requires transitive com.esotericsoftware.kryo.kryo5;

    exports sdmxdl.format.kryo;

    provides Persistence with KryoPersistence;
}