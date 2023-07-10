import sdmxdl.file.spi.FileCaching;
import sdmxdl.web.spi.WebCaching;
import sdmxdl.format.spi.Persistence;

module sdmxdl.format.kryo {

    requires static lombok;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.format.base;
    requires transitive com.esotericsoftware.kryo.kryo5;

    exports sdmxdl.format.kryo;

    provides FileCaching with sdmxdl.format.kryo.KryoProvider;
    provides WebCaching with sdmxdl.format.kryo.KryoProvider;
    provides Persistence with sdmxdl.format.kryo.KryoProvider;
}