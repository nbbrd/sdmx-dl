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
package internal.sdmxdl.provider.connectors;

import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.*;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxResponseException;
import nbbrd.io.text.BooleanProperty;
import sdmxdl.*;

import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Connectors {

    public sdmxdl.Dataflow toFlow(Dataflow o) {
        return sdmxdl.Dataflow.of(
                DataflowRef.parse(o.getFullIdentifier()),
                toStructureRef(o.getDsdIdentifier()),
                o.getDescription()
        );
    }

    public DataStructureRef toStructureRef(DSDIdentifier o) {
        return DataStructureRef.of(
                o.getAgency(),
                o.getId(),
                o.getVersion()
        );
    }

    public sdmxdl.Dimension toDimension(Dimension o) {
        return toComponent(sdmxdl.Dimension.builder(), o)
                .position(o.getPosition())
                .build();
    }

    public Attribute toAttribute(SdmxAttribute o) {
        return toComponent(Attribute.builder(), o)
                .build();
    }

    private <T extends Component.Builder<T>> T toComponent(T result, SdmxMetaElement o) {
        return result
                .id(o.getId())
                .codelist(toCodelist(o.getCodeList()))
                .label(toLabel(o));
    }

    private sdmxdl.Codelist toCodelist(Codelist o) {
        return o != null ? sdmxdl.Codelist.builder().ref(CodelistRef.of(o.getAgency(), o.getId(), o.getVersion())).codes(o).build() : null;
    }

    private String toLabel(SdmxMetaElement o) {
        String name = o.getName();
        return name != null ? name : o.getId();
    }

    public DataStructure toStructure(DataFlowStructure dsd) {
        return DataStructure.builder()
                .ref(DataStructureRef.of(dsd.getAgency(), dsd.getId(), dsd.getVersion()))
                .label(dsd.getName())
                .timeDimensionId(dsd.getTimeDimension())
                .primaryMeasureId(dsd.getMeasure())
                .dimensions(dsd.getDimensions().stream().map(Connectors::toDimension).collect(Collectors.toSet()))
                .attributes(dsd.getAttributes().stream().map(Connectors::toAttribute).collect(Collectors.toSet()))
                .build();
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
        fromComponent(result, o);
        result.setPosition(o.getPosition());
        return result;
    }

    public SdmxAttribute fromAttribute(Attribute o) {
        SdmxAttribute result = new SdmxAttribute();
        fromComponent(result, o);
        return result;
    }

    private void fromComponent(SdmxMetaElement result, Component o) {
        result.setId(o.getId());
        result.setName(o.getLabel());
        result.setCodeList(fromCodelist(o.getCodelist()));
    }

    private Codelist fromCodelist(sdmxdl.Codelist o) {
        if (o == null) {
            return null;
        }
        Codelist result = new Codelist();
        result.setAgency(o.getRef().getAgency());
        result.setId(o.getRef().getId());
        result.setVersion(o.getRef().getVersion());
        result.setCodes(o.getCodes());
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
        dsd.getAttributes().forEach(o -> result.setAttribute(fromAttribute(o)));
        return result;
    }

    public it.bancaditalia.oss.sdmx.util.LanguagePriorityList fromLanguages(LanguagePriorityList l) {
        return it.bancaditalia.oss.sdmx.util.LanguagePriorityList.parse(l.toString());
    }

    public boolean isNoResultMatchingQuery(SdmxException ex) {
        return ex instanceof SdmxResponseException && ((SdmxResponseException) ex).getResponseCode() == SdmxResponseException.SDMX_NO_RESULTS_FOUND;
    }

    public static final BooleanProperty SUPPORTS_COMPRESSION_PROPERTY =
            BooleanProperty.of("supportsCompression", false);

    public static final BooleanProperty NEEDS_CREDENTIALS_PROPERTY =
            BooleanProperty.of("needsCredentials", false);

    public static final BooleanProperty NEEDS_URL_ENCODING_PROPERTY =
            BooleanProperty.of("needsURLEncoding", false);
}
