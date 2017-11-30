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
package internal.web.sdmx21;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.parser.DataFactory;
import be.nbb.sdmx.facade.util.SdmxExceptions;
import static be.nbb.sdmx.facade.util.SdmxMediaType.*;
import static be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams.*;
import be.nbb.util.IO;
import be.nbb.util.Stax;
import internal.web.RestClient;
import static internal.web.sdmx21.Sdmx21RestQueries.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
public final class Sdmx21RestClient implements RestClient {

    private final URL endpoint;
    private final boolean seriesKeysOnlySupported;
    private final LanguagePriorityList langs;
    private final RestExecutor executor;

    private final DataFactory dialect = DataFactory.sdmx21();

    @Override
    public List<Dataflow> getFlows() throws IOException {
        URL url = getFlowsQuery(endpoint);
        return flow21(langs)
                .onInputStream(Stax.getInputFactory(), UTF_8)
                .parseWithIO(calling(url, XML));
    }

    @Override
    public Dataflow getFlow(DataflowRef ref) throws IOException {
        URL url = getFlowQuery(endpoint, ref);
        return flow21(langs)
                .onInputStream(Stax.getInputFactory(), UTF_8)
                .parseWithIO(calling(url, XML))
                .stream()
                .filter(ref::containsRef)
                .findFirst()
                .orElseThrow(() -> SdmxExceptions.missingFlow(ref));
    }

    @Override
    public DataStructure getStructure(DataStructureRef ref) throws IOException {
        URL url = getStructureQuery(endpoint, ref);
        return struct21(langs)
                .onInputStream(Stax.getInputFactory(), UTF_8)
                .parseWithIO(calling(url, STRUCTURE_21))
                .stream()
                .filter(ref::equalsRef)
                .findFirst()
                .orElseThrow(() -> SdmxExceptions.missingStructure(ref));
    }

    @Override
    public DataCursor getData(DataflowRef flowRef, DataQuery query, DataStructure dsd) throws IOException {
        URL url = getDataQuery(endpoint, flowRef, query);
        return compactData21(dsd, dialect)
                .onInputStream(Stax.getInputFactoryWithoutNamespace(), UTF_8)
                .parseWithIO(calling(url, STRUCTURE_SPECIFIC_DATA_21));
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        return seriesKeysOnlySupported;
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
        return null;
    }

    private IO.Supplier<? extends InputStream> calling(URL query, String mediaType) throws IOException {
        return () -> executor.execute(query, mediaType, langs);
    }
}
