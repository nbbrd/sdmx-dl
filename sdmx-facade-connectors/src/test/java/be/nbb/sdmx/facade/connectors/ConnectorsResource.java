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

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.samples.ByteSource;
import be.nbb.sdmx.facade.samples.SdmxSource;
import be.nbb.sdmx.facade.util.MemSdmxRepository;
import be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.parser.v20.GenericDataParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class ConnectorsResource {

    @Nonnull
    public MemSdmxRepository nbb() throws IOException {
        List<DataFlowStructure> structs = struct20(SdmxSource.NBB_DATA_STRUCTURE);
        List<Dataflow> flows = flow20(SdmxSource.NBB_DATA_STRUCTURE);
        List<PortableTimeSeries> data = data20(SdmxSource.NBB_DATA, structs.get(0));

        DataflowRef ref = flows.stream().map(o -> Util.toDataflow(o).getFlowRef()).findFirst().get();

        return MemSdmxRepository.builder()
                .dataStructures(structs.stream().map(Util::toDataStructure).collect(Collectors.toList()))
                .dataflows(flows.stream().map(Util::toDataflow).collect(Collectors.toList()))
                .copyOf(ref, new DataCursorAdapter(data))
                .name("NBB")
                .seriesKeysOnlySupported(false)
                .build();
    }

    @Nonnull
    public MemSdmxRepository ecb() throws IOException {
        List<DataFlowStructure> structs = struct21(SdmxSource.ECB_DATA_STRUCTURE);
        List<Dataflow> flows = flow21(SdmxSource.ECB_DATAFLOWS);
        List<PortableTimeSeries> data = data21(SdmxSource.ECB_DATA, structs.get(0));

        DataflowRef ref = flows.stream().map(o -> Util.toDataflow(o).getFlowRef()).findFirst().get();

        return MemSdmxRepository.builder()
                .dataStructures(structs.stream().map(Util::toDataStructure).collect(Collectors.toList()))
                .dataflows(flows.stream().map(Util::toDataflow).collect(Collectors.toList()))
                .copyOf(ref, new DataCursorAdapter(data))
                .name("ECB")
                .seriesKeysOnlySupported(true)
                .build();
    }

    List<DataFlowStructure> struct20(ByteSource xml) throws IOException {
        try (InputStreamReader r = xml.openReader()) {
            return it.bancaditalia.oss.sdmx.parser.v20.DataStructureParser.parse(r);
        } catch (XMLStreamException | SdmxException ex) {
            throw new IOException(ex);
        }
    }

    List<Dataflow> flow20(ByteSource xml) throws IOException {
        return struct20(xml).stream()
                .map(ConnectorsResource::asDataflow)
                .collect(Collectors.toList());
    }

    List<PortableTimeSeries> data20(ByteSource xml, DataFlowStructure dsd) throws IOException {
        try (InputStreamReader r = xml.openReader()) {
            return GenericDataParser.parse(r, dsd, null, true).getData();
        } catch (XMLStreamException | SdmxException ex) {
            throw new IOException(ex);
        }
    }

    List<DataFlowStructure> struct21(ByteSource xml) throws IOException {
        try (InputStreamReader r = xml.openReader()) {
            return it.bancaditalia.oss.sdmx.parser.v21.DataStructureParser.parse(r);
        } catch (XMLStreamException | SdmxException ex) {
            throw new IOException(ex);
        }
    }

    List<Dataflow> flow21(ByteSource xml) throws IOException {
        try (InputStreamReader r = xml.openReader()) {
            return it.bancaditalia.oss.sdmx.parser.v21.DataflowParser.parse(r);
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    List<PortableTimeSeries> data21(ByteSource xml, DataFlowStructure dsd) throws IOException {
        // FIXME: no connectors impl yet
        try (DataCursor cursor = SdmxXmlStreams.genericData21(XMLInputFactory.newFactory(), xml.openReader(), Util.toDataStructure(dsd))) {
            List<Dimension> dims = dsd.getDimensions();
            List<PortableTimeSeries> result = new ArrayList<>();
            while (cursor.nextSeries()) {
                PortableTimeSeries series = new PortableTimeSeries();
                series.setFrequency("A");
                cursor.getSeriesAttributes().forEach((k, v) -> series.addAttribute(k + '=' + v));
                Key key = cursor.getSeriesKey();
                for (int i = 0; i < key.size(); i++) {
                    series.addDimension(dims.get(i).getId() + '=' + key.get(i));
                }
                while (cursor.nextObs()) {
                    LocalDateTime period = cursor.getObsPeriod();
                    if (period != null) {
                        Double value = cursor.getObsValue();
                        series.addObservation(value != null ? value.toString() : "", String.valueOf(period.getYear()), null);
                    }
                }
                result.add(series);
            }
            return result;
        }
    }

    Dataflow asDataflow(DataFlowStructure o) {
        Dataflow result = new Dataflow();
        result.setAgency(o.getAgency());
        result.setDsdIdentifier(new DSDIdentifier(o.getId(), o.getAgency(), o.getVersion()));
        result.setId(o.getId());
        result.setName(o.getName());
        result.setVersion(o.getVersion());
        return result;
    }
}
