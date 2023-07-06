import sdmxdl.ext.spi.Caching;
import sdmxdl.format.spi.Persistence;

module sdmxdl.format.kryo {

    requires static lombok;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.format.base;
    requires transitive com.esotericsoftware.kryo.kryo5;

    exports sdmxdl.format.kryo;

    provides Caching with sdmxdl.format.kryo.KryoProvider;
    provides Persistence with sdmxdl.format.kryo.KryoProvider;
}