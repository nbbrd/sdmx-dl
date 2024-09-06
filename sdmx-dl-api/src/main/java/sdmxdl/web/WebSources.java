package sdmxdl.web;

import sdmxdl.HasPersistence;

import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class WebSources implements HasPersistence {

    public static final WebSources EMPTY = WebSources.builder().build();

    @lombok.NonNull
    @lombok.Singular
    List<WebSource> sources;
}
