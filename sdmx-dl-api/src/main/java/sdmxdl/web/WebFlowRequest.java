package sdmxdl.web;

import internal.sdmxdl.web.URIs;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import sdmxdl.DatabaseRef;
import sdmxdl.FlowRequest;
import sdmxdl.Languages;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static sdmxdl.DatabaseRef.NO_DATABASE_KEYWORD;
import static sdmxdl.Languages.ANY_KEYWORD;

@lombok.Value
@lombok.Builder
@RepresentableAs(URI.class)
public class WebFlowRequest {

    @StaticFactoryMethod
    public static @NonNull WebFlowRequest parse(@NonNull URI uri) {
        if (!URIs.SDMX_DL_SCHEME.equals(uri.getScheme()))
            throw new IllegalArgumentException("Unsupported URI scheme: " + uri);

        String[] parts = URIs.getPathArray(uri, 2);
        if (parts == null)
            throw new IllegalArgumentException("Invalid URI: " + uri);

        Map<String, String> queryMap = URIs.getQueryMap(uri);

        return WebFlowRequest
                .builder()
                .source(parts[0])
                .request(FlowRequest
                        .builder()
                        .languagesOf(queryMap.getOrDefault("l", ANY_KEYWORD))
                        .databaseOf(queryMap.getOrDefault("d", NO_DATABASE_KEYWORD))
                        .flowOf(parts[1])
                        .build())
                .build();
    }

    @NonNull
    String source;

    @NonNull
    FlowRequest request;

    @Override
    public String toString() {
        String result = URIs.SDMX_DL_SCHEME + ":/" + URIs.encode(source) + "/" + URIs.encode(request.getFlow().toString());
        Map<String, String> query = new HashMap<>();
        if (!request.getLanguages().equals(Languages.ANY)) query.put("l", request.getLanguages().toString());
        if (!request.getDatabase().equals(DatabaseRef.NO_DATABASE)) query.put("d", request.getDatabase().toString());
        return result + URIs.toRawQuery(query);
    }

    public @NonNull URI toURI() {
        return URI.create(this.toString());
    }
}
