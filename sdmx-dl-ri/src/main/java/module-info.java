module sdmxdl.web.ri {

    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires sdmxdl.api;
    requires sdmxdl.util;
    requires sdmxdl.util.xml;
    requires nbbrd.io.base;
    requires java.logging;

    provides sdmxdl.web.spi.SdmxWebDriver with
            internal.sdmxdl.ri.drivers.AbsDriver2,
            internal.sdmxdl.ri.drivers.DotStatDriver2,
            internal.sdmxdl.ri.drivers.NbbDriver2,
            internal.sdmxdl.ri.drivers.Sdmx21Driver2,
            internal.sdmxdl.ri.drivers.WbDriver2;
}