package internal.sdmxdl.desktop;

import sdmxdl.provider.URIs;
import lombok.NonNull;
import sdmxdl.DatabaseRef;
import sdmxdl.FlowRequest;
import sdmxdl.KeyRequest;
import sdmxdl.Languages;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@lombok.experimental.UtilityClass
public class SdmxUri {

    public static final String SDMX_DL_SCHEME = "sdmx-dl";

    public static URI fromFlowRequest(String source, FlowRequest request) {
        String result = SDMX_DL_SCHEME + ":/" + URIs.encode(source) + "/" + URIs.encode(request.getFlow().toString());
        Map<String, String> query = new HashMap<>();
        if (!request.getLanguages().equals(Languages.ANY)) query.put("l", request.getLanguages().toString());
        if (!request.getDatabase().equals(DatabaseRef.NO_DATABASE)) query.put("d", request.getDatabase().toString());
        return URI.create(result + URIs.toRawQuery(query));
    }

    public static @NonNull FlowRequest toFlowRequest(@NonNull URI uri) {
        if (!SDMX_DL_SCHEME.equals(uri.getScheme()))
            throw new IllegalArgumentException("Unsupported URI scheme: " + uri);

        String[] parts = URIs.getPathArray(uri, 2);
        if (parts == null)
            throw new IllegalArgumentException("Invalid URI: " + uri);

        Map<String, String> queryMap = URIs.getQueryMap(uri);

        return FlowRequest.builder()
                .languagesOf(queryMap.getOrDefault("l", Languages.ANY_KEYWORD))
                .databaseOf(queryMap.getOrDefault("d", DatabaseRef.NO_DATABASE_KEYWORD))
                .flowOf(parts[1])
                .build();
    }

    public static URI fromKeyRequest(String source, KeyRequest request) {
        String result = SDMX_DL_SCHEME + ":/" + URIs.encode(source) + "/" + URIs.encode(request.getFlow().toString()) + "/" + URIs.encode(request.getKey().toString());
        Map<String, String> query = new HashMap<>();
        if (!request.getLanguages().equals(Languages.ANY)) query.put("l", request.getLanguages().toString());
        if (!request.getDatabase().equals(DatabaseRef.NO_DATABASE)) query.put("d", request.getDatabase().toString());
        return URI.create(result + URIs.toRawQuery(query));
    }

    public static @NonNull KeyRequest toKeyRequest(@NonNull URI uri) {
        if (!SDMX_DL_SCHEME.equals(uri.getScheme()))
            throw new IllegalArgumentException("Unsupported URI scheme: " + uri);

        String[] parts = URIs.getPathArray(uri, 3);
        if (parts == null)
            throw new IllegalArgumentException("Invalid URI: " + uri);

        Map<String, String> queryMap = URIs.getQueryMap(uri);

        return KeyRequest.builder()
                .languagesOf(queryMap.getOrDefault("l", Languages.ANY_KEYWORD))
                .databaseOf(queryMap.getOrDefault("d", DatabaseRef.NO_DATABASE_KEYWORD))
                .flowOf(parts[1])
                .keyOf(parts[2])
                .build();
    }
}
