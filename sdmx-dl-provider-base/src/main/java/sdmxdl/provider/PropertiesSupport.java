package sdmxdl.provider;

import lombok.NonNull;
import nbbrd.io.text.BaseProperty;
import org.jspecify.annotations.Nullable;
import sdmxdl.file.FileSource;
import sdmxdl.web.WebSource;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@lombok.experimental.UtilityClass
public class PropertiesSupport {

    public static @NonNull Function<? super String, ? extends CharSequence> asFunction(@NonNull FileSource source) {
        return key -> PropertiesSupport.getProperty(Collections.emptyMap(), key);
    }

    public static @NonNull Function<? super String, ? extends CharSequence> asFunction(@NonNull WebSource source) {
        return key -> PropertiesSupport.getProperty(source.getProperties(), key);
    }

    public static @Nullable String getProperty(
            @NonNull Map<String, String> properties,
            @NonNull String key) {
        return getProperty(properties, System.getProperties(), System.getenv(), key);
    }

    public static @Nullable String getProperty(
            @NonNull Map<String, String> properties,
            @NonNull Properties system,
            @NonNull Map<String, String> env,
            @NonNull String key) {
        String result;
        if ((result = properties.get(key)) != null) return result;
        if ((result = system.getProperty(key)) != null) return result;
        if ((result = env.get(toEnvKey(key))) != null) return result;
        return null;
    }

    public static String toEnvKey(String key) {
        return key.replace('.', '_').toUpperCase(Locale.ROOT);
    }

    public static @NonNull List<BaseProperty> merge(@NonNull List<BaseProperty> first, @NonNull BaseProperty... second) {
        return Stream.concat(first.stream(), Stream.of(second)).collect(toList());
    }

    public static @NonNull List<BaseProperty> merge(@NonNull List<BaseProperty> first, @NonNull List<BaseProperty> second) {
        return Stream.concat(first.stream(), second.stream()).collect(toList());
    }
}
