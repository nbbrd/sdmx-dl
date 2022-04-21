package internal.util.http;

import lombok.NonNull;

import java.io.IOException;

public interface HttpClient {

    @NonNull HttpResponse requestGET(@NonNull HttpRequest request) throws IOException;
}
