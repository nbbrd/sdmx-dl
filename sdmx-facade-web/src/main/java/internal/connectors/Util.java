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
package internal.connectors;

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.util.Property.BoolProperty;
import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxResponseException;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Util {

    public be.nbb.sdmx.facade.Dataflow toFlow(Dataflow flow) {
        return be.nbb.sdmx.facade.Dataflow.of(DataflowRef.parse(flow.getFullIdentifier()), toStructureRef(flow.getDsdIdentifier()), flow.getDescription());
    }

    public be.nbb.sdmx.facade.DataStructureRef toStructureRef(DSDIdentifier input) {
        return DataStructureRef.of(input.getAgency(), input.getId(), input.getVersion());
    }

    public be.nbb.sdmx.facade.Dimension toDimension(Dimension o) {
        be.nbb.sdmx.facade.Dimension.Builder result = be.nbb.sdmx.facade.Dimension.builder()
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

    public Dataflow fromFlowQuery(be.nbb.sdmx.facade.DataflowRef ref) {
        Dataflow result = new Dataflow();
        result.setAgency(ref.getAgency());
        result.setId(ref.getId());
        result.setVersion(ref.getVersion());
        return result;
    }

    public Dataflow fromFlow(be.nbb.sdmx.facade.Dataflow flow) {
        Dataflow result = new Dataflow();
        result.setAgency(flow.getRef().getAgency());
        result.setId(flow.getRef().getId());
        result.setVersion(flow.getRef().getVersion());
        result.setDsdIdentifier(fromStructureRef(flow.getStructureRef()));
        result.setName(flow.getLabel());
        return result;
    }

    public DSDIdentifier fromStructureRef(be.nbb.sdmx.facade.DataStructureRef ref) {
        return new DSDIdentifier(ref.getId(), ref.getAgency(), ref.getVersion());
    }

    public Dimension fromDimension(be.nbb.sdmx.facade.Dimension o) {
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

    public it.bancaditalia.oss.sdmx.util.LanguagePriorityList fromLanguages(be.nbb.sdmx.facade.LanguagePriorityList l) {
        return it.bancaditalia.oss.sdmx.util.LanguagePriorityList.parse(l.toString());
    }

    public boolean isNoResultMatchingQuery(SdmxException ex) {
        return ex instanceof SdmxResponseException && ((SdmxResponseException) ex).getResponseCode() == SdmxResponseException.SDMX_NO_RESULTS_FOUND;
    }

    public static final BoolProperty SUPPORTS_COMPRESSION = new BoolProperty("supportsCompression");
    public static final BoolProperty NEEDS_CREDENTIALS = new BoolProperty("needsCredentials");
    public static final BoolProperty NEEDS_URL_ENCODING = new BoolProperty("needsURLEncoding");
    public static final BoolProperty SERIES_KEYS_ONLY_SUPPORTED = new BoolProperty("seriesKeysOnlySupported");
}
