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
package internal.file.xml;

import be.nbb.sdmx.facade.DataStructure;
import static internal.file.SdmxDecoder.DataType.*;
import static be.nbb.sdmx.facade.parser.Freqs.TIME_FORMAT_CONCEPT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import be.nbb.util.Stax;

/**
 *
 * @author Philippe Charles
 */
final class DataStructureDecoder {

    public static Stax.Parser<DataStructure> generic20() {
        return Stax.Parser.of(DataStructureDecoder::generic20);
    }

    public static Stax.Parser<DataStructure> compact20() {
        return Stax.Parser.of(DataStructureDecoder::compact20);
    }

    public static Stax.Parser<DataStructure> generic21() {
        return Stax.Parser.of(DataStructureDecoder::generic21);
    }

    public static Stax.Parser<DataStructure> compact21() {
        return Stax.Parser.of(DataStructureDecoder::compact21);
    }

    private static boolean isTagMatch(XMLStreamReader r, String tag) {
        return r.getLocalName().endsWith(tag);
    }

    //<editor-fold defaultstate="collapsed" desc="Generic20">
    private static DataStructure generic20(XMLStreamReader reader) throws XMLStreamException {
        CustomDataStructureBuilder builder = new CustomDataStructureBuilder().fileType(GENERIC20);
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (isTagMatch(reader, "DataSet")) {
                        generic20DataSet(reader, builder);
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
                    if (isTagMatch(reader, "KeyFamilyRef")) {
                        builder.refId(reader.getElementText());
                    } else if (isTagMatch(reader, "Series")) {
                        generic20Series(reader, builder);
                    }
                    break;
                case END_ELEMENT:
                    if (isTagMatch(reader, "DataSet")) {
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
                    if (isTagMatch(reader, "SeriesKey")) {
                        generic20SeriesKey(reader, builder);
                    } else if (isTagMatch(reader, "Attributes")) {
                        generic20Attributes(reader, builder);
                    }
                    break;
                case END_ELEMENT:
                    if (isTagMatch(reader, "Series")) {
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
                    if (isTagMatch(reader, "Value")) {
                        builder.dimension(reader.getAttributeValue(null, "concept"), reader.getAttributeValue(null, "value"));
                    }
                    break;
                case END_ELEMENT:
                    if (isTagMatch(reader, "SeriesKey")) {
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
                    if (isTagMatch(reader, "Value")) {
                        builder.attribute(reader.getAttributeValue(null, "concept"), reader.getAttributeValue(null, "value"));
                    }
                    break;
                case END_ELEMENT:
                    if (isTagMatch(reader, "Attributes")) {
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
        builder.refId("UNKNOWN"); // FIXME: find a way to parse/guess this information
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (isTagMatch(reader, "DataSet")) {
                        compact20DataSet(reader, builder);
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
                    if (isTagMatch(reader, "Series")) {
                        for (int i = 0; i < reader.getAttributeCount(); i++) {
                            String concept = reader.getAttributeLocalName(i);
                            if (concept.equals(TIME_FORMAT_CONCEPT)) {
                                builder.attribute(concept, reader.getAttributeValue(i));
                            } else {
                                builder.dimension(concept, reader.getAttributeValue(i));
                            }
                        }
                    }
                    break;
                case END_ELEMENT:
                    if (isTagMatch(reader, "DataSet")) {
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
                    if (isTagMatch(reader, "DataSet")) {
                        generic21DataSet(reader, builder);
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
                    if (isTagMatch(reader, "Series")) {
                        generic21Series(reader, builder);
                    }
                    break;
                case END_ELEMENT:
                    if (isTagMatch(reader, "DataSet")) {
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
                    if (isTagMatch(reader, "SeriesKey")) {
                        generic21SeriesKey(reader, builder);
                    } else if (isTagMatch(reader, "Attributes")) {
                        generic21Attributes(reader, builder);
                    }
                    break;
                case END_ELEMENT:
                    if (isTagMatch(reader, "Series")) {
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
                    if (isTagMatch(reader, "Value")) {
                        builder.dimension(reader.getAttributeValue(null, "id"), reader.getAttributeValue(null, "value"));
                    }
                    break;
                case END_ELEMENT:
                    if (isTagMatch(reader, "SeriesKey")) {
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
                    if (isTagMatch(reader, "Value")) {
                        builder.attribute(reader.getAttributeValue(null, "id"), reader.getAttributeValue(null, "value"));
                    }
                    break;
                case END_ELEMENT:
                    if (isTagMatch(reader, "Attributes")) {
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
                    if (isTagMatch(reader, "Header")) {
                        compact21Header(reader, builder);
                    } else if (isTagMatch(reader, "DataSet")) {
                        compact21DataSet(reader, builder);
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
                    if (isTagMatch(reader, "Structure")) {
                        String structureId = reader.getAttributeValue(null, "structureID");
                        String dimensionAtObservation = reader.getAttributeValue(null, "dimensionAtObservation");
                        if (structureId != null && dimensionAtObservation != null) {
                            builder.refId(structureId);
                            builder.timeDimensionId(dimensionAtObservation);
                        }

                    }
                    break;
                case END_ELEMENT:
                    if (isTagMatch(reader, "Header")) {
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
                    if (isTagMatch(reader, "Series")) {
                        for (int i = 0; i < reader.getAttributeCount(); i++) {
                            String concept = reader.getAttributeLocalName(i);
                            if (concept.equals(TIME_FORMAT_CONCEPT)) {
                                builder.attribute(concept, reader.getAttributeValue(i));
                            } else {
                                builder.dimension(concept, reader.getAttributeValue(i));
                            }
                        }

                    }
                    break;
                case END_ELEMENT:
                    if (isTagMatch(reader, "DataSet")) {
                        return;
                    }
                    break;
            }
        }
    }
    //</editor-fold>
}
