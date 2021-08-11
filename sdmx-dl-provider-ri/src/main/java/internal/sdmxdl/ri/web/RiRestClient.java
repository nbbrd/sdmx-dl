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
import sdmxdl.ext.SdmxException;
import sdmxdl.util.parser.ObsFactories;
import sdmxdl.util.web.DataRequest;
import sdmxdl.util.web.SdmxWebClient;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;

import javax.net.ssl.HttpsURLConnection;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

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
                HttpRest.newClient(RestClients.getRestContext(s, c)),
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
        try (HttpRest.Response response = executor.requestGET(url, parsers.getFlowsTypes(), langs.toString())) {
            return parsers
                    .getFlowsParser(response.getContentType(), langs)
                    .parseStream(response::getBody);
        }
    }

    @NonNull
    protected URL getFlowQuery(@NonNull DataflowRef ref) throws IOException {
        return queries.getFlowQuery(endpoint, ref).build();
    }

    @NonNull
    protected Dataflow getFlow(@NonNull URL url, @NonNull DataflowRef ref) throws IOException {
        try (HttpRest.Response response = executor.requestGET(url, parsers.getFlowTypes(), langs.toString())) {
            return parsers
                    .getFlowParser(response.getContentType(), langs, ref)
                    .parseStream(response::getBody)
                    .orElseThrow(() -> SdmxException.missingFlow(name, ref));
        } catch (HttpRest.ResponseError error) {
            if (error.getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                throw SdmxException.missingFlow(getName(), ref);
            }
            throw error;
        }
    }

    @NonNull
    protected URL getStructureQuery(@NonNull DataStructureRef ref) throws IOException {
        return queries.getStructureQuery(endpoint, ref).build();
    }

    @NonNull
    protected DataStructure getStructure(@NonNull URL url, @NonNull DataStructureRef ref) throws IOException {
        try (HttpRest.Response response = executor.requestGET(url, parsers.getStructureTypes(), langs.toString())) {
            return parsers
                    .getStructureParser(response.getContentType(), langs, ref)
                    .parseStream(response::getBody)
                    .orElseThrow(() -> SdmxException.missingStructure(name, ref));
        } catch (HttpRest.ResponseError error) {
            if (error.getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                throw SdmxException.missingStructure(getName(), ref);
            }
            throw error;
        }
    }

    @NonNull
    protected URL getDataQuery(@NonNull DataRequest request) throws IOException {
        return queries.getDataQuery(endpoint, request.getFlowRef(), request.getKey(), request.getFilter()).build();
    }

    @NonNull
    protected DataCursor getData(@NonNull URL url, @NonNull DataStructure dsd) throws IOException {
        HttpRest.Response response = executor.requestGET(url, parsers.getDataTypes(), langs.toString());
        return parsers
                .getDataParser(response.getContentType(), dsd, obsFactory)
                .parseStream(() -> DisconnectingInputStream.of(response));
    }

    @lombok.RequiredArgsConstructor
    private static final class DisconnectingInputStream extends InputStream {

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
