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
package internal.file;

import static internal.file.SdmxDecoder.DataType.COMPACT20;
import static internal.file.SdmxDecoder.DataType.COMPACT21;
import static internal.file.SdmxDecoder.DataType.GENERIC20;
import static internal.file.SdmxDecoder.DataType.GENERIC21;
import static internal.file.SdmxDecoder.DataType.UNKNOWN;
import java.io.IOException;
import java.io.Reader;
import javax.xml.stream.XMLInputFactory;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Philippe Charles
 */
final class DataTypeProbe {

    private static final String NS_10 = "http://www.SDMX.org/resources/SDMXML/schemas/v1_0/message";
    private static final String NS_20 = "http://www.SDMX.org/resources/SDMXML/schemas/v2_0/message";
    private static final String NS_21 = "http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message";

    public static SdmxDecoder.DataType probeDataType(XMLInputFactory factory, Reader stream) throws IOException {
        try {
            XMLStreamReader reader = factory.createXMLStreamReader(stream);
            try {
                return probeDataType(reader);
            } finally {
                reader.close();
            }
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    private static SdmxDecoder.DataType probeDataType(XMLStreamReader reader) throws XMLStreamException {
        int level = 0;
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    level++;
                    if (level == 2 && reader.getLocalName().equals("Header")) {
                        switch (reader.getNamespaceURI()) {
                            case NS_10:
                                return UNKNOWN;
                            case NS_20:
                                while (reader.hasNext()) {
                                    switch (reader.next()) {
                                        case START_ELEMENT:
                                            level++;
                                            if (level == 3 && reader.getLocalName().equals("KeyFamilyRef")) {
                                                return GENERIC20;
                                            }
                                            break;
                                        case END_ELEMENT:
                                            level--;
                                            break;
                                    }
                                }
                                return COMPACT20;
                            case NS_21:
                                while (reader.hasNext()) {
                                    switch (reader.next()) {
                                        case START_ELEMENT:
                                            level++;
                                            if (level == 4 && reader.getLocalName().equals("SeriesKey")) {
                                                return GENERIC21;
                                            }
                                            break;
                                        case END_ELEMENT:
                                            level--;
                                            break;
                                    }
                                }
                                return COMPACT21;
                        }
                    }
                    break;
                case END_ELEMENT:
                    level--;
                    break;
            }
        }
        return UNKNOWN;
    }
}
