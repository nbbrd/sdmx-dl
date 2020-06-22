package sdmxdl.ext;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.Frequency;
import sdmxdl.Key;

import java.time.LocalDateTime;
import java.util.function.UnaryOperator;

public interface ObsParser {

    @NonNull
    Frequency getFrequency();

    @Nullable
    String getPeriod();

    @Nullable
    String getValue();

    @NonNull
    ObsParser clear();

    @NonNull
    ObsParser frequency(Key.@NonNull Builder key, @NonNull UnaryOperator<String> attributes);

    @NonNull
    ObsParser period(@Nullable String period);

    @NonNull
    ObsParser value(@Nullable String value);

    @Nullable
    LocalDateTime parsePeriod();

    @Nullable
    Double parseValue();
}
