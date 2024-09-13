package sdmxdl;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class Options {

    public static final Options DEFAULT = Options.builder().build();

    public static Options of(Languages languages) {
        return Options.builder().languages(languages).build();
    }

    @NonNull
    @lombok.Builder.Default
    Languages languages = Languages.ANY;

    @Nullable
    String catalogId;
}
