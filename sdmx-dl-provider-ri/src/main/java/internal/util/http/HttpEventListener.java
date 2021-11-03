package internal.util.http;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.Proxy;
import java.net.URL;
import java.util.List;

public interface HttpEventListener {

    void onOpen(@NonNull HttpRequest request, @NonNull Proxy proxy, @NonNull HttpAuthScheme scheme);

    void onSuccess(@NonNull MediaType mediaType);

    void onRedirection(@NonNull URL oldUrl, @NonNull URL newUrl);

    void onUnauthorized(@NonNull URL url, @NonNull HttpAuthScheme oldScheme, @NonNull HttpAuthScheme newScheme);

    void onEvent(@NonNull String message);

    static @NonNull HttpEventListener noOp() {
        return HttpImpl.EventListeners.NONE;
    }
}
