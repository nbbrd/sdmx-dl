module sdmxdl.kryo {

    requires transitive sdmxdl.api;
    requires transitive sdmxdl.util;
    requires com.esotericsoftware.kryo;

    exports sdmxdl.kryo;
}