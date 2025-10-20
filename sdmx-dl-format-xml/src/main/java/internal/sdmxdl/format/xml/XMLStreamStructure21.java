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
@SuppressWarnings("SwitchStatementWithTooFewBranches")
public final class XMLStreamStructure21 {

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
    private static final String CORE_REPRESENTATION_TAG = "CoreRepresentation";
    private static final String LOCAL_REPRESENTATION_TAG = "LocalRepresentation";
    private static final String CONCEPT_IDENTITY_TAG = "ConceptIdentity";
    private static final String REF_TAG = "Ref";
    private static final String ATTRIBUTE_LIST_TAG = "AttributeList";
    private static final String ATTRIBUTE_TAG = "Attribute";
    private static final String ATTRIBUTE_RELATIONSHIP_TAG = "AttributeRelationship";

    private static final String ID_ATTR = "id";
    private static final String AGENCY_ID_ATTR = "agencyID";
    private static final String VERSION_ATTR = "version";
    private static final String LANG_ATTR = "lang";
    private static final String POSITION_ATTR = "position";

    private final TextBuilder structureLabel;
    private final TextBuilder label;

    public XMLStreamStructure21(Languages languages) {
        this.structureLabel = new TextBuilder(languages);
        this.label = new TextBuilder(languages);
    }

    @NonNull
    public List<Structure> parse(@NonNull XMLStreamReader reader) throws XMLStreamException {
        if (XMLStreamUtil.isNotNamespaceAware(reader)) {
            throw new XMLStreamException("Cannot parse structure");
        }

        List<Structure> result = new ArrayList<>();
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

    private void parseStructures(XMLStreamReader reader, List<Structure> structs) throws XMLStreamException {
        DsdContext context = new DsdContext();
        while (XMLStreamUtil.nextTags(reader, STRUCTURES_TAG)) {
            switch (reader.getLocalName()) {
                case CODELISTS_TAG:
                    parseCodelists(reader, context.getCodelists());
                    break;
                case CONCEPTS_TAG:
                    parseConcepts(reader, context.getConcepts());
                    break;
                case DATA_STUCTURES_TAG:
                    parseDataStructures(reader, structs, context);
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

    private void parseConcepts(XMLStreamReader reader, List<Concept> concepts) throws XMLStreamException {
        while (XMLStreamUtil.nextTag(reader, CONCEPTS_TAG, CONCEPT_TAG)) {
            parseConcept(reader, concepts);
        }
    }

    private void parseConcept(XMLStreamReader reader, List<Concept> concepts) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing Concept id");

        label.clear();
        CodelistRef coreRepresentation = null;
        while (XMLStreamUtil.nextTags(reader, CONCEPT_TAG)) {
            switch (reader.getLocalName()) {
                case NAME_TAG:
                    parseNameTag(reader, label);
                    break;
                case CORE_REPRESENTATION_TAG:
                    coreRepresentation = parseCoreRepresentation(reader);
                    break;
            }
        }
        concepts.add(new Concept(id, label.build(id), coreRepresentation));
    }

    private void parseDataStructures(XMLStreamReader reader, List<Structure> result, DsdContext context) throws XMLStreamException {
        while (XMLStreamUtil.nextTag(reader, DATA_STUCTURES_TAG, DATA_STUCTURE_TAG)) {
            parseDataStructure(reader, result, context);
        }
    }

    private void parseDataStructure(XMLStreamReader reader, List<Structure> result, DsdContext context) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing DataStrucure id");

        Structure.Builder ds = Structure.builder();
        ds.ref(StructureRef.of(reader.getAttributeValue(null, AGENCY_ID_ATTR), id, reader.getAttributeValue(null, VERSION_ATTR)));
        structureLabel.clear();
        while (XMLStreamUtil.nextTags(reader, DATA_STUCTURE_TAG)) {
            switch (reader.getLocalName()) {
                case NAME_TAG:
                    parseNameTag(reader, structureLabel);
                    break;
                case DATA_STUCTURE_COMPONENTS_TAG:
                    parseDataStructureComponents(reader, ds, context);
                    break;
            }
        }
        ds.name(structureLabel.build(id));
        result.add(ds.build());
    }

    private void parseDataStructureComponents(XMLStreamReader reader, Structure.Builder ds, DsdContext context) throws XMLStreamException {
        while (XMLStreamUtil.nextTags(reader, DATA_STUCTURE_COMPONENTS_TAG)) {
            switch (reader.getLocalName()) {
                case DIMENSION_LIST_TAG:
                    parseDimensionList(reader, ds, context);
                    break;
                case MEASURE_LIST_TAG:
                    parseMeasureList(reader, ds);
                    break;
                case ATTRIBUTE_LIST_TAG:
                    parseAttributeList(reader, ds, context);
                    break;
            }
        }
    }

    private void parseDimensionList(XMLStreamReader reader, Structure.Builder ds, DsdContext context) throws XMLStreamException {
        while (XMLStreamUtil.nextTags(reader, DIMENSION_LIST_TAG)) {
            switch (reader.getLocalName()) {
                case DIMENSION_TAG:
                    parseDimension(reader, ds, context);
                    break;
                case TIME_DIMENSION_TAG:
                    parseTimeDimension(reader, ds);
                    break;
            }
        }
    }

    private void parseDimension(XMLStreamReader reader, Structure.Builder ds, DsdContext context) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing Dimension id");

        String position = reader.getAttributeValue(null, POSITION_ATTR);
        XMLStreamUtil.check(position != null, reader, "Missing Dimension position for Dimension '%s'", id);

        String conceptIdentity = null;
        CodelistRef localRepresentation = null;
        while (XMLStreamUtil.nextTags(reader, DIMENSION_TAG)) {
            switch (reader.getLocalName()) {
                case CONCEPT_IDENTITY_TAG:
                    conceptIdentity = parseConceptIdentity(reader);
                    break;
                case LOCAL_REPRESENTATION_TAG:
                    localRepresentation = parseLocalRepresentation(reader);
                    break;
            }
        }

        XMLStreamUtil.check(conceptIdentity != null, reader, "Missing Concept identity for Dimension '%s'", id);

        String conceptId = conceptIdentity;
        Concept concept = context.getConcept(conceptId)
                .orElseThrow(() -> new XMLStreamException("Cannot find Concept '" + conceptId + "' for Dimension '" + id + "'"));

        CodelistRef ref = concept.resolveRef(localRepresentation)
                .orElseThrow(() -> new XMLStreamException("Cannot resolve CodelistRef for Concept '" + conceptId + "' Dimension '" + id + "'"));

        ds.dimension(Dimension
                .builder()
                .id(id)
                .position(parseInt(position))
                .name(concept.getName())
                .codelist(context.getCodelist(ref).orElse(Codelist.builder().ref(ref).build()))
                .build()
        );

        context.incrementDimensionCount();
    }

    private String parseConceptIdentity(XMLStreamReader reader) throws XMLStreamException {
        if (XMLStreamUtil.nextTag(reader, CONCEPT_IDENTITY_TAG, REF_TAG)) {
            String id = reader.getAttributeValue(null, ID_ATTR);

            XMLStreamUtil.check(id != null, reader, "Missing Ref id");

            return id;
        }
        return null;
    }

    private CodelistRef parseLocalRepresentation(XMLStreamReader reader) throws XMLStreamException {
        if (XMLStreamUtil.nextTag(reader, LOCAL_REPRESENTATION_TAG, REF_TAG)) {
            String id = reader.getAttributeValue(null, ID_ATTR);
            String version = reader.getAttributeValue(null, VERSION_ATTR);
            String agencyID = reader.getAttributeValue(null, AGENCY_ID_ATTR);

            XMLStreamUtil.check(id != null, reader, "Missing Local Codelist id");

            return CodelistRef.of(agencyID, id, version);
        }
        return null;
    }

    private CodelistRef parseCoreRepresentation(XMLStreamReader reader) throws XMLStreamException {
        if (XMLStreamUtil.nextTag(reader, CORE_REPRESENTATION_TAG, REF_TAG)) {
            String id = reader.getAttributeValue(null, ID_ATTR);
            String version = reader.getAttributeValue(null, VERSION_ATTR);
            String agencyID = reader.getAttributeValue(null, AGENCY_ID_ATTR);

            XMLStreamUtil.check(id != null, reader, "Missing Core Codelist id");

            return CodelistRef.of(agencyID, id, version);
        }
        return null;
    }

    private void parseTimeDimension(XMLStreamReader reader, Structure.Builder ds) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing TimeDimension id");

        ds.timeDimensionId(id);
    }

    private void parseMeasureList(XMLStreamReader reader, Structure.Builder ds) throws XMLStreamException {
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

    private void parseAttributeList(XMLStreamReader reader, Structure.Builder ds, DsdContext context) throws XMLStreamException {
        while (XMLStreamUtil.nextTags(reader, ATTRIBUTE_LIST_TAG)) {
            switch (reader.getLocalName()) {
                case ATTRIBUTE_TAG:
                    parseAttribute(reader, ds, context);
                    break;
            }
        }
    }

    private void parseAttribute(XMLStreamReader reader, Structure.Builder ds, DsdContext context) throws XMLStreamException {
        String id = reader.getAttributeValue(null, ID_ATTR);
        XMLStreamUtil.check(id != null, reader, "Missing Attribute id");

        String conceptIdentity = null;
        CodelistRef localRepresentation = null;
        AttributeRelationship attributeRelationship = AttributeRelationship.DATAFLOW;
        while (XMLStreamUtil.nextTags(reader, ATTRIBUTE_TAG)) {
            switch (reader.getLocalName()) {
                case CONCEPT_IDENTITY_TAG:
                    conceptIdentity = parseConceptIdentity(reader);
                    break;
                case LOCAL_REPRESENTATION_TAG:
                    localRepresentation = parseLocalRepresentation(reader);
                    break;
                case ATTRIBUTE_RELATIONSHIP_TAG:
                    attributeRelationship = parseAttributeRelationship(reader, context);
                    break;
            }
        }

        XMLStreamUtil.check(conceptIdentity != null, reader, "Missing Concept identity for Attribute '%s'", id);

        String conceptId = conceptIdentity;
        Concept concept = context.getConcept(conceptId)
                .orElseThrow(() -> new XMLStreamException("Cannot find Concept '" + conceptId + "' for Attribute '" + id + "'"));

        CodelistRef ref = concept.resolveRef(localRepresentation)
                .orElse(null);

        ds.attribute(Attribute
                .builder()
                .id(id)
                .name(concept.getName())
                .codelist(ref != null ? context.getCodelist(ref).orElse(null) : null)
                .relationship(attributeRelationship)
                .build()
        );
    }

    private AttributeRelationship parseAttributeRelationship(XMLStreamReader reader, DsdContext context) throws XMLStreamException {
        AttributeRelationship result = AttributeRelationship.DATAFLOW;
        int dimensionCount = 0;
        while (XMLStreamUtil.nextTags(reader, ATTRIBUTE_RELATIONSHIP_TAG)) {
            switch (reader.getLocalName()) {
                case PRIMARY_MEASURE_TAG:
                    result = AttributeRelationship.OBSERVATION;
                    break;
                case DIMENSION_TAG:
                    dimensionCount++;
                    break;
            }
        }
        if (dimensionCount > 0) {
            result = dimensionCount < context.getDimensionCount() ? AttributeRelationship.GROUP : AttributeRelationship.SERIES;
        }
        return result;
    }

    private static int parseInt(String value) throws XMLStreamException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new XMLStreamException(ex);
        }
    }
}