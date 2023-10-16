import sdmxdl.file.spi.FileCaching;
import sdmxdl.file.spi.Reader;
import sdmxdl.provider.ri.authenticators.WinPasswordVaultAuthenticator;
import sdmxdl.provider.ri.caching.RiCaching;
import sdmxdl.provider.ri.drivers.FileRiDriver;
import sdmxdl.provider.ri.drivers.RngRiDriver;
import sdmxdl.provider.ri.drivers.Sdmx21RiDriver;
import sdmxdl.provider.ri.monitors.UpptimeMonitor;
import sdmxdl.provider.ri.monitors.UptimeRobotMonitor;
import sdmxdl.provider.ri.networking.RiNetworking;
import sdmxdl.provider.ri.readers.XmlReader;
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
    requires nbbrd.io.http;
    requires nbbrd.net.proxy;

    exports sdmxdl.provider.ri.drivers to sdmxdl.provider.dialects, sdmxdl.provider.px;

    provides Driver with
            FileRiDriver,
            RngRiDriver,
            Sdmx21RiDriver;

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

    opens sdmxdl.provider.ri.monitors to com.google.gson;
}