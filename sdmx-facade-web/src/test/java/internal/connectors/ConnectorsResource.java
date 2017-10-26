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

import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.samples.ByteSource;
import be.nbb.sdmx.facade.samples.SdmxSource;
import be.nbb.sdmx.facade.repo.SdmxRepository;
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.util.ObsParser;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.LanguagePriorityList;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class ConnectorsResource {

    @Nonnull
    public SdmxRepository nbb() throws IOException {
        LanguagePriorityList l = LanguagePriorityList.parse("fr");

        List<DataFlowStructure> structs = struct20(SdmxSource.NBB_DATA_STRUCTURE, l);
        List<Dataflow> flows = flow20(SdmxSource.NBB_DATA_STRUCTURE, l);
        List<PortableTimeSeries> data = data20(SdmxSource.NBB_DATA, structs.get(0), l);

        DataflowRef ref = firstOf(flows);

        return SdmxRepository.builder()
                .dataStructures(structs.stream().map(Util::toDataStructure).collect(Collectors.toList()))
                .dataflows(flows.stream().map(Util::toDataflow).collect(Collectors.toList()))
                .copyOf(ref, new PortableTimeSeriesCursor(data, ObsParser.standard()))
                .name("NBB")
                .seriesKeysOnlySupported(false)
                .build();
    }

    @Nonnull
    public SdmxRepository ecb() throws IOException {
        LanguagePriorityList l = LanguagePriorityList.parse("fr");

        List<DataFlowStructure> structs = struct21(SdmxSource.ECB_DATA_STRUCTURE, l);
        List<Dataflow> flows = flow21(SdmxSource.ECB_DATAFLOWS, l);
        List<PortableTimeSeries> data = data21(SdmxSource.ECB_DATA, structs.get(0), l);

        DataflowRef ref = firstOf(flows);

        return SdmxRepository.builder()
                .dataStructures(structs.stream().map(Util::toDataStructure).collect(Collectors.toList()))
                .dataflows(flows.stream().map(Util::toDataflow).collect(Collectors.toList()))
                .copyOf(ref, new PortableTimeSeriesCursor(data, ObsParser.standard()))
                .name("ECB")
                .seriesKeysOnlySupported(true)
                .build();
    }

    DataflowRef firstOf(List<Dataflow> flows) {
        return flows.stream().map(o -> Util.toDataflow(o).getFlowRef()).findFirst().get();
    }

    List<DataFlowStructure> struct20(ByteSource xml, LanguagePriorityList l) throws IOException {
        return parse(xml, l, new it.bancaditalia.oss.sdmx.parser.v20.DataStructureParser());
    }

    List<Dataflow> flow20(ByteSource xml, LanguagePriorityList l) throws IOException {
        return struct20(xml, l).stream()
                .map(ConnectorsResource::asDataflow)
                .collect(Collectors.toList());
    }

    List<PortableTimeSeries> data20(ByteSource xml, DataFlowStructure dsd, LanguagePriorityList l) throws IOException {
        // No connectors impl
        return FacadeResource.data20(XMLInputFactory.newFactory(), xml, Util.toDataStructure(dsd))
                .stream()
                .map((Series o) -> toPortableTimeSeries(o, dsd.getDimensions()))
                .collect(Collectors.toList());
    }

    List<DataFlowStructure> struct21(ByteSource xml, LanguagePriorityList l) throws IOException {
        return parse(xml, l, new it.bancaditalia.oss.sdmx.parser.v21.DataStructureParser());
    }

    List<Dataflow> flow21(ByteSource xml, LanguagePriorityList l) throws IOException {
        return parse(xml, l, new it.bancaditalia.oss.sdmx.parser.v21.DataflowParser());
    }

    List<PortableTimeSeries> data21(ByteSource xml, DataFlowStructure dsd, LanguagePriorityList l) throws IOException {
        // No connectors impl
        return FacadeResource.data21(XMLInputFactory.newFactory(), xml, Util.toDataStructure(dsd))
                .stream()
                .map((Series o) -> toPortableTimeSeries(o, dsd.getDimensions()))
                .collect(Collectors.toList());
    }

    PortableTimeSeries toPortableTimeSeries(Series o, List<Dimension> dims) {
        PortableTimeSeries result = new PortableTimeSeries();
        result.setFrequency(String.valueOf(formatByStandardFreq(o.getFrequency())));
        o.getMeta().forEach(result::addAttribute);
        Key key = o.getKey();
        for (int i = 0; i < key.size(); i++) {
            result.addDimension(dims.get(i).getId(), key.get(i));
        }
        o.getObs().forEach(x -> result.addObservation(valueToString(x.getValue()), periodToString(o.getFrequency(), x.getPeriod()), null));
        return result;
    }

    String valueToString(Double o) {
        return o != null ? o.toString() : "";
    }

    String periodToString(Frequency f, LocalDateTime o) {
        if (o == null) {
            return "";
        }
        switch (f) {
            case ANNUAL:
                return String.valueOf(o.getYear());
            case MONTHLY:
                return YearMonth.from(o).toString();
            default:
                throw new RuntimeException("Not implemented yet");
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

    <T> T parse(ByteSource xml, LanguagePriorityList l, Parser<T> parser) throws IOException {
        XMLEventReader r = null;
        try {
            r = xml.openXmlEvent(SdmxSource.XIF);
            return parser.parse(r, l);
        } catch (XMLStreamException | SdmxException ex) {
            throw new IOException(ex);
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (XMLStreamException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private char formatByStandardFreq(Frequency code) {
        switch (code) {
            case ANNUAL:
                return 'A';
            case HALF_YEARLY:
                return 'S';
            case QUARTERLY:
                return 'Q';
            case MONTHLY:
                return 'M';
            case WEEKLY:
                return 'W';
            case DAILY:
                return 'D';
            case HOURLY:
                return 'H';
            case DAILY_BUSINESS:
                return 'B';
            case MINUTELY:
                return 'N';
            default:
                return '?';
        }
    }
}
