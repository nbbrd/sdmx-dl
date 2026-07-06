package sdmxdl.provider.ri.monitors;

import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.io.http.UriQueryBuilder;

import java.net.URI;
import java.util.Locale;

@RepresentableAs(URI.class)
@lombok.Value
@lombok.Builder(toBuilder = true)
class UpptimeId {

    public static final String URI_SCHEME = "upptime";

    @lombok.NonNull
    String owner;

    @lombok.NonNull
    String repo;

    @lombok.NonNull
    String site;

    private String getNormalizedSite() {
        return site.toLowerCase(Locale.ROOT).replace('_', '-');
    }

    @Override
    public String toString() {
        return URI_SCHEME + ":/" + owner + "/" + repo + "/" + site;
    }

    public @NonNull URI toSummaryURI() {
        return UriQueryBuilder
                .of(URI.create("https://raw.githubusercontent.com"))
                .path(owner)
                .path(repo)
                .path("master")
                .path("history")
                .path("summary.json")
                .build();
    }

    public @NonNull URI toReportURI() {
        return UriQueryBuilder
                .of(URI.create("https://" + owner + ".github.io"))
                .path(repo)
                .path("history")
                .path(getNormalizedSite())
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
