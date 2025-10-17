import sdmxdl.provider.connectors.drivers.*;
import sdmxdl.web.spi.Driver;

module sdmxdl.provider.connectors {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires sdmxdl.format.xml;
    requires sdmxdl.provider.base;
    requires it.bancaditalia.oss.sdmx;
    requires java.logging;

    requires transitive sdmxdl.api;

    provides Driver with
            EurostatDriver,
            ImfDriver,
            InseeDriver,
            NbbDriver,
            OecdDriver,
            Sdmx20Driver,
            Sdmx21Driver,
            UisDriver;
}