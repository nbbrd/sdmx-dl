/*
 * Copyright 2018 National Bank of Belgium
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
package internal.web.drivers;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.util.SdmxExceptions;
import static be.nbb.sdmx.facade.util.SdmxMediaType.XML;
import be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams;
import internal.util.rest.RestClient;
import internal.util.rest.RestQueryBuilder;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;

/**
 *
 * @author Philippe Charles
 */
@AllArgsConstructor
class AbstractDotStat extends AbstractRestClient {

    protected final URL endpoint;
    protected final LanguagePriorityList langs;
    protected final RestClient executor;

    @Override
    protected URL getFlowsQuery() throws IOException {
        return getFlowsQuery(endpoint).build();
    }

    @Override
    protected List<Dataflow> getFlows(URL url) throws IOException {
        return SdmxXmlStreams
                .struct20(langs)
                .parseStream(() -> executor.openStream(url, XML, langs.toString()))
                .stream()
                .map(AbstractDotStat::asDataflow)
                .collect(Collectors.toList());
    }

    @Override
    protected URL getFlowQuery(DataflowRef ref) throws IOException {
        return getFlowQuery(endpoint, ref).build();
    }

    @Override
    protected Dataflow getFlow(URL url, DataflowRef ref) throws IOException {
        return SdmxXmlStreams
                .struct20(langs)
                .parseStream(() -> executor.openStream(url, XML, langs.toString()))
                .stream()
                .map(AbstractDotStat::asDataflow)
                .findFirst()
                .orElseThrow(() -> SdmxExceptions.missingFlow(ref));
    }

    @Override
    protected URL getStructureQuery(DataStructureRef ref) throws IOException {
        return getStructureQuery(endpoint, ref).build();
    }

    @Override
    protected DataStructure getStructure(URL url, DataStructureRef ref) throws IOException {
        return SdmxXmlStreams
                .struct20(langs)
                .parseStream(() -> executor.openStream(url, XML, langs.toString()))
                .stream()
                .findFirst()
                .orElseThrow(() -> SdmxExceptions.missingStructure(ref));
    }

    @Override
    protected URL getDataQuery(DataflowRef flowRef, DataQuery query) throws IOException {
        return getDataQuery(endpoint, flowRef, query).build();
    }

    @Override
    protected DataCursor getData(DataStructure dsd, URL url) throws IOException {
        return SdmxXmlStreams
                .compactData20(dsd)
                .parseStream(() -> executor.openStream(url, XML, langs.toString()));
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        return false;
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
        return asDataStructureRef(flowRef);
    }

    @Nonnull
    static RestQueryBuilder getFlowsQuery(@Nonnull URL endpoint) throws IOException {
        return RestQueryBuilder.of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path("ALL");
    }

    @Nonnull
    static RestQueryBuilder getFlowQuery(@Nonnull URL endpoint, @Nonnull DataflowRef ref) throws IOException {
        return RestQueryBuilder.of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path(ref.getId());
    }

    @Nonnull
    static RestQueryBuilder getStructureQuery(@Nonnull URL endpoint, @Nonnull DataStructureRef ref) throws IOException {
        return RestQueryBuilder.of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path(ref.getId());
    }

    @Nonnull
    static RestQueryBuilder getDataQuery(@Nonnull URL endpoint, @Nonnull DataflowRef flowRef, @Nonnull DataQuery query) throws IOException {
        return RestQueryBuilder.of(endpoint)
                .path(DATA_RESOURCE)
                .path(flowRef.getId())
                .path(query.getKey().toString())
                .param("format", "compact_v2");
    }

    @Nonnull
    static Dataflow asDataflow(@Nonnull DataStructure o) {
        return Dataflow.of(asDataflowRef(o.getRef()), o.getRef(), o.getLabel());
    }

    @Nonnull
    static DataflowRef asDataflowRef(@Nonnull DataStructureRef o) {
        return DataflowRef.of(o.getAgency(), o.getId(), o.getVersion());
    }

    @Nonnull
    static DataStructureRef asDataStructureRef(@Nonnull DataflowRef o) {
        return DataStructureRef.of(o.getAgency(), o.getId(), o.getVersion());
    }

    static final String DATASTRUCTURE_RESOURCE = "GetDataStructure";
    static final String DATA_RESOURCE = "GetData";
}
