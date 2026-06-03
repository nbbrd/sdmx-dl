package sdmxdl.provider.ri.monitors;

import com.google.gson.*;
import sdmxdl.provider.ri.drivers.RiHttpUtils;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpResponse;
import lombok.NonNull;
import nbbrd.io.net.MediaType;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static sdmxdl.Languages.ANY;

@lombok.Value
class UpptimeSummary {

    String name;
    String status;
    String uptime;
    long time;

    static @NonNull List<UpptimeSummary> parseAll(@NonNull Reader reader) {
        return Arrays.asList(GSON.fromJson(reader, UpptimeSummary[].class));
    }

    static @NonNull List<UpptimeSummary> request(@NonNull HttpClient client, @NonNull URI summaryURI) throws IOException {
        try (HttpResponse response = client.send(RiHttpUtils.newHttpRequest(summaryURI, MEDIA_TYPES, ANY))) {
            try (Reader reader = response.getBodyAsReader()) {
                return parseAll(reader);
            }
        }
    }

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(UpptimeSummary.class, (JsonDeserializer<UpptimeSummary>) UpptimeSummary::deserialize)
            .create();

    private static UpptimeSummary deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject x = json.getAsJsonObject();
        return new UpptimeSummary(
                x.get("name").getAsString(),
                x.get("status").getAsString(),
                x.get("uptime").getAsString(),
                x.get("time").getAsLong()
        );
    }

    private static final List<MediaType> MEDIA_TYPES = Collections.singletonList(MediaType.ANY_TYPE);
}
