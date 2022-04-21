module sdmxdl.format.kryo {

    requires static lombok;
    requires static org.checkerframework.checker.qual;

    requires transitive sdmxdl.format.util;
    requires transitive com.esotericsoftware.kryo.kryo5;

    exports sdmxdl.format.kryo;
}