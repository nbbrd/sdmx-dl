package internal.sdmxdl.xml;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.URI;

@lombok.Value
public class Xmlns {

    public static @NonNull Xmlns of(@NonNull String uri) {
        return new Xmlns(URI.create(uri));
    }

    @lombok.NonNull
    private final URI uri;

    public boolean is(@NonNull String found) {
        return is(URI.create(found));
    }

    public boolean is(@NonNull URI found) {
        return uri.getRawSchemeSpecificPart().equalsIgnoreCase(found.getRawSchemeSpecificPart());
    }
}
