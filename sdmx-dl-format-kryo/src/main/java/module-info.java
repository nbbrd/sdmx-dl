module sdmxdl.format.kryo {

    requires static lombok;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.format.base;
    requires transitive com.esotericsoftware.kryo.kryo5;

    exports sdmxdl.format.kryo;

    provides sdmxdl.format.spi.FileFormatProvider with internal.sdmxdl.format.kryo.KryoProvider;
}