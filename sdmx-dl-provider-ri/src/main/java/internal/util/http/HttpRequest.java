package internal.util.http;

import java.net.URL;
import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class HttpRequest {

    @lombok.NonNull
    URL query;

    @lombok.Singular
    List<MediaType> mediaTypes;

    @lombok.NonNull
    @lombok.Builder.Default
    String langs = "";
}
