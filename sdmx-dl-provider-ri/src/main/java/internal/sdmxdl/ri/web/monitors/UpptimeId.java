package internal.sdmxdl.ri.web.monitors;

import internal.util.http.URLQueryBuilder;
import nbbrd.design.StringValue;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.MalformedURLException;
import java.net.URL;

@StringValue
@lombok.Value
public class UpptimeId {

    @lombok.NonNull
    String owner;

    @lombok.NonNull
    String repo;

    @lombok.NonNull
    String site;

    @Override
    public String toString() {
        return owner + ":" + repo + ":" + site;
    }

    public @NonNull URL toSummaryURL() throws MalformedURLException {
        return URLQueryBuilder
                .of(new URL("https://raw.githubusercontent.com"))
                .path(owner)
                .path(repo)
                .path("master")
                .path("history")
                .path("summary.json")
                .build();
    }

    public static @NonNull UpptimeId parse(@NonNull CharSequence id) throws IllegalArgumentException {
        String[] items = id.toString().split(":", -1);
        if (items.length != 3) {
            throw new IllegalArgumentException("Cannot parse id; expected 3 parts, found " + items.length);
        }
        return new UpptimeId(items[0], items[1], items[2]);
    }
}
