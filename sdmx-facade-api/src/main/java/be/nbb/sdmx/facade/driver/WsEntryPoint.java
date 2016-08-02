/*
 * Copyright 2015 National Bank of Belgium
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
package be.nbb.sdmx.facade.driver;

import java.util.Properties;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Data
public final class WsEntryPoint {

    private String name;
    private String description;
    private String url;
    private Properties properties;

    @Nonnull
    public WsEntryPoint copy() {
        WsEntryPoint result = new WsEntryPoint();
        result.setName(name);
        result.setDescription(description);
        result.setUrl(url);
        result.setProperties(properties);
        return result;
    }

    @Nonnull
    public static WsEntryPoint of(@Nonnull String name, @Nonnull String description, @Nonnull String url) {
        WsEntryPoint result = new WsEntryPoint();
        result.setName(name);
        result.setDescription(description);
        result.setUrl(url);
        result.setProperties(new Properties());
        return result;
    }

    @Nonnull
    public static WsEntryPoint of(@Nonnull String name, @Nonnull String description, @Nonnull String url, @Nonnull Properties p) {
        WsEntryPoint result = new WsEntryPoint();
        result.setName(name);
        result.setDescription(description);
        result.setUrl(url);
        result.setProperties(p);
        return result;
    }
}
