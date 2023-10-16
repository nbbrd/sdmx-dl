/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package sdmxdl.provider.ext;

import lombok.NonNull;
import nbbrd.io.text.Parser;
import sdmxdl.*;
import sdmxdl.format.SeriesMetaUtil;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * @author Philippe Charles
 */
@lombok.Builder(toBuilder = true)
public final class SeriesMetaFactory {

    @lombok.NonNull
    private final Function<Series, TemporalAmount> timeUnit;

    @lombok.NonNull
    private final Function<Series, String> valueUnit;

    @lombok.NonNull
    private final Function<Series, String> decimal;

    @lombok.NonNull
    private final Function<Series, String> name;

    @lombok.NonNull
    private final Function<Series, String> description;

    public @NonNull SeriesMeta get(@NonNull Series series) {
        return SeriesMeta
                .builder()
                .timeUnit(timeUnit.apply(series))
                .valueUnit(valueUnit.apply(series))
                .decimals(decimal.apply(series))
                .name(name.apply(series))
                .description(description.apply(series))
                .build();
    }

    @NonNull
    public static SeriesMetaFactory getDefault(@NonNull Structure dsd) {
        return builder()
                .byContent()
                .valueUnit(getValueUnit(dsd))
                .decimal(getDecimal(dsd))
                .name(getName(dsd))
                .description(getDescription(dsd))
                .build();
    }

    @NonNull
    public static SeriesMetaFactory sdmx20(@NonNull Structure dsd) {
        return builder()
                .byAttribute(SeriesMetaUtil.TIME_FORMAT_CONCEPT, TimeUnitParsers.onTimeFormatCodeList())
                .valueUnit(getValueUnit(dsd))
                .decimal(getDecimal(dsd))
                .name(getName(dsd))
                .description(getDescription(dsd))
                .build();
    }

    @NonNull
    public static SeriesMetaFactory sdmx21(@NonNull Structure dsd) {
        return builder()
                .byDimension(getFrequencyCodeIdIndex(dsd), TimeUnitParsers.onFreqCodeList())
                .valueUnit(getValueUnit(dsd))
                .decimal(getDecimal(dsd))
                .name(getName(dsd))
                .description(getDescription(dsd))
                .build();
    }

    public static final class Builder {

        public Builder byContent() {
            return timeUnit(series -> series.getObs().stream().map(obs -> obs.getPeriod().getDuration()).distinct().findFirst().orElse(null));
        }

        public Builder byAttribute(@NonNull String attributeName, Parser<TemporalAmount> timeUnitParser) {
            return timeUnit(series -> timeUnitParser.parse(series.getMeta().get(attributeName)));
        }

        public Builder byDimension(int dimensionIndex, Parser<TemporalAmount> timeUnitParser) {
            return timeUnit(
                    dimensionIndex != NO_FREQUENCY_CODE_ID_INDEX
                            ? series -> timeUnitParser.parse(series.getKey().get(dimensionIndex))
                            : series -> null
            );
        }
    }

    public static final int NO_FREQUENCY_CODE_ID_INDEX = -1;

    public static int getFrequencyCodeIdIndex(@NonNull Structure dsd) {
        List<Dimension> dimensions = dsd.getDimensionList();
        return IntStream.range(0, dimensions.size())
                .filter(i -> isFrequencyCodeId(dimensions.get(i)))
                .findFirst()
                .orElse(NO_FREQUENCY_CODE_ID_INDEX);
    }

    private static boolean isFrequencyCodeId(Dimension o) {
        switch (o.getId()) {
            case SeriesMetaUtil.FREQ_CONCEPT:
            case "FREQUENCY":
                return true;
            default:
                return false;
        }
    }

    private static Function<Series, String> getValueUnit(Structure dsd) {
        Dimension dimension = first(dsd.getDimensions(), o -> o.getId().contains("UNIT") && !o.getId().contains("MULT"), BY_LENGTH_ID);
        if (dimension != null) {
            return onDimension(dsd.getDimensionList().indexOf(dimension));
        }
        Attribute attribute = first(dsd.getAttributes(), o -> o.getId().contains("UNIT") && !o.getId().contains("MULT"), BY_LENGTH_ID);
        if (attribute != null) {
            return onAttribute(attribute);
        }
        return NOT_FOUND;
    }

    private static Function<Series, String> getDecimal(Structure dsd) {
        Attribute attribute = first(dsd.getAttributes(), o -> o.getId().contains("DECIMALS"), BY_LENGTH_ID);
        return attribute != null ? onAttribute(attribute) : NOT_FOUND;
    }

    private static Function<Series, String> getName(Structure dsd) {
        Attribute attribute = first(dsd.getAttributes(), o -> !o.isCoded() && o.getId().contains("TITLE"), BY_LENGTH_ID);
        return attribute != null ? onAttribute(attribute) : NOT_FOUND;
    }

    private static Function<Series, String> getDescription(Structure dsd) {
        Attribute attribute = first(dsd.getAttributes(), o -> !o.isCoded() && o.getId().contains("TITLE"), BY_LENGTH_ID.reversed());
        return attribute != null ? onAttribute(attribute) : NOT_FOUND;
    }

    private static Function<Series, String> NOT_FOUND = series -> "";

    private static Function<Series, String> onDimension(int dimensionIndex) {
        return series -> series.getKey().get(dimensionIndex);
    }

    private static Function<Series, String> onAttribute(Attribute component) {
        return series -> series.getMeta().get(component.getId());
    }

    private static <T> T first(Collection<T> list, Predicate<? super T> filter, Comparator<? super T> sorter) {
        return list.stream().filter(filter).sorted(sorter).findAny().orElse(null);
    }

    private static final Comparator<Component> BY_LENGTH_ID
            = Comparator.<Component>comparingInt(o -> o.getId().length()).thenComparing(Component::getId);

    public static final TemporalAmount ANNUAL = Period.parse("P1Y");
    public static final TemporalAmount HALF_YEARLY = Period.parse("P6M");
    public static final TemporalAmount QUARTERLY = Period.parse("P3M");
    public static final TemporalAmount MONTHLY = Period.parse("P1M");
    public static final TemporalAmount WEEKLY = Period.parse("P7D");
    public static final TemporalAmount DAILY = Period.parse("P1D");
    public static final TemporalAmount DAILY_BUSINESS = Period.parse("P1D");
    public static final TemporalAmount HOURLY = Duration.parse("PT1H");
    public static final TemporalAmount MINUTELY = Duration.parse("PT1M");
    public static final TemporalAmount UNDEFINED = null;
}
