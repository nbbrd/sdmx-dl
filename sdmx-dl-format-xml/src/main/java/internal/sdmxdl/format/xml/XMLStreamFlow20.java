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
import sdmxdl.DataStructureRef;
import sdmxdl.Dataflow;
import sdmxdl.DataflowRef;
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
public final class XMLStreamFlow20 {

    private static final String HEADER_TAG = "Header";
    private static final String DATAFLOWS_TAG = "Dataflows";
    private static final String DATAFLOW_TAG = "Dataflow";
    private static final String NAME_TAG = "Name";
    private static final String DESCRIPTION_TAG = "Description";

    private static final String ID_ATTR = "id";
    private static final String AGENCY_ID_ATTR = "agencyID";
    private static final String VERSION_ATTR = "version";
    private static final String LANG_ATTR = "lang";
    private static final String KEY_FAMILY_REF_TAG = "KeyFamilyRef";
    private static final String KEY_FAMILY_ID_TAG = "KeyFamilyID";
    private static final String KEY_FAMILY_AGENCY_ID_TAG = "KeyFamilyAgencyID";

    private final TextBuilder flowName;
    private final TextBuilder flowDescription;

    public XMLStreamFlow20(Languages languages) {
        this.flowName = new TextBuilder(languages);
        this.flowDescription = new TextBuilder(languages);
    }

    @NonNull
    public List<Dataflow> parse(@NonNull XMLStreamReader reader) throws XMLStreamException {
        if (isNotNamespaceAware(reader)) {
            throw new XMLStreamException("Cannot parse flows");
        }

        List<Dataflow> result = new ArrayList<>();
        while (nextTags(reader, "")) {
            switch (reader.getLocalName()) {
                case HEADER_TAG:
                    parseHeader(reader);
                    break;
                case DATAFLOWS_TAG:
                    parseDataflows(reader, result);
                    break;
            }
        }
        return result;
    }

    private void parseHeader(XMLStreamReader reader) throws XMLStreamException {
        String ns = reader.getNamespaceURI();
        check(Sdmxml.MESSAGE_V20.is(ns), reader, "Invalid namespace '%s'", ns);
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
                case KEY_FAMILY_REF_TAG:
                    structRef = parseKeyFamilyRef(reader);
                    break;
            }
        }

        check(structRef != null, reader, "Missing DataStructureRef");

        return Dataflow
                .builder()
                .ref(flowRef)
                .structureRef(structRef)
                .name(flowName.build(id))
                .description(flowDescription.build())
                .build();
    }

    private DataStructureRef parseKeyFamilyRef(XMLStreamReader reader) throws XMLStreamException {
        String agency = null;
        String id = null;
        String version = null;
        while (nextTags(reader, KEY_FAMILY_REF_TAG)) {
            switch (reader.getLocalName()) {
                case KEY_FAMILY_ID_TAG:
                    id = reader.getElementText();
                    break;
                case KEY_FAMILY_AGENCY_ID_TAG:
                    agency = reader.getElementText();
                    break;
            }
        }
        check(id != null, reader, "Missing DataStructureRef id");
        return DataStructureRef.of(agency, id, version);
    }

    private void parseTextWithLangAttr(XMLStreamReader reader, TextBuilder langStack) throws XMLStreamException {
        String lang = reader.getAttributeValue(null, LANG_ATTR);
        if (lang != null) {
            langStack.put(lang, reader.getElementText());
        }
    }
}
