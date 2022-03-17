module sdmxdl.provider.connectors {

    requires static lombok;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires sdmxdl.format.xml;
    requires sdmxdl.provider.util;
    requires it.bancaditalia.oss.sdmx;
    requires java.logging;

    requires transitive sdmxdl.api;

    provides sdmxdl.web.spi.WebDriver with
            internal.sdmxdl.connectors.drivers.AbsDriver,
            internal.sdmxdl.connectors.drivers.EurostatDriver,
            internal.sdmxdl.connectors.drivers.ImfDriver,
            internal.sdmxdl.connectors.drivers.InseeDriver,
            internal.sdmxdl.connectors.drivers.NbbDriver,
            internal.sdmxdl.connectors.drivers.OecdDriver,
            internal.sdmxdl.connectors.drivers.Sdmx20Driver,
            internal.sdmxdl.connectors.drivers.Sdmx21Driver,
            internal.sdmxdl.connectors.drivers.SeDriver,
            internal.sdmxdl.connectors.drivers.UisDriver;
}