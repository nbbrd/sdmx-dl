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
package _test.sdmxdl.connectors.samples;

import sdmxdl.DataCursor;
import sdmxdl.DataFilter;
import sdmxdl.DataStructure;
import sdmxdl.DataStructureRef;
import sdmxdl.Dataflow;
import sdmxdl.DataflowRef;
import sdmxdl.LanguagePriorityList;
import sdmxdl.samples.ByteSource;
import sdmxdl.samples.SdmxSource;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.Series;
import sdmxdl.util.parser.DataFactory;
import sdmxdl.xml.stream.SdmxXmlStreams;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class FacadeResource {

    public static final DataflowRef ECB_FLOW_REF = DataflowRef.of("ECB", "AME", "1.0");
    public static final DataStructureRef ECB_STRUCT_REF = DataStructureRef.of("ECB", "ECB_AME1", "1.0");

    public static final DataflowRef NBB_FLOW_REF = DataflowRef.of("NBB", "TEST_DATASET", null);

    public SdmxRepository nbb() throws IOException {
        SdmxRepository result = NBB.get();
        if (result == null) {
            LanguagePriorityList l = LanguagePriorityList.parse("fr");

            List<DataStructure> structs = struct20(SdmxSource.NBB_DATA_STRUCTURE, l);
            List<Dataflow> flows = flow20(SdmxSource.NBB_DATA_STRUCTURE, l);
            List<Series> data = data20(SdmxSource.NBB_DATA, structs.get(0));

            result = SdmxRepository.builder()
                    .structures(structs)
                    .flows(flows)
                    .data(NBB_FLOW_REF, data)
                    .name("NBB")
                    .seriesKeysOnlySupported(false)
                    .build();

            NBB.set(result);
        }
        return result;
    }

    public SdmxRepository ecb() throws IOException {
        SdmxRepository result = ECB.get();
        if (result == null) {
            LanguagePriorityList l = LanguagePriorityList.parse("fr");

            List<DataStructure> structs = struct21(SdmxSource.ECB_DATA_STRUCTURE, l);
            List<Dataflow> flows = flow21(SdmxSource.ECB_DATAFLOWS, l);
            List<Series> data = data21(SdmxSource.ECB_DATA, structs.get(0));

            result = SdmxRepository.builder()
                    .structures(structs)
                    .flows(flows)
                    .data(ECB_FLOW_REF, data)
                    .name("ECB")
                    .seriesKeysOnlySupported(true)
                    .build();

            ECB.set(result);
        }
        return result;
    }

    private static final AtomicReference<SdmxRepository> NBB = new AtomicReference<>();
    private static final AtomicReference<SdmxRepository> ECB = new AtomicReference<>();

    private List<DataStructure> struct20(ByteSource xml, LanguagePriorityList l) throws IOException {
        return SdmxXmlStreams.struct20(l).parseReader(xml::openReader);
    }

    private List<Dataflow> flow20(ByteSource xml, LanguagePriorityList l) throws IOException {
        // FIXME: find sample of dataflow20 ?
        return struct20(xml, l).stream()
                .map(FacadeResource::asDataflow)
                .collect(Collectors.toList());
    }

    List<Series> data20(ByteSource xml, DataStructure dsd) throws IOException {
        try (DataCursor c = SdmxXmlStreams.genericData20(dsd, DataFactory.sdmx20()).parseReader(xml::openReader)) {
            return c.toStream(DataFilter.Detail.FULL).collect(Collectors.toList());
        }
    }

    private List<DataStructure> struct21(ByteSource xml, LanguagePriorityList l) throws IOException {
        return SdmxXmlStreams.struct21(l).parseReader(xml::openReader);
    }

    private List<Dataflow> flow21(ByteSource xml, LanguagePriorityList l) throws IOException {
        return SdmxXmlStreams.flow21(l).parseReader(xml::openReader);
    }

    List<Series> data21(ByteSource xml, DataStructure dsd) throws IOException {
        try (DataCursor c = SdmxXmlStreams.genericData21(dsd, DataFactory.sdmx21()).parseReader(xml::openReader)) {
            return c.toStream(DataFilter.Detail.FULL).collect(Collectors.toList());
        }
    }

    private Dataflow asDataflow(DataStructure o) {
        DataflowRef ref = DataflowRef.of(o.getRef().getAgency(), o.getRef().getId(), o.getRef().getVersion());
        return Dataflow.of(ref, o.getRef(), o.getLabel());
    }
}
