module sdmxdl.kryo {

    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires transitive sdmxdl.api;
    requires transitive nbbrd.io.base;
    requires com.esotericsoftware.kryo.kryo5;

    exports sdmxdl.kryo;
}