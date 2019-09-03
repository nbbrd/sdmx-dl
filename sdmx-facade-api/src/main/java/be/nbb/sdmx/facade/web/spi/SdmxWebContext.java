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
package be.nbb.sdmx.facade.web.spi;

import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.web.SdmxWebManager;
import java.net.ProxySelector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class SdmxWebContext {

    @lombok.NonNull
    LanguagePriorityList languages;

    @lombok.NonNull
    ProxySelector proxySelector;

    @lombok.NonNull
    SSLSocketFactory sslSocketFactory;

    @lombok.NonNull
    Logger logger;

    @lombok.NonNull
    ConcurrentMap cache;

    // Fix lombok.Builder.Default bug in NetBeans
    public static Builder builder() {
        return new Builder()
                .languages(LanguagePriorityList.ANY)
                .proxySelector(ProxySelector.getDefault())
                .sslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory())
                .logger(Logger.getLogger(SdmxWebManager.class.getName()))
                .cache(new ConcurrentHashMap());
    }
}
