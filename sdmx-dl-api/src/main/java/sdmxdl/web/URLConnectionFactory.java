package sdmxdl.web;

import lombok.NonNull;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

@FunctionalInterface
public interface URLConnectionFactory {

    @NonNull URLConnection openConnection(@NonNull URL url, @NonNull Proxy proxy) throws IOException;

    static @NonNull URLConnectionFactory getDefault() {
        return URL::openConnection;
    }
}
