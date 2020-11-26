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
package sdmxdl.xml;

import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import sdmxdl.web.SdmxWebSource;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public final class XmlWebSource {

    public static Xml.Parser<List<SdmxWebSource>> getParser() {
        return PARSER;
    }

    private static final Xml.Parser<List<SdmxWebSource>> PARSER = Stax.StreamParser.valueOf(XmlWebSource::parse);

    private static List<SdmxWebSource> parse(XMLStreamReader reader) throws XMLStreamException {
        List<SdmxWebSource> result = new ArrayList<>();
        SdmxWebSource.Builder item = SdmxWebSource.builder();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "source":
                            item = SdmxWebSource.builder();
                            break;
                        case "name":
                            item.name(reader.getElementText());
                            break;
                        case "description":
                            item.description(reader.getElementText());
                            break;
                        case "driver":
                            item.driver(reader.getElementText());
                            break;
                        case "dialect":
                            item.dialect(reader.getElementText());
                            break;
                        case "endpoint":
                            item.endpointOf(reader.getElementText());
                            break;
                        case "property":
                            item.property(reader.getAttributeValue(null, "key"), reader.getAttributeValue(null, "value"));
                            break;
                        case "alias":
                            item.alias(reader.getElementText());
                            break;
                        case "website":
                            item.websiteOf(reader.getElementText());
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "source":
                            result.add(item.build());
                            break;
                    }
                    break;
            }
        }
        return result;
    }
}
