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
package sdmxdl.xml;

import java.net.URI;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
public enum SdmxmlUri {

    NS_V10_URI("http://www.SDMX.org/resources/SDMXML/schemas/v1_0/message"),
    NS_V20_URI("http://www.SDMX.org/resources/SDMXML/schemas/v2_0/message"),
    NS_V21_URI("http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message");

    private final URI uri;

    SdmxmlUri(String uri) {
        this.uri = URI.create(uri);
    }

    public boolean is(@NonNull URI found) {
        return uri.getRawSchemeSpecificPart().equalsIgnoreCase(found.getRawSchemeSpecificPart());
    }
}
