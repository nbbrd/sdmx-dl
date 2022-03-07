package sdmxdl.ext;

import nbbrd.design.NotThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDateTime;
import java.util.function.UnaryOperator;

@NotThreadSafe
public interface ObsParser {

    @NonNull
    ObsParser clear();

    @NonNull
    ObsParser period(@Nullable String period);

    @NonNull
    ObsParser value(@Nullable String value);

    @Nullable
    LocalDateTime parsePeriod(@NonNull UnaryOperator<String> obsAttributes);

    @Nullable
    Double parseValue();
}
