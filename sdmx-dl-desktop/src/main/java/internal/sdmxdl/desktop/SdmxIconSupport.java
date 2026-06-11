package internal.sdmxdl.desktop;

import lombok.RequiredArgsConstructor;
import nbbrd.desktop.favicon.DomainName;
import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.FaviconSupport;
import sdmxdl.Confidentiality;
import sdmxdl.desktop.DataSourceRef;
import sdmxdl.desktop.HasSdmxProperties;
import sdmxdl.swing.SdmxLogo;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Network;

import javax.swing.*;
import java.io.IOException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

@RequiredArgsConstructor
public final class SdmxIconSupport {

    public static SdmxIconSupport of(HasSdmxProperties<SdmxWebManager> properties) {
        return new SdmxIconSupport(properties);
    }

    private final HasSdmxProperties<SdmxWebManager> properties;

    private final FaviconSupport faviconSupport = FaviconSupport.ofServiceLoader()
            .toBuilder()
            .client(this::openConnection)
            .build();


    private URLConnection openConnection(URL url) throws IOException {
        try {
            WebSource source = WebSource.builder().id("").driver("").endpoint(url.toURI()).build();
            Network network = properties.getSdmxManager().getNetworking().getNetwork(source, null, null);
            return network.getURLConnectionFactory().openConnection(url, Proxy.NO_PROXY);
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    public Icon getIcon(DataSourceRef dataSourceRef, int size, Runnable onUpdate) {
        return getIcon(dataSourceRef.toWebSource(properties.getSdmxManager()), size, onUpdate);
    }

    public Icon getIcon(WebSource source, int size, Runnable onUpdate) {
        URL website = source.getWebsite();
        return website != null && !isForbidden(source.getConfidentiality())
                ? faviconSupport.getOrDefault(FaviconRef.of(DomainName.of(website), size), onUpdate, new SdmxLogo(size))
                : new SdmxLogo(size);
    }

    private static boolean isForbidden(Confidentiality confidentiality) {
        return confidentiality.compareTo(MAX_CONFIDENTIALITY) > 0;
    }

    private static final Confidentiality MAX_CONFIDENTIALITY = Confidentiality.PUBLIC;
}
