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
package internal.sdmxdl.format.xml;

import lombok.NonNull;
import sdmxdl.StructureRef;
import sdmxdl.Flow;
import sdmxdl.FlowRef;
import sdmxdl.Languages;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

import static internal.sdmxdl.format.xml.XMLStreamUtil.*;

/**
 * @author Philippe Charles
 */
//@NotThreadSafe
@SuppressWarnings("SwitchStatementWithTooFewBranches")
public final class XMLStreamFlow21 {

    private static final String HEADER_TAG = "Header";
    private static final String STRUCTURES_TAG = "Structures";
    private static final String DATAFLOWS_TAG = "Dataflows";
    private static final String DATAFLOW_TAG = "Dataflow";
    private static final String NAME_TAG = "Name";
    private static final String DESCRIPTION_TAG = "Description";
    private static final String STRUCTURE_TAG = "Structure";
    private static final String REF_TAG = "Ref";

    private static final String ID_ATTR = "id";
    private static final String AGENCY_ID_ATTR = "agencyID";
    private static final String VERSION_ATTR = "version";
    private static final String LANG_ATTR = "lang";
    private static final String IS_EXTERNAL_REFERENCE_ATTR = "isExternalReference";

    private final TextBuilder flowName;
    private final TextBuilder flowDescription;

    public XMLStreamFlow21(Languages languages) {
        this.flowName = new TextBuilder(languages);
        this.flowDescription = new TextBuilder(languages);
    }

    @NonNull
    public List<Flow> parse(@NonNull XMLStreamReader reader) throws XMLStreamException {
        if (isNotNamespaceAware(reader)) {
            throw new XMLStreamException("Cannot parse flows");
        }

        List<Flow> result = new ArrayList<>();
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
        check(Sdmxml.MESSAGE_V21.is(ns), reader, "Invalid namespace '%s'", ns);
    }

    private void parseStructures(XMLStreamReader reader, List<Flow> flows) throws XMLStreamException {
        if (nextTag(reader, STRUCTURES_TAG, DATAFLOWS_TAG)) {
            parseDataflows(reader, flows);
        }
    }

    private void parseDataflows(XMLStreamReader reader, List<Flow> flows) throws XMLStreamException {
        while (nextTags(reader, DATAFLOWS_TAG)) {
            switch (reader.getLocalName()) {
                case DATAFLOW_TAG:
                    if (!isExternalReference(reader)) {
                        flows.add(parseDataflow(reader));
                    }
                    break;
            }
        }
    }

    // FIXME: API currently not designed to handle external references
    private boolean isExternalReference(XMLStreamReader reader) {
        return "true".equals(reader.getAttributeValue(null, IS_EXTERNAL_REFERENCE_ATTR));
    }

    @SuppressWarnings("null")
    private Flow parseDataflow(XMLStreamReader reader) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        check(id != null, reader, "Missing Dataflow id");

        FlowRef flowRef = FlowRef.of(reader.getAttributeValue(null, AGENCY_ID_ATTR), id, reader.getAttributeValue(null, VERSION_ATTR));
        StructureRef structRef = null;
        flowName.clear();
        flowDescription.clear();
        while (nextTags(reader, DATAFLOW_TAG)) {
            switch (reader.getLocalName()) {
                case NAME_TAG:
                    parseTextWithLangAttr(reader, flowName);
                    break;
                case DESCRIPTION_TAG:
                    parseTextWithLangAttr(reader, flowDescription);
                    break;
                case STRUCTURE_TAG:
                    structRef = parseStructure(reader);
                    break;
            }
        }

        check(structRef != null, reader, "Missing DataStructureRef");

        return Flow
                .builder()
                .ref(flowRef)
                .structureRef(structRef)
                .name(flowName.build(id))
                .description(flowDescription.build())
                .build();
    }

    private StructureRef parseStructure(XMLStreamReader reader) throws XMLStreamException {
        if (nextTag(reader, STRUCTURE_TAG, REF_TAG)) {
            return parseRef(reader);
        }
        throw new XMLStreamException("Missing DataStructureRef");
    }

    private StructureRef parseRef(XMLStreamReader reader) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        check(id != null, reader, "Missing DataStructureRef id");

        return StructureRef.of(reader.getAttributeValue(null, AGENCY_ID_ATTR), id, reader.getAttributeValue(null, VERSION_ATTR));
    }

    private void parseTextWithLangAttr(XMLStreamReader reader, TextBuilder langStack) throws XMLStreamException {
        String lang = reader.getAttributeValue(null, LANG_ATTR);
        if (lang != null) {
            langStack.put(lang, reader.getElementText());
        }
    }
}
