package sdmxdl.ext;

import nbbrd.design.NotThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.Frequency;
import sdmxdl.Key;

import java.time.LocalDateTime;
import java.util.function.UnaryOperator;

@NotThreadSafe
public interface ObsParser {

    @NonNull
    ObsParser clear();

    @NonNull
    ObsParser head(Key.@NonNull Builder seriesKey, @NonNull UnaryOperator<String> seriesAttributes);

    @NonNull
    ObsParser period(@Nullable String period);

    @NonNull
    ObsParser value(@Nullable String value);

    @NonNull
    Frequency getFrequency();

    @Nullable
    LocalDateTime parsePeriod(@NonNull UnaryOperator<String> obsAttributes);

    @Nullable
    Double parseValue();
}
