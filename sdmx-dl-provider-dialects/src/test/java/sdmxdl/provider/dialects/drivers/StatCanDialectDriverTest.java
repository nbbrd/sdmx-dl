package sdmxdl.provider.dialects.drivers;

import nbbrd.io.Resource;
import nbbrd.io.text.TextParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import sdmxdl.*;
import sdmxdl.format.MemCachingSupport;
import sdmxdl.provider.ri.networking.RiNetworking;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.spi.DriverAssert;
import tests.sdmxdl.web.spi.EnableWebQueriesOnSystemProperty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.DatabaseRef.NO_DATABASE;
import static sdmxdl.Languages.ANY;
import static sdmxdl.provider.dialects.drivers.StatCanDialectDriver.Converter.*;
import static tests.sdmxdl.api.SdmxConditions.uniqueObs;
import static tests.sdmxdl.api.SdmxConditions.uniqueSeriesKeys;

public class StatCanDialectDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new StatCanDialectDriver());
    }

    @Test
    public void testConnectionArgs() throws IOException {
        StatCanDialectDriver driver = new StatCanDialectDriver();
        WebSource source = driver.getDefaultSources().iterator().next();
        try (Connection connection = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
            FlowRef badFlowRef = FlowRef.parse("F_10100001");
            String msg = "Expecting DataflowRef id 'F_10100001' to match pattern 'DF_\\d+'";

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> connection.getMeta(NO_DATABASE, badFlowRef))
                    .withMessageContaining(msg);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> connection.getData(NO_DATABASE, badFlowRef, Query.ALL))
                    .withMessageContaining(msg);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> connection.getDataStream(NO_DATABASE, badFlowRef, Query.ALL))
                    .withMessageContaining(msg);
        }
    }

    @Test
    public void testDataTableParseAll() throws IOException {
        TextParser<StatCanDialectDriver.DataTable[]> x = TextParser.onParsingReader(StatCanDialectDriver.DataTable::parseAll);

        Assertions.assertThat(x.parseResource(StatCanDialectDriverTest.class, "statcan-datatables.json", StandardCharsets.UTF_8))
                .hasSize(2)
                .contains(new StatCanDialectDriver.DataTable(
                        10100001,
                        "Federal public sector employment reconciliation of Treasury Board of Canada Secretariat, Public Service Commission of Canada and Statistics Canada statistical universes, as at December 31",
                        "Emploi du secteur public fédéral rapprochement des univers statistiques du Secrétariat du Conseil du Trésor du Canada, de la Commission de la fonction publique du Canada et de Statistique Canada, au 31 décembre"
                ), atIndex(0));
    }

    @Nested
    class ConverterTest {

        @Test
        public void testToDataflowRef() {
            assertThat(toDataflowRef(1234))
                    .isEqualTo(FlowRef.of("StatCan", "DF_1234", "1.0"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> toDataflowRef(-1))
                    .withMessageContaining("Product ID");
        }

        @Test
        public void testFromDataflowRef() {
            assertThat(fromDataflowRef(FlowRef.of("StatCan", "DF_1234", "1.0")))
                    .isEqualTo(1234);

            assertThat(fromDataflowRef(FlowRef.of("all", "DF_1234", "1.0")))
                    .isEqualTo(1234);

            assertThat(fromDataflowRef(FlowRef.of("StatCan", "DF_1234", "latest")))
                    .isEqualTo(1234);

            assertThatNullPointerException()
                    .isThrownBy(() -> fromDataflowRef(null));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(FlowRef.of("StatCan", "F_1234", "1.0")))
                    .withMessage("Expecting DataflowRef id 'F_1234' to match pattern 'DF_\\d+'");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(FlowRef.of("tatCan", "DF_1234", "1.0")))
                    .withMessage("Expecting DataflowRef agency 'tatCan' to match pattern 'StatCan|all'");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(FlowRef.of("StatCan", "DF_1234", "1.")))
                    .withMessage("Expecting DataflowRef version '1.' to match pattern '1\\.0|latest'");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(FlowRef.of("StatCan", "DF_", "1.0")))
                    .withMessage("Expecting DataflowRef id 'DF_' to match pattern 'DF_\\d+'");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(FlowRef.of("StatCan", "DF_1234X", "1.0")))
                    .withMessage("Expecting DataflowRef id 'DF_1234X' to match pattern 'DF_\\d+'");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(FlowRef.of("StatCan", "DF_-1234", "1.0")))
                    .withMessage("Expecting DataflowRef id 'DF_-1234' to match pattern 'DF_\\d+'");
        }

        @Test
        public void testToDataStructureRef() {
            assertThat(toDataStructureRef(1234))
                    .isEqualTo(StructureRef.of("StatCan", "Data_Structure_1234", "1.0"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> toDataStructureRef(-1))
                    .withMessageContaining("Product ID");
        }

        @Test
        public void testToSdmxRepository(@TempDir File tmp) throws IOException {
            String fileName = "statcan-10100001.zip";
            File x = tmp.toPath().resolve(fileName).toFile();
            Files.copy(StatCanDialectDriverTest.class.getResourceAsStream(fileName), x.toPath());

            Map<Languages, String> labels = new HashMap<>();
            labels.put(ANY, "Data Structure");
            labels.put(Languages.parse("en"), "Data Structure");
            labels.put(Languages.parse("fr"), "Structure de données");

            for (Map.Entry<Languages, String> label : labels.entrySet()) {
                assertThat(toSdmxRepository(x, 10100001, label.getKey()))
                        .satisfies(repo -> {
                            assertThat(repo.getStructures())
                                    .singleElement()
                                    .satisfies(dsd -> {
                                        assertThat(dsd.getDimensions())
                                                .hasSize(2);
                                        assertThat(dsd.getAttributes())
                                                .hasSize(8);
                                        assertThat(dsd.getName())
                                                .startsWith(label.getValue());
                                        assertThat(dsd.getRef())
                                                .isEqualTo(toDataStructureRef(10100001));
                                    });
                            assertThat(repo.getDataSets())
                                    .singleElement()
                                    .satisfies(dataSet -> {
                                        assertThat(dataSet.getData())
                                                .hasSize(14);
                                        assertThat(dataSet.getRef())
                                                .isEqualTo(toDataflowRef(10100001));
                                    });
                        });
            }
        }

        @Test
        public void testRevisions(@TempDir File tmp) throws IOException {
            String fileName = "statcan-34100158.zip";
            File x = tmp.toPath().resolve(fileName).toFile();
            try (InputStream stream = Resource.newInputStream(StatCanDialectDriverTest.class, fileName)) {
                Files.copy(stream, x.toPath());
            }

            assertThat(toSdmxRepository(x, 34100158, ANY).getDataSets().get(0).getData())
                    .has(uniqueSeriesKeys())
                    .have(uniqueObs())
                    .filteredOn(Series::getKey, Key.parse("1"))
                    .singleElement()
                    .satisfies(
                            series -> assertThat(series.getObs())
                                    .hasSize(388)
                                    .startsWith(obsOf("1990-01-01", "P1M", 276.428))
                                    .endsWith(obsOf("2022-04-01", "P1M", 267.330))
                                    .filteredOn(Obs::getPeriod, periodOf("2021-07-01", "P1M"))
                                    .singleElement()
                                    .isEqualTo(obsOf("2021-07-01", "P1M", 274.067))
                    )
            ;
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "StatCanDialectDriverTest.csv", useHeadersInDisplayName = true)
    @EnableWebQueriesOnSystemProperty
    public void testBuiltinSources(String source, String flow, String key, int minFlowCount, int dimCount, int minSeriesCount, int minObsCount, String details) throws IOException {
        DriverAssert.assertBuiltinSource(new StatCanDialectDriver(), DriverAssert.SourceQuery
                        .builder()
                        .source(source)
                        .keyRequest(KeyRequest.builder().flowOf(flow).keyOf(key).build())
                        .minFlowCount(minFlowCount)
                        .dimCount(dimCount)
                        .minSeriesCount(minSeriesCount)
                        .minObsCount(minObsCount)
                        .build(),
                context
        );
    }

    private final WebContext context = WebContext
            .builder()
            .caching(MemCachingSupport.builder().id("local").build())
            .networking(new RiNetworking())
            .onEvent(source -> DriverAssert.eventOf(source, System.out::println))
            .build();

    private static TimeInterval periodOf(String localDate, String duration) {
        return TimeInterval.of(LocalDate.parse(localDate).atStartOfDay(), Duration.parse(duration));
    }

    private static Obs obsOf(String localDate, String duration, double value) {
        return Obs
                .builder()
                .period(periodOf(localDate, duration))
                .value(value)
                .build();
    }
}

