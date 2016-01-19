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
package be.nbb.sdmx.facade.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.nio.charset.StandardCharsets.UTF_8;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Philippe Charles
 */
public abstract class SdmxTestResource {

    public abstract InputStream openStream() throws IOException;

    public XMLStreamReader open() throws XMLStreamException, IOException {
        return XMLInputFactory.newInstance().createXMLStreamReader(new InputStreamReader(openStream(), UTF_8));
    }

    public static final SdmxTestResource NBB_DATAFLOWS = onResource("Dataflows.xml");
    public static final SdmxTestResource NBB_DATA_STRUCTURE = onResource("DataflowStructure.xml");
    public static final SdmxTestResource NBB_DATA = onResource("TimeSeries.xml");
    public static final SdmxTestResource ECB_DATAFLOWS = onResource("EcbDataflows.xml");
    public static final SdmxTestResource ECB_DATA_STRUCTURE = onResource("EcbDataStructure.xml");
    public static final SdmxTestResource ECB_DATA = onResource("EcbDataKeys.xml");

    public static SdmxTestResource onResource(final String id) {
        return new SdmxTestResource() {
            @Override
            public InputStream openStream() throws IOException {
                return SdmxTestResource.class.getResource(id).openStream();
            }
        };
    }
}
