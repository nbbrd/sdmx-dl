package internal.util.rest;

import nbbrd.io.Resource;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@lombok.AllArgsConstructor
public final class InterceptingClient implements HttpRest.Client {

    @lombok.NonNull
    private final HttpRest.Client delegate;

    @lombok.NonNull
    private final InterceptingClient.Interceptor interceptor;

    @Override
    public HttpRest.@NonNull Response requestGET(@NonNull URL query, @NonNull List<MediaType> mediaTypes, @NonNull String langs) throws IOException {
        HttpRest.Response result = delegate.requestGET(query, mediaTypes, langs);
        try {
            return interceptor.handle(delegate, query, mediaTypes, langs, result);
        } catch (Throwable ex) {
            Resource.ensureClosed(ex, result);
            throw ex;
        }
    }

    @FunctionalInterface
    public interface Interceptor {

        HttpRest.@NonNull Response handle(
                HttpRest.@NonNull Client client,
                @NonNull URL query, @NonNull List<MediaType> mediaTypes, @NonNull String langs,
                HttpRest.@NonNull Response response)
                throws IOException;
    }
}
