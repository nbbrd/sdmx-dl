package internal.sdmxdl.provider.ri.web.drivers;

import nbbrd.design.MightBePromoted;
import nbbrd.io.text.TextParser;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sdmxdl.*;
import sdmxdl.web.SdmxWebSource;
import tests.sdmxdl.web.WebDriverAssert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static internal.sdmxdl.provider.ri.web.drivers.StatCanDriver.Converter.*;
import static nbbrd.io.Resource.getResourceAsStream;
import static org.assertj.core.api.Assertions.*;

public class StatCanDriverTest {

    @Test
    public void testCompliance() {
        WebDriverAssert.assertCompliance(new StatCanDriver());
    }

    @Test
    public void testConnectionArgs() throws IOException {
        StatCanDriver driver = new StatCanDriver();
        SdmxWebSource source = driver.getDefaultSources().iterator().next();
        try (Connection connection = driver.connect(source, WebDriverAssert.noOpWebContext())) {
            DataflowRef badDataflowRef = DataflowRef.parse("F_10100001");
            String msg = "Expecting DataflowRef id 'F_10100001' to match pattern 'DF_\\d+'";

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> connection.getFlow(badDataflowRef))
                    .withMessageContaining(msg);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> connection.getStructure(badDataflowRef))
                    .withMessageContaining(msg);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> connection.getData(badDataflowRef, DataQuery.ALL))
                    .withMessageContaining(msg);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> connection.getDataStream(badDataflowRef, DataQuery.ALL))
                    .withMessageContaining(msg);
        }
    }

    @Test
    public void testDataTableParseAll() throws IOException {
        TextParser<StatCanDriver.DataTable[]> x = TextParser.onParsingReader(StatCanDriver.DataTable::parseAll);

        Assertions.assertThat(x.parseResource(StatCanDriverTest.class, "statcan-datatables.json", StandardCharsets.UTF_8))
                .hasSize(2)
                .contains(new StatCanDriver.DataTable(
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
                    .isEqualTo(DataflowRef.of("StatCan", "DF_1234", "1.0"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> toDataflowRef(-1))
                    .withMessageContaining("Product ID");
        }

        @Test
        public void testFromDataflowRef() {
            assertThat(fromDataflowRef(DataflowRef.of("StatCan", "DF_1234", "1.0")))
                    .isEqualTo(1234);

            assertThat(fromDataflowRef(DataflowRef.of("all", "DF_1234", "1.0")))
                    .isEqualTo(1234);

            assertThat(fromDataflowRef(DataflowRef.of("StatCan", "DF_1234", "latest")))
                    .isEqualTo(1234);

            assertThatNullPointerException()
                    .isThrownBy(() -> fromDataflowRef(null));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(DataflowRef.of("StatCan", "F_1234", "1.0")))
                    .withMessage("Expecting DataflowRef id 'F_1234' to match pattern 'DF_\\d+'");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(DataflowRef.of("tatCan", "DF_1234", "1.0")))
                    .withMessage("Expecting DataflowRef agency 'tatCan' to match pattern 'StatCan|all'");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(DataflowRef.of("StatCan", "DF_1234", "1.")))
                    .withMessage("Expecting DataflowRef version '1.' to match pattern '1\\.0|latest'");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(DataflowRef.of("StatCan", "DF_", "1.0")))
                    .withMessage("Expecting DataflowRef id 'DF_' to match pattern 'DF_\\d+'");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(DataflowRef.of("StatCan", "DF_1234X", "1.0")))
                    .withMessage("Expecting DataflowRef id 'DF_1234X' to match pattern 'DF_\\d+'");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(DataflowRef.of("StatCan", "DF_-1234", "1.0")))
                    .withMessage("Expecting DataflowRef id 'DF_-1234' to match pattern 'DF_\\d+'");
        }

        @Test
        public void testToDataStructureRef() {
            assertThat(toDataStructureRef(1234))
                    .isEqualTo(DataStructureRef.of("StatCan", "Data_Structure_1234", "1.0"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> toDataStructureRef(-1))
                    .withMessageContaining("Product ID");
        }

        @Test
        public void testToSdmxRepository(@TempDir File tmp) throws IOException {
            String fileName = "statcan-10100001.zip";
            File x = new File(tmp, fileName);
            Files.copy(StatCanDriverTest.class.getResourceAsStream(fileName), x.toPath());

            Map<LanguagePriorityList, String> labels = new HashMap<>();
            labels.put(LanguagePriorityList.ANY, "Data Structure");
            labels.put(LanguagePriorityList.parse("en"), "Data Structure");
            labels.put(LanguagePriorityList.parse("fr"), "Structure de données");

            for (Map.Entry<LanguagePriorityList, String> label : labels.entrySet()) {
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
            File x = new File(tmp, fileName);
            Files.copy(getResourceAsStream(StatCanDriverTest.class, fileName).orElseThrow(FileNotFoundException::new), x.toPath());

            assertThat(toSdmxRepository(x, 34100158, LanguagePriorityList.ANY).getDataSets().get(0).getData())
                    .has(uniqueKeys())
                    .have(uniqueObs())
                    .filteredOn(Series::getKey, Key.parse("1"))
                    .singleElement()
                    .satisfies(
                            series -> assertThat(series.getObs())
                                    .hasSize(388)
                                    .startsWith(obsOf("1990-01-01", 276.428))
                                    .endsWith(obsOf("2022-04-01", 267.330))
                                    .filteredOn(Obs::getPeriod, LocalDate.parse("2021-07-01").atStartOfDay())
                                    .singleElement()
                                    .isEqualTo(obsOf("2021-07-01", 274.067))
                    )
            ;
        }
    }

    private static Obs obsOf(String localDate, double value) {
        return Obs.builder().period(LocalDate.parse(localDate).atStartOfDay()).value(value).build();
    }

    @MightBePromoted
    private static Condition<Collection<? extends Series>> uniqueKeys() {
        return new Condition<>(o -> o.stream().map(Series::getKey).distinct().count() == o.size(), "unique keys");
    }

    @MightBePromoted
    private static Condition<Series> uniqueObs() {
        return new Condition<>(o -> o.getObs().stream().map(Obs::getPeriod).count() == o.getObs().size(), "unique obs");
    }
}

