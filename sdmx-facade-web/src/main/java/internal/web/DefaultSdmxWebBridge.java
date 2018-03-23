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

import be.nbb.sdmx.facade.web.SdmxWebEntryPoint;
import be.nbb.sdmx.facade.web.spi.SdmxWebBridge;
import java.net.ProxySelector;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxWebBridge.class)
public final class DefaultSdmxWebBridge implements SdmxWebBridge {

    @Override
    public ProxySelector getProxySelector(SdmxWebEntryPoint o) {
        return ProxySelector.getDefault();
    }

    @Override
    public SSLSocketFactory getSslSocketFactory(SdmxWebEntryPoint o) {
        return HttpsURLConnection.getDefaultSSLSocketFactory();
    }
}
