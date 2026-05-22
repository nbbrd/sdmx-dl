package sdmxdl;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.Detail.*;
import static tests.sdmxdl.api.RepoSamples.*;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("ConstantConditions")
public class QueryTest {

    @Test
    public void testAllConstant() {
        assertThat(Query.ALL.getKey()).isEqualTo(Key.ALL);
        assertThat(Query.ALL.getDetail()).isEqualTo(FULL);
    }

    @Test
    public void testExecuteNullPointer() {
        assertThatNullPointerException()
                .isThrownBy(() -> Query.ALL.execute(null));
    }

    @Test
    public void testExecuteKeyFiltering() {
        // Key.ALL includes all series
        assertThat(Query.builder().key(Key.ALL).build().execute(stream()))
                .containsExactly(S1, S2, S3);

        // Specific key matches exactly one series
        assertThat(Query.builder().key(K1).build().execute(stream()))
                .containsExactly(S1);

        // Wildcard key matches a subset
        assertThat(Query.builder().key(Key.of("M", "BE", "")).build().execute(stream()))
                .containsExactly(S1, S2);

        // Key that matches nothing
        assertThat(Query.builder().key(Key.of("X", "BE", "INDUSTRY")).build().execute(stream()))
                .isEmpty();
    }

    @Test
    public void testExecuteDetailFull() {
        Query query = Query.builder().key(K1).detail(FULL).build();

        assertThat(query.execute(stream()))
                .singleElement()
                .satisfies(s -> {
                    assertThat(s.getObs()).isNotEmpty();
                    assertThat(s.getMeta()).isNotEmpty();
                });
    }

    @Test
    public void testExecuteDetailDataOnly() {
        Query query = Query.builder().key(K1).detail(DATA_ONLY).build();

        assertThat(query.execute(stream()))
                .singleElement()
                .satisfies(s -> {
                    assertThat(s.getObs()).isNotEmpty();
                    assertThat(s.getMeta()).isEmpty();
                });
    }

    @Test
    public void testExecuteDetailSeriesKeysOnly() {
        Query query = Query.builder().key(K1).detail(SERIES_KEYS_ONLY).build();

        assertThat(query.execute(stream()))
                .singleElement()
                .satisfies(s -> {
                    assertThat(s.getObs()).isEmpty();
                    assertThat(s.getMeta()).isEmpty();
                });
    }

    @Test
    public void testExecuteDetailNoData() {
        Query query = Query.builder().key(K1).detail(NO_DATA).build();

        assertThat(query.execute(stream()))
                .singleElement()
                .satisfies(s -> {
                    assertThat(s.getObs()).isEmpty();
                    assertThat(s.getMeta()).isNotEmpty();
                });
    }

    @Test
    public void testExecuteAllQueryReturnsSameInstances() {
        // With Query.ALL (key=ALL, detail=FULL) the series must not be copied
        assertThat(Query.ALL.execute(stream()))
                .containsExactly(S1, S2, S3);
    }

    private static Stream<Series> stream() {
        return DATA_SET.stream();
    }
}

