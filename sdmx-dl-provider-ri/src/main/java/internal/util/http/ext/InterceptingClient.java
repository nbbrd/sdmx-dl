package internal.util.http.ext;

import internal.util.http.HttpClient;
import internal.util.http.HttpRequest;
import internal.util.http.HttpResponse;
import lombok.NonNull;
import nbbrd.io.Resource;

import java.io.IOException;

@lombok.AllArgsConstructor
public final class InterceptingClient implements HttpClient {

    @lombok.NonNull
    private final HttpClient delegate;

    @lombok.NonNull
    private final InterceptingClient.Interceptor interceptor;

    @Override
    public @NonNull HttpResponse requestGET(@NonNull HttpRequest request) throws IOException {
        HttpResponse result = delegate.requestGET(request);
        try {
            return interceptor.handle(delegate, request, result);
        } catch (Throwable ex) {
            Resource.ensureClosed(ex, result);
            throw ex;
        }
    }

    @FunctionalInterface
    public interface Interceptor {

        @NonNull HttpResponse handle(
                @NonNull HttpClient client,
                @NonNull HttpRequest request,
                @NonNull HttpResponse response)
                throws IOException;
    }
}
