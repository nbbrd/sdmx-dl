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

import be.nbb.sdmx.facade.DataStructure;
import static internal.file.SdmxDecoder.DataType.*;
import static be.nbb.sdmx.facade.util.FreqUtil.TIME_FORMAT_CONCEPT;
import be.nbb.sdmx.facade.xml.stream.XMLStream;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Philippe Charles
 */
final class DataStructureDecoder {

    public static XMLStream<DataStructure> of(SdmxDecoder.DataType dataType) {
        return XMLStream.of(o -> decodeDataStructure(dataType, o));
    }

    private static DataStructure decodeDataStructure(SdmxDecoder.DataType dataType, XMLStreamReader reader) throws XMLStreamException {
        switch (dataType) {
            case GENERIC20:
                return generic20(reader);
            case COMPACT20:
                return compact20(reader);
            case GENERIC21:
                return generic21(reader);
            case COMPACT21:
                return compact21(reader);
            default:
                throw new XMLStreamException("Don't know how to handle '" + dataType + "'");
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Generic20">
    private static DataStructure generic20(XMLStreamReader reader) throws XMLStreamException {
        CustomDataStructureBuilder builder = new CustomDataStructureBuilder().fileType(GENERIC20);
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "DataSet":
                            generic20DataSet(reader, builder);
                            break;
                    }
                    break;
            }
        }
        return builder.build();
    }

    private static void generic20DataSet(XMLStreamReader reader, CustomDataStructureBuilder builder) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "KeyFamilyRef":
                            builder.refId(reader.getElementText());
                            break;
                        case "Series":
                            generic20Series(reader, builder);
                            break;
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getLocalName().equals("DataSet")) {
                        return;
                    }
                    break;
            }
        }
    }

    private static void generic20Series(XMLStreamReader reader, CustomDataStructureBuilder builder) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "SeriesKey":
                            generic20SeriesKey(reader, builder);
                            break;
                        case "Attributes":
                            generic20Attributes(reader, builder);
                            break;
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getLocalName().equals("Series")) {
                        return;
                    }
                    break;
            }
        }
    }

    private static void generic20SeriesKey(XMLStreamReader reader, CustomDataStructureBuilder builder) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Value":
                            builder.dimension(reader.getAttributeValue(null, "concept"), reader.getAttributeValue(null, "value"));
                            break;
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getLocalName().equals("SeriesKey")) {
                        return;
                    }
                    break;
            }
        }
    }

    private static void generic20Attributes(XMLStreamReader reader, CustomDataStructureBuilder builder) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Value":
                            builder.attribute(reader.getAttributeValue(null, "concept"), reader.getAttributeValue(null, "value"));
                            break;
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getLocalName().equals("Attributes")) {
                        return;
                    }
                    break;
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Compact20">
    private static DataStructure compact20(XMLStreamReader reader) throws XMLStreamException {
        CustomDataStructureBuilder builder = new CustomDataStructureBuilder().fileType(COMPACT20);
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "CompactData":
                            builder.refId("TODO");
                            break;
                        case "DataSet":
                            compact20DataSet(reader, builder);
                            break;
                    }
                    break;
            }
        }
        return builder.build();
    }

    private static void compact20DataSet(XMLStreamReader reader, CustomDataStructureBuilder builder) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Series":
                            for (int i = 0; i < reader.getAttributeCount(); i++) {
                                String concept = reader.getAttributeLocalName(i);
                                if (concept.equals(TIME_FORMAT_CONCEPT)) {
                                    builder.attribute(concept, reader.getAttributeValue(i));
                                } else {
                                    builder.dimension(concept, reader.getAttributeValue(i));
                                }
                            }
                            break;
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getLocalName().equals("DataSet")) {
                        return;
                    }
                    break;
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Generic21">
    private static DataStructure generic21(XMLStreamReader reader) throws XMLStreamException {
        CustomDataStructureBuilder builder = new CustomDataStructureBuilder().fileType(GENERIC21);
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "DataSet":
                            generic21DataSet(reader, builder);
                            break;
                    }
                    break;
            }
        }
        return builder.build();
    }

    private static void generic21DataSet(XMLStreamReader reader, CustomDataStructureBuilder builder) throws XMLStreamException {
        builder.refId(reader.getAttributeValue(null, "structureRef"));
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Series":
                            generic21Series(reader, builder);
                            break;
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getLocalName().equals("DataSet")) {
                        return;
                    }
                    break;
            }
        }
    }

    private static void generic21Series(XMLStreamReader reader, CustomDataStructureBuilder builder) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "SeriesKey":
                            generic21SeriesKey(reader, builder);
                            break;
                        case "Attributes":
                            generic21Attributes(reader, builder);
                            break;
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getLocalName().equals("Series")) {
                        return;
                    }
                    break;
            }
        }
    }

    private static void generic21SeriesKey(XMLStreamReader reader, CustomDataStructureBuilder builder) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Value":
                            builder.dimension(reader.getAttributeValue(null, "id"), reader.getAttributeValue(null, "value"));
                            break;
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getLocalName().equals("SeriesKey")) {
                        return;
                    }
                    break;
            }
        }
    }

    private static void generic21Attributes(XMLStreamReader reader, CustomDataStructureBuilder builder) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Value":
                            builder.attribute(reader.getAttributeValue(null, "id"), reader.getAttributeValue(null, "value"));
                            break;
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getLocalName().equals("Attributes")) {
                        return;
                    }
                    break;
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Compact21">
    private static DataStructure compact21(XMLStreamReader reader) throws XMLStreamException {
        CustomDataStructureBuilder builder = new CustomDataStructureBuilder().fileType(COMPACT21);
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Header":
                            compact21Header(reader, builder);
                            break;
                        case "DataSet":
                            compact21DataSet(reader, builder);
                            break;
                    }
                    break;
            }
        }
        return builder.build();
    }

    private static void compact21Header(XMLStreamReader reader, CustomDataStructureBuilder builder) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Structure":
                            String structureId = reader.getAttributeValue(null, "structureID");
                            String dimensionAtObservation = reader.getAttributeValue(null, "dimensionAtObservation");
                            if (structureId != null && dimensionAtObservation != null) {
                                builder.refId(structureId);
                                builder.timeDimensionId(dimensionAtObservation);
                            }
                            break;
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getLocalName().equals("Header")) {
                        return;
                    }
                    break;
            }
        }
    }

    private static void compact21DataSet(XMLStreamReader reader, CustomDataStructureBuilder builder) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Series":
                            for (int i = 0; i < reader.getAttributeCount(); i++) {
                                String concept = reader.getAttributeLocalName(i);
                                if (concept.equals(TIME_FORMAT_CONCEPT)) {
                                    builder.attribute(concept, reader.getAttributeValue(i));
                                } else {
                                    builder.dimension(concept, reader.getAttributeValue(i));
                                }
                            }
                            break;
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getLocalName().equals("DataSet")) {
                        return;
                    }
                    break;
            }
        }
    }
    //</editor-fold>
}
