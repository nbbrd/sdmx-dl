package sdmxdl.web;

import sdmxdl.HasPersistence;

import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class WebSources implements HasPersistence {

    @lombok.NonNull
    @lombok.Singular
    List<WebSource> sources;
}
