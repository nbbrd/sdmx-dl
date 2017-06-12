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

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dimension;
import static be.nbb.sdmx.facade.xml.stream.XMLStreamUtil.check;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import static be.nbb.sdmx.facade.xml.stream.XMLStreamUtil.nextTags;
import static be.nbb.sdmx.facade.xml.stream.XMLStreamUtil.nextTag;
import static be.nbb.sdmx.facade.xml.stream.XMLStreamUtil.toInt;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
final class XMLStreamStructure21 {

    private final String preferredLang;

    XMLStreamStructure21(String preferredLang) {
        this.preferredLang = preferredLang;
    }

    @Nonnull
    public List<DataStructure> parse(@Nonnull XMLStreamReader reader) throws XMLStreamException {
        List<DataStructure> result = new ArrayList<>();
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

    private static final String NS_21 = "http://www.sdmx.org/resources/sdmxml/schemas/v2_1/message";

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
    private static final String POSITION_ATTR = "position";
    private static final String LANG_ATTR = "lang";

    private boolean isPreferredLang(XMLStreamReader reader) {
        return preferredLang.equals(reader.getAttributeValue(null, LANG_ATTR));
    }

    private void parseHeader(XMLStreamReader reader) throws XMLStreamException {
        String ns = reader.getNamespaceURI();
        check(NS_21.equals(ns), reader, "Invalid namespace '%s'", ns);
    }

    private void parseStructures(XMLStreamReader reader, List<DataStructure> structs) throws XMLStreamException {
        Map<String, Map<String, String>> codelists = new HashMap<>();
        Map<String, String> concepts = new HashMap<>();
        while (nextTags(reader, STRUCTURES_TAG)) {
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
        while (nextTag(reader, CODELISTS_TAG, CODELIST_TAG)) {
            parseCodelist(reader, codelists);
        }
    }

    private void parseCodelist(XMLStreamReader reader, Map<String, Map<String, String>> codelists) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        check(id != null, reader, "Missing Codelist id");

        Map<String, String> codelist = codelists.computeIfAbsent(id, o -> new HashMap<>());
        while (nextTag(reader, CODELIST_TAG, CODE_TAG)) {
            parseCode(reader, codelist);
        }
    }

    private void parseCode(XMLStreamReader reader, Map<String, String> codelist) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        check(id != null, reader, "Missing Code id");

        String label = null;
        while (nextTag(reader, CODE_TAG, NAME_TAG)) {
            if (label == null || isPreferredLang(reader)) {
                label = reader.getElementText();
            }
        }
        codelist.put(id, label != null ? label : id);
    }

    private void parseConcepts(XMLStreamReader reader, Map<String, String> concepts) throws XMLStreamException {
        while (nextTag(reader, CONCEPTS_TAG, CONCEPT_TAG)) {
            parseConcept(reader, concepts);
        }
    }

    private void parseConcept(XMLStreamReader reader, Map<String, String> concepts) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        check(id != null, reader, "Missing Concept id");

        String label = null;
        while (nextTag(reader, CONCEPT_TAG, NAME_TAG)) {
            if (label == null || isPreferredLang(reader)) {
                label = reader.getElementText();
            }
        }
        concepts.put(id, label != null ? label : id);
    }

    private void parseDataStructures(XMLStreamReader reader, List<DataStructure> result, Function<String, String> toConceptName, Function<String, Map<String, String>> toCodes) throws XMLStreamException {
        while (nextTag(reader, DATA_STUCTURES_TAG, DATA_STUCTURE_TAG)) {
            parseDataStructure(reader, result, toConceptName, toCodes);
        }
    }

    private void parseDataStructure(XMLStreamReader reader, List<DataStructure> result, Function<String, String> toConceptName, Function<String, Map<String, String>> toCodes) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        check(id != null, reader, "Missing DataStrucure id");

        DataStructure.Builder ds = DataStructure.builder();
        ds.ref(DataStructureRef.of(reader.getAttributeValue(null, AGENCY_ID_ATTR), id, reader.getAttributeValue(null, VERSION_ATTR)));
        String label = null;
        while (nextTags(reader, DATA_STUCTURE_TAG)) {
            switch (reader.getLocalName()) {
                case NAME_TAG:
                    if (label == null || isPreferredLang(reader)) {
                        label = reader.getElementText();
                    }
                    break;
                case DATA_STUCTURE_COMPONENTS_TAG:
                    parseDataStructureComponents(reader, ds, toConceptName, toCodes);
                    break;
            }
        }
        ds.label(label != null ? label : id);
        result.add(ds.build());
    }

    private void parseDataStructureComponents(XMLStreamReader reader, DataStructure.Builder ds, Function<String, String> toConceptName, Function<String, Map<String, String>> toCodes) throws XMLStreamException {
        while (nextTags(reader, DATA_STUCTURE_COMPONENTS_TAG)) {
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

    private void parseDimensionList(XMLStreamReader reader, DataStructure.Builder ds, Function<String, String> toConceptName, Function<String, Map<String, String>> toCodes) throws XMLStreamException {
        while (nextTags(reader, DIMENSION_LIST_TAG)) {
            switch (reader.getLocalName()) {
                case DIMENSION_TAG:
                    parseDimension(reader, ds, toConceptName, toCodes);
                    break;
                case TIME_DIMENSION_TAG:
                    parseTimeDimension(reader, ds);
                    break;
            }
        }
    }

    private void parseDimension(XMLStreamReader reader, DataStructure.Builder ds, Function<String, String> toConceptName, Function<String, Map<String, String>> toCodes) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        check(id != null, reader, "Missing Dimension id");

        int position = toInt(reader.getAttributeValue(null, POSITION_ATTR), -1);
        check(position != -1, reader, "Missing Dimension position");

        Dimension.Builder dimension = Dimension.builder().id(id).position(position).label(id);
        while (nextTags(reader, DIMENSION_TAG)) {
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

    private void parseConceptIdentity(XMLStreamReader reader, Dimension.Builder dimension, Function<String, String> toConceptName) throws XMLStreamException {
        if (nextTag(reader, CONCEPT_IDENTITY_TAG, REF_TAG)) {
            String id = reader.getAttributeValue(null, ID_ATTR);
            check(id != null, reader, "Missing Ref id");

            dimension.label(toConceptName.apply(id));
        }
    }

    private void parseLocalRepresentation(XMLStreamReader reader, Dimension.Builder dimension, Function<String, Map<String, String>> toCodes) throws XMLStreamException {
        if (nextTag(reader, LOCAL_REPRESENTATION_TAG, REF_TAG)) {
            String id = reader.getAttributeValue(null, ID_ATTR);
            check(id != null, reader, "Missing Ref id");

            dimension.codes(toCodes.apply(id));
        }
    }

    private void parseTimeDimension(XMLStreamReader reader, DataStructure.Builder ds) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        check(id != null, reader, "Missing TimeDimension id");

        ds.timeDimensionId(id);
    }

    private void parseMeasureList(XMLStreamReader reader, DataStructure.Builder ds) throws XMLStreamException {
        if (nextTag(reader, MEASURE_LIST_TAG, PRIMARY_MEASURE_TAG)) {
            String id = reader.getAttributeValue(null, ID_ATTR);
            check(id != null, reader, "Missing PrimaryMeasure id");

            ds.primaryMeasureId(id);
        }
    }
}
