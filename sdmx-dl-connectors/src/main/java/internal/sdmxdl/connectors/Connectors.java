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
package internal.sdmxdl.connectors;

import sdmxdl.DataStructure;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.LanguagePriorityList;
import sdmxdl.util.Property;
import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxResponseException;
import java.util.Map;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Connectors {

    public sdmxdl.Dataflow toFlow(Dataflow flow) {
        return sdmxdl.Dataflow.of(DataflowRef.parse(flow.getFullIdentifier()), toStructureRef(flow.getDsdIdentifier()), flow.getDescription());
    }

    public DataStructureRef toStructureRef(DSDIdentifier input) {
        return DataStructureRef.of(input.getAgency(), input.getId(), input.getVersion());
    }

    public sdmxdl.Dimension toDimension(Dimension o) {
        sdmxdl.Dimension.Builder result = sdmxdl.Dimension.builder()
                .id(o.getId())
                .position(o.getPosition())
                .codes(o.getCodeList().getCodes());

        String name = o.getName();
        result.label(name != null ? name : o.getId());

        return result.build();
    }

    public DataStructure toStructure(DataFlowStructure dsd) {
        DataStructure.Builder result = DataStructure.builder()
                .ref(DataStructureRef.of(dsd.getAgency(), dsd.getId(), dsd.getVersion()))
                .label(dsd.getName())
                .timeDimensionId(dsd.getTimeDimension())
                .primaryMeasureId(dsd.getMeasure());
        dsd.getDimensions().forEach(o -> result.dimension(toDimension(o)));
        return result.build();
    }

    public Dataflow fromFlowQuery(DataflowRef flowRef, DataStructureRef structRef) {
        Dataflow result = new Dataflow();
        result.setAgency(flowRef.getAgency());
        result.setId(flowRef.getId());
        result.setVersion(flowRef.getVersion());
        result.setDsdIdentifier(fromStructureRef(structRef));
        return result;
    }

    public Dataflow fromFlow(sdmxdl.Dataflow flow) {
        Dataflow result = new Dataflow();
        result.setAgency(flow.getRef().getAgency());
        result.setId(flow.getRef().getId());
        result.setVersion(flow.getRef().getVersion());
        result.setDsdIdentifier(fromStructureRef(flow.getStructureRef()));
        result.setName(flow.getLabel());
        return result;
    }

    public DSDIdentifier fromStructureRef(DataStructureRef ref) {
        return new DSDIdentifier(ref.getId(), ref.getAgency(), ref.getVersion());
    }

    public Dimension fromDimension(sdmxdl.Dimension o) {
        Dimension result = new Dimension();
        result.setId(o.getId());
        result.setPosition(o.getPosition());
        result.setName(o.getLabel());
        Codelist codelist = new Codelist();
        codelist.setCodes(o.getCodes());
        result.setCodeList(codelist);
        return result;
    }

    public DataFlowStructure fromStructure(DataStructure dsd) {
        DataFlowStructure result = new DataFlowStructure();
        result.setAgency(dsd.getRef().getAgency());
        result.setId(dsd.getRef().getId());
        result.setVersion(dsd.getRef().getVersion());
        result.setName(dsd.getLabel());
        result.setTimeDimension(dsd.getTimeDimensionId());
        result.setMeasure(dsd.getPrimaryMeasureId());
        dsd.getDimensions().forEach(o -> result.setDimension(fromDimension(o)));
        return result;
    }

    public it.bancaditalia.oss.sdmx.util.LanguagePriorityList fromLanguages(LanguagePriorityList l) {
        return it.bancaditalia.oss.sdmx.util.LanguagePriorityList.parse(l.toString());
    }

    public boolean isNoResultMatchingQuery(SdmxException ex) {
        return ex instanceof SdmxResponseException && ((SdmxResponseException) ex).getResponseCode() == SdmxResponseException.SDMX_NO_RESULTS_FOUND;
    }

    public static final String SUPPORTS_COMPRESSION_PROPERTY = "supportsCompression";
    public static final boolean DEFAULT_SUPPORTS_COMPRESSION = false;

    public boolean isSupportsCompresson(Map<?, ?> o) {
        return Property.get(SUPPORTS_COMPRESSION_PROPERTY, DEFAULT_SUPPORTS_COMPRESSION, o);
    }

    public static final String NEEDS_CREDENTIALS_PROPERTY = "needsCredentials";
    public static final boolean DEFAULT_NEEDS_CREDENTIALS = false;

    public boolean isNeedsCredentials(Map<?, ?> o) {
        return Property.get(NEEDS_CREDENTIALS_PROPERTY, DEFAULT_NEEDS_CREDENTIALS, o);
    }

    public static final String NEEDS_URL_ENCODING_PROPERTY = "needsURLEncoding";
    public static final boolean DEFAULT_NEEDS_URL_ENCODING = false;

    public boolean isNeedsURLEncoding(Map<?, ?> o) {
        return Property.get(NEEDS_URL_ENCODING_PROPERTY, DEFAULT_NEEDS_URL_ENCODING, o);
    }
}
