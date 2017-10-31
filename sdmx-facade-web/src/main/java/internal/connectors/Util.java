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
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;

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

    public it.bancaditalia.oss.sdmx.util.LanguagePriorityList fromLanguages(be.nbb.sdmx.facade.LanguagePriorityList l) {
        return it.bancaditalia.oss.sdmx.util.LanguagePriorityList.parse(l.toString());
    }

    public static final BoolProperty SUPPORTS_COMPRESSION = new BoolProperty("supportsCompression");
    public static final BoolProperty NEEDS_CREDENTIALS = new BoolProperty("needsCredentials");
    public static final BoolProperty NEEDS_URL_ENCODING = new BoolProperty("needsURLEncoding");
    public static final BoolProperty SERIES_KEYS_ONLY_SUPPORTED = new BoolProperty("seriesKeysOnlySupported");
}
