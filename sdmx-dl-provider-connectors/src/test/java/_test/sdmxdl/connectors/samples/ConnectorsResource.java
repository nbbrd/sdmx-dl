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
package _test.sdmxdl.connectors.samples;

import internal.sdmxdl.connectors.Connectors;
import internal.sdmxdl.connectors.PortableTimeSeriesCursor;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.*;
import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.util.LanguagePriorityList;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.repo.DataSet;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.samples.ByteSource;
import sdmxdl.samples.SdmxSource;
import sdmxdl.util.parser.ObsFactories;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class ConnectorsResource {

    @NonNull
    public SdmxRepository nbb() throws IOException {
        LanguagePriorityList l = LanguagePriorityList.parse("fr");

        List<DataFlowStructure> structs = struct20(SdmxSource.NBB_DATA_STRUCTURE, l);
        List<Dataflow> flows = flow20(SdmxSource.NBB_DATA_STRUCTURE, l);
        List<PortableTimeSeries<Double>> data = data20(SdmxSource.NBB_DATA, structs.get(0), l);

        DataflowRef ref = firstOf(flows);

        return SdmxRepository.builder()
                .structures(structs.stream().map(Connectors::toStructure).collect(Collectors.toList()))
                .flows(flows.stream().map(Connectors::toFlow).collect(Collectors.toList()))
                .dataSet(DataSet.builder().ref(ref).copyOf(PortableTimeSeriesCursor.of(data, ObsFactories.SDMX20, Connectors.toStructure(structs.get(0)))).build())
                .name("NBB")
                .detailSupported(false)
                .build();
    }

    @NonNull
    public SdmxRepository ecb() throws IOException {
        LanguagePriorityList l = LanguagePriorityList.parse("fr");

        List<DataFlowStructure> structs = struct21(SdmxSource.ECB_DATA_STRUCTURE, l);
        List<Dataflow> flows = flow21(SdmxSource.ECB_DATAFLOWS, l);
        List<PortableTimeSeries<Double>> data = data21(SdmxSource.ECB_DATA, structs.get(0), l);

        DataflowRef ref = firstOf(flows);

        return SdmxRepository.builder()
                .structures(structs.stream().map(Connectors::toStructure).collect(Collectors.toList()))
                .flows(flows.stream().map(Connectors::toFlow).collect(Collectors.toList()))
                .dataSet(DataSet.builder().ref(ref).copyOf(PortableTimeSeriesCursor.of(data, ObsFactories.SDMX21, Connectors.toStructure(structs.get(0)))).build())
                .name("ECB")
                .detailSupported(true)
                .build();
    }

    private DataflowRef firstOf(List<Dataflow> flows) {
        return flows.stream().map(o -> Connectors.toFlow(o).getRef()).findFirst().orElseThrow(RuntimeException::new);
    }

    private List<DataFlowStructure> struct20(ByteSource xml, LanguagePriorityList l) throws IOException {
        return parse(xml, l, new it.bancaditalia.oss.sdmx.parser.v20.DataStructureParser());
    }

    private List<Dataflow> flow20(ByteSource xml, LanguagePriorityList l) throws IOException {
        return struct20(xml, l).stream()
                .map(ConnectorsResource::asDataflow)
                .collect(Collectors.toList());
    }

    private List<PortableTimeSeries<Double>> data20(ByteSource xml, DataFlowStructure dsd, LanguagePriorityList l) throws IOException {
        // No connectors impl
        return FacadeResource.data20(xml, Connectors.toStructure(dsd))
                .stream()
                .map((Series o) -> toPortableTimeSeries(o, dsd.getDimensions()))
                .collect(Collectors.toList());
    }

    public List<DataFlowStructure> struct21(ByteSource xml, LanguagePriorityList l) throws IOException {
        return parse(xml, l, new it.bancaditalia.oss.sdmx.parser.v21.DataStructureParser());
    }

    private List<Dataflow> flow21(ByteSource xml, LanguagePriorityList l) throws IOException {
        return parse(xml, l, new it.bancaditalia.oss.sdmx.parser.v21.DataflowParser());
    }

    public List<PortableTimeSeries<Double>> data21(ByteSource xml, DataFlowStructure dsd, LanguagePriorityList l) throws IOException {
        // No connectors impl
        return FacadeResource.data21(xml, Connectors.toStructure(dsd))
                .stream()
                .map((Series o) -> toPortableTimeSeries(o, dsd.getDimensions()))
                .collect(Collectors.toList());
    }

    private PortableTimeSeries<Double> toPortableTimeSeries(Series series, List<Dimension> dims) {
        PortableTimeSeries<Double> result = new PortableTimeSeries<>();
        result.setFrequency(String.valueOf(formatByStandardFreq(series.getFreq())));
        series.getMeta().forEach(result::addAttribute);
        Key key = series.getKey();
        for (int i = 0; i < key.size(); i++) {
            result.addDimension(dims.get(i).getId(), key.get(i));
        }
        series.getObs().forEach(obs -> result.add(toObservation(series.getFreq(), obs)));
        return result;
    }

    private static DoubleObservation toObservation(Frequency frequency, Obs obs) {
        return new DoubleObservation(toTimeslot(frequency, obs.getPeriod()), toValue(obs.getValue()), obs.getMeta());
    }

    private static String toTimeslot(Frequency f, LocalDateTime o) {
        if (o == null) {
            return "NULL";
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

    private static double toValue(Double nullableValue) {
        return nullableValue == null ? Double.NaN : nullableValue;
    }

    private Dataflow asDataflow(DataFlowStructure o) {
        Dataflow result = new Dataflow();
        result.setAgency(o.getAgency());
        result.setDsdIdentifier(new DSDIdentifier(o.getId(), o.getAgency(), o.getVersion()));
        result.setId(o.getId());
        result.setName(o.getName());
        result.setVersion(o.getVersion());
        return result;
    }

    private <T> T parse(ByteSource xml, LanguagePriorityList l, Parser<T> parser) throws IOException {
        XMLEventReader r = null;
        try {
            r = XIF.createXMLEventReader(xml.openReader());
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

    private final XMLInputFactory XIF = XMLInputFactory.newFactory();
}
