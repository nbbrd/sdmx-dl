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

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataStructure;
import sdmxdl.DataStructureRef;
import sdmxdl.Dimension;
import sdmxdl.LanguagePriorityList;
import sdmxdl.xml.SdmxmlUri;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * @author Philippe Charles
 */
//@NotThreadSafe
final class XMLStreamStructure21 {

    private static final String HEADER_TAG = "Header";
    private static final String STRUCTURES_TAG = "Structures";
    private static final String CODELISTS_TAG = "Codelists";
    private static final String CONCEPTS_TAG = "Concepts";
    private static final String DATA_STUCTURES_TAG = "DataStructures";
    private static final String CODELIST_TAG = "Codelist";
    private static final String CONCEPT_TAG = "Concept";
    private static final String CODE_TAG = "Code";
    private static final String DATA_STUCTURE_TAG = "DataStructure";
    private static final String DATA_STUCTURE_COMPONENTS_TAG = "DataStructureComponents";
    private static final String DIMENSION_LIST_TAG = "DimensionList";
    private static final String MEASURE_LIST_TAG = "MeasureList";
    private static final String DIMENSION_TAG = "Dimension";
    private static final String TIME_DIMENSION_TAG = "TimeDimension";
    private static final String PRIMARY_MEASURE_TAG = "PrimaryMeasure";
    private static final String NAME_TAG = "Name";
    private static final String LOCAL_REPRESENTATION_TAG = "LocalRepresentation";
    private static final String CONCEPT_IDENTITY_TAG = "ConceptIdentity";
    private static final String REF_TAG = "Ref";

    private static final String ID_ATTR = "id";
    private static final String AGENCY_ID_ATTR = "agencyID";
    private static final String VERSION_ATTR = "version";
    private static final String LANG_ATTR = "lang";

    private final TextBuilder structureLabel;
    private final TextBuilder label;

    XMLStreamStructure21(LanguagePriorityList languages) {
        this.structureLabel = new TextBuilder(languages);
        this.label = new TextBuilder(languages);
    }

    @NonNull
    public List<DataStructure> parse(@NonNull XMLStreamReader reader) throws XMLStreamException {
        if (StaxUtil.isNotNamespaceAware(reader)) {
            throw new XMLStreamException("Cannot parse structure");
        }

        List<DataStructure> result = new ArrayList<>();
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
        XMLStreamUtil.check(SdmxmlUri.NS_V21_URI.is(URI.create(ns)), reader, "Invalid namespace '%s'", ns);
    }

    private void parseStructures(XMLStreamReader reader, List<DataStructure> structs) throws XMLStreamException {
        Map<String, Map<String, String>> codelists = new HashMap<>();
        Map<String, String> concepts = new HashMap<>();
        while (XMLStreamUtil.nextTags(reader, STRUCTURES_TAG)) {
            switch (reader.getLocalName()) {
                case CODELISTS_TAG:
                    parseCodelists(reader, codelists);
                    break;
                case CONCEPTS_TAG:
                    parseConcepts(reader, concepts);
                    break;
                case DATA_STUCTURES_TAG:
                    parseDataStructures(reader, structs, concepts::get, codelists::get);
                    break;
            }
        }
    }

    private void parseCodelists(XMLStreamReader reader, Map<String, Map<String, String>> codelists) throws XMLStreamException {
        while (XMLStreamUtil.nextTag(reader, CODELISTS_TAG, CODELIST_TAG)) {
            parseCodelist(reader, codelists);
        }
    }

    private void parseCodelist(XMLStreamReader reader, Map<String, Map<String, String>> codelists) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing Codelist id");

        Map<String, String> codelist = codelists.computeIfAbsent(id, o -> new HashMap<>());
        while (XMLStreamUtil.nextTag(reader, CODELIST_TAG, CODE_TAG)) {
            parseCode(reader, codelist);
        }
    }

    private void parseCode(XMLStreamReader reader, Map<String, String> codelist) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing Code id");

        label.clear();
        while (XMLStreamUtil.nextTag(reader, CODE_TAG, NAME_TAG)) {
            parseNameTag(reader, label);
        }
        codelist.put(id, label.build(id));
    }

    private void parseConcepts(XMLStreamReader reader, Map<String, String> concepts) throws XMLStreamException {
        while (XMLStreamUtil.nextTag(reader, CONCEPTS_TAG, CONCEPT_TAG)) {
            parseConcept(reader, concepts);
        }
    }

    private void parseConcept(XMLStreamReader reader, Map<String, String> concepts) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing Concept id");

        label.clear();
        while (XMLStreamUtil.nextTag(reader, CONCEPT_TAG, NAME_TAG)) {
            parseNameTag(reader, label);
        }
        concepts.put(id, label.build(id));
    }

    private void parseDataStructures(XMLStreamReader reader, List<DataStructure> result, UnaryOperator<String> toConceptName, Function<String, Map<String, String>> toCodes) throws XMLStreamException {
        while (XMLStreamUtil.nextTag(reader, DATA_STUCTURES_TAG, DATA_STUCTURE_TAG)) {
            parseDataStructure(reader, result, toConceptName, toCodes);
        }
    }

    private void parseDataStructure(XMLStreamReader reader, List<DataStructure> result, UnaryOperator<String> toConceptName, Function<String, Map<String, String>> toCodes) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing DataStrucure id");

        DataStructure.Builder ds = DataStructure.builder();
        ds.ref(DataStructureRef.of(reader.getAttributeValue(null, AGENCY_ID_ATTR), id, reader.getAttributeValue(null, VERSION_ATTR)));
        structureLabel.clear();
        while (XMLStreamUtil.nextTags(reader, DATA_STUCTURE_TAG)) {
            switch (reader.getLocalName()) {
                case NAME_TAG:
                    parseNameTag(reader, structureLabel);
                    break;
                case DATA_STUCTURE_COMPONENTS_TAG:
                    parseDataStructureComponents(reader, ds, toConceptName, toCodes);
                    break;
            }
        }
        ds.label(structureLabel.build(id));
        result.add(ds.build());
    }

    private void parseDataStructureComponents(XMLStreamReader reader, DataStructure.Builder ds, UnaryOperator<String> toConceptName, Function<String, Map<String, String>> toCodes) throws XMLStreamException {
        while (XMLStreamUtil.nextTags(reader, DATA_STUCTURE_COMPONENTS_TAG)) {
            switch (reader.getLocalName()) {
                case DIMENSION_LIST_TAG:
                    parseDimensionList(reader, ds, toConceptName, toCodes);
                    break;
                case MEASURE_LIST_TAG:
                    parseMeasureList(reader, ds);
                    break;
            }
        }
    }

    private void parseDimensionList(XMLStreamReader reader, DataStructure.Builder ds, UnaryOperator<String> toConceptName, Function<String, Map<String, String>> toCodes) throws XMLStreamException {
        int position = 1;
        while (XMLStreamUtil.nextTags(reader, DIMENSION_LIST_TAG)) {
            switch (reader.getLocalName()) {
                case DIMENSION_TAG:
                    parseDimension(reader, ds, toConceptName, toCodes, position++);
                    break;
                case TIME_DIMENSION_TAG:
                    parseTimeDimension(reader, ds);
                    break;
            }
        }
    }

    private void parseDimension(XMLStreamReader reader, DataStructure.Builder ds, UnaryOperator<String> toConceptName, Function<String, Map<String, String>> toCodes, int position) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing Dimension id");

        Dimension.Builder dimension = Dimension.builder().id(id).position(position).label(id);
        while (XMLStreamUtil.nextTags(reader, DIMENSION_TAG)) {
            switch (reader.getLocalName()) {
                case CONCEPT_IDENTITY_TAG:
                    parseConceptIdentity(reader, dimension, toConceptName);
                    break;
                case LOCAL_REPRESENTATION_TAG:
                    parseLocalRepresentation(reader, dimension, toCodes);
                    break;
            }
        }
        ds.dimension(dimension.build());
    }

    private void parseConceptIdentity(XMLStreamReader reader, Dimension.Builder dimension, UnaryOperator<String> toConceptName) throws XMLStreamException {
        if (XMLStreamUtil.nextTag(reader, CONCEPT_IDENTITY_TAG, REF_TAG)) {
            String id = reader.getAttributeValue(null, ID_ATTR);
            XMLStreamUtil.check(id != null, reader, "Missing Ref id");

            String conceptName = toConceptName.apply(id);
            dimension.label(conceptName != null ? conceptName : id);
        }
    }

    private void parseLocalRepresentation(XMLStreamReader reader, Dimension.Builder dimension, Function<String, Map<String, String>> toCodes) throws XMLStreamException {
        if (XMLStreamUtil.nextTag(reader, LOCAL_REPRESENTATION_TAG, REF_TAG)) {
            String id = reader.getAttributeValue(null, ID_ATTR);
            XMLStreamUtil.check(id != null, reader, "Missing Ref id");

            Map<String, String> codes = toCodes.apply(id);
            if (codes != null) {
                dimension.codes(codes);
            }
        }
    }

    private void parseTimeDimension(XMLStreamReader reader, DataStructure.Builder ds) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing TimeDimension id");

        ds.timeDimensionId(id);
    }

    private void parseMeasureList(XMLStreamReader reader, DataStructure.Builder ds) throws XMLStreamException {
        if (XMLStreamUtil.nextTag(reader, MEASURE_LIST_TAG, PRIMARY_MEASURE_TAG)) {
            String id = reader.getAttributeValue(null, ID_ATTR);
            XMLStreamUtil.check(id != null, reader, "Missing PrimaryMeasure id");

            ds.primaryMeasureId(id);
        }
    }

    private void parseNameTag(XMLStreamReader reader, TextBuilder langStack) throws XMLStreamException {
        String lang = reader.getAttributeValue(null, LANG_ATTR);
        if (lang != null) {
            langStack.put(lang, reader.getElementText());
        }
    }
}
