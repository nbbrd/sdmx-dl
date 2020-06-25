module sdmxdl.web.ri {

    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires sdmxdl.util;
    requires sdmxdl.util.xml;

    requires transitive sdmxdl.api;

    provides sdmxdl.web.spi.SdmxWebDriver with
            internal.sdmxdl.ri.web.drivers.AbsDriver2,
            internal.sdmxdl.ri.web.drivers.DotStatDriver2,
            internal.sdmxdl.ri.web.drivers.NbbDriver2,
            internal.sdmxdl.ri.web.drivers.Sdmx21Driver2,
            internal.sdmxdl.ri.web.drivers.WbDriver2;

    provides sdmxdl.file.spi.SdmxFileReader with
            internal.sdmxdl.ri.file.readers.XmlFileReader;
}