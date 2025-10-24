import sdmxdl.ext.Persistence;
import sdmxdl.format.xml.XmlPersistence;

module sdmxdl.format.xml {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive sdmxdl.format.base;
    requires transitive nbbrd.io.xml;

    exports sdmxdl.format.xml;

    provides Persistence with XmlPersistence;
}