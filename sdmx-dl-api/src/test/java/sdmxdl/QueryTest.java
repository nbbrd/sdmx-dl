package sdmxdl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.Detail.*;
import static tests.sdmxdl.api.RepoSamples.*;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

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
        assertThatNullPointerException().isThrownBy(() -> Query.ALL.execute(null));
    }

    @Test
    public void testExecuteKeyFiltering() {
        // Key.ALL includes all series
        assertThat(Query.builder().key(Key.ALL).build().execute(stream()))
                .containsExactly(S1, S2, S3);

        // Specific key matches exactly one series
        assertThat(Query.builder().key(K1).build().execute(stream())).containsExactly(S1);

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
                .satisfies(
                        s -> {
                            assertThat(s.getObs()).isNotEmpty();
                            assertThat(s.getMeta()).isNotEmpty();
                        });
    }

    @Test
    public void testExecuteDetailDataOnly() {
        Query query = Query.builder().key(K1).detail(DATA_ONLY).build();

        assertThat(query.execute(stream()))
                .singleElement()
                .satisfies(
                        s -> {
                            assertThat(s.getObs()).isNotEmpty();
                            assertThat(s.getMeta()).isEmpty();
                        });
    }

    @Test
    public void testExecuteDetailSeriesKeysOnly() {
        Query query = Query.builder().key(K1).detail(SERIES_KEYS_ONLY).build();

        assertThat(query.execute(stream()))
                .singleElement()
                .satisfies(
                        s -> {
                            assertThat(s.getObs()).isEmpty();
                            assertThat(s.getMeta()).isEmpty();
                        });
    }

    @Test
    public void testExecuteDetailNoData() {
        Query query = Query.builder().key(K1).detail(NO_DATA).build();

        assertThat(query.execute(stream()))
                .singleElement()
                .satisfies(
                        s -> {
                            assertThat(s.getObs()).isEmpty();
                            assertThat(s.getMeta()).isNotEmpty();
                        });
    }

    @Test
    public void testExecuteAllQueryReturnsSameInstances() {
        // With Query.ALL (key=ALL, detail=FULL) the series must not be copied
        assertThat(Query.ALL.execute(stream())).containsExactly(S1, S2, S3);
    }

    @Test
    public void testHasObsLevelFilter() {
        assertThat(Query.ALL.hasObsLevelFilter()).isFalse();
        assertThat(Query.builder().key(K1).detail(NO_DATA).build().hasObsLevelFilter()).isFalse();
        assertThat(
                        Query.builder()
                                .startPeriod(LocalDateTime.of(2010, 1, 1, 0, 0))
                                .build()
                                .hasObsLevelFilter())
                .isTrue();
        assertThat(
                        Query.builder()
                                .endPeriod(LocalDateTime.of(2010, 1, 1, 0, 0))
                                .build()
                                .hasObsLevelFilter())
                .isTrue();
        assertThat(Query.builder().firstNObservations(1).build().hasObsLevelFilter()).isTrue();
        assertThat(Query.builder().lastNObservations(1).build().hasObsLevelFilter()).isTrue();
    }

    @Test
    public void testExecuteStartPeriod() {
        // S1 has OBS1 (2010-01) and OBS2 (2010-02); keep only from 2010-02 onwards
        assertThat(
                        Query.builder()
                                .key(K1)
                                .startPeriod(LocalDateTime.of(2010, 2, 1, 0, 0))
                                .build()
                                .execute(stream()))
                .singleElement()
                .satisfies(s -> assertThat(s.getObs()).containsExactly(OBS2));
    }

    @Test
    public void testExecuteEndPeriod() {
        // keep only up to 2010-01
        assertThat(
                        Query.builder()
                                .key(K1)
                                .endPeriod(LocalDateTime.of(2010, 1, 1, 0, 0))
                                .build()
                                .execute(stream()))
                .singleElement()
                .satisfies(s -> assertThat(s.getObs()).containsExactly(OBS1));
    }

    @Test
    public void testExecutePeriodRange() {
        // keep only 2010-01
        assertThat(
                        Query.builder()
                                .key(K1)
                                .startPeriod(LocalDateTime.of(2010, 1, 1, 0, 0))
                                .endPeriod(LocalDateTime.of(2010, 1, 1, 0, 0))
                                .build()
                                .execute(stream()))
                .singleElement()
                .satisfies(s -> assertThat(s.getObs()).containsExactly(OBS1));
    }

    @Test
    public void testExecutePeriodRangeExcludingAll() {
        assertThat(
                        Query.builder()
                                .key(K1)
                                .startPeriod(LocalDateTime.of(9999, 1, 1, 0, 0))
                                .build()
                                .execute(stream()))
                .singleElement()
                .satisfies(s -> assertThat(s.getObs()).isEmpty());
    }

    @Test
    public void testExecuteFirstNObservations() {
        assertThat(Query.builder().key(K1).firstNObservations(1).build().execute(stream()))
                .singleElement()
                .satisfies(s -> assertThat(s.getObs()).containsExactly(OBS1));
    }

    @Test
    public void testExecuteLastNObservations() {
        assertThat(Query.builder().key(K1).lastNObservations(1).build().execute(stream()))
                .singleElement()
                .satisfies(s -> assertThat(s.getObs()).containsExactly(OBS2));
    }

    @Test
    public void testExecuteFirstAndLastUnion() {
        // On a 2-observation series, first-1 + last-1 covers the whole series (union,
        // non-overlapping)
        assertThat(
                        Query.builder()
                                .key(K1)
                                .firstNObservations(1)
                                .lastNObservations(1)
                                .build()
                                .execute(stream()))
                .singleElement()
                .satisfies(s -> assertThat(s.getObs()).containsExactly(OBS1, OBS2));
    }

    @Test
    public void testExecuteCountExceedingSize() {
        assertThat(Query.builder().key(K1).firstNObservations(10).build().execute(stream()))
                .singleElement()
                .satisfies(s -> assertThat(s.getObs()).containsExactly(OBS1, OBS2));
    }

    @Test
    public void testValidateOn() {
        assertThat(Query.ALL.validateOn(STRUCT)).isNull();
        assertThat(Query.builder().key(K1).build().validateOn(STRUCT)).isNull();
        assertThat(
                        Query.builder()
                                .key(K1)
                                .firstNObservations(1)
                                .lastNObservations(2)
                                .build()
                                .validateOn(STRUCT))
                .isNull();

        assertThat(Query.builder().key(K1).firstNObservations(0).build().validateOn(STRUCT))
                .contains("firstNObservations");
        assertThat(Query.builder().key(K1).lastNObservations(-1).build().validateOn(STRUCT))
                .contains("lastNObservations");
        assertThat(
                        Query.builder()
                                .key(K1)
                                .startPeriod(LocalDateTime.of(2011, 1, 1, 0, 0))
                                .endPeriod(LocalDateTime.of(2010, 1, 1, 0, 0))
                                .build()
                                .validateOn(STRUCT))
                .contains("startPeriod");

        // key validation is delegated
        assertThat(Query.builder().key(INVALID_KEY).build().validateOn(STRUCT))
                .contains("Expecting key");
    }

    private static Stream<Series> stream() {
        return DATA_SET.stream();
    }
}
