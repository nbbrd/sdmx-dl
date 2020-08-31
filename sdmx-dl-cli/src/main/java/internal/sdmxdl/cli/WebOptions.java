/*
 * Copyright 2018 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package internal.sdmxdl.cli;

import nbbrd.net.proxy.SystemProxySelector;
import nl.altindag.sslcontext.SSLFactory;
import picocli.CommandLine;
import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.SdmxCache;
import sdmxdl.kryo.KryoSerialization;
import sdmxdl.util.ext.FileCache;
import sdmxdl.util.ext.Serializer;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.xml.XmlWebSource;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.Data
@SuppressWarnings("FieldMayBeFinal")
public class WebOptions {

    @CommandLine.Option(
            names = {"-v", "--verbose"},
            defaultValue = "false",
            descriptionKey = "sdmxdl.cli.verbose"
    )
    private boolean verbose;

    @CommandLine.Option(
            names = {"-s", "--sources"},
            paramLabel = "<file>",
            descriptionKey = "sdmxdl.cli.sources"
    )
    private File sourcesFile;

    @CommandLine.Option(
            names = {"-l", "--languages"},
            paramLabel = "<langs>",
            converter = LangsConverter.class,
            defaultValue = "*",
            descriptionKey = "sdmxdl.cli.languages"
    )
    private LanguagePriorityList langs;

    @CommandLine.Option(
            names = {"--no-cache"},
            defaultValue = "false",
            descriptionKey = "sdmxdl.cli.noCache"
    )
    private boolean noCache;

    @CommandLine.Option(
            names = {"--no-sys-proxy"},
            defaultValue = "false",
            descriptionKey = "sdmxdl.cli.noSysProxy"
    )
    private boolean noSysProxy;

    @CommandLine.Option(
            names = {"--no-sys-ssl"},
            defaultValue = "false",
            descriptionKey = "sdmxdl.cli.noSysSsl"
    )
    private boolean noSysSsl;

    public SdmxWebManager getManager() throws IOException {
        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .languages(langs)
                .proxySelector(getProxySelector())
                .sslSocketFactory(getSSLFactory())
                .cache(getCache())
                .eventListener(getEventListener())
                .customSources(getCustomSources())
                .build();
    }

    private ProxySelector getProxySelector() {
        return isNoSysProxy()
                ? ProxySelector.getDefault()
                : SystemProxySelector.ofServiceLoader();
    }

    private SSLSocketFactory getSSLFactory() {
        if (isNoSysSsl() || SystemTrustStore.hasStaticSslProperties(System.getProperties())) {
            return HttpsURLConnection.getDefaultSSLSocketFactory();
        }
        SSLFactory.Builder result = SSLFactory
                .builder()
                .withDefaultTrustMaterial();
        Stream.of(SystemTrustStore.values())
                .filter(o -> o.isAvailable(System.getProperties()))
                .map(SystemTrustStore::load)
                .forEach(keyStore -> result.withTrustMaterial(keyStore, new char[0]));
        return result.build().getSslContext().getSocketFactory();
    }

    private SdmxCache getCache() {
        return noCache
                ? SdmxCache.noOp()
                : FileCache
                .builder()
                .serializer(Serializer.gzip(new KryoSerialization()))
                .onIOException(this::reportIOException)
                .build();
    }

    private SdmxWebListener getEventListener() {
        return verbose
                ? new VerboseWebListener(SdmxWebListener.getDefault())
                : SdmxWebListener.getDefault();
    }

    private List<SdmxWebSource> getCustomSources() throws IOException {
        return sourcesFile != null
                ? XmlWebSource.getParser().parseFile(sourcesFile)
                : Collections.emptyList();
    }

    private void reportIOException(String message, IOException ex) {
        if (verbose) {
            System.out.println("IO: " + message + " - " + ex.getMessage());
        }
    }

    @lombok.AllArgsConstructor
    private static class VerboseWebListener implements SdmxWebListener {

        @lombok.NonNull
        private final SdmxWebListener main;

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void onSourceEvent(SdmxWebSource source, String message) {
            if (main.isEnabled()) {
                main.onSourceEvent(source, message);
            }
            System.out.println(source.getName() + ": " + message);
        }
    }
}
