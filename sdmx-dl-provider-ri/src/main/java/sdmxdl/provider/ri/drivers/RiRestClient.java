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
import nbbrd.io.http.ext.ThrowingStatusException;
import sdmxdl.*;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.Marker;
import sdmxdl.provider.ri.http.HttpManager;
import sdmxdl.provider.web.RestClient;

import java.io.IOException;
import java.net.URI;
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

    @lombok.Getter
    protected final Marker marker;
    protected final URI endpoint;
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
        HttpRequest request = HttpManager.newHttpRequest(getFlowsQuery(), parsers.getFlowsTypes(), langs);
        try (HttpResponse ignore = httpClient.send(request)) {
            return Optional.of(request.getQuery());
        } catch (ThrowingStatusException ex) {
            return Optional.of(request.getQuery());
        }
    }

    @NonNull
    protected URI getFlowsQuery() throws IOException {
        try {
            return queries.getFlowsQuery(endpoint).build();
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }

    @NonNull
    protected List<Flow> getFlows(@NonNull URI url) throws IOException {
        HttpRequest request = HttpManager.newHttpRequest(url, parsers.getFlowsTypes(), langs);
        try (HttpResponse response = httpClient.send(request)) {
            return parsers
                    .getFlowsParser(response.getContentType(), langs)
                    .parseStream(response::getBody);
        } catch (ThrowingStatusException ex) {
            if (errors.getFlowsError(ex) == CLIENT_NO_RESULTS_FOUND) {
                return Collections.emptyList();
            }
            throw ex;
        }
    }

    @NonNull
    protected URI getStructureQuery(@NonNull StructureRef ref) throws IOException {
        try {
            return queries.getStructureQuery(endpoint, ref).build();
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }

    @NonNull
    protected Structure getStructure(@NonNull URI url, @NonNull StructureRef ref) throws IOException {
        HttpRequest request = HttpManager.newHttpRequest(url, parsers.getStructureTypes(), langs);
        try (HttpResponse response = httpClient.send(request)) {
            return parsers
                    .getStructureParser(response.getContentType(), langs, ref)
                    .parseStream(response::getBody)
                    .orElseThrow(() -> missingStructure(this, ref));
        } catch (ThrowingStatusException ex) {
            if (errors.getStructureError(ex) == CLIENT_NO_RESULTS_FOUND) {
                throw missingStructure(this, ref);
            }
            throw ex;
        }
    }

    @NonNull
    protected URI getDataQuery(@NonNull DataRef ref, @NonNull StructureRef dsdRef) throws IOException {
        try {
            return queries.getDataQuery(endpoint, ref, dsdRef).build();
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }

    @NonNull
    protected Stream<Series> getData(@NonNull URI url, @NonNull Structure dsd) throws IOException {
        HttpRequest request = HttpManager.newHttpRequest(url, parsers.getDataTypes(), langs);
        try {
            HttpResponse response = httpClient.send(request);
            return parsers
                    .getDataParser(response.getContentType(), dsd, obsFactory)
                    .parseStream(response::asDisconnectingInputStream)
                    .asCloseableStream();
        } catch (ThrowingStatusException ex) {
            if (errors.getDataError(ex) == CLIENT_NO_RESULTS_FOUND) {
                return Stream.empty();
            }
            throw ex;
        }
    }

    @NonNull
    protected URI getCodelistQuery(@NonNull CodelistRef ref) throws IOException {
        try {
            return queries.getCodelistQuery(endpoint, ref).build();
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
    }

    @NonNull
    protected Codelist getCodelist(@NonNull URI url, @NonNull CodelistRef ref) throws IOException {
        HttpRequest request = HttpManager.newHttpRequest(url, parsers.getCodelistTypes(), langs);
        try (HttpResponse response = httpClient.send(request)) {
            return parsers
                    .getCodelistParser(response.getContentType(), langs, ref)
                    .parseStream(response::getBody)
                    .orElseThrow(() -> missingCodelist(this, ref));
        } catch (ThrowingStatusException ex) {
            if (errors.getCodelistError(ex) == CLIENT_NO_RESULTS_FOUND) {
                throw missingCodelist(this, ref);
            }
            throw ex;
        }
    }
}
