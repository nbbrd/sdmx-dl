package internal.sdmxdl.ri.web.monitors;

import com.google.gson.*;
import internal.util.http.HttpClient;
import internal.util.http.HttpResponse;
import internal.util.http.MediaType;
import lombok.NonNull;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static internal.sdmxdl.ri.web.RiHttpUtils.newRequest;
import static sdmxdl.LanguagePriorityList.ANY;

@lombok.Value
class UpptimeSummary {

    String name;
    String status;
    String uptime;
    long time;

    static @NonNull List<UpptimeSummary> parseAll(@NonNull Reader reader) {
        return Arrays.asList(GSON.fromJson(reader, UpptimeSummary[].class));
    }

    static @NonNull List<UpptimeSummary> request(@NonNull HttpClient client, @NonNull UpptimeId id) throws IOException {
        try (HttpResponse response = client.requestGET(newRequest(id.toSummaryURL(), MEDIA_TYPES, ANY))) {
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
