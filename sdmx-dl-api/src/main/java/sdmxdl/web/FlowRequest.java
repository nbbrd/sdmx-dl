package sdmxdl.web;

import internal.sdmxdl.URIs;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import sdmxdl.DatabaseRef;
import sdmxdl.FlowRef;
import sdmxdl.Languages;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RepresentableAs(URI.class)
@lombok.Value
@lombok.Builder
public class FlowRequest {

    @StaticFactoryMethod
    public static @NonNull FlowRequest parse(@NonNull URI uri) {
        if (!URIs.SDMX_DL_SCHEME.equals(uri.getScheme()))
            throw new IllegalArgumentException("Unsupported URI scheme: " + uri);

        String[] parts = URIs.getPathArray(uri, 2);
        if (parts == null)
            throw new IllegalArgumentException("Invalid URI: " + uri);

        Map<String, String> queryMap = URIs.getQueryMap(uri);

        return builder()
                .source(parts[0])
                .languagesOf(queryMap.getOrDefault("l", Languages.ANY_KEYWORD))
                .databaseOf(queryMap.getOrDefault("d", DatabaseRef.NO_DATABASE_KEYWORD))
                .flowOf(parts[1])
                .build();
    }

    @NonNull
    String source;

    @NonNull
    @lombok.Builder.Default
    DatabaseRef database = DatabaseRef.NO_DATABASE;

    @NonNull
    FlowRef flow;

    @NonNull
    @lombok.Builder.Default
    Languages languages = Languages.ANY;

    public @NonNull URI toURI() {
        String result = URIs.SDMX_DL_SCHEME + ":/" + URIs.encode(source) + "/" + URIs.encode(flow.toString());
        Map<String, String> query = new HashMap<>();
        if (!languages.equals(Languages.ANY)) query.put("l", languages.toString());
        if (!database.equals(DatabaseRef.NO_DATABASE)) query.put("d", database.toString());
        return URI.create(result + URIs.toRawQuery(query));
    }

    public static @NonNull Builder builderOf(@NonNull DatabaseRequest request) {
        return builder()
                .source(request.getSource())
                .database(request.getDatabase())
                .languages(request.getLanguages());
    }

    public static final class Builder {

        public Builder databaseOf(@NonNull String database) {
            return database(DatabaseRef.parse(database));
        }

        public Builder flowOf(@NonNull String flow) {
            return flow(FlowRef.parse(flow));
        }

        public Builder languagesOf(@NonNull String languages) {
            return languages(Languages.parse(languages));
        }
    }
}
