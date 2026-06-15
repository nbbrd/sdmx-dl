package sdmxdl.web;

import internal.sdmxdl.web.URIs;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import sdmxdl.DatabaseRef;
import sdmxdl.KeyRequest;
import sdmxdl.Languages;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static internal.sdmxdl.web.URIs.SDMX_DL_SCHEME;
import static sdmxdl.DatabaseRef.NO_DATABASE_KEYWORD;
import static sdmxdl.Languages.ANY_KEYWORD;

@lombok.Value
@lombok.Builder
@RepresentableAs(URI.class)
public class WebKeyRequest {

    @StaticFactoryMethod
    public static @NonNull WebKeyRequest parse(@NonNull URI uri) {
        if (!SDMX_DL_SCHEME.equals(uri.getScheme()))
            throw new IllegalArgumentException("Unsupported URI scheme: " + uri);

        String[] parts = URIs.getPathArray(uri, 3);
        if (parts == null)
            throw new IllegalArgumentException("Invalid URI: " + uri);

        Map<String, String> queryMap = URIs.getQueryMap(uri);

        return WebKeyRequest
                .builder()
                .source(parts[0])
                .request(KeyRequest
                        .builder()
                        .languagesOf(queryMap.getOrDefault("l", ANY_KEYWORD))
                        .databaseOf(queryMap.getOrDefault("d", NO_DATABASE_KEYWORD))
                        .flowOf(parts[1])
                        .keyOf(parts[2])
                        .build())
                .build();
    }

    @NonNull
    String source;

    @NonNull
    KeyRequest request;

    @Override
    public String toString() {
        String result = SDMX_DL_SCHEME + ":/" + URIs.encode(source) + "/" + URIs.encode(request.getFlow().toString()) + "/" + URIs.encode(request.getKey().toString());
        Map<String, String> query = new HashMap<>();
        if (!request.getLanguages().equals(Languages.ANY)) query.put("l", request.getLanguages().toString());
        if (!request.getDatabase().equals(DatabaseRef.NO_DATABASE)) query.put("d", request.getDatabase().toString());
        return result + URIs.toRawQuery(query);
    }

    public @NonNull URI toURI() {
        return URI.create(this.toString());
    }

    public static @NonNull Builder builderOf(@NonNull WebFlowRequest request) {
        return builder()
                .source(request.getSource())
                .request(KeyRequest.builderOf(request.getRequest()).build());
    }
}
