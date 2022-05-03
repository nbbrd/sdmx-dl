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
package internal.sdmxdl.provider.ri.web;

import internal.util.http.HttpClient;
import internal.util.http.HttpRequest;
import internal.util.http.HttpResponse;
import internal.util.http.HttpResponseException;
import lombok.NonNull;
import nbbrd.io.Resource;
import sdmxdl.*;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.CommonSdmxExceptions;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.web.SdmxRestClient;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;

import javax.net.ssl.HttpsURLConnection;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public class RiRestClient implements SdmxRestClient {

    public static @NonNull RiRestClient of(@NonNull SdmxWebSource s, @NonNull WebContext c,
                                           @NonNull RiRestQueries queries, @NonNull RiRestParsers parsers, boolean detailSupported) throws IOException {
        return new RiRestClient(
                s.getId(),
                s.getEndpoint().toURL(),
                c.getLanguages(),
                ObsParser::newDefault,
                RiHttpUtils.newClient(s, c),
                queries,
                parsers,
                detailSupported);
    }

    @lombok.Getter
    protected final String name;
    protected final URL endpoint;
    protected final LanguagePriorityList langs;
    protected final Supplier<ObsParser> obsFactory;
    protected final HttpClient httpClient;
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
    public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull DataStructure dsd) throws IOException {
        return getData(getDataQuery(ref), dsd).toCloseableStream();
    }

    @Override
    public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
        return getCodelist(getCodelistQuery(ref), ref);
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
    public void testClient() throws IOException {
        getFlows();
    }

    @NonNull
    protected URL getFlowsQuery() throws IOException {
        return queries.getFlowsQuery(endpoint).build();
    }

    @NonNull
    protected List<Dataflow> getFlows(@NonNull URL url) throws IOException {
        HttpRequest request = RiHttpUtils.newRequest(url, parsers.getFlowsTypes(), langs);
        try (HttpResponse response = httpClient.requestGET(request)) {
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
        HttpRequest request = RiHttpUtils.newRequest(url, parsers.getFlowTypes(), langs);
        try (HttpResponse response = httpClient.requestGET(request)) {
            return parsers
                    .getFlowParser(response.getContentType(), langs, ref)
                    .parseStream(response::getBody)
                    .orElseThrow(() -> CommonSdmxExceptions.missingFlow(name, ref));
        } catch (HttpResponseException ex) {
            if (ex.getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                throw CommonSdmxExceptions.missingFlow(getName(), ref);
            }
            throw ex;
        }
    }

    @NonNull
    protected URL getStructureQuery(@NonNull DataStructureRef ref) throws IOException {
        return queries.getStructureQuery(endpoint, ref).build();
    }

    @NonNull
    protected DataStructure getStructure(@NonNull URL url, @NonNull DataStructureRef ref) throws IOException {
        HttpRequest request = RiHttpUtils.newRequest(url, parsers.getStructureTypes(), langs);
        try (HttpResponse response = httpClient.requestGET(request)) {
            return parsers
                    .getStructureParser(response.getContentType(), langs, ref)
                    .parseStream(response::getBody)
                    .orElseThrow(() -> CommonSdmxExceptions.missingStructure(name, ref));
        } catch (HttpResponseException ex) {
            if (ex.getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                throw CommonSdmxExceptions.missingStructure(getName(), ref);
            }
            throw ex;
        }
    }

    @NonNull
    protected URL getDataQuery(@NonNull DataRef ref) throws IOException {
        return queries.getDataQuery(endpoint, ref).build();
    }

    @NonNull
    protected DataCursor getData(@NonNull URL url, @NonNull DataStructure dsd) throws IOException {
        HttpRequest request = RiHttpUtils.newRequest(url, parsers.getDataTypes(), langs);
        HttpResponse response = httpClient.requestGET(request);
        return parsers
                .getDataParser(response.getContentType(), dsd, obsFactory)
                .parseStream(() -> DisconnectingInputStream.of(response));
    }

    @NonNull
    protected URL getCodelistQuery(@NonNull CodelistRef ref) throws IOException {
        return queries.getCodelistQuery(endpoint, ref).build();
    }

    @NonNull
    protected Codelist getCodelist(@NonNull URL url, @NonNull CodelistRef ref) throws IOException {
        HttpRequest request = RiHttpUtils.newRequest(url, parsers.getCodelistTypes(), langs);
        try (HttpResponse response = httpClient.requestGET(request)) {
            return parsers
                    .getCodelistParser(response.getContentType(), langs, ref)
                    .parseStream(response::getBody)
                    .orElseThrow(() -> CommonSdmxExceptions.missingCodelist(name, ref));
        } catch (HttpResponseException ex) {
            if (ex.getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                throw CommonSdmxExceptions.missingCodelist(getName(), ref);
            }
            throw ex;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class DisconnectingInputStream extends InputStream {

        public static DisconnectingInputStream of(HttpResponse response) throws IOException {
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
