package internal.util.http;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@lombok.Getter
public final class HttpResponseException extends IOException {

    private final int responseCode;
    private final String responseMessage;
    private final Map<String, List<String>> headerFields;

    public HttpResponseException(int responseCode, @Nullable String responseMessage) {
        this(responseCode, responseMessage, Collections.emptyMap());
    }

    public HttpResponseException(int responseCode, @Nullable String responseMessage, @NonNull Map<String, List<String>> headerFields) {
        super(responseCode + ": " + responseMessage);
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.headerFields = headerFields;
    }
}
