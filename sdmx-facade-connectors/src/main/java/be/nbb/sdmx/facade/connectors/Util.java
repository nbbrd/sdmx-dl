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
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.util.Property.BoolProperty;
import be.nbb.sdmx.facade.util.SdmxFix;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class Util {

    be.nbb.sdmx.facade.Dataflow toDataflow(Dataflow dataflow) {
        return be.nbb.sdmx.facade.Dataflow.of(DataflowRef.parse(dataflow.getFullIdentifier()), toDataStructureRef(dataflow.getDsdIdentifier()), dataflow.getDescription());
    }

    be.nbb.sdmx.facade.DataStructureRef toDataStructureRef(DSDIdentifier input) {
        return DataStructureRef.of(input.getAgency(), input.getId(), input.getVersion());
    }

    be.nbb.sdmx.facade.Dimension toDimension(Dimension o) {
        be.nbb.sdmx.facade.Dimension.Builder result = be.nbb.sdmx.facade.Dimension.builder()
                .id(o.getId())
                .position(o.getPosition());

        String name = o.getName();
        result.label(name != null ? name : o.getId());
        SdmxFix.codes(result, o.getCodeList().getCodes());

        return result.build();
    }

    DataStructure toDataStructure(DataFlowStructure dfs) {
        DataStructure.Builder result = DataStructure.builder()
                .ref(DataStructureRef.of(dfs.getAgency(), dfs.getId(), dfs.getVersion()))
                .label(getNonNullName(dfs))
                .timeDimensionId(dfs.getTimeDimension())
                .primaryMeasureId(dfs.getMeasure());
        dfs.getDimensions().forEach(o -> result.dimension(toDimension(o)));
        return result.build();
    }

    it.bancaditalia.oss.sdmx.util.LanguagePriorityList fromLanguages(be.nbb.sdmx.facade.LanguagePriorityList l) {
        return it.bancaditalia.oss.sdmx.util.LanguagePriorityList.parse(l.toString());
    }

    @Nonnull
    private String getNonNullName(DataFlowStructure dfs) {
        // FIXME: PR parsing code for name of data structure v2.1 in connectors 
        String result = dfs.getName();
        return result != null ? result : dfs.getId();
    }

    static final BoolProperty SUPPORTS_COMPRESSION = new BoolProperty("supportsCompression");
    static final BoolProperty NEEDS_CREDENTIALS = new BoolProperty("needsCredentials");
    static final BoolProperty NEEDS_URL_ENCODING = new BoolProperty("needsURLEncoding");
    static final BoolProperty SERIES_KEYS_ONLY_SUPPORTED = new BoolProperty("seriesKeysOnlySupported");
}
