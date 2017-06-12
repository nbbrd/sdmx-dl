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
package be.nbb.sdmx.facade.connectors;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.samples.ByteSource;
import be.nbb.sdmx.facade.samples.SdmxSource;
import be.nbb.sdmx.facade.util.MemSdmxRepository;
import be.nbb.sdmx.facade.util.MemSdmxRepository.Series;
import be.nbb.sdmx.facade.xml.stream.XMLStreamCursors;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Philippe Charles
 */
public final class FacadeResource {

    public static MemSdmxRepository nbb() throws IOException {
        XMLInputFactory f = XMLInputFactory.newFactory();

        DataflowRef ref = DataflowRef.of("NBB", "TEST_DATASET", null);

        List<DataStructure> structs = struct20(f, SdmxSource.NBB_DATA_STRUCTURE);
        List<Dataflow> flows = flow20(f, SdmxSource.NBB_DATA_STRUCTURE);
        List<Series> data = data20(f, SdmxSource.NBB_DATA, structs.get(0), ref);

        return MemSdmxRepository.builder()
                .dataStructures(structs)
                .dataflows(flows)
                .data(data)
                .name("NBB")
                .seriesKeysOnlySupported(false)
                .build();
    }

    public static MemSdmxRepository ecb() throws IOException {
        XMLInputFactory f = XMLInputFactory.newFactory();

        DataflowRef ref = DataflowRef.of("ECB", "AME", "1.0");

        List<DataStructure> structs = struct21(f, SdmxSource.ECB_DATA_STRUCTURE);
        List<Dataflow> flows = flow21(f, SdmxSource.ECB_DATAFLOWS);
        List<Series> data = data21(f, SdmxSource.ECB_DATA, structs.get(0), ref);

        return MemSdmxRepository.builder()
                .dataStructures(structs)
                .dataflows(flows)
                .data(data)
                .name("ECB")
                .seriesKeysOnlySupported(true)
                .build();
    }

    private static List<DataStructure> struct20(XMLInputFactory f, ByteSource xml) throws IOException {
        try (InputStreamReader r = xml.openReader()) {
            return XMLStreamCursors.struct20(f, r, "en");
        }
    }

    private static List<Dataflow> flow20(XMLInputFactory f, ByteSource xml) throws IOException {
        return struct20(f, xml).stream()
                .map(FacadeResource::asDataflow)
                .collect(Collectors.toList());
    }

    private static List<Series> data20(XMLInputFactory f, ByteSource xml, DataStructure dsd, DataflowRef ref) throws IOException {
        try (DataCursor cursor = XMLStreamCursors.genericData20(f, xml.openReader(), dsd)) {
            return Series.copyOf(ref, cursor);
        }
    }

    private static List<DataStructure> struct21(XMLInputFactory f, ByteSource xml) throws IOException {
        try (InputStreamReader r = xml.openReader()) {
            return XMLStreamCursors.struct21(f, r, "en");
        }
    }

    private static List<Dataflow> flow21(XMLInputFactory f, ByteSource xml) throws IOException {
        try (InputStreamReader r = xml.openReader()) {
            // FIXME: no facade impl yet
            return it.bancaditalia.oss.sdmx.parser.v21.DataflowParser.parse(r).stream()
                    .map(Util::toDataflow)
                    .collect(Collectors.toList());
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    private static List<Series> data21(XMLInputFactory f, ByteSource xml, DataStructure dsd, DataflowRef ref) throws IOException {
        try (DataCursor cursor = XMLStreamCursors.genericData21(f, xml.openReader(), dsd)) {
            return Series.copyOf(ref, cursor);
        }
    }

    private static Dataflow asDataflow(DataStructure o) {
        DataflowRef ref = DataflowRef.of(o.getRef().getAgencyId(), o.getRef().getId(), o.getRef().getVersion());
        return Dataflow.of(ref, o.getRef(), o.getLabel());
    }
}
