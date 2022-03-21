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
package sdmxdl.xml.stream;

import internal.sdmxdl.xml.Sdmxml;
import lombok.NonNull;
import sdmxdl.Codelist;
import sdmxdl.CodelistRef;
import sdmxdl.LanguagePriorityList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philippe Charles
 */
//@NotThreadSafe
@SuppressWarnings("SwitchStatementWithTooFewBranches")
final class XMLStreamCodelist21 {

    private static final String HEADER_TAG = "Header";
    private static final String STRUCTURES_TAG = "Structures";
    private static final String CODELISTS_TAG = "Codelists";
    private static final String CODELIST_TAG = "Codelist";
    private static final String CODE_TAG = "Code";
    private static final String NAME_TAG = "Name";

    private static final String ID_ATTR = "id";
    private static final String AGENCY_ID_ATTR = "agencyID";
    private static final String VERSION_ATTR = "version";
    private static final String LANG_ATTR = "lang";

    private final TextBuilder label;

    XMLStreamCodelist21(LanguagePriorityList languages) {
        this.label = new TextBuilder(languages);
    }

    @NonNull
    public List<Codelist> parse(@NonNull XMLStreamReader reader) throws XMLStreamException {
        if (StaxUtil.isNotNamespaceAware(reader)) {
            throw new XMLStreamException("Cannot parse structure");
        }

        List<Codelist> result = new ArrayList<>();
        while (XMLStreamUtil.nextTags(reader, "")) {
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
        XMLStreamUtil.check(Sdmxml.MESSAGE_V21.is(ns), reader, "Invalid namespace '%s'", ns);
    }

    private void parseStructures(XMLStreamReader reader, List<Codelist> structs) throws XMLStreamException {
        while (XMLStreamUtil.nextTags(reader, STRUCTURES_TAG)) {
            switch (reader.getLocalName()) {
                case CODELISTS_TAG:
                    parseCodelists(reader, structs);
                    break;
            }
        }
    }

    private void parseCodelists(XMLStreamReader reader, List<Codelist> codelists) throws XMLStreamException {
        while (XMLStreamUtil.nextTag(reader, CODELISTS_TAG, CODELIST_TAG)) {
            parseCodelist(reader, codelists);
        }
    }

    private void parseCodelist(XMLStreamReader reader, List<Codelist> codelists) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        String version = reader.getAttributeValue(null, VERSION_ATTR);
        String agencyID = reader.getAttributeValue(null, AGENCY_ID_ATTR);

        XMLStreamUtil.check(id != null, reader, "Missing Codelist id");

        CodelistRef ref = CodelistRef.of(agencyID, id, version);

        Codelist.Builder codelist = Codelist.builder().ref(ref);
        while (XMLStreamUtil.nextTag(reader, CODELIST_TAG, CODE_TAG)) {
            parseCode(reader, codelist);
        }
        codelists.add(codelist.build());
    }

    private void parseCode(XMLStreamReader reader, Codelist.Builder codelist) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing Code id");

        label.clear();
        while (XMLStreamUtil.nextTag(reader, CODE_TAG, NAME_TAG)) {
            parseNameTag(reader, label);
        }
        codelist.code(id, label.build(id));
    }

    private void parseNameTag(XMLStreamReader reader, TextBuilder langStack) throws XMLStreamException {
        String lang = reader.getAttributeValue(null, LANG_ATTR);
        if (lang != null) {
            langStack.put(lang, reader.getElementText());
        }
    }
}
