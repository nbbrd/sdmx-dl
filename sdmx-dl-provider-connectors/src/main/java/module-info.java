import sdmxdl.provider.connectors.drivers.*;
import sdmxdl.web.spi.Driver;

module sdmxdl.provider.connectors {
    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;
    requires sdmxdl.format.xml;
    requires sdmxdl.provider.base;
    requires sdmx;
    requires java.logging;
    requires transitive sdmxdl.api;

    provides Driver with
            BbkDriver,
            EurostatDriver,
            IloDriver,
            InegiDriver,
            InseeDriver,
            Sdmx21Driver,
            Sdmx30Driver,
            WbDriver;
}
