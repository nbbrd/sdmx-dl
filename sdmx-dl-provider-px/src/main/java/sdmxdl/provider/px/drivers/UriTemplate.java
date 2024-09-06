package sdmxdl.provider.px.drivers;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;

@MightBePromoted
final class UriTemplate {

    private UriTemplate() {
        // static class
    }

    public static @NonNull String getVariable(@NonNull String name) {
        return "_" + name.toUpperCase(Locale.ROOT) + "_";
    }

    public static @NonNull URI expand(@NonNull URI uri, @NonNull Map<String, String> variables) throws URISyntaxException {
        String template = uri.toString();
        for (Map.Entry<String, String> variable : variables.entrySet()) {
            template = template.replace(variable.getKey(), variable.getValue());
        }
        return new URI(template);
    }
}
