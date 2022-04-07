package internal.sdmxdl.provider.ri.web.monitors;

import internal.util.http.URLQueryBuilder;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@RepresentableAs(URI.class)
@lombok.Value
@lombok.Builder
class UpptimeId {

    public static final String URI_SCHEME = "upptime";

    @lombok.NonNull
    String owner;

    @lombok.NonNull
    String repo;

    @lombok.NonNull
    String site;

    @Override
    public String toString() {
        return URI_SCHEME + ":/" + owner + "/" + repo + "/" + site;
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

    public @NonNull URI toURI() {
        return URI.create(toString());
    }

    @StaticFactoryMethod
    public static @NonNull UpptimeId parse(@NonNull URI uri) throws IllegalArgumentException {
        if (!uri.getScheme().equals(URI_SCHEME)) {
            throw new IllegalArgumentException("Invalid scheme");
        }
        String path = uri.getRawPath();
        if (path == null) {
            throw new IllegalArgumentException("Missing path");
        }
        String[] items = path.substring(1).split("/", -1);
        if (items.length != 3) {
            throw new IllegalArgumentException("Invalid path; expected 3 parts, found " + items.length);
        }
        return new UpptimeId(items[0], items[1], items[2]);
    }
}
