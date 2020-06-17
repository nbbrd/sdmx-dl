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
package internal.sdmxdl.ri.drivers;

import sdmxdl.DataCursor;
import sdmxdl.DataStructure;
import sdmxdl.DataStructureRef;
import sdmxdl.Dataflow;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.LanguagePriorityList;
import sdmxdl.ResourceRef;
import sdmxdl.util.parser.DataFactory;
import sdmxdl.SdmxExceptions;
import sdmxdl.xml.stream.SdmxXmlStreams;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import internal.util.rest.RestClient;
import internal.util.rest.RestQueryBuilder;
import sdmxdl.util.web.DataRequest;
import nbbrd.io.function.IOSupplier;
import org.checkerframework.checker.nullness.qual.NonNull;

import static sdmxdl.util.SdmxMediaType.*;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
class Sdmx21RestClient extends AbstractRestClient {

    protected final String name;
    protected final URL endpoint;
    protected final LanguagePriorityList langs;
    protected final RestClient executor;
    protected final boolean seriesKeysOnlySupported;
    protected final DataFactory dialect;

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
                .flow21(langs)
                .parseStream(calling(url, XML));
    }

    @Override
    protected URL getFlowQuery(DataflowRef ref) throws IOException {
        return getFlowQuery(endpoint, ref).build();
    }

    @Override
    protected Dataflow getFlow(URL url, DataflowRef ref) throws IOException {
        return SdmxXmlStreams
                .flow21(langs)
                .parseStream(calling(url, XML))
                .stream()
                .filter(ref::containsRef)
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
                .struct21(langs)
                .parseStream(calling(url, STRUCTURE_21))
                .stream()
                .filter(ref::equalsRef)
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
                .compactData21(dsd, dialect)
                .parseStream(calling(url, STRUCTURE_SPECIFIC_DATA_21));
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        return seriesKeysOnlySupported;
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
        return null;
    }

    private IOSupplier<? extends InputStream> calling(URL query, String mediaType) throws IOException {
        return () -> executor.openStream(query, mediaType, langs.toString());
    }

    @NonNull
    static RestQueryBuilder onMeta(@NonNull URL endpoint, @NonNull String resourceType, @NonNull ResourceRef ref) {
        return RestQueryBuilder
                .of(endpoint)
                .path(resourceType)
                .path(ref.getAgency())
                .path(ref.getId())
                .path(ref.getVersion());
    }

    @NonNull
    static RestQueryBuilder onData(@NonNull URL endpoint, @NonNull DataflowRef flowRef, @NonNull Key key) {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATA_RESOURCE)
                .path(flowRef.toString())
                .path(key.toString())
                .path(DEFAULT_PROVIDER_REF);
    }

    @NonNull
    static RestQueryBuilder getFlowsQuery(@NonNull URL endpoint) throws IOException {
        return onMeta(endpoint, DATAFLOW_RESOURCE, FLOWS);
    }

    @NonNull
    static RestQueryBuilder getFlowQuery(@NonNull URL endpoint, @NonNull DataflowRef ref) throws IOException {
        return onMeta(endpoint, DATAFLOW_RESOURCE, ref);
    }

    @NonNull
    static RestQueryBuilder getStructureQuery(@NonNull URL endpoint, @NonNull DataStructureRef ref) throws IOException {
        return onMeta(endpoint, DATASTRUCTURE_RESOURCE, ref).param(REFERENCES_PARAM, "children");
    }

    @NonNull
    static RestQueryBuilder getDataQuery(@NonNull URL endpoint, @NonNull DataRequest request) throws IOException {
        RestQueryBuilder result = onData(endpoint, request.getFlowRef(), request.getKey());
        switch (request.getFilter().getDetail()) {
            case SERIES_KEYS_ONLY:
                result.param(DETAIL_PARAM, "serieskeysonly");
                break;
        }
        return result;
    }

    static final DataflowRef FLOWS = DataflowRef.of("all", "all", "latest");

    static final String DATAFLOW_RESOURCE = "dataflow";
    static final String DATASTRUCTURE_RESOURCE = "datastructure";
    static final String DATA_RESOURCE = "data";

    static final String DEFAULT_PROVIDER_REF = "all";

    static final String REFERENCES_PARAM = "references";
    static final String DETAIL_PARAM = "detail";
}
