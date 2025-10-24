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
import sdmxdl.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philippe Charles
 */
//@NotThreadSafe
public final class XMLStreamStructure20 {

    private static final String HEADER_TAG = "Header";
    private static final String CODE_LISTS_TAG = "CodeLists";
    private static final String CONCEPTS_TAG = "Concepts";
    private static final String KEY_FAMILIES_TAG = "KeyFamilies";
    private static final String CODE_LIST_TAG = "CodeList";
    private static final String CONCEPT_TAG = "Concept";
    private static final String KEY_FAMILY_TAG = "KeyFamily";
    private static final String CODE_TAG = "Code";
    private static final String DESCRIPTION_TAG = "Description";
    private static final String NAME_TAG = "Name";
    private static final String COMPONENTS_TAG = "Components";
    private static final String DIMENSION_TAG = "Dimension";
    private static final String TIME_DIMENSION_TAG = "TimeDimension";
    private static final String PRIMARY_MEASURE_TAG = "PrimaryMeasure";
    private static final String ATTRIBUTE_TAG = "Attribute";

    private static final String ID_ATTR = "id";
    private static final String AGENCY_ID_ATTR = "agencyID";
    private static final String VERSION_ATTR = "version";
    private static final String LANG_ATTR = "lang";
    private static final String VALUE_ATTR = "value";
    private static final String CONCEPT_REF_ATTR = "conceptRef";
    private static final String CODELIST_ATTR = "codelist";
    private static final String ATTACHMENT_LEVEL_ATTR = "attachmentLevel";

    private final TextBuilder structureLabel;
    private final TextBuilder label;

    public XMLStreamStructure20(Languages languages) {
        this.structureLabel = new TextBuilder(languages);
        this.label = new TextBuilder(languages);
    }

    @NonNull
    public List<Structure> parse(@NonNull XMLStreamReader reader) throws XMLStreamException {
        if (XMLStreamUtil.isNotNamespaceAware(reader)) {
            throw new XMLStreamException("Cannot parse structure");
        }

        List<Structure> result = new ArrayList<>();
        DsdContext context = new DsdContext();
        while (XMLStreamUtil.nextTags(reader, "")) {
            switch (reader.getLocalName()) {
                case HEADER_TAG:
                    parseHeader(reader);
                    break;
                case CODE_LISTS_TAG:
                    parseCodelists(reader, context.getCodelists());
                    break;
                case CONCEPTS_TAG:
                    parseConcepts(reader, context.getConcepts());
                    break;
                case KEY_FAMILIES_TAG:
                    parseDataStructures(reader, result, context);
                    break;
            }
        }
        return result;
    }

    private void parseHeader(XMLStreamReader reader) throws XMLStreamException {
        String ns = reader.getNamespaceURI();
        XMLStreamUtil.check(Sdmxml.MESSAGE_V20.is(ns), reader, "Invalid namespace '%s'", ns);
    }

    private void parseCodelists(XMLStreamReader reader, List<Codelist> codelists) throws XMLStreamException {
        while (XMLStreamUtil.nextTag(reader, CODE_LISTS_TAG, CODE_LIST_TAG)) {
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
        while (XMLStreamUtil.nextTag(reader, CODE_LIST_TAG, CODE_TAG)) {
            parseCode(reader, codelist);
        }
        codelists.add(codelist.build());
    }

    private void parseCode(XMLStreamReader reader, Codelist.Builder codelist) throws XMLStreamException {
        String id = reader.getAttributeValue(null, VALUE_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing Code value");

        label.clear();
        while (XMLStreamUtil.nextTag(reader, CODE_TAG, DESCRIPTION_TAG)) {
            parseNameTag(reader, label);
        }
        codelist.code(id, label.build(id));
    }

    private void parseConcepts(XMLStreamReader reader, List<Concept> concepts) throws XMLStreamException {
        while (XMLStreamUtil.nextTag(reader, CONCEPTS_TAG, CONCEPT_TAG)) {
            parseConcept(reader, concepts);
        }
    }

    private void parseConcept(XMLStreamReader reader, List<Concept> concepts) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing Concept id");

        label.clear();
        while (XMLStreamUtil.nextTag(reader, CONCEPT_TAG, NAME_TAG)) {
            parseNameTag(reader, label);
        }
        concepts.add(new Concept(id, label.build(id), null));
    }

    private void parseDataStructures(XMLStreamReader reader, List<Structure> result, DsdContext context) throws XMLStreamException {
        while (XMLStreamUtil.nextTag(reader, KEY_FAMILIES_TAG, KEY_FAMILY_TAG)) {
            parseDataStructure(reader, result, context);
        }
    }

    private void parseDataStructure(XMLStreamReader reader, List<Structure> result, DsdContext context) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing DataStrucure id");

        String optionalAgency = reader.getAttributeValue(null, AGENCY_ID_ATTR);
        String optionalVersion = reader.getAttributeValue(null, VERSION_ATTR);

        Structure.Builder ds = Structure
                .builder()
                .ref(StructureRef.of(optionalAgency, id, optionalVersion))
                .primaryMeasureId("");
        structureLabel.clear();
        while (XMLStreamUtil.nextTags(reader, KEY_FAMILY_TAG)) {
            switch (reader.getLocalName()) {
                case NAME_TAG:
                    parseNameTag(reader, structureLabel);
                    break;
                case COMPONENTS_TAG:
                    parseDataStructureComponents(reader, ds, context);
                    break;
            }
        }
        ds.name(structureLabel.build(id));
        result.add(ds.build());
    }

    private void parseDataStructureComponents(XMLStreamReader reader, Structure.Builder ds, DsdContext context) throws XMLStreamException {
        while (XMLStreamUtil.nextTags(reader, COMPONENTS_TAG)) {
            switch (reader.getLocalName()) {
                case DIMENSION_TAG:
                    parseDimension(reader, ds, context);
                    break;
                case TIME_DIMENSION_TAG:
                    parseTimeDimension(reader, ds);
                    break;
                case PRIMARY_MEASURE_TAG:
                    parsePrimaryMeasure(reader, ds);
                    break;
                case ATTRIBUTE_TAG:
                    parseAttribute(reader, ds, context);
                    break;
            }
        }
    }

    private void parseComponent(XMLStreamReader reader, Component.Builder<?> component, DsdContext context) throws XMLStreamException {
        String id = reader.getAttributeValue(null, CONCEPT_REF_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing Dimension id");

        String codelist = reader.getAttributeValue(null, CODELIST_ATTR);
        XMLStreamUtil.check(codelist != null, reader, "Missing Dimension codelist");

        component.id(id);

        Concept concept = context.findConceptById(id).orElse(null);
        component.name(concept != null ? concept.getName() : id);

        CodelistRef ref = CodelistRef.of(null, codelist, null);

        component.codelist(context.findCodelistByRef(ref).orElse(Codelist.builder().ref(ref).build()));
    }

    private void parseDimension(XMLStreamReader reader, Structure.Builder ds, DsdContext context) throws XMLStreamException {
        Dimension.Builder result = Dimension.builder();
        parseComponent(reader, result, context);
        ds.dimension(result.build());
    }

    private void parseTimeDimension(XMLStreamReader reader, Structure.Builder ds) throws XMLStreamException {
        String id = reader.getAttributeValue(null, CONCEPT_REF_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing TimeDimension id");

        ds.timeDimensionId(id);
    }

    private void parsePrimaryMeasure(XMLStreamReader reader, Structure.Builder ds) throws XMLStreamException {
        String id = reader.getAttributeValue(null, CONCEPT_REF_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing PrimaryMeasure id");

        ds.primaryMeasureId(id);
    }

    private void parseNameTag(XMLStreamReader reader, TextBuilder langStack) throws XMLStreamException {
        String lang = reader.getAttributeValue(null, LANG_ATTR);
        if (lang != null) {
            langStack.put(lang, reader.getElementText());
        }
    }

    private void parseAttribute(XMLStreamReader reader, Structure.Builder ds, DsdContext context) throws XMLStreamException {
        Attribute.Builder result = Attribute.builder();
        parseComponent(reader, result, context);
        result.relationship(getAttributeRelationship(reader.getAttributeValue(null, ATTACHMENT_LEVEL_ATTR)));
        ds.attribute(result.build());
    }

    private AttributeRelationship getAttributeRelationship(String attachmentLevel) {
        if (attachmentLevel != null) {
            switch (attachmentLevel) {
                case "DataSet":
                    return AttributeRelationship.DATAFLOW;
                case "Group":
                    return AttributeRelationship.GROUP;
                case "Series":
                    return AttributeRelationship.SERIES;
                case "Observation":
                    return AttributeRelationship.OBSERVATION;
            }
        }
        return AttributeRelationship.UNKNOWN;
    }
}
