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
package internal.sdmxdl.ri.web;

import internal.util.rest.RestQueryBuilder;
import internal.util.rest.RestClient;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.SdmxExceptions;
import sdmxdl.util.SdmxFix;
import sdmxdl.util.parser.DataFactories;
import sdmxdl.util.web.DataRequest;
import sdmxdl.xml.stream.SdmxXmlStreams;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static sdmxdl.ext.SdmxMediaType.XML;

/**
 * @author Philippe Charles
 */
public class DotStatRestClient extends RiRestClient {

    public DotStatRestClient(String name, URL endpoint, LanguagePriorityList langs, RestClient executor) {
        super(name, endpoint, langs, executor);
    }

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

    @SdmxFix(id = 1, category = SdmxFix.Category.CONTENT, cause = "Time dimension is always TIME in data")
    @Override
    protected DataCursor getData(DataStructure dsd, URL url) throws IOException {
        DataStructure modifiedDsd = dsd.toBuilder().timeDimensionId("TIME").build();
        return SdmxXmlStreams
                .compactData20(modifiedDsd, DataFactories.SDMX20)
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

    @NonNull
    public static RestQueryBuilder getFlowsQuery(@NonNull URL endpoint) throws IOException {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path("ALL");
    }

    @NonNull
    public static RestQueryBuilder getFlowQuery(@NonNull URL endpoint, @NonNull DataflowRef ref) throws IOException {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path(ref.getId());
    }

    @NonNull
    public static RestQueryBuilder getStructureQuery(@NonNull URL endpoint, @NonNull DataStructureRef ref) throws IOException {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATASTRUCTURE_RESOURCE)
                .path(ref.getId());
    }

    @NonNull
    public static RestQueryBuilder getDataQuery(@NonNull URL endpoint, @NonNull DataRequest request) throws IOException {
        return RestQueryBuilder
                .of(endpoint)
                .path(DATA_RESOURCE)
                .path(request.getFlowRef().getId())
                .path(request.getKey().toString())
                .param("format", "compact_v2");
    }

    @NonNull
    public static Dataflow getFlowFromStructure(@NonNull DataStructure o) {
        return Dataflow.of(getFlowRefFromStructureRef(o.getRef()), o.getRef(), o.getLabel());
    }

    @NonNull
    public static DataflowRef getFlowRefFromStructureRef(@NonNull DataStructureRef o) {
        return DataflowRef.of(o.getAgency(), o.getId(), o.getVersion());
    }

    @NonNull
    public static DataStructureRef getStructureRefFromFlowRef(@NonNull DataflowRef o) {
        return DataStructureRef.of(o.getAgency(), o.getId(), o.getVersion());
    }

    public static final String DATASTRUCTURE_RESOURCE = "GetDataStructure";
    public static final String DATA_RESOURCE = "GetData";
}
