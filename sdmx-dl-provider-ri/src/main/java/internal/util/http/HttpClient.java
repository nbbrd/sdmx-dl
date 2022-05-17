package internal.util.http;

import lombok.NonNull;

import java.io.IOException;

public interface HttpClient {

    @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException;
}
