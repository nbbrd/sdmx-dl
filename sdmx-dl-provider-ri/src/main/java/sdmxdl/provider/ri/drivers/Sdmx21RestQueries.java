package sdmxdl.provider.ri.drivers;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.io.http.UriQueryBuilder;
import sdmxdl.*;
import sdmxdl.provider.DataRef;

@lombok.AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Sdmx21RestQueries implements RiRestQueries {

    public static final Sdmx21RestQueries DEFAULT = new Sdmx21RestQueries(false);
    public static final Sdmx21RestQueries WITH_TRAILING_SLASH = new Sdmx21RestQueries(true);

    private final boolean trailingSlashRequired;

    @Override
    public @NonNull UriQueryBuilder getFlowsQuery(@NonNull URI endpoint) {
        return onMeta(endpoint, DEFAULT_DATAFLOW_PATH, FLOWS).trailingSlash(trailingSlashRequired);
    }

    @Override
    public @NonNull UriQueryBuilder getStructureQuery(
            @NonNull URI endpoint, @NonNull StructureRef ref) {
        return onMeta(endpoint, DEFAULT_DATASTRUCTURE_PATH, ref)
                .param(REFERENCES_PARAM, "descendants")
                .trailingSlash(trailingSlashRequired);
    }

    @Override
    public @NonNull UriQueryBuilder getDataQuery(
            @NonNull URI endpoint, @NonNull DataRef ref, @NonNull StructureRef dsdRef) {
        Query query = ref.getQuery();
        UriQueryBuilder result =
                onData(
                        endpoint,
                        DEFAULT_DATA_PATH,
                        ref.getFlowRef(),
                        query.getKey(),
                        DEFAULT_PROVIDER_REF);
        applyFilter(query.getDetail(), result);
        applyPeriodAndCount(query, result);
        return result.trailingSlash(trailingSlashRequired);
    }

    @Override
    public @NonNull UriQueryBuilder getCodelistQuery(
            @NonNull URI endpoint, @NonNull CodelistRef ref) {
        return onMeta(endpoint, DEFAULT_CODELIST_PATH, ref).trailingSlash(trailingSlashRequired);
    }

    protected void applyFilter(Detail detail, UriQueryBuilder result) {
        switch (detail) {
            case SERIES_KEYS_ONLY:
                result.param(DETAIL_PARAM, "serieskeysonly");
                break;
            case DATA_ONLY:
                result.param(DETAIL_PARAM, "dataonly");
                break;
            case NO_DATA:
                result.param(DETAIL_PARAM, "nodata");
                break;
        }
    }

    protected void applyPeriodAndCount(Query query, UriQueryBuilder result) {
        if (query.getStartPeriod() != null) {
            result.param(START_PERIOD_PARAM, formatPeriod(query.getStartPeriod()));
        }
        if (query.getEndPeriod() != null) {
            result.param(END_PERIOD_PARAM, formatPeriod(query.getEndPeriod()));
        }
        if (query.getFirstNObservations() != null) {
            result.param(FIRST_N_OBS_PARAM, String.valueOf(query.getFirstNObservations()));
        }
        if (query.getLastNObservations() != null) {
            result.param(LAST_N_OBS_PARAM, String.valueOf(query.getLastNObservations()));
        }
    }

    static String formatPeriod(LocalDateTime period) {
        if (period.getNano() == 0
                && period.getSecond() == 0
                && period.getMinute() == 0
                && period.getHour() == 0) {
            if (period.getMonthValue() == 1 && period.getDayOfMonth() == 1) {
                return String.format(Locale.ROOT, "%04d", period.getYear());
            }
            if (period.getDayOfMonth() == 1) {
                return String.format(
                        Locale.ROOT, "%04d-%02d", period.getYear(), period.getMonthValue());
            }
            return String.format(
                    Locale.ROOT,
                    "%04d-%02d-%02d",
                    period.getYear(),
                    period.getMonthValue(),
                    period.getDayOfMonth());
        }
        return period.toString();
    }

    protected UriQueryBuilder onMeta(URI endpoint, String resourcePath, ResourceRef<?> ref) {
        return UriQueryBuilder.of(endpoint)
                .path(resourcePath)
                .path(ref.getAgency())
                .path(ref.getId())
                .path(ref.getVersion());
    }

    protected UriQueryBuilder onData(
            URI endpoint, String resourcePath, FlowRef flowRef, Key key, String providerRef) {
        return UriQueryBuilder.of(endpoint)
                .path(resourcePath)
                .path(flowRef.toString())
                .path(key.toString())
                .path(providerRef);
    }

    protected static final String DEFAULT_DATAFLOW_PATH = "dataflow";
    protected static final String DEFAULT_DATASTRUCTURE_PATH = "datastructure";
    protected static final String DEFAULT_DATA_PATH = "data";
    protected static final String DEFAULT_CODELIST_PATH = "codelist";

    protected static final String DEFAULT_PROVIDER_REF = "all";

    protected static final String REFERENCES_PARAM = "references";
    protected static final String DETAIL_PARAM = "detail";
    protected static final String START_PERIOD_PARAM = "startPeriod";
    protected static final String END_PERIOD_PARAM = "endPeriod";
    protected static final String FIRST_N_OBS_PARAM = "firstNObservations";
    protected static final String LAST_N_OBS_PARAM = "lastNObservations";

    protected static final FlowRef FLOWS = FlowRef.of("all", "all", "latest");
}
