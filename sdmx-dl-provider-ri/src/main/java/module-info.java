import sdmxdl.provider.ri.file.readers.XmlReader;
import sdmxdl.provider.ri.web.authenticators.WinPasswordVaultAuthenticator;
import sdmxdl.provider.ri.web.drivers.*;
import sdmxdl.provider.ri.web.monitors.UpptimeMonitoring;
import sdmxdl.provider.ri.web.monitors.UptimeRobotMonitoring;
import sdmxdl.file.spi.Reader;
import sdmxdl.provider.ri.web.networking.RiNetworking;
import sdmxdl.web.spi.Authenticator;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.Monitor;
import sdmxdl.web.spi.Networking;

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

    provides Driver with
            BbkDriver,
            DotStatDriver2,
            EurostatDriver2,
            FileDriver,
            ImfDriver2,
            InseeDriver2,
            NbbDriver2,
            PxWebDriver,
            RngDriver,
            Sdmx21Driver2,
            StatCanDriver;

    provides Reader with
            XmlReader;

    provides Authenticator with
            WinPasswordVaultAuthenticator;

    provides Monitor with
            UpptimeMonitoring,
            UptimeRobotMonitoring;

    provides Networking with
            RiNetworking;

    opens sdmxdl.provider.ri.web.monitors to com.google.gson;
}