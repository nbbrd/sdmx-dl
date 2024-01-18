package sdmxdl.format;

import sdmxdl.web.WebSource;

import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class WebSources {

    @lombok.NonNull
    @lombok.Singular
    List<WebSource> sources;
}
