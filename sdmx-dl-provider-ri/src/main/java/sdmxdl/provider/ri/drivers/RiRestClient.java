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
package sdmxdl.provider.ri.drivers;

import lombok.NonNull;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.HttpResponse;
import nbbrd.io.http.HttpResponseException;
import sdmxdl.*;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.Marker;
import sdmxdl.provider.web.RestClient;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static sdmxdl.provider.CommonSdmxExceptions.missingCodelist;
import static sdmxdl.provider.CommonSdmxExceptions.missingStructure;
import static sdmxdl.provider.web.RestErrorMapping.CLIENT_NO_RESULTS_FOUND;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public class RiRestClient implements RestClient {

    public static @NonNull RiRestClient of(@NonNull WebSource s, @NonNull Languages languages, @NonNull WebContext c,
                                           @NonNull RiRestQueries queries, @NonNull RiRestParsers parsers, @NonNull Set<Feature> supportedFeatures) throws IOException {
        return new RiRestClient(
                HasMarker.of(s),
                s.getEndpoint().toURL(),
                languages,
                ObsParser::newDefault,
                RiHttpUtils.newClient(s, c),
                queries,
                parsers,
                Sdmx21RestErrors.DEFAULT,
                supportedFeatures);
    }

    @lombok.Getter
    protected final Marker marker;
    protected final URL endpoint;
    protected final Languages langs;
    protected final Supplier<ObsParser> obsFactory;
    protected final HttpClient httpClient;
    protected final RiRestQueries queries;
    protected final RiRestParsers parsers;
    protected final RiRestErrors errors;
    protected final Set<Feature> supportedFeatures;

    @Override
    public @NonNull List<Flow> getFlows() throws IOException {
        return getFlows(getFlowsQuery());
    }

    @Override
    public @NonNull Structure getStructure(@NonNull StructureRef ref) throws IOException {
        return getStructure(getStructureQuery(ref), ref);
    }

    @Override
    public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull Structure dsd) throws IOException {
        return getData(getDataQuery(ref, dsd.getRef()), dsd);
    }

    @Override
    public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
        return getCodelist(getCodelistQuery(ref), ref);
    }

    @Override
    public @NonNull Set<Feature> getSupportedFeatures() {
        return supportedFeatures;
    }

    @NonNull
    @Override
    public Optional<URI> testClient() throws IOException {
        HttpRequest request = RiHttpUtils.newRequest(getFlowsQuery(), parsers.getFlowsTypes(), langs);
        try {
            try (HttpResponse ignore = httpClient.send(request)) {
                return Optional.of(request.getQuery().toURI());
            } catch (HttpResponseException ex) {
                return Optional.of(request.getQuery().toURI());
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    @NonNull
    protected URL getFlowsQuery() throws IOException {
        return queries.getFlowsQuery(endpoint).build();
    }

    @NonNull
    protected List<Flow> getFlows(@NonNull URL url) throws IOException {
        HttpRequest request = RiHttpUtils.newRequest(url, parsers.getFlowsTypes(), langs);
        try (HttpResponse response = httpClient.send(request)) {
            return parsers
                    .getFlowsParser(response.getContentType(), langs)
                    .parseStream(response::getBody);
        } catch (HttpResponseException ex) {
            if (errors.getFlowsError(ex) == CLIENT_NO_RESULTS_FOUND) {
                return Collections.emptyList();
            }
            throw ex;
        }
    }

    @NonNull
    protected URL getStructureQuery(@NonNull StructureRef ref) throws IOException {
        return queries.getStructureQuery(endpoint, ref).build();
    }

    @NonNull
    protected Structure getStructure(@NonNull URL url, @NonNull StructureRef ref) throws IOException {
        HttpRequest request = RiHttpUtils.newRequest(url, parsers.getStructureTypes(), langs);
        try (HttpResponse response = httpClient.send(request)) {
            return parsers
                    .getStructureParser(response.getContentType(), langs, ref)
                    .parseStream(response::getBody)
                    .orElseThrow(() -> missingStructure(this, ref));
        } catch (HttpResponseException ex) {
            if (errors.getStructureError(ex) == CLIENT_NO_RESULTS_FOUND) {
                throw missingStructure(this, ref);
            }
            throw ex;
        }
    }

    @NonNull
    protected URL getDataQuery(@NonNull DataRef ref, @NonNull StructureRef dsdRef) throws IOException {
        return queries.getDataQuery(endpoint, ref, dsdRef).build();
    }

    @NonNull
    protected Stream<Series> getData(@NonNull URL url, @NonNull Structure dsd) throws IOException {
        HttpRequest request = RiHttpUtils.newRequest(url, parsers.getDataTypes(), langs);
        try {
            HttpResponse response = httpClient.send(request);
            return parsers
                    .getDataParser(response.getContentType(), dsd, obsFactory)
                    .parseStream(response::asDisconnectingInputStream)
                    .asCloseableStream();
        } catch (HttpResponseException ex) {
            if (errors.getDataError(ex) == CLIENT_NO_RESULTS_FOUND) {
                return Stream.empty();
            }
            throw ex;
        }
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
                    .orElseThrow(() -> missingCodelist(this, ref));
        } catch (HttpResponseException ex) {
            if (errors.getCodelistError(ex) == CLIENT_NO_RESULTS_FOUND) {
                throw missingCodelist(this, ref);
            }
            throw ex;
        }
    }
}
