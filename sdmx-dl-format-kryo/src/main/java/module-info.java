module sdmxdl.format.kryo {

    requires static lombok;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.api;
    requires transitive nbbrd.io.base;
    requires transitive com.esotericsoftware.kryo.kryo5;

    exports sdmxdl.kryo;
}