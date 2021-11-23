package internal.sdmxdl.ri.web.drivers;

import _test.sdmxdl.ri.TextParsers;
import nbbrd.io.text.TextParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sdmxdl.LanguagePriorityList;
import sdmxdl.tck.web.SdmxWebDriverAssert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

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

    @Test
    public void testConverterToSdmxRepository(@TempDir File tmp) throws IOException {
        String fileName = "statcan-10100001.zip";
        File x = new File(tmp, fileName);
        Files.copy(StatCanDriverTest.class.getResourceAsStream(fileName), x.toPath());

        Map<LanguagePriorityList, String> labels = new HashMap<>();
        labels.put(LanguagePriorityList.ANY, "Data Structure");
        labels.put(LanguagePriorityList.parse("en"), "Data Structure");
        labels.put(LanguagePriorityList.parse("fr"), "Structure de données");

        for (Map.Entry<LanguagePriorityList, String> label : labels.entrySet()) {
            assertThat(StatCanDriver.Converter.toSdmxRepository(x, 10100001, label.getKey()))
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
                                            .isEqualTo(StatCanDriver.Converter.toDataStructureRef(10100001));
                                });
                        assertThat(repo.getDataSets())
                                .hasSize(1)
                                .element(0)
                                .satisfies(dataSet -> {
                                    assertThat(dataSet.getData())
                                            .hasSize(14);
                                    assertThat(dataSet.getRef())
                                            .isEqualTo(StatCanDriver.Converter.toDataflowRef(10100001));
                                });
                    });
        }
    }
}
