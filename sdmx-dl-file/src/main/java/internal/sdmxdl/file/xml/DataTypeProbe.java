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
package internal.sdmxdl.file.xml;

import sdmxdl.xml.stream.StaxUtil;
import static internal.sdmxdl.file.SdmxDecoder.DataType.*;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static sdmxdl.xml.SdmxmlUri.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import internal.sdmxdl.file.SdmxDecoder;
import java.net.URI;
import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;

/**
 *
 * @author Philippe Charles
 */
final class DataTypeProbe {

    public static Xml.Parser<SdmxDecoder.DataType> of() {
        return Stax.StreamParser.valueOf(DataTypeProbe::probeDataType);
    }

    private static SdmxDecoder.DataType probeDataType(XMLStreamReader reader) throws XMLStreamException {
        if (StaxUtil.isNotNamespaceAware(reader)) {
            throw new XMLStreamException("Cannot probe data type");
        }

        int level = 0;
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    level++;
                    if (level == 2 && reader.getLocalName().equals("Header")) {
                        URI uri = URI.create(reader.getNamespaceURI());
                        if (NS_V10_URI.is(uri)) {
                            return UNKNOWN;
                        } else if (NS_V20_URI.is(uri)) {
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
                        } else if (NS_V21_URI.is(uri)) {
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
