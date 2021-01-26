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

import nl.altindag.ssl.SSLFactory;
import picocli.CommandLine;
import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.SdmxCache;
import sdmxdl.kryo.KryoSerialization;
import sdmxdl.sys.SdmxSystemUtil;
import sdmxdl.util.ext.FileCache;
import sdmxdl.util.ext.Serializer;
import sdmxdl.web.SdmxWebAuthenticator;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class WebNetOptions extends WebOptions {

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

    @CommandLine.Mixin
    private ProxyOptions proxyOptions;

    @CommandLine.Mixin
    private SslOptions sslOptions;

    @CommandLine.Mixin
    private AuthOptions authOptions;

    @Override
    public SdmxWebManager getManager() throws IOException {
        SSLFactory sslFactory = sslOptions.getSSLFactory();
        return super.getManager()
                .toBuilder()
                .languages(langs)
                .proxySelector(proxyOptions.getProxySelector())
                .sslSocketFactory(sslFactory.getSslContext().getSocketFactory())
                .hostnameVerifier(sslFactory.getHostnameVerifier())
                .cache(getCache())
                .authenticator(getAuthenticator())
                .build();
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

    private SdmxWebAuthenticator getAuthenticator() {
        PasswordAuthentication user = authOptions.getUser();
        SdmxWebAuthenticator result = !authOptions.isNoSysAuth() ? SdmxSystemUtil.getAuthenticatorOrNull(user, this::reportIOException) : null;
        return result != null ? result : new CachedAuthenticator(new ConsoleAuthenticator(user), new ConcurrentHashMap<>());
    }
}
