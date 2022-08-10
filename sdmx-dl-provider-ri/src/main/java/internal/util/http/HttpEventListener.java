package internal.util.http;

import lombok.NonNull;
import sdmxdl.format.MediaType;

import java.net.Proxy;
import java.net.URL;
import java.util.Optional;
import java.util.function.Supplier;

public interface HttpEventListener {

    void onOpen(@NonNull HttpRequest request, @NonNull Proxy proxy, @NonNull HttpAuthScheme scheme);

    void onSuccess(@NonNull Supplier<String> contentType);

    void onRedirection(@NonNull URL oldUrl, @NonNull URL newUrl);

    void onUnauthorized(@NonNull URL url, @NonNull HttpAuthScheme oldScheme, @NonNull HttpAuthScheme newScheme);

    void onEvent(@NonNull String message);

    static @NonNull HttpEventListener noOp() {
        return HttpImpl.EventListeners.NONE;
    }
}
