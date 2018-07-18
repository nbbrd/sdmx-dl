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
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.parser.DataFactory;
import be.nbb.sdmx.facade.util.SdmxExceptions;
import static be.nbb.sdmx.facade.util.SdmxMediaType.XML;
import be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams;
import internal.util.rest.RestClient;
import internal.util.rest.RestQueryBuilder;
import internal.web.DataRequest;
import ioutil.IO;
import java.io.IOException;
import java.io.InputStream;
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
class DotStatRestClient extends AbstractRestClient {

    protected final String name;
    protected final URL endpoint;
    protected final LanguagePriorityList langs;
    protected final RestClient executor;

    @Override
    public String getName() throws IOException {
        return name;
    }

    @Override
    protected URL getFlowsQuery() throws IOException {
        return getFlowsQuery(endpoint).build();
    }

    @Override
    protected List<Dataflow> getFlows(URL url) throws IOException {
        return SdmxXmlStreams
                .struct20(langs)
                .parseStream(calling(url, XML))
                .stream()
                .map(DotStatRestClient::getFlowFromStructure)
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
                .parseStream(calling(url, XML))
                .stream()
                .map(DotStatRestClient::getFlowFromStructure)
                .findFirst()
                .orElseThrow(() -> SdmxExceptions.missingFlow(name, ref));
    }

    @Override
    protected URL getStructureQuery(DataStructureRef ref) throws IOException {
        return getStructureQuery(endpoint, ref).build();
    }

    @Override
    protected DataStructure getStructure(URL url, DataStructureRef ref) throws IOException {
        return SdmxXmlStreams
                .struct20(langs)
                .parseStream(calling(url, XML))
                .stream()
                .findFirst()
                .orElseThrow(() -> SdmxExceptions.missingStructure(name, ref));
    }

    @Override
    protected URL getDataQuery(DataRequest request) throws IOException {
        return getDataQuery(endpoint, request).build();
    }

    @Override
    protected DataCursor getData(DataStructure dsd, URL url) throws IOException {
        return SdmxXmlStreams
                .compactData20(dsd, DataFactory.sdmx20())
                .parseStream(calling(url, XML));
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        return false;
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
        return getStructureRefFromFlowRef(flowRef);
    }

    private IO.Supplier<? extends InputStream> calling(URL query, String mediaType) throws IOException {
        return () -> executor.openStream(query, mediaType, langs.toString());
    }

    @Nonnull
    static RestQueryBuilder getFlowsQuery(@Nonnull URL endpoint) throws IOException {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path("ALL");
    }

    @Nonnull
    static RestQueryBuilder getFlowQuery(@Nonnull URL endpoint, @Nonnull DataflowRef ref) throws IOException {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path(ref.getId());
    }

    @Nonnull
    static RestQueryBuilder getStructureQuery(@Nonnull URL endpoint, @Nonnull DataStructureRef ref) throws IOException {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path(ref.getId());
    }

    @Nonnull
    static RestQueryBuilder getDataQuery(@Nonnull URL endpoint, @Nonnull DataRequest request) throws IOException {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATA_RESOURCE)
                .path(request.getFlowRef().getId())
                .path(request.getKey().toString())
                .param("format", "compact_v2");
    }

    @Nonnull
    static Dataflow getFlowFromStructure(@Nonnull DataStructure o) {
        return Dataflow.of(getFlowRefFromStructureRef(o.getRef()), o.getRef(), o.getLabel());
    }

    @Nonnull
    static DataflowRef getFlowRefFromStructureRef(@Nonnull DataStructureRef o) {
        return DataflowRef.of(o.getAgency(), o.getId(), o.getVersion());
    }

    @Nonnull
    static DataStructureRef getStructureRefFromFlowRef(@Nonnull DataflowRef o) {
        return DataStructureRef.of(o.getAgency(), o.getId(), o.getVersion());
    }

    static final String DATASTRUCTURE_RESOURCE = "GetDataStructure";
    static final String DATA_RESOURCE = "GetData";
}
