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
package be.nbb.sdmx.facade.xml.stream;

import be.nbb.util.StaxUtil;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.LanguagePriorityList;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import static be.nbb.sdmx.facade.xml.stream.XMLStreamUtil.*;
import static be.nbb.sdmx.facade.xml.Sdmxml.NS_V21_URI;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@NotThreadSafe
final class XMLStreamFlow21 {

    private static final String HEADER_TAG = "Header";
    private static final String STRUCTURES_TAG = "Structures";
    private static final String DATAFLOWS_TAG = "Dataflows";
    private static final String DATAFLOW_TAG = "Dataflow";
    private static final String NAME_TAG = "Name";
    private static final String STRUCTURE_TAG = "Structure";
    private static final String REF_TAG = "Ref";

    private static final String ID_ATTR = "id";
    private static final String AGENCY_ID_ATTR = "agencyID";
    private static final String VERSION_ATTR = "version";
    private static final String LANG_ATTR = "lang";

    private final TextBuilder flowLabel;

    XMLStreamFlow21(LanguagePriorityList languages) {
        this.flowLabel = new TextBuilder(languages);
    }

    @Nonnull
    public List<Dataflow> parse(@Nonnull XMLStreamReader reader) throws XMLStreamException {
        if (StaxUtil.isNotNamespaceAware(reader)) {
            throw new XMLStreamException("Cannot parse flows");
        }

        List<Dataflow> result = new ArrayList<>();
        while (nextTags(reader, "")) {
            switch (reader.getLocalName()) {
                case HEADER_TAG:
                    parseHeader(reader);
                    break;
                case STRUCTURES_TAG:
                    parseStructures(reader, result);
                    break;
            }
        }
        return result;
    }

    private void parseHeader(XMLStreamReader reader) throws XMLStreamException {
        String ns = reader.getNamespaceURI();
        check(NS_V21_URI.equals(ns), reader, "Invalid namespace '%s'", ns);
    }

    private void parseStructures(XMLStreamReader reader, List<Dataflow> flows) throws XMLStreamException {
        if (nextTag(reader, STRUCTURES_TAG, DATAFLOWS_TAG)) {
            parseDataflows(reader, flows);
        }
    }

    private void parseDataflows(XMLStreamReader reader, List<Dataflow> flows) throws XMLStreamException {
        while (nextTags(reader, DATAFLOWS_TAG)) {
            switch (reader.getLocalName()) {
                case DATAFLOW_TAG:
                    flows.add(parseDataflow(reader));
                    break;
            }
        }
    }

    @SuppressWarnings("null")
    private Dataflow parseDataflow(XMLStreamReader reader) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        check(id != null, reader, "Missing Dataflow id");

        DataflowRef flowRef = DataflowRef.of(reader.getAttributeValue(null, AGENCY_ID_ATTR), id, reader.getAttributeValue(null, VERSION_ATTR));
        DataStructureRef structRef = null;
        flowLabel.clear();
        while (nextTags(reader, DATAFLOW_TAG)) {
            switch (reader.getLocalName()) {
                case NAME_TAG:
                    parseNameTag(reader, flowLabel);
                    break;
                case STRUCTURE_TAG:
                    structRef = parseStructure(reader);
                    break;
            }
        }

        check(structRef != null, reader, "Missing DataStructureRef");

        return Dataflow.of(flowRef, structRef, flowLabel.build(id));
    }

    private DataStructureRef parseStructure(XMLStreamReader reader) throws XMLStreamException {
        if (nextTag(reader, STRUCTURE_TAG, REF_TAG)) {
            return parseRef(reader);
        }
        throw new XMLStreamException("Missing DataStructureRef");
    }

    private DataStructureRef parseRef(XMLStreamReader reader) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        check(id != null, reader, "Missing DataStructureRef id");

        return DataStructureRef.of(reader.getAttributeValue(null, AGENCY_ID_ATTR), id, reader.getAttributeValue(null, VERSION_ATTR));
    }

    private void parseNameTag(XMLStreamReader reader, TextBuilder langStack) throws XMLStreamException {
        String lang = reader.getAttributeValue(null, LANG_ATTR);
        if (lang != null) {
            langStack.put(lang, reader.getElementText());
        }
    }
}
