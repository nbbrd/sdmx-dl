/*
 * Copyright 2017 National Bank of Belgium
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
package sdmxdl.web.spi;

import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.SdmxCache;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.web.SdmxWebAuthenticator;
import sdmxdl.web.SdmxWebListener;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.net.ProxySelector;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class SdmxWebContext {

    @lombok.NonNull
    @lombok.Builder.Default
    LanguagePriorityList languages = LanguagePriorityList.ANY;

    @lombok.NonNull
    @lombok.Builder.Default
    ProxySelector proxySelector = ProxySelector.getDefault();

    @lombok.NonNull
    @lombok.Builder.Default
    SSLSocketFactory sslSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();

    @lombok.NonNull
    @lombok.Builder.Default
    HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

    @lombok.NonNull
    @lombok.Builder.Default
    SdmxCache cache = SdmxCache.noOp();

    @lombok.NonNull
    @lombok.Singular
    List<SdmxDialect> dialects;

    @lombok.NonNull
    @lombok.Builder.Default
    SdmxWebListener eventListener = SdmxWebListener.getDefault();

    @lombok.NonNull
    @lombok.Builder.Default
    SdmxWebAuthenticator authenticator = SdmxWebAuthenticator.noOp();
}
