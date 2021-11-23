package internal.sdmxdl.ri.web.monitors;

import com.google.gson.Gson;
import internal.util.http.HttpClient;
import internal.util.http.HttpResponse;
import internal.util.http.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static internal.sdmxdl.ri.web.RiHttpUtils.newRequest;
import static sdmxdl.LanguagePriorityList.ANY;

@lombok.Data
class UpptimeSummary {

    String name;
    String status;
    String uptime;
    long time;

    static @NonNull List<UpptimeSummary> parseAll(@NonNull Reader reader) {
        return Arrays.asList(new Gson().fromJson(reader, UpptimeSummary[].class));
    }

    static @NonNull List<UpptimeSummary> request(@NonNull HttpClient client, @NonNull UpptimeId id) throws IOException {
        try (HttpResponse response = client.requestGET(newRequest(id.toSummaryURL(), MEDIA_TYPES, ANY))) {
            try (Reader reader = response.getBodyAsReader()) {
                return parseAll(reader);
            }
        }
    }

    private static final List<MediaType> MEDIA_TYPES = Collections.singletonList(MediaType.ANY_TYPE);

    static @NonNull UpptimeSummary of(String name, String status, String uptime, long time) {
        UpptimeSummary result = new UpptimeSummary();
        result.setName(name);
        result.setStatus(status);
        result.setUptime(uptime);
        result.setTime(time);
        return result;
    }
}
