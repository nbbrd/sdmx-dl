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
package internal.sdmxdl.ri.web;

import internal.util.rest.HttpRest;
import internal.util.rest.RestQueryBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.SdmxExceptions;
import sdmxdl.util.web.DataRequest;
import sdmxdl.xml.stream.SdmxXmlStreams;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static sdmxdl.ext.SdmxMediaType.*;

/**
 * @author Philippe Charles
 */
public class Sdmx21RestClient extends RiRestClient {

    protected final boolean detailSupported;
    protected final boolean trailingSlashRequired;
    protected final ObsFactory dataFactory;

    public Sdmx21RestClient(String name, URL endpoint, LanguagePriorityList langs, HttpRest.Client executor,
                            boolean detailSupported, boolean trailingSlashRequired, ObsFactory dataFactory) {
        super(name, endpoint, langs, executor, dataFactory);
        this.detailSupported = detailSupported;
        this.trailingSlashRequired = trailingSlashRequired;
        this.dataFactory = dataFactory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected URL getFlowsQuery() throws IOException {
        return getFlowsQuery(endpoint).trailingSlash(trailingSlashRequired).build();
    }

    @Override
    protected List<Dataflow> getFlows(URL url) throws IOException {
        return SdmxXmlStreams
                .flow21(langs)
                .parseStream(calling(url, XML));
    }

    @Override
    protected URL getFlowQuery(DataflowRef ref) throws IOException {
        return getFlowQuery(endpoint, ref).trailingSlash(trailingSlashRequired).build();
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
        return getStructureQuery(endpoint, ref).trailingSlash(trailingSlashRequired).build();
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
        return getDataQuery(endpoint, request).trailingSlash(trailingSlashRequired).build();
    }

    @Override
    protected DataCursor getData(DataStructure dsd, URL url) throws IOException {
        return SdmxXmlStreams
                .compactData21(dsd, dataFactory)
                .parseStream(calling(url, STRUCTURE_SPECIFIC_DATA_21));
    }

    @Override
    public boolean isDetailSupported() {
        return detailSupported;
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) {
        return null;
    }

    @NonNull
    public static RestQueryBuilder onMeta(@NonNull URL endpoint, @NonNull String resourceType, @NonNull ResourceRef<?> ref) {
        return RestQueryBuilder
                .of(endpoint)
                .path(resourceType)
                .path(ref.getAgency())
                .path(ref.getId())
                .path(ref.getVersion());
    }

    @NonNull
    public static RestQueryBuilder onData(@NonNull URL endpoint, @NonNull DataflowRef flowRef, @NonNull Key key) {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATA_RESOURCE)
                .path(flowRef.toString())
                .path(key.toString())
                .path(DEFAULT_PROVIDER_REF);
    }

    @NonNull
    public static RestQueryBuilder getFlowsQuery(@NonNull URL endpoint) {
        return onMeta(endpoint, DATAFLOW_RESOURCE, FLOWS);
    }

    @NonNull
    public static RestQueryBuilder getFlowQuery(@NonNull URL endpoint, @NonNull DataflowRef ref) {
        return onMeta(endpoint, DATAFLOW_RESOURCE, ref);
    }

    @NonNull
    public static RestQueryBuilder getStructureQuery(@NonNull URL endpoint, @NonNull DataStructureRef ref) {
        return onMeta(endpoint, DATASTRUCTURE_RESOURCE, ref).param(REFERENCES_PARAM, "children");
    }

    @NonNull
    public static RestQueryBuilder getDataQuery(@NonNull URL endpoint, @NonNull DataRequest request) {
        RestQueryBuilder result = onData(endpoint, request.getFlowRef(), request.getKey());
        switch (request.getFilter().getDetail()) {
            case SERIES_KEYS_ONLY:
                result.param(DETAIL_PARAM, "serieskeysonly");
                break;
            case DATA_ONLY:
                result.param(DETAIL_PARAM, "dataonly");
                break;
            case NO_DATA:
                result.param(DETAIL_PARAM, "nodata");
                break;
        }
        return result;
    }

    public static final DataflowRef FLOWS = DataflowRef.of("all", "all", "latest");

    public static final String DATAFLOW_RESOURCE = "dataflow";
    public static final String DATASTRUCTURE_RESOURCE = "datastructure";
    public static final String DATA_RESOURCE = "data";

    public static final String DEFAULT_PROVIDER_REF = "all";

    public static final String REFERENCES_PARAM = "references";
    public static final String DETAIL_PARAM = "detail";
}
