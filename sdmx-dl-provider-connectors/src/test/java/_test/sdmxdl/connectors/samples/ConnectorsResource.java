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

import internal.sdmxdl.provider.connectors.Connectors;
import internal.sdmxdl.provider.connectors.PortableTimeSeriesCursor;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.*;
import it.bancaditalia.oss.sdmx.client.Parser;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.format.ObsParser;
import tests.sdmxdl.api.ByteSource;
import tests.sdmxdl.format.xml.SdmxXmlSources;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;
import static sdmxdl.DataSet.toDataSet;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class ConnectorsResource {

    @NonNull
    public DataRepository nbb() throws IOException {
        List<Locale.LanguageRange> l = Locale.LanguageRange.parse("fr");

        List<DataFlowStructure> structs = struct20(SdmxXmlSources.NBB_DATA_STRUCTURE, l);
        List<Dataflow> flows = flow20(SdmxXmlSources.NBB_DATA_STRUCTURE, l);
        List<PortableTimeSeries<Double>> data = data20(SdmxXmlSources.NBB_DATA, structs.get(0), l);

        DataflowRef ref = firstOf(flows);

        return DataRepository
                .builder()
                .structures(structs.stream().map(Connectors::toStructure).collect(toList()))
                .flows(flows.stream().map(Connectors::toFlow).collect(toList()))
                .dataSet(
                        PortableTimeSeriesCursor
                                .of(data, ObsParser::newDefault, Connectors.toStructure(structs.get(0)))
                                .asStream()
                                .collect(toDataSet(ref, DataQuery.ALL)))
                .name("NBB")
                .build();
    }

    @NonNull
    public DataRepository ecb() throws IOException {
        List<Locale.LanguageRange> l = Locale.LanguageRange.parse("fr");

        List<DataFlowStructure> structs = struct21(SdmxXmlSources.ECB_DATA_STRUCTURE, l);
        List<Dataflow> flows = flow21(SdmxXmlSources.ECB_DATAFLOWS, l);
        List<PortableTimeSeries<Double>> data = data21(SdmxXmlSources.ECB_DATA, structs.get(0), l);

        DataflowRef ref = firstOf(flows);

        return DataRepository
                .builder()
                .structures(structs.stream().map(Connectors::toStructure).collect(toList()))
                .flows(flows.stream().map(Connectors::toFlow).collect(toList()))
                .dataSet(
                        PortableTimeSeriesCursor
                                .of(data, ObsParser::newDefault, Connectors.toStructure(structs.get(0)))
                                .asStream()
                                .collect(toDataSet(ref, DataQuery.ALL)))
                .name("ECB")
                .build();
    }

    private DataflowRef firstOf(List<Dataflow> flows) {
        return flows.stream().map(o -> Connectors.toFlow(o).getRef()).findFirst().orElseThrow(RuntimeException::new);
    }

    private List<DataFlowStructure> struct20(ByteSource xml, List<Locale.LanguageRange> l) throws IOException {
        return parseXml(xml, l, new it.bancaditalia.oss.sdmx.parser.v20.DataStructureParser());
    }

    private List<Dataflow> flow20(ByteSource xml, List<Locale.LanguageRange> l) throws IOException {
        return struct20(xml, l).stream()
                .map(ConnectorsResource::asDataflow)
                .collect(toList());
    }

    private List<PortableTimeSeries<Double>> data20(ByteSource xml, DataFlowStructure dsd, List<Locale.LanguageRange> l) throws IOException {
        // No connectors impl
        return FacadeResource.data20(xml, Connectors.toStructure(dsd))
                .stream()
                .map((Series series) -> toPortableTimeSeries(series, dsd.getDimensions()))
                .collect(toList());
    }

    public List<DataFlowStructure> struct21(ByteSource xml, List<Locale.LanguageRange> l) throws IOException {
        return parseXml(xml, l, new it.bancaditalia.oss.sdmx.parser.v21.DataStructureParser());
    }

    private List<Dataflow> flow21(ByteSource xml, List<Locale.LanguageRange> l) throws IOException {
        return parseXml(xml, l, new it.bancaditalia.oss.sdmx.parser.v21.DataflowParser());
    }

    public List<PortableTimeSeries<Double>> data21(ByteSource xml, DataFlowStructure dsd, List<Locale.LanguageRange> l) throws IOException {
        return parseXml(xml, l, new it.bancaditalia.oss.sdmx.parser.v21.GenericDataParser(dsd, null, true));
    }

    private PortableTimeSeries<Double> toPortableTimeSeries(Series series, List<Dimension> dims) {
        PortableTimeSeries<Double> result = new PortableTimeSeries<>();
//        result.setFrequency(String.valueOf(formatByStandardFreq(series.getFreq())));
        series.getMeta().forEach(result::addAttribute);
        Key key = series.getKey();
        for (int i = 0; i < key.size(); i++) {
            result.addDimension(dims.get(i).getId(), key.get(i));
        }
        series.getObs().forEach(obs -> result.add(toObservation(obs)));
        return result;
    }

    private static DoubleObservation toObservation(Obs obs) {
        return new DoubleObservation(toTimeslot(obs.getPeriod()), toValue(obs.getValue()), obs.getMeta());
    }

    private static String toTimeslot(LocalDateTime o) {
        return o == null ? "NULL" : o.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private static double toValue(Double nullableValue) {
        return nullableValue == null ? Double.NaN : nullableValue;
    }

    private Dataflow asDataflow(DataFlowStructure o) {
        Dataflow result = new Dataflow(
                o.getId(),
                o.getAgency(),
                o.getVersion());
        result.setDsdIdentifier(new SDMXReference(o.getId(), o.getAgency(), o.getVersion()));
        result.setName(o.getName());
        return result;
    }

    private <T> T parseXml(ByteSource xml, List<Locale.LanguageRange> l, Parser<T> parser) throws IOException {
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

//    private char formatByStandardFreq(Frequency code) {
//        switch (code) {
//            case ANNUAL:
//                return 'A';
//            case HALF_YEARLY:
//                return 'S';
//            case QUARTERLY:
//                return 'Q';
//            case MONTHLY:
//                return 'M';
//            case WEEKLY:
//                return 'W';
//            case DAILY:
//                return 'D';
//            case HOURLY:
//                return 'H';
//            case DAILY_BUSINESS:
//                return 'B';
//            case MINUTELY:
//                return 'N';
//            default:
//                return '?';
//        }
//    }

    private final XMLInputFactory XIF = XMLInputFactory.newFactory();
}
