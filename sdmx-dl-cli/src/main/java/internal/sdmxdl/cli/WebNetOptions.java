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
            defaultValue = LanguagePriorityList.ANY_KEYWORD,
            descriptionKey = "cli.sdmx.languages"
    )
    private LanguagePriorityList langs;

    @CommandLine.ArgGroup(validate = false, headingKey = "network")
    private NetworkOptions networkOptions = new NetworkOptions();

    @Override
    public SdmxWebManager loadManager() throws IOException {
        SSLFactory sslFactory = networkOptions.getSslOptions().getSSLFactory();
        return super.loadManager()
                .toBuilder()
                .languages(langs)
                .proxySelector(networkOptions.getProxyOptions().getProxySelector())
                .sslSocketFactory(sslFactory.getSslSocketFactory())
                .hostnameVerifier(sslFactory.getHostnameVerifier())
                .cache(getCache())
                .authenticator(getAuthenticator())
                .build();
    }

    private SdmxCache getCache() {
        return networkOptions.getCacheOptions().isNoCache()
                ? SdmxCache.noOp()
                : FileCache
                .builder()
                .serializer(Serializer.gzip(new KryoSerialization()))
                .onIOException(this::reportIOException)
                .build();
    }

    private SdmxWebAuthenticator getAuthenticator() {
        PasswordAuthentication user = networkOptions.getAuthOptions().getUser();
        SdmxWebAuthenticator result = !networkOptions.getAuthOptions().isNoSystemAuth() ? SdmxSystemUtil.getAuthenticatorOrNull(user, this::reportIOException) : null;
        return result != null ? result : new CachedAuthenticator(new ConsoleAuthenticator(user), new ConcurrentHashMap<>());
    }
}
