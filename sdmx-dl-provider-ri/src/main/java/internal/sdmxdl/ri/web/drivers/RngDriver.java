package internal.sdmxdl.ri.web.drivers;

import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.ext.SdmxException;
import sdmxdl.util.web.SdmxValidators;
import sdmxdl.util.web.Validator;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static sdmxdl.DataSet.toDataSet;

@ServiceProvider
public final class RngDriver implements WebDriver {

    private static final String RI_RNG = "ri:rng";

    private static final BooleanProperty ENABLE =
            BooleanProperty.of("enableRngDriver", false);

    private final Validator<SdmxWebSource> sourceValidator = SdmxValidators.onDriverName(RI_RNG);

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
    public @NonNull Connection connect(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IOException, IllegalArgumentException {
        sourceValidator.checkValidity(source);

        RngDriverId config = RngDriverId.parse(source.getEndpoint());

        return new RngWebConnection(source.getId(), config);
    }

    @Override
    public @NonNull Collection<SdmxWebSource> getDefaultSources() {
        return singleton(
                SdmxWebSource
                        .builder()
                        .name("RNG")
                        .descriptionOf("Random number generator")
                        .driver(RI_RNG)
                        .endpointOf("rng:3:4:0:2010-01-01")
                        .build()
        );
    }

    @Override
    public @NonNull Collection<String> getSupportedProperties() {
        return emptyList();
    }

    @Override
    public @NonNull String getDefaultDialect() {
        return NO_DEFAULT_DIALECT;
    }

    @RepresentableAs(URI.class)
    @lombok.Value
    @lombok.Builder
    private static class RngDriverId {

        int seriesCount;
        int yearCount;
        int seed;
        LocalDateTime start;

        public URI toURI() {
            return URI.create("rng" + ":" + seriesCount + ":" + yearCount + ":" + seed + ":" + start.toLocalDate().toString());
        }

        @StaticFactoryMethod
        public static RngDriverId parse(URI endpoint) throws IllegalArgumentException {
            String[] tmp = endpoint.toString().split(":", -1);

            if (tmp.length != 5) {
                throw new IllegalArgumentException("Invalid uri");
            }

            return new RngDriverId(
                    INTEGER_PARSER.parseValue(tmp[1]).orElse(3),
                    INTEGER_PARSER.parseValue(tmp[2]).orElse(4),
                    INTEGER_PARSER.parseValue(tmp[3]).orElse(0),
                    LOCAL_DATE_PARSER.parseValue(tmp[4]).map(LocalDate::atStartOfDay).orElse(LocalDate.of(2010, Month.JANUARY, 1).atStartOfDay())
            );
        }

        private static final Parser<Integer> INTEGER_PARSER = Parser.onInteger();
        private static final Parser<LocalDate> LOCAL_DATE_PARSER = Parser.onDateTimeFormatter(DateTimeFormatter.ISO_DATE, LocalDate::from);
    }

    @lombok.AllArgsConstructor
    private static class RngWebConnection implements Connection {

        private static final String FREQ = "FREQ";
        private static final String INDEX = "INDEX";

        private final String sourceId;
        private final RngDriverId config;

        @Override
        public void testConnection() {
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
                    .orElseThrow(() -> SdmxException.missingFlow(sourceId, flowRef));
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
                                            .range(0, config.getSeriesCount())
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
        public @NonNull DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
            return getDataStream(flowRef, query).collect(toDataSet(flowRef, query));
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
            return Freq.stream().flatMap(freq -> newSeriesStream(freq, query));
        }

        private Stream<Series> newSeriesStream(Freq freq, DataQuery query) {
            return IntStream
                    .range(0, config.getSeriesCount())
                    .mapToObj(series -> Key.of(freq.name(), String.valueOf(series)))
                    .filter(query.getKey()::contains)
                    .map(key -> newSeries(key, freq, query.getDetail()));
        }

        private Series newSeries(Key key, Freq freq, DataDetail detail) {
            Series.Builder result = Series.builder().key(key);
            if (detail.isDataRequested()) {
                int series = Integer.parseInt(key.get(1));
                int obsCount = (int) freq.getUnit().between(config.getStart(), config.getStart().plusYears(config.getYearCount()));
                long startTimeMillis = config.getStart().toInstant(ZoneOffset.UTC).toEpochMilli();
                Random random = new Random(config.getSeed());
                IntStream
                        .range(0, obsCount)
                        .mapToObj(j -> Obs.builder().period(config.getStart().plus(j, freq.getUnit())).value(getValue(series, startTimeMillis, random)).build())
                        .forEach(result::obs);
            }
            return result.build();
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            return EnumSet.allOf(Feature.class);
        }

        @Override
        public void close() {
        }

        private double getValue(int series, long startTimeMillis, Random random) {
            return Math.round(Math.abs((100 * (Math.cos(startTimeMillis * series))) + (100 * (Math.sin(startTimeMillis) - Math.cos(random.nextDouble()) + Math.tan(random.nextDouble())))) - 50);
        }

        @lombok.AllArgsConstructor
        @lombok.Getter
        enum Freq {
            A("Annual", ChronoUnit.YEARS),
            M("Monthly", ChronoUnit.MONTHS),
            D("Daily", ChronoUnit.DAYS);

            private final String label;
            private final ChronoUnit unit;

            public static Stream<Freq> stream() {
                return Stream.of(values());
            }
        }
    }
}
