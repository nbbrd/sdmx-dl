package internal.sdmxdl.ri.web.monitors;

import com.google.gson.Gson;
import internal.util.rest.HttpRest;
import internal.util.rest.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.LanguagePriorityList;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@lombok.Data
class UpptimeSummary {

    String name;
    String status;
    String uptime;
    long time;

    static @NonNull List<UpptimeSummary> parseAll(@NonNull Reader reader) {
        return Arrays.asList(new Gson().fromJson(reader, UpptimeSummary[].class));
    }

    static @NonNull List<UpptimeSummary> request(HttpRest.@NonNull Client client, @NonNull UpptimeId id) throws IOException {
        try (HttpRest.Response response = client.requestGET(id.toSummaryURL(), MEDIA_TYPES, LANGS)) {
            try (InputStreamReader reader = new InputStreamReader(response.getBody(), response.getContentType().getCharset().orElse(StandardCharsets.UTF_8))) {
                return parseAll(reader);
            }
        }
    }

    private static final List<MediaType> MEDIA_TYPES = Collections.singletonList(MediaType.ANY_TYPE);
    private static final String LANGS = LanguagePriorityList.ANY.toString();

    static @NonNull UpptimeSummary of(String name, String status, String uptime, long time) {
        UpptimeSummary result = new UpptimeSummary();
        result.setName(name);
        result.setStatus(status);
        result.setUptime(uptime);
        result.setTime(time);
        return result;
    }
}
