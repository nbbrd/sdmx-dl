module sdmxdl.format.kryo {

    requires static lombok;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.format.base;
    requires transitive com.esotericsoftware.kryo.kryo5;

    exports sdmxdl.format.kryo;

    provides sdmxdl.ext.spi.CacheProvider with sdmxdl.format.kryo.KryoProvider;
    provides sdmxdl.format.spi.FileFormatProvider with sdmxdl.format.kryo.KryoProvider;
}