package internal.util.http;

import lombok.NonNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

public final class DefaultHttpURLConnectionFactory implements HttpURLConnectionFactory {

    @Override
    public @NonNull HttpURLConnection openConnection(@NonNull URL url, @NonNull Proxy proxy) throws IOException {
        URLConnection result = url.openConnection(proxy);
        if (result instanceof HttpURLConnection) {
            return (HttpURLConnection) result;
        }
        throw new IOException("Unsupported connection type");
    }
}
