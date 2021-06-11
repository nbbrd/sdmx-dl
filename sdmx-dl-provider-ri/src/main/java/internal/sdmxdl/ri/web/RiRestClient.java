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

import internal.util.rest.HttpRest;
import nbbrd.io.Resource;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.SdmxExceptions;
import sdmxdl.util.MediaType;
import sdmxdl.util.parser.ObsFactories;
import sdmxdl.util.web.DataRequest;
import sdmxdl.util.web.SdmxWebClient;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public class RiRestClient implements SdmxWebClient {

    public static @NonNull RiRestClient of(@NonNull SdmxWebSource s, @NonNull SdmxWebContext c, @NonNull String defaultDialect,
                                           @NonNull RiRestQueries queries, @NonNull RiRestParsers parsers, boolean detailSupported) throws IOException {
        return new RiRestClient(
                SdmxWebClient.getClientName(s),
                s.getEndpoint(),
                c.getLanguages(),
                ObsFactories.getObsFactory(c, s, defaultDialect),
                RestClients.getRestClient(s, c),
                queries,
                parsers,
                detailSupported);
    }

    @lombok.Getter
    protected final String name;
    protected final URL endpoint;
    protected final LanguagePriorityList langs;
    protected final ObsFactory obsFactory;
    protected final HttpRest.Client executor;
    protected final RiRestQueries queries;
    protected final RiRestParsers parsers;
    protected final boolean detailSupported;

    @Override
    public @NonNull List<Dataflow> getFlows() throws IOException {
        return getFlows(getFlowsQuery());
    }

    @Override
    public @NonNull Dataflow getFlow(@NonNull DataflowRef ref) throws IOException {
        return getFlow(getFlowQuery(ref), ref);
    }

    @Override
    public @NonNull DataStructure getStructure(@NonNull DataStructureRef ref) throws IOException {
        return getStructure(getStructureQuery(ref), ref);
    }

    @Override
    public @NonNull DataCursor getData(@NonNull DataRequest request, @NonNull DataStructure dsd) throws IOException {
        return getData(getDataQuery(request), dsd);
    }

    @Override
    public boolean isDetailSupported() {
        return detailSupported;
    }

    @Override
    public DataStructureRef peekStructureRef(@NonNull DataflowRef flowRef) {
        return queries.peekStructureRef(flowRef);
    }

    @Override
    public @NonNull Duration ping() throws IOException {
        Clock clock = Clock.systemDefaultZone();
        Instant start = clock.instant();
        getFlows();
        return Duration.between(start, clock.instant());
    }

    @NonNull
    protected URL getFlowsQuery() throws IOException {
        return queries.getFlowsQuery(endpoint).build();
    }

    @NonNull
    protected List<Dataflow> getFlows(@NonNull URL url) throws IOException {
        try (HttpRest.Response response = open(url, parsers.getFlowsTypes())) {
            return parsers
                    .getFlowsParser(getResponseType(response), langs)
                    .parseStream(response::getBody);
        }
    }

    @NonNull
    protected URL getFlowQuery(@NonNull DataflowRef ref) throws IOException {
        return queries.getFlowQuery(endpoint, ref).build();
    }

    @NonNull
    protected Dataflow getFlow(@NonNull URL url, @NonNull DataflowRef ref) throws IOException {
        try (HttpRest.Response response = open(url, parsers.getFlowTypes())) {
            return parsers
                    .getFlowParser(getResponseType(response), langs, ref)
                    .parseStream(response::getBody)
                    .orElseThrow(() -> SdmxExceptions.missingFlow(name, ref));
        }
    }

    @NonNull
    protected URL getStructureQuery(@NonNull DataStructureRef ref) throws IOException {
        return queries.getStructureQuery(endpoint, ref).build();
    }

    @NonNull
    protected DataStructure getStructure(@NonNull URL url, @NonNull DataStructureRef ref) throws IOException {
        try (HttpRest.Response response = open(url, parsers.getStructureTypes())) {
            return parsers
                    .getStructureParser(getResponseType(response), langs, ref)
                    .parseStream(response::getBody)
                    .orElseThrow(() -> SdmxExceptions.missingStructure(name, ref));
        }
    }

    @NonNull
    protected URL getDataQuery(@NonNull DataRequest request) throws IOException {
        return queries.getDataQuery(endpoint, request.getFlowRef(), request.getKey(), request.getFilter()).build();
    }

    @NonNull
    protected DataCursor getData(@NonNull URL url, @NonNull DataStructure dsd) throws IOException {
        HttpRest.Response response = open(url, parsers.getDataTypes());
        return parsers
                .getDataParser(getResponseType(response), dsd, obsFactory)
                .parseStream(() -> DisconnectingInputStream.of(response));
    }

    protected HttpRest.@NonNull Response open(@NonNull URL query, @NonNull List<MediaType> mediaTypes) throws IOException {
        return executor.requestGET(query, mediaTypes.stream().map(MediaType::toString).collect(Collectors.toList()), langs.toString());
    }

    protected @NonNull MediaType getResponseType(HttpRest.@NonNull Response response) throws IOException {
        return MediaType.parse(response.getContentType());
    }

    @lombok.RequiredArgsConstructor
    protected static final class DisconnectingInputStream extends InputStream {

        public static DisconnectingInputStream of(HttpRest.Response response) throws IOException {
            return new DisconnectingInputStream(response.getBody(), response);
        }

        @lombok.experimental.Delegate(excludes = Closeable.class)
        private final InputStream delegate;

        private final Closeable onClose;

        @Override
        public void close() throws IOException {
            Resource.closeBoth(delegate, onClose);
        }
    }
}
