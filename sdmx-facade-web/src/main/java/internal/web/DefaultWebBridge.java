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
package internal.web;

import be.nbb.sdmx.facade.web.SdmxWebSource;
import be.nbb.sdmx.facade.web.spi.SdmxWebBridge;
import java.net.ProxySelector;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author Philippe Charles
 */
public enum DefaultWebBridge implements SdmxWebBridge {

    INSTANCE;

    @Override
    public ProxySelector getProxySelector(SdmxWebSource o) {
        return ProxySelector.getDefault();
    }

    @Override
    public SSLSocketFactory getSslSocketFactory(SdmxWebSource o) {
        return HttpsURLConnection.getDefaultSSLSocketFactory();
    }
}
