package internal.util.http;

import lombok.AccessLevel;
import sdmxdl.format.MediaType;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpRequest {

    @lombok.NonNull
    @lombok.Builder.Default
    HttpMethod method = HttpMethod.GET;

    @lombok.NonNull
    URL query;

    @lombok.Singular
    List<MediaType> mediaTypes;

    @lombok.NonNull
    @lombok.Builder.Default
    String langs = "";

    @lombok.Builder.Default
    byte[] body = null;

    public static final class Builder {

        public Builder bodyOf(String content) {
            return body(content.getBytes(StandardCharsets.UTF_8));
        }
    }
}
