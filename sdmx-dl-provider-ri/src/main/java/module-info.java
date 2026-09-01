import internal.util.credentials.SwingPasswordPrompt;
import internal.util.credentials.WindowsPasswordPrompt;
import internal.util.credentials.WindowsVaultService;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.file.spi.Reader;
import sdmxdl.provider.ri.authenticators.BasicAuthenticator;
import sdmxdl.provider.ri.authenticators.MsalAuthenticator;
import sdmxdl.provider.ri.caching.RiCaching;
import sdmxdl.provider.ri.drivers.FileRiDriver;
import sdmxdl.provider.ri.drivers.MockRiDriver;
import sdmxdl.provider.ri.drivers.RngRiDriver;
import sdmxdl.provider.ri.drivers.Sdmx21RiDriver;
import sdmxdl.provider.ri.monitors.UpptimeMonitor;
import sdmxdl.provider.ri.monitors.UptimeRobotMonitor;
import sdmxdl.provider.ri.networking.RiNetworking;
import sdmxdl.provider.ri.readers.XmlReader;
import sdmxdl.provider.ri.registry.RiRegistry;
import sdmxdl.provider.ri.spi.PasswordPrompt;
import sdmxdl.provider.ri.spi.VaultService;
import sdmxdl.web.spi.*;

module sdmxdl.provider.ri {
    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;
    requires sdmxdl.format.csv;
    requires sdmxdl.format.xml;
    requires sdmxdl.format.kryo;
    requires sdmxdl.provider.base;
    requires com.github.tuupertunut.powershelllibjava;
    requires com.google.gson;
    requires java.logging;
    requires nl.altindag.ssl;
    requires nbbrd.io.curl;
    requires nbbrd.io.http;
    requires nbbrd.net.proxy;
    requires com.microsoft.aad.msal4j;
    requires java.desktop;

    exports sdmxdl.provider.ri.drivers to
            sdmxdl.provider.dialects,
            sdmxdl.provider.px;
    exports sdmxdl.provider.ri.http to
            sdmxdl.provider.dialects,
            sdmxdl.provider.px;
    exports sdmxdl.provider.ri.spi;

    provides Driver with
            FileRiDriver,
            MockRiDriver,
            RngRiDriver,
            Sdmx21RiDriver;
    provides Reader with
            XmlReader;
    provides Authenticator with
            MsalAuthenticator,
            BasicAuthenticator;
    provides Monitor with
            UpptimeMonitor,
            UptimeRobotMonitor;
    provides Networking with
            RiNetworking;
    provides FileCaching with
            RiCaching;
    provides WebCaching with
            RiCaching;
    provides Registry with
            RiRegistry;
    provides PasswordPrompt with
            SwingPasswordPrompt,
            WindowsPasswordPrompt;
    provides VaultService with
            WindowsVaultService;

    uses PasswordPrompt;
    uses VaultService;

    opens sdmxdl.provider.ri.monitors to
            com.google.gson;
}
