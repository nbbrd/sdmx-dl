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
package be.nbb.sdmx.facade.connectors;

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.FlowRef;
import be.nbb.sdmx.facade.ResourceRef;
import be.nbb.sdmx.facade.util.Property.BoolProperty;
import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class Util {

    be.nbb.sdmx.facade.Dataflow toDataflow(Dataflow dataflow) {
        return be.nbb.sdmx.facade.Dataflow.of(FlowRef.parse(dataflow.getFullIdentifier()), toDataStructureRef(dataflow.getDsdIdentifier()), dataflow.getDescription());
    }

    be.nbb.sdmx.facade.ResourceRef toDataStructureRef(DSDIdentifier input) {
        return ResourceRef.of(input.getAgency(), input.getId(), input.getVersion());
    }

    be.nbb.sdmx.facade.Codelist toCodelist(Codelist input) {
        return be.nbb.sdmx.facade.Codelist.builder()
                .ref(ResourceRef.of(input.getAgency(), input.getId(), input.getVersion()))
                .codes(input.getCodes())
                .build();
    }

    DataStructure toDataStructure(DataFlowStructure dfs) {
        DataStructure.Builder result = DataStructure.builder()
                .ref(ResourceRef.of(dfs.getAgency(), dfs.getId(), dfs.getVersion()))
                .name(dfs.getName())
                .timeDimensionId(dfs.getTimeDimension())
                .primaryMeasureId(dfs.getMeasure());
        for (Dimension o : dfs.getDimensions()) {
            result.dimension(be.nbb.sdmx.facade.Dimension.of(o.getId(), o.getPosition(), toCodelist(o.getCodeList()), o.getName()));
        }
        return result.build();
    }

    static final BoolProperty SUPPORTS_COMPRESSION = new BoolProperty("supportsCompression");
    static final BoolProperty NEEDS_CREDENTIALS = new BoolProperty("needsCredentials");
    static final BoolProperty NEEDS_URL_ENCODING = new BoolProperty("needsURLEncoding");
    static final BoolProperty SERIES_KEYS_ONLY_SUPPORTED = new BoolProperty("seriesKeysOnlySupported");
}
