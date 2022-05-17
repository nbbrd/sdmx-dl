package internal.util.http;

import lombok.AccessLevel;
import sdmxdl.format.MediaType;

import java.net.URL;
import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpRequest {

    @lombok.NonNull
    URL query;

    @lombok.Singular
    List<MediaType> mediaTypes;

    @lombok.NonNull
    @lombok.Builder.Default
    String langs = "";

    @lombok.NonNull
    @lombok.Builder.Default
    HttpMethod method = HttpMethod.GET;

    @lombok.Builder.Default
    byte[] body = null;
}
