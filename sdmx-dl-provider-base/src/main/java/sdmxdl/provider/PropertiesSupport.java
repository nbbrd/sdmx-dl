package sdmxdl.provider;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.file.FileSource;
import sdmxdl.web.WebSource;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

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

    private static String toEnvKey(String key) {
        return key.replace('.', '_').toUpperCase(Locale.ROOT);
    }
}
