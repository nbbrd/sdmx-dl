package tests.sdmxdl.api;

import lombok.NonNull;
import org.assertj.core.api.SoftAssertions;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.sdmxdl.api.TckUtil.startingWith;

@lombok.Value
@lombok.Builder
public class ExtensionPoint<T> {

    @NonNull Function<T, String> id;
    @NonNull Pattern idPattern;
    @NonNull ToIntFunction<T> rank;
    int rankLowerBound;
    @NonNull Function<T, Collection<String>> properties;
    @NonNull String propertiesPrefix;

    public void assertCompliance(SoftAssertions s, T instance) {
        assertThat(id.apply(instance))
                .containsPattern(idPattern)
                .isNotBlank();

        assertThat(rank.applyAsInt(instance))
                .isGreaterThanOrEqualTo(rankLowerBound);

        assertThat(properties.apply(instance))
                .are(startingWith(propertiesPrefix))
                .doesNotHaveDuplicates();

        assertThat(instance.getClass()).isFinal();
    }
}
