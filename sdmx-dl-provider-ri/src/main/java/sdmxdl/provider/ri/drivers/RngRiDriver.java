package sdmxdl.provider.ri.drivers;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.NonNegative;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.provider.ConnectionSupport;
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.Marker;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static sdmxdl.Confidentiality.PUBLIC;

@DirectImpl
@ServiceProvider
public final class RngRiDriver implements Driver {

    private static final String RI_RNG = "RI_RNG";

    @PropertyDefinition
    private static final BooleanProperty ENABLE_PROPERTY =
            BooleanProperty.of("enableRngDriver", false);

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(RI_RNG)
            .rank(NATIVE_DRIVER_RANK)
            .availability(ENABLE_PROPERTY::get)
            .connector(RngRiDriver::newConnection)
            .source(WebSource
                    .builder()
                    .id("RNG")
                    .name("en", "Random number generator")
                    .driver(RI_RNG)
                    .confidentiality(PUBLIC)
                    .endpointOf("rng:3:4:0:2010-01-01")
                    .build())
            .build();

    private static @NonNull Connection newConnection(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) {
        RngDriverId config = RngDriverId.parse(source.getEndpoint());

        return new RngConnection(HasMarker.of(source), config);
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
    private static final class RngConnection implements Connection, HasMarker {

        private static final String FREQ = "FREQ";
        private static final String INDEX = "INDEX";

        @lombok.Getter
        private final Marker marker;
        private final RngDriverId config;

        @Override
        public @NonNull Optional<URI> testConnection() {
            return Optional.empty();
        }

        @Override
        public @NonNull Collection<Database> getDatabases() {
            return emptyList();
        }

        @Override
        public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database) {
            return singleton(Flow.builder().ref(FlowRef.parse("RNG")).structureRef(StructureRef.parse("STRUCT_RNG")).name("RNG").build());
        }

        @Override
        public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef) throws IOException, IllegalArgumentException {
            Flow flow = ConnectionSupport.getFlowFromFlows(database, flowRef, this, this);
            Structure dsd = Structure
                    .builder()
                    .ref(flow.getStructureRef())
                    .dimension(Dimension
                            .builder()
                            .id(FREQ)
                            .name("Frequency")
                            .codelist(Codelist
                                    .builder()
                                    .ref(CodelistRef.parse("CL_FREQ"))
                                    .codes(Freq.stream().collect(Collectors.toMap(Freq::name, Freq::getLabel)))
                                    .build())
                            .build())
                    .dimension(Dimension
                            .builder()
                            .id(INDEX)
                            .name("Index")
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
                    .name("RNG")
                    .build();
            return MetaSet
                    .builder()
                    .flow(flow)
                    .structure(dsd)
                    .build();
        }

        @Override
        public @NonNull DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
            return ConnectionSupport.getDataSetFromStream(database, flowRef, query, this);
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) {
            return Freq.stream().flatMap(freq -> newSeriesStream(freq, query));
        }

        @Override
        public @NonNull Collection<String> getAvailableDimensionCodes(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Key constraints, @NonNegative int dimensionIndex) throws IOException, IllegalArgumentException {
            return ConnectionSupport.getAvailableDimensionCodes(this, database, flowRef, constraints, dimensionIndex);
        }

        private Stream<Series> newSeriesStream(Freq freq, Query query) {
            return IntStream
                    .range(0, config.getSeriesCount())
                    .mapToObj(series -> Key.of(freq.name(), String.valueOf(series)))
                    .filter(query.getKey()::contains)
                    .map(key -> newSeries(key, freq, query.getDetail()));
        }

        private Series newSeries(Key key, Freq freq, Detail detail) {
            Series.Builder result = Series.builder().key(key);
            if (!detail.isIgnoreData()) {
                int series = Integer.parseInt(key.get(1));
                int obsCount = (int) freq.getUnit().between(config.getStart(), config.getStart().plusYears(config.getYearCount()));
                long startTimeMillis = config.getStart().toInstant(ZoneOffset.UTC).toEpochMilli();
                Random random = new Random(config.getSeed());
                IntStream
                        .range(0, obsCount)
                        .mapToObj(j -> Obs.builder().period(TimeInterval.of(config.getStart().plus(j, freq.getUnit()), Duration.ZERO)).value(getValue(series, startTimeMillis, random)).build())
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
            return (Math.abs((100 * (Math.cos(startTimeMillis * series))) + (100 * (Math.sin(startTimeMillis) - Math.cos(random.nextDouble()) + Math.tan(random.nextDouble())))) - 50);
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
