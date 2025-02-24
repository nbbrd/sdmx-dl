package sdmxdl.provider.px.drivers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import sdmxdl.Confidentiality;
import sdmxdl.ext.FileFormat;
import sdmxdl.format.FileFormatSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.WebSources;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static sdmxdl.provider.px.drivers.PxWebDriver.*;

final class PxWebSourcesFormat implements FileFormat<WebSources> {

    public static final PxWebSourcesFormat INSTANCE = new PxWebSourcesFormat();

    @lombok.experimental.Delegate
    private final FileFormat<WebSources> support = FileFormatSupport
            .builder(WebSources.class)
            .parser(Apis.JSON_PARSER.andThen(Apis::toSources).asFileParser(UTF_8))
            .extension(".json")
            .build();

    @VisibleForTesting
    @lombok.Value
    static class Apis {

        Map<String, Api> apis;
        Map<String, Api> local_apis;

        @NonNull
        WebSources toSources() {
            WebSources.Builder result = WebSources.builder();
            apis.entrySet()
                    .stream()
                    .map(apiByHost -> apiByHost.getValue().toSource(apiByHost.getKey()))
                    .forEach(result::source);
            return result.build();
        }

        private static final GsonIO.GsonParser<Apis> JSON_PARSER =
                GsonIO.GsonParser
                        .builder(Apis.class)
                        .deserializer(Apis.class, Apis::deserialize)
                        .deserializer(Api.class, Api::deserialize)
                        .build();

        @MightBeGenerated
        static Apis deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            return new Apis(
                    GsonUtil.getAsMap(obj, "apis", context, Api.class),
                    GsonUtil.getAsMap(obj, "local_apis", context, Api.class)
            );
        }
    }

    @VisibleForTesting
    @lombok.Value
    static class Api {

        String[] alias;
        String description;
        String url;
        String[] version;
        String[] lang;

        @NonNull
        WebSource toSource(@NonNull String host) {
            String sourceId;
            List<String> sourceAliases;
            if (alias != null && alias.length > 0) {
                sourceId = normalizeId(alias[0]);
                sourceAliases = Stream.of(alias).skip(1).map(Api::normalizeId).collect(toList());
            } else {
                sourceId = normalizeId(host);
                sourceAliases = Collections.emptyList();
            }
            return WebSource
                    .builder()
                    .id(sourceId)
                    .driver(PX_PXWEB)
                    .confidentiality(Confidentiality.PUBLIC)
                    .endpointOf(normalizeEndpoint(url))
                    .aliases(sourceAliases)
                    .name(DEFAULT_LANG, description)
                    .propertyOf(VERSIONS_PROPERTY, String.join(",", version))
                    .propertyOf(LANGUAGES_PROPERTY, String.join(",", lang))
                    .build();
        }

        private static @NonNull String normalizeId(@NonNull String id) {
            return id.toUpperCase(Locale.ROOT)
                    .replace("-", "_")
                    .replace(".", "_");
        }

        private static @NonNull String normalizeEndpoint(@NonNull String endpoint) {
            return endpoint
                    .replace("[version]", VERSION_VARIABLE)
                    .replace("[lang]", LANGUAGE_VARIABLE);
        }

        @MightBeGenerated
        static Api deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            return new Api(
                    GsonUtil.getAsStringArray(obj, "alias"),
                    GsonUtil.getAsString(obj, "description"),
                    GsonUtil.getAsString(obj, "url"),
                    GsonUtil.getAsStringArray(obj, "version"),
                    GsonUtil.getAsStringArray(obj, "lang")
            );
        }
    }
}
