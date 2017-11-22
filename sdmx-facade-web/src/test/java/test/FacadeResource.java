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
package test;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.samples.ByteSource;
import be.nbb.sdmx.facade.samples.SdmxSource;
import be.nbb.sdmx.facade.repo.SdmxRepository;
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.util.SeriesSupport;
import be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams;
import be.nbb.sdmx.facade.xml.stream.Stax;
import internal.connectors.Util;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.stream.XMLInputFactory;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class FacadeResource {

    public SdmxRepository nbb() throws IOException {
        LanguagePriorityList l = LanguagePriorityList.parse("fr");

        List<DataStructure> structs = struct20(XIF, SdmxSource.NBB_DATA_STRUCTURE, l);
        List<Dataflow> flows = flow20(XIF, SdmxSource.NBB_DATA_STRUCTURE, l);
        List<Series> data = data20(XIF, SdmxSource.NBB_DATA, structs.get(0));

        DataflowRef ref = DataflowRef.of("NBB", "TEST_DATASET", null);

        return SdmxRepository.builder()
                .dataStructures(structs)
                .dataflows(flows)
                .data(ref, data)
                .name("NBB")
                .seriesKeysOnlySupported(false)
                .build();
    }

    public SdmxRepository ecb() throws IOException {
        LanguagePriorityList l = LanguagePriorityList.parse("fr");

        List<DataStructure> structs = struct21(XIF, SdmxSource.ECB_DATA_STRUCTURE, l);
        List<Dataflow> flows = flow21(XIF, SdmxSource.ECB_DATAFLOWS, l);
        List<Series> data = data21(XIF, SdmxSource.ECB_DATA, structs.get(0));

        DataflowRef ref = DataflowRef.of("ECB", "AME", "1.0");

        return SdmxRepository.builder()
                .dataStructures(structs)
                .dataflows(flows)
                .data(ref, data)
                .name("ECB")
                .seriesKeysOnlySupported(true)
                .build();
    }

    private List<DataStructure> struct20(XMLInputFactory f, ByteSource xml, LanguagePriorityList l) throws IOException {
        return SdmxXmlStreams.struct20(l).parseReader(f, xml::openReader);
    }

    private List<Dataflow> flow20(XMLInputFactory f, ByteSource xml, LanguagePriorityList l) throws IOException {
        return struct20(f, xml, l).stream()
                .map(FacadeResource::asDataflow)
                .collect(Collectors.toList());
    }

    List<Series> data20(XMLInputFactory f, ByteSource xml, DataStructure dsd) throws IOException {
        try (DataCursor c = SdmxXmlStreams.genericData20(dsd).parseReader(f, xml::openReader)) {
            return SeriesSupport.copyOf(c);
        }
    }

    private List<DataStructure> struct21(XMLInputFactory f, ByteSource xml, LanguagePriorityList l) throws IOException {
        return SdmxXmlStreams.struct21(l).parseReader(f, xml::openReader);
    }

    private List<Dataflow> flow21(XMLInputFactory f, ByteSource xml, LanguagePriorityList l) throws IOException {
        // FIXME: no facade impl yet
        return ConnectorsResource.parse(xml, Util.fromLanguages(l), new it.bancaditalia.oss.sdmx.parser.v21.DataflowParser())
                .stream()
                .map(Util::toFlow)
                .collect(Collectors.toList());
    }

    List<Series> data21(XMLInputFactory f, ByteSource xml, DataStructure dsd) throws IOException {
        try (DataCursor c = SdmxXmlStreams.genericData21(dsd).parseReader(f, xml::openReader)) {
            return SeriesSupport.copyOf(c);
        }
    }

    private Dataflow asDataflow(DataStructure o) {
        DataflowRef ref = DataflowRef.of(o.getRef().getAgency(), o.getRef().getId(), o.getRef().getVersion());
        return Dataflow.of(ref, o.getRef(), o.getLabel());
    }
    
    private final XMLInputFactory XIF = Stax.getInputFactory();
}
