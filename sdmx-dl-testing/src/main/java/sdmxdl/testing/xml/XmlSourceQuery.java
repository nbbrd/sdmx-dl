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
package sdmxdl.testing.xml;

import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import org.checkerframework.checker.index.qual.NonNegative;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.testing.WebRequest;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("SwitchStatementWithTooFewBranches")
@lombok.experimental.UtilityClass
public class XmlSourceQuery {

    public static List<WebRequest> getDefaultRequests() {
        try {
            return XmlSourceQuery.getParser().parseResource(XmlSourceQuery.class, "/sdmxdl/testing/xml/Checks.xml");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static Xml.Parser<List<WebRequest>> getParser() {
        return PARSER;
    }

    private static final Xml.Parser<List<WebRequest>> PARSER = Stax.StreamParser.valueOf(XmlSourceQuery::parseEndpoints).andThen(XmlSourceQuery::toRequests);

    private static List<WebRequest> toRequests(List<SourceNode> list) {
        List<WebRequest> result = new ArrayList<>();
        for (SourceNode source : list) {
            for (FlowNode flow : source.getFlowQueries()) {
                for (DataNode data : flow.getData()) {
                    result.add(WebRequest
                            .builder()
                            .source(source.getName())
                            .flow(flow.getRef())
                            .key(data.getKey())
                            .minFlowCount(source.getMinFlowCount())
                            .dimensionCount(flow.getDimensionCount())
                            .minSeriesCount(data.getMinSeriesCount())
                            .minObsCount(data.getMinObsCount())
                            .build()
                    );
                }
            }
        }
        return result;
    }

    private static List<SourceNode> parseEndpoints(XMLStreamReader reader) throws XMLStreamException {
        List<SourceNode> result = new ArrayList<>();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "source":
                            result.add(parseEndpoint(reader));
                            break;
                    }
                    break;
            }
        }
        return result;
    }

    private static SourceNode parseEndpoint(XMLStreamReader reader) throws XMLStreamException {
        SourceNode.Builder result = SourceNode.builder()
                .name(reader.getAttributeValue(null, "name"))
                .minFlowCount(Integer.parseInt(reader.getAttributeValue(null, "minFlows")));
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "flow":
                            result.flowQuery(parseFlow(reader));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "source":
                            return result.build();
                    }
                    break;
            }
        }
        return result.build();
    }

    private static FlowNode parseFlow(XMLStreamReader reader) throws XMLStreamException {
        FlowNode.Builder result = FlowNode.builder()
                .ref(DataflowRef.parse(reader.getAttributeValue(null, "ref")))
                .dimensionCount(Integer.parseInt(reader.getAttributeValue(null, "dims")));
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "data":
                            result.data(DataNode.builder()
                                    .key(Key.parse(reader.getAttributeValue(null, "key")))
                                    .minSeriesCount(Integer.parseInt(reader.getAttributeValue(null, "minSeries")))
                                    .minObsCount(Integer.parseInt(reader.getAttributeValue(null, "minObs")))
                                    .build());
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "flow":
                            return result.build();
                    }
                    break;
            }
        }
        return result.build();
    }

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    private static class SourceNode {

        @lombok.NonNull
        String name;

        @NonNegative
        int minFlowCount;

        @lombok.Singular
        List<FlowNode> flowQueries;
    }

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    private static class FlowNode {

        @lombok.NonNull
        DataflowRef ref;

        @NonNegative
        int dimensionCount;

        @lombok.Singular(value = "data")
        List<DataNode> data;
    }

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    private static class DataNode {

        @lombok.NonNull
        Key key;

        @NonNegative
        int minSeriesCount;

        @NonNegative
        int minObsCount;
    }
}
