module sdmxdl.web.connectors {

    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires sdmxdl.util;
    requires sdmxdl.util.xml;
    requires it.bancaditalia.oss.sdmx;
    requires java.logging;

    requires transitive sdmxdl.api;

    provides sdmxdl.web.spi.SdmxWebDriver with
            internal.sdmxdl.connectors.drivers.AbsDriver,
            internal.sdmxdl.connectors.drivers.EurostatDriver,
            internal.sdmxdl.connectors.drivers.IloDriver,
            internal.sdmxdl.connectors.drivers.ImfDriver,
            internal.sdmxdl.connectors.drivers.InseeDriver,
            internal.sdmxdl.connectors.drivers.NbbDriver,
            internal.sdmxdl.connectors.drivers.OecdDriver,
            internal.sdmxdl.connectors.drivers.Sdmx20Driver,
            internal.sdmxdl.connectors.drivers.Sdmx21Driver,
            internal.sdmxdl.connectors.drivers.SeDriver,
            internal.sdmxdl.connectors.drivers.UisDriver;
}