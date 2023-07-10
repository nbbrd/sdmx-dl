module sdmxdl.provider.ri {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires sdmxdl.format.csv;
    requires sdmxdl.format.xml;
    requires sdmxdl.provider.base;
    requires com.github.tuupertunut.powershelllibjava;
    requires com.google.gson;
    requires java.logging;

    provides sdmxdl.web.spi.WebDriver with
            internal.sdmxdl.provider.ri.web.drivers.BbkDriver,
            internal.sdmxdl.provider.ri.web.drivers.DotStatDriver2,
            internal.sdmxdl.provider.ri.web.drivers.EurostatDriver2,
            internal.sdmxdl.provider.ri.web.drivers.FileDriver,
            internal.sdmxdl.provider.ri.web.drivers.ImfDriver2,
            internal.sdmxdl.provider.ri.web.drivers.InseeDriver2,
            internal.sdmxdl.provider.ri.web.drivers.NbbDriver2,
            internal.sdmxdl.provider.ri.web.drivers.PxWebDriver,
            internal.sdmxdl.provider.ri.web.drivers.RngDriver,
            internal.sdmxdl.provider.ri.web.drivers.Sdmx21Driver2,
            internal.sdmxdl.provider.ri.web.drivers.StatCanDriver;

    provides sdmxdl.file.spi.FileReader with
            internal.sdmxdl.provider.ri.file.readers.XmlReader;

    provides sdmxdl.web.spi.WebAuthenticator with
            internal.sdmxdl.provider.ri.web.authenticators.WinPasswordVaultAuthenticator;

    provides sdmxdl.web.spi.WebMonitoring with
            internal.sdmxdl.provider.ri.web.monitors.UpptimeMonitoring,
            internal.sdmxdl.provider.ri.web.monitors.UptimeRobotMonitoring;

    opens internal.sdmxdl.provider.ri.web.monitors to com.google.gson;
}