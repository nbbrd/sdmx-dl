package internal.util.http;

import lombok.NonNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@lombok.Getter
public final class HttpResponseException extends IOException {

    private final int responseCode;
    private final String responseMessage;
    private final Map<String, List<String>> headerFields;

    public HttpResponseException(int responseCode, @NonNull String responseMessage) {
        this(responseCode, responseMessage, Collections.emptyMap());
    }

    public HttpResponseException(int responseCode, @NonNull String responseMessage, @NonNull Map<String, List<String>> headerFields) {
        super(responseCode + ": " + responseMessage);
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.headerFields = headerFields;
    }
}
