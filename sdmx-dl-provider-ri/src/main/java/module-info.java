module sdmxdl.web.ri {

    requires static nbbrd.service;
    requires static nbbrd.design;
    requires static org.checkerframework.checker.qual;
    requires static lombok;

    requires sdmxdl.util;
    requires sdmxdl.util.xml;
    requires com.github.tuupertunut.powershelllibjava;
    requires nbbrd.picocsv;
    requires com.google.gson;
    requires java.logging;

    requires transitive sdmxdl.api;

    provides sdmxdl.web.spi.SdmxWebDriver with
            internal.sdmxdl.ri.web.drivers.AbsDriver2,
            internal.sdmxdl.ri.web.drivers.BbkDriver,
            internal.sdmxdl.ri.web.drivers.DotStatDriver2,
            internal.sdmxdl.ri.web.drivers.EurostatDriver2,
            internal.sdmxdl.ri.web.drivers.FileDriver,
            internal.sdmxdl.ri.web.drivers.InseeDriver2,
            internal.sdmxdl.ri.web.drivers.NbbDriver2,
            internal.sdmxdl.ri.web.drivers.RngDriver,
            internal.sdmxdl.ri.web.drivers.Sdmx21Driver2;

    provides sdmxdl.file.spi.SdmxFileReader with
            internal.sdmxdl.ri.file.readers.XmlFileReader;

    provides sdmxdl.web.spi.SdmxWebAuthenticator with
            internal.sdmxdl.ri.web.authenticators.WinPasswordVaultAuthenticator;

    provides internal.util.http.HttpURLConnectionFactory with
            internal.util.http.curl.CurlHttpURLConnectionFactory;

    uses internal.util.http.HttpURLConnectionFactory;

    provides sdmxdl.web.spi.SdmxWebMonitoring with
            internal.sdmxdl.ri.web.monitors.Upptime,
            internal.sdmxdl.ri.web.monitors.UptimeRobot;

    opens internal.sdmxdl.ri.web.monitors to com.google.gson;
}