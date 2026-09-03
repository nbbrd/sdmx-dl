package sdmxdl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class Query {

    public static final Query ALL = Query.builder().build();

    @lombok.NonNull @lombok.Builder.Default Key key = Key.ALL;

    @lombok.NonNull @lombok.Builder.Default Detail detail = Detail.FULL;

    /**
     * Inclusive lower bound on the observation period.
     *
     * <p>A {@code null} value means "from the beginning". The bound is compared against the {@link
     * TimeInterval#getStart() start} of each observation period.
     */
    @Nullable LocalDateTime startPeriod;

    /**
     * Inclusive upper bound on the observation period.
     *
     * <p>A {@code null} value means "until the most recent". The bound is compared against the
     * {@link TimeInterval#getStart() start} of each observation period.
     */
    @Nullable LocalDateTime endPeriod;

    /**
     * Keep only the first N observations (chronologically) of each series after period filtering.
     *
     * <p>A {@code null} value means no first-N truncation.
     */
    @Nullable Integer firstNObservations;

    /**
     * Keep only the last N observations (chronologically) of each series after period filtering.
     *
     * <p>A {@code null} value means no last-N truncation.
     *
     * <p>When combined with {@link #firstNObservations}, the result is the union of the first-N and
     * last-N observations, computed independently on the period-filtered observations.
     */
    @Nullable Integer lastNObservations;

    public @NonNull Stream<Series> execute(@NonNull Stream<Series> data) {
        return data.filter(key::containsKey).map(this::filterObs).map(this::mapDetail);
    }

    /**
     * Tells whether this query carries at least one observation-level filter ({@link #startPeriod},
     * {@link #endPeriod}, {@link #firstNObservations} or {@link #lastNObservations}).
     *
     * <p>This is used to decide whether observation-level filtering must be (re-)applied
     * client-side, regardless of what a data source supports server-side.
     *
     * @return true if any observation-level filter is set
     */
    public boolean hasObsLevelFilter() {
        return startPeriod != null
                || endPeriod != null
                || firstNObservations != null
                || lastNObservations != null;
    }

    /**
     * Validates this query against a data structure definition.
     *
     * <p>Delegates key validation to {@link Key#validateOn(Structure)} then checks that
     * observation-count filters are strictly positive and that {@link #startPeriod} is not after
     * {@link #endPeriod}.
     *
     * @param dsd the data structure definition to validate against; must not be null
     * @return {@code null} if the query is valid, or an error message describing the first
     *     violation found
     */
    @Nullable public String validateOn(@NonNull Structure dsd) {
        String keyError = key.validateOn(dsd);
        if (keyError != null) {
            return keyError;
        }
        if (firstNObservations != null && firstNObservations <= 0) {
            return String.format(
                    Locale.ROOT,
                    "Expecting firstNObservations to be greater than 0 instead of %d",
                    firstNObservations);
        }
        if (lastNObservations != null && lastNObservations <= 0) {
            return String.format(
                    Locale.ROOT,
                    "Expecting lastNObservations to be greater than 0 instead of %d",
                    lastNObservations);
        }
        if (startPeriod != null && endPeriod != null && startPeriod.isAfter(endPeriod)) {
            return String.format(
                    Locale.ROOT,
                    "Expecting startPeriod '%s' to not be after endPeriod '%s'",
                    startPeriod,
                    endPeriod);
        }
        return null;
    }

    private Series filterObs(Series series) {
        if (!hasObsLevelFilter()) {
            return series;
        }
        List<Obs> obs = new ArrayList<>(series.getObs());
        obs = filterRange(obs);
        obs = truncate(obs);
        return series.toBuilder().clearObs().obs(obs).build();
    }

    private List<Obs> filterRange(List<Obs> obs) {
        if (startPeriod == null && endPeriod == null) {
            return obs;
        }
        List<Obs> result = new ArrayList<>();
        for (Obs o : obs) {
            LocalDateTime start = o.getPeriod().getStart();
            if (startPeriod != null && start.isBefore(startPeriod)) {
                continue;
            }
            if (endPeriod != null && start.isAfter(endPeriod)) {
                continue;
            }
            result.add(o);
        }
        return result;
    }

    private List<Obs> truncate(List<Obs> obs) {
        if (firstNObservations == null && lastNObservations == null) {
            return obs;
        }
        int size = obs.size();
        int keepFirst =
                firstNObservations != null ? Math.min(Math.max(firstNObservations, 0), size) : 0;
        int keepLast =
                lastNObservations != null ? Math.min(Math.max(lastNObservations, 0), size) : 0;
        if (keepFirst + keepLast >= size) {
            return obs;
        }
        List<Obs> result = new ArrayList<>(keepFirst + keepLast);
        result.addAll(obs.subList(0, keepFirst));
        result.addAll(obs.subList(size - keepLast, size));
        return result;
    }

    private Series mapDetail(Series series) {
        if (detail.isIgnoreData()) {
            if (detail.isIgnoreMeta()) {
                return series.toBuilder().clearObs().clearMeta().build();
            } else {
                return series.toBuilder().clearObs().build();
            }
        } else {
            if (detail.isIgnoreMeta()) {
                return series.toBuilder().clearMeta().build();
            } else {
                return series;
            }
        }
    }
}
