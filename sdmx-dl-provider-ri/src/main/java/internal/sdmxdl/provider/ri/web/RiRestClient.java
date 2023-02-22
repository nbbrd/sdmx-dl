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
import sdmxdl.*;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.CommonSdmxExceptions;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.Marker;
import sdmxdl.provider.web.RestClient;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public class RiRestClient implements RestClient {

    public static @NonNull RiRestClient of(@NonNull SdmxWebSource s, @NonNull WebContext c,
                                           @NonNull RiRestQueries queries, @NonNull RiRestParsers parsers, @NonNull Set<Feature> supportedFeatures) throws IOException {
        return new RiRestClient(
                Marker.of(s),
                s.getEndpoint().toURL(),
                c.getLanguages(),
                ObsParser::newDefault,
                RiHttpUtils.newClient(s, c),
                queries,
                parsers,
                supportedFeatures);
    }

    @lombok.Getter
    protected final Marker marker;
    protected final URL endpoint;
    protected final LanguagePriorityList langs;
    protected final Supplier<ObsParser> obsFactory;
    protected final HttpClient httpClient;
    protected final RiRestQueries queries;
    protected final RiRestParsers parsers;
    protected final Set<Feature> supportedFeatures;

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
        return getData(getDataQuery(ref, dsd.getRef()), dsd).asCloseableStream();
    }

    @Override
    public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
        return getCodelist(getCodelistQuery(ref), ref);
    }

    @Override
    public Set<Feature> getSupportedFeatures() {
        return supportedFeatures;
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
        try (HttpResponse response = httpClient.send(request)) {
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
        try (HttpResponse response = httpClient.send(request)) {
            return parsers
                    .getFlowParser(response.getContentType(), langs, ref)
                    .parseStream(response::getBody)
                    .orElseThrow(() -> CommonSdmxExceptions.missingFlow(this, ref));
        } catch (HttpResponseException ex) {
            if (ex.getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                throw CommonSdmxExceptions.missingFlow(this, ref);
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
        try (HttpResponse response = httpClient.send(request)) {
            return parsers
                    .getStructureParser(response.getContentType(), langs, ref)
                    .parseStream(response::getBody)
                    .orElseThrow(() -> CommonSdmxExceptions.missingStructure(this, ref));
        } catch (HttpResponseException ex) {
            if (ex.getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                throw CommonSdmxExceptions.missingStructure(this, ref);
            }
            throw ex;
        }
    }

    @NonNull
    protected URL getDataQuery(@NonNull DataRef ref, @NonNull DataStructureRef dsdRef) throws IOException {
        return queries.getDataQuery(endpoint, ref, dsdRef).build();
    }

    @NonNull
    protected DataCursor getData(@NonNull URL url, @NonNull DataStructure dsd) throws IOException {
        HttpRequest request = RiHttpUtils.newRequest(url, parsers.getDataTypes(), langs);
        HttpResponse response = httpClient.send(request);
        return parsers
                .getDataParser(response.getContentType(), dsd, obsFactory)
                .parseStream(response::asDisconnectingInputStream);
    }

    @NonNull
    protected URL getCodelistQuery(@NonNull CodelistRef ref) throws IOException {
        return queries.getCodelistQuery(endpoint, ref).build();
    }

    @NonNull
    protected Codelist getCodelist(@NonNull URL url, @NonNull CodelistRef ref) throws IOException {
        HttpRequest request = RiHttpUtils.newRequest(url, parsers.getCodelistTypes(), langs);
        try (HttpResponse response = httpClient.send(request)) {
            return parsers
                    .getCodelistParser(response.getContentType(), langs, ref)
                    .parseStream(response::getBody)
                    .orElseThrow(() -> CommonSdmxExceptions.missingCodelist(this, ref));
        } catch (HttpResponseException ex) {
            if (ex.getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                throw CommonSdmxExceptions.missingCodelist(this, ref);
            }
            throw ex;
        }
    }
}
