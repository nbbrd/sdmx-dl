module sdmxdl.kryo {

    requires transitive sdmxdl.api;
    requires transitive sdmxdl.util;
    requires com.esotericsoftware.kryo.kryo5;

    exports sdmxdl.kryo;
}