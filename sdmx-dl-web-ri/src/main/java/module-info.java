module sdmxdl.web.ri {

    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires sdmxdl.api;
    requires sdmxdl.util;
    requires sdmxdl.util.xml;
    requires nbbrd.io.base;
    requires java.logging;

    provides be.nbb.sdmx.facade.web.spi.SdmxWebDriver with
            internal.web.drivers.AbsDriver2,
            internal.web.drivers.DotStatDriver2,
            internal.web.drivers.NbbDriver2,
            internal.web.drivers.Sdmx21Driver2,
            internal.web.drivers.WbDriver2;
}