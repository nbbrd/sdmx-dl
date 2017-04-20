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
package be.nbb.sdmx.facade.file.impl;

import be.nbb.sdmx.facade.DataStructure;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import be.nbb.sdmx.facade.file.SdmxDecoder;
import static be.nbb.sdmx.facade.file.SdmxDecoder.FileType.COMPACT20;
import static be.nbb.sdmx.facade.file.SdmxDecoder.FileType.COMPACT21;
import static be.nbb.sdmx.facade.file.SdmxDecoder.FileType.GENERIC20;
import static be.nbb.sdmx.facade.file.SdmxDecoder.FileType.GENERIC21;
import static be.nbb.sdmx.facade.file.SdmxDecoder.FileType.UNKNOWN;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 *
 * @author Philippe Charles
 */
public final class XMLStreamSdmxDecoder implements SdmxDecoder {

    private static final String NS_10 = "http://www.SDMX.org/resources/SDMXML/schemas/v1_0/message";
    private static final String NS_20 = "http://www.SDMX.org/resources/SDMXML/schemas/v2_0/message";
    private static final String NS_21 = "http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message";

    private final XMLInputFactory factory;

    public XMLStreamSdmxDecoder(XMLInputFactory factory) {
        this.factory = factory;
    }

    @Override
    public Info decode(File file) throws IOException {
        try {
            FileType fileType = probeType(file);
            return Info.of(fileType, getDataStructure(fileType, file));
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    private FileType probeType(File file) throws XMLStreamException, IOException {
        try (InputStream stream = new FileInputStream(file)) {
            XMLStreamReader reader = factory.createXMLStreamReader(stream, StandardCharsets.UTF_8.name());
            try {
                return probeType(reader);
            } finally {
                reader.close();
            }
        }
    }

    private DataStructure getDataStructure(FileType fileType, File file) throws XMLStreamException, IOException {
        try (InputStream stream = new FileInputStream(file)) {
            XMLStreamReader reader = factory.createXMLStreamReader(stream, StandardCharsets.UTF_8.name());
            try {
                return getDataStructure(fileType, reader);
            } finally {
                reader.close();
            }
        }
    }

    private FileType probeType(XMLStreamReader reader) throws XMLStreamException {
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

    private DataStructure getDataStructure(FileType fileType, XMLStreamReader reader) throws IOException, XMLStreamException {
        switch (fileType) {
            case GENERIC20:
                return generic20(reader);
            case COMPACT20:
                return compact20(reader);
            case GENERIC21:
                return generic21(reader);
            case COMPACT21:
                return compact21(reader);
            default:
                throw new IOException("Don't know how to handle '" + fileType + "'");
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
                                if (concept.equals("TIME_FORMAT")) {
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
                        case "Structure":
                            if (reader.getName().getNamespaceURI().equals(NS_21)) {
                                builder.refId(reader.getAttributeValue(null, "structureID"));
                                builder.timeDimensionId(reader.getAttributeValue(null, "dimensionAtObservation"));
                            }
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

    private static void compact21DataSet(XMLStreamReader reader, CustomDataStructureBuilder builder) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Series":
                            for (int i = 0; i < reader.getAttributeCount(); i++) {
                                String concept = reader.getAttributeLocalName(i);
                                if (concept.equals("TIME_FORMAT")) {
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
