module sdmxdl.provider.connectors {

    requires static lombok;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires sdmxdl.format.xml;
    requires sdmxdl.provider.base;
    requires it.bancaditalia.oss.sdmx;
    requires java.logging;

    requires transitive sdmxdl.api;

    provides sdmxdl.web.spi.WebDriver with
            internal.sdmxdl.provider.connectors.drivers.EurostatDriver,
            internal.sdmxdl.provider.connectors.drivers.ImfDriver,
            internal.sdmxdl.provider.connectors.drivers.InseeDriver,
            internal.sdmxdl.provider.connectors.drivers.NbbDriver,
            internal.sdmxdl.provider.connectors.drivers.OecdDriver,
            internal.sdmxdl.provider.connectors.drivers.Sdmx20Driver,
            internal.sdmxdl.provider.connectors.drivers.Sdmx21Driver,
            internal.sdmxdl.provider.connectors.drivers.SeDriver,
            internal.sdmxdl.provider.connectors.drivers.UisDriver;
}