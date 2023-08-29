import sdmxdl.file.spi.FileCaching;
import sdmxdl.file.spi.Reader;
import sdmxdl.provider.ri.caching.RiCaching;
import sdmxdl.provider.ri.file.readers.XmlReader;
import sdmxdl.provider.ri.web.authenticators.WinPasswordVaultAuthenticator;
import sdmxdl.provider.ri.web.drivers.FileDriver;
import sdmxdl.provider.ri.web.drivers.RngDriver;
import sdmxdl.provider.ri.web.drivers.Sdmx21Driver2;
import sdmxdl.provider.ri.web.monitors.UpptimeMonitor;
import sdmxdl.provider.ri.web.monitors.UptimeRobotMonitor;
import sdmxdl.provider.ri.web.networking.RiNetworking;
import sdmxdl.web.spi.*;

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
    requires nl.altindag.ssl;
    requires nbbrd.io.curl;
    requires nbbrd.net.proxy;

    exports sdmxdl.provider.ri.web to sdmxdl.provider.dialects, sdmxdl.provider.px;
    exports internal.util.http to sdmxdl.provider.dialects, sdmxdl.provider.px;
    exports internal.util.http.ext to sdmxdl.provider.dialects, sdmxdl.provider.px;

    provides Driver with
            FileDriver,
            RngDriver,
            Sdmx21Driver2;

    provides Reader with
            XmlReader;

    provides Authenticator with
            WinPasswordVaultAuthenticator;

    provides Monitor with
            UpptimeMonitor,
            UptimeRobotMonitor;

    provides Networking with
            RiNetworking;

    provides FileCaching with
            RiCaching;

    provides WebCaching with
            RiCaching;

    opens sdmxdl.provider.ri.web.monitors to com.google.gson;
}