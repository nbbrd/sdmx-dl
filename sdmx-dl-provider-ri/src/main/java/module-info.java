module sdmxdl.provider.ri {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires sdmxdl.format.csv;
    requires sdmxdl.format.xml;
    requires sdmxdl.provider.util;
    requires com.github.tuupertunut.powershelllibjava;
    requires com.google.gson;
    requires java.logging;

    provides sdmxdl.web.spi.WebDriver with
            internal.sdmxdl.ri.web.drivers.AbsDriver2,
            internal.sdmxdl.ri.web.drivers.BbkDriver,
            internal.sdmxdl.ri.web.drivers.DotStatDriver2,
            internal.sdmxdl.ri.web.drivers.EurostatDriver2,
            internal.sdmxdl.ri.web.drivers.FileDriver,
            internal.sdmxdl.ri.web.drivers.InseeDriver2,
            internal.sdmxdl.ri.web.drivers.NbbDriver2,
            internal.sdmxdl.ri.web.drivers.RngDriver,
            internal.sdmxdl.ri.web.drivers.Sdmx21Driver2,
            internal.sdmxdl.ri.web.drivers.StatCanDriver;

    provides sdmxdl.file.spi.FileReader with
            internal.sdmxdl.ri.file.readers.XmlReader;

    provides sdmxdl.web.spi.WebAuthenticator with
            internal.sdmxdl.ri.web.authenticators.WinPasswordVaultAuthenticator;

    provides internal.util.http.HttpURLConnectionFactory with
            internal.util.http.curl.CurlHttpURLConnectionFactory;

    uses internal.util.http.HttpURLConnectionFactory;

    provides sdmxdl.web.spi.WebMonitoring with
            internal.sdmxdl.ri.web.monitors.UpptimeMonitoring,
            internal.sdmxdl.ri.web.monitors.UptimeRobotMonitoring;

    opens internal.sdmxdl.ri.web.monitors to com.google.gson;

    provides sdmxdl.ext.spi.Dialect with
            internal.sdmxdl.ri.ext.EcbDialect,
            internal.sdmxdl.ri.ext.InseeDialect,
            internal.sdmxdl.ri.ext.Sdmx20Dialect,
            internal.sdmxdl.ri.ext.Sdmx21Dialect;

    uses sdmxdl.ext.spi.Dialect;
}