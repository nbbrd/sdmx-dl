package internal.sdmxdl.ri.web.drivers;

import nbbrd.io.text.BooleanProperty;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.SdmxException;
import sdmxdl.util.web.SdmxRestDriverSupport;
import sdmxdl.web.SdmxWebConnection;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

@ServiceProvider
public final class RngDriver implements SdmxWebDriver {

    private static final String RI_RNG = "ri:rng";

    private static final BooleanProperty ENABLE =
            BooleanProperty.of("enableRngDriver", false);

    @Override
    public @NonNull String getName() {
        return RI_RNG;
    }

    @Override
    public int getRank() {
        return NATIVE_RANK;
    }

    @Override
    public boolean isAvailable() {
        return ENABLE.get(System.getProperties());
    }

    @Override
    public @NonNull SdmxWebConnection connect(@NonNull SdmxWebSource source, @NonNull SdmxWebContext context) throws IOException, IllegalArgumentException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(context);
        SdmxRestDriverSupport.checkSource(source, getName());

        return new RngWebConnection(source.getId(), 3, 24, new Random(), System.currentTimeMillis());
    }

    @Override
    public @NonNull Collection<SdmxWebSource> getDefaultSources() {
        return singleton(
                SdmxWebSource
                        .builder()
                        .name("RNG")
                        .description("Random number generator")
                        .driver(RI_RNG)
                        .endpointOf("http://localhost")
                        .build()
        );
    }

    @Override
    public @NonNull Collection<String> getSupportedProperties() {
        return emptyList();
    }

    @lombok.AllArgsConstructor
    private static class RngWebConnection implements SdmxWebConnection {

        private static final String FREQ = "FREQ";
        private static final String INDEX = "INDEX";

        private final String name;
        private final int seriesCount;
        private final int obsCount;
        private final Random rng;
        private final long startTimeMillis;

        @Override
        public @NonNull Duration ping() {
            return Duration.ZERO;
        }

        @Override
        public @NonNull String getDriver() {
            return RI_RNG;
        }

        @Override
        public @NonNull Collection<Dataflow> getFlows() {
            return singleton(Dataflow.of(DataflowRef.parse("RNG"), DataStructureRef.parse("STRUCT_RNG"), "RNG"));
        }

        @Override
        public @NonNull Dataflow getFlow(@NonNull DataflowRef flowRef) throws IOException {
            return getFlows()
                    .stream()
                    .filter(flowRef::containsRef)
                    .findFirst()
                    .orElseThrow(() -> SdmxException.missingFlow(name, flowRef));
        }

        @Override
        public @NonNull DataStructure getStructure(@NonNull DataflowRef flowRef) throws IOException {
            Dataflow dataflow = getFlow(flowRef);
            return DataStructure
                    .builder()
                    .ref(dataflow.getStructureRef())
                    .dimension(Dimension
                            .builder()
                            .id(FREQ)
                            .label("Frequency")
                            .position(1)
                            .codelist(Codelist
                                    .builder()
                                    .ref(CodelistRef.parse("CL_FREQ"))
                                    .codes(Freq.stream().collect(Collectors.toMap(Freq::name, Freq::getLabel)))
                                    .build())
                            .build())
                    .dimension(Dimension
                            .builder()
                            .id(INDEX)
                            .label("Index")
                            .position(2)
                            .codelist(Codelist
                                    .builder()
                                    .ref(CodelistRef.parse("CL_INDEX"))
                                    .codes(IntStream
                                            .range(0, seriesCount)
                                            .mapToObj(String::valueOf)
                                            .collect(Collectors.toMap(series -> series, series -> "S" + series)))
                                    .build())
                            .build())
                    .timeDimensionId("TIME_PERIOD")
                    .primaryMeasureId("OBS_VALUE")
                    .label("RNG")
                    .build();
        }

        @Override
        public @NonNull Collection<Series> getData(@NonNull DataRef dataRef) {
            return getDataStream(dataRef).collect(Collectors.toList());
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DataRef dataRef) {
            LocalDateTime start = LocalDate.of(2010, Month.JANUARY, 1).atStartOfDay();
            return Freq.stream().flatMap(freq -> newSeriesStream(freq, start, dataRef));
        }

        private Stream<Series> newSeriesStream(Freq freq, LocalDateTime start, DataRef dataRef) {
            return IntStream
                    .range(0, seriesCount)
                    .mapToObj(series -> Key.of(freq.name(), String.valueOf(series)))
                    .filter(dataRef.getKey()::contains)
                    .map(key -> newSeries(key, freq, start, dataRef.getFilter().getDetail()));
        }

        private Series newSeries(Key key, Freq freq, LocalDateTime start, DataFilter.Detail detail) {
            Series.Builder result = Series.builder().key(key).freq(freq.getFrequency());
            if (detail.isDataRequested()) {
                int series = Integer.parseInt(key.get(1));
                result.obs(IntStream.range(0, obsCount).mapToObj(j -> Obs.builder().period(start.plus(j, freq.getUnit())).value(getValue(series)).build()).collect(Collectors.toList()));
            }
            return result.build();
        }

        @Override
        public @NonNull DataCursor getDataCursor(@NonNull DataRef dataRef) {
            return DataCursor.of(getDataStream(dataRef).iterator());
        }

        @Override
        public boolean isDetailSupported() {
            return true;
        }

        @Override
        public void close() {
        }

        private double getValue(int series) {
            return Math.round(Math.abs((100 * (Math.cos(startTimeMillis * series))) + (100 * (Math.sin(startTimeMillis) - Math.cos(rng.nextDouble()) + Math.tan(rng.nextDouble())))) - 50);
        }

        @lombok.AllArgsConstructor
        @lombok.Getter
        enum Freq {
            A("Annual", Frequency.ANNUAL, ChronoUnit.YEARS),
            M("Monthly", Frequency.MONTHLY, ChronoUnit.MONTHS),
            D("Daily", Frequency.DAILY, ChronoUnit.DAYS);

            private final String label;
            private final Frequency frequency;
            private final ChronoUnit unit;

            public static Stream<Freq> stream() {
                return Stream.of(values());
            }
        }
    }
}
