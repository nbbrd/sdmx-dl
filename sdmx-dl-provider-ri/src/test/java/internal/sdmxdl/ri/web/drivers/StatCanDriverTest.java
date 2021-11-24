package internal.sdmxdl.ri.web.drivers;

import _test.sdmxdl.ri.TextParsers;
import nbbrd.io.text.TextParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.LanguagePriorityList;
import sdmxdl.tck.web.SdmxWebDriverAssert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static internal.sdmxdl.ri.web.drivers.StatCanDriver.Converter.*;
import static org.assertj.core.api.Assertions.*;

public class StatCanDriverTest {

    @Test
    public void testCompliance() {
        SdmxWebDriverAssert.assertCompliance(new StatCanDriver());
    }

    @Test
    public void testDataTableParseAll() throws IOException {
        TextParser<StatCanDriver.DataTable[]> x = TextParsers.of(StatCanDriver.DataTable::parseAll);

        assertThat(x.parseResource(StatCanDriverTest.class, "statcan-datatables.json", StandardCharsets.UTF_8))
                .hasSize(2)
                .contains(StatCanDriver.DataTable
                        .builder()
                        .productId(10100001)
                        .cubeTitleEn("Federal public sector employment reconciliation of Treasury Board of Canada Secretariat, Public Service Commission of Canada and Statistics Canada statistical universes, as at December 31")
                        .cubeTitleFr("Emploi du secteur public fédéral rapprochement des univers statistiques du Secrétariat du Conseil du Trésor du Canada, de la Commission de la fonction publique du Canada et de Statistique Canada, au 31 décembre")
                        .build(), atIndex(0));
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
                    .withMessageContaining("Id");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(DataflowRef.of("tatCan", "DF_1234", "1.0")))
                    .withMessageContaining("Agency");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(DataflowRef.of("StatCan", "DF_1234", "1.")))
                    .withMessageContaining("Version");

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(DataflowRef.of("StatCan", "DF_", "1.0")))
                    .isInstanceOf(NumberFormatException.class);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(DataflowRef.of("StatCan", "DF_1234X", "1.0")))
                    .isInstanceOf(NumberFormatException.class);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> fromDataflowRef(DataflowRef.of("StatCan", "DF_-1234", "1.0")))
                    .withMessageContaining("Product ID");
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
                                    .hasSize(1)
                                    .element(0)
                                    .satisfies(dsd -> {
                                        assertThat(dsd.getDimensions())
                                                .hasSize(2);
                                        assertThat(dsd.getAttributes())
                                                .hasSize(8);
                                        assertThat(dsd.getLabel())
                                                .startsWith(label.getValue());
                                        assertThat(dsd.getRef())
                                                .isEqualTo(toDataStructureRef(10100001));
                                    });
                            assertThat(repo.getDataSets())
                                    .hasSize(1)
                                    .element(0)
                                    .satisfies(dataSet -> {
                                        assertThat(dataSet.getData())
                                                .hasSize(14);
                                        assertThat(dataSet.getRef())
                                                .isEqualTo(toDataflowRef(10100001));
                                    });
                        });
            }
        }
    }
}
