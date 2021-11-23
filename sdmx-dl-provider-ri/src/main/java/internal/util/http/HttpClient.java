package internal.util.http;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

public interface HttpClient {

    @NonNull HttpResponse requestGET(@NonNull HttpRequest request) throws IOException;
}
