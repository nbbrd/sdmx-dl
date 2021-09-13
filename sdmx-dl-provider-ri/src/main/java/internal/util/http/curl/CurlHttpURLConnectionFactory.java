package internal.util.http.curl;

import internal.util.http.HttpURLConnectionFactory;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

@ServiceProvider
public final class CurlHttpURLConnectionFactory implements HttpURLConnectionFactory {

    @Override
    public @NonNull String getName() {
        return "curl";
    }

    @Override
    public @NonNull HttpURLConnection openConnection(@NonNull URL url, @NonNull Proxy proxy) {
        return new CurlHttpURLConnection(url, proxy, false);
    }
}
