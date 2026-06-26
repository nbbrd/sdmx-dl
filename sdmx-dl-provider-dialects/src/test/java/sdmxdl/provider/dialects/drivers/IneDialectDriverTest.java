package sdmxdl.provider.dialects.drivers;

import nbbrd.io.text.TextParser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import sdmxdl.*;
import sdmxdl.provider.caching.MemCachingSupport;
import sdmxdl.provider.ri.networking.RiNetworking;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.provider.dialects.drivers.IneDialectDriver.Converter.*;

public class IneDialectDriverTest {

    @nbbrd.design.Demo
    public static void main(String[] args) throws IOException {

        Provider<WebSource> ine = SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .onEvent(SdmxWebManager::printEvent)
                .onError(SdmxWebManager::printError)
                .caching(WebCaching.noOp())
                .build()
                .usingName("INE");

        SourceRequest sourceRequest = SourceRequest.builder().languagesOf("en").build();
//        ine.getDatabases(sourceRequest).forEach(System.out::println);

        DatabaseRequest databaseRequest = DatabaseRequest.builderOf(sourceRequest).databaseOf("IPC").build();
//        ine.getFlows(databaseRequest).forEach(System.out::println);

        FlowRequest flowRequest = FlowRequest.builderOf(databaseRequest).flowOf("INE,24077,1.0").build();
//        ine.getMeta(flowRequest).getStructure().getDimensions().forEach(System.out::println);

        KeyRequest keyRequest = KeyRequest.builderOf(flowRequest).build();
        ine.getData(keyRequest).getData().stream().limit(3).forEach(System.out::println);
    }

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new IneDialectDriver());
    }

    @Test
    public void testOperationParseAll() throws IOException {
        TextParser<IneDialectDriver.Operation[]> parser =
                TextParser.onParsingReader(IneDialectDriver.Operation::parseAll);

        IneDialectDriver.Operation[] ops = parser.parseResource(
                IneDialectDriverTest.class, "ine-operations.json", StandardCharsets.UTF_8);

        assertThat(ops).hasSizeGreaterThan(0);
        IneDialectDriver.Operation first = ops[0];
        assertThat(first.getCod()).isNotBlank();
        assertThat(first.getNombre()).isNotBlank();
    }

    @Test
    public void testTableParseAll() throws IOException {
        TextParser<IneDialectDriver.Table[]> parser =
                TextParser.onParsingReader(IneDialectDriver.Table::parseAll);

        IneDialectDriver.Table[] tables = parser.parseResource(
                IneDialectDriverTest.class, "ine-tables.json", StandardCharsets.UTF_8);

        assertThat(tables).hasSizeGreaterThan(0);
        IneDialectDriver.Table first = tables[0];
        assertThat(first.getId()).isGreaterThan(0);
        assertThat(first.getNombre()).isNotBlank();
    }

    @Test
    public void testSeriesEntryParseAll() throws IOException {
        TextParser<IneDialectDriver.SeriesEntry[]> parser =
                TextParser.onParsingReader(IneDialectDriver.SeriesEntry::parseAll);

        IneDialectDriver.SeriesEntry[] series = parser.parseResource(
                IneDialectDriverTest.class, "ine-series.json", StandardCharsets.UTF_8);

        assertThat(series).hasSizeGreaterThan(0);
        IneDialectDriver.SeriesEntry first = series[0];
        assertThat(first.getCod()).isNotBlank();
        assertThat(first.getNombre()).isNotBlank();
        assertThat(first.getMetaData()).isNotEmpty();
        assertThat(first.getData()).isNotEmpty();
    }

    @Test
    public void testConverterToDatabase() {
        IneDialectDriver.Operation op = new IneDialectDriver.Operation(25, "30138", "IPC", "Índice de Precios de Consumo");
        Database db = toDatabase(op);
        assertThat(db.getRef()).isEqualTo(DatabaseRef.parse("IPC"));
        assertThat(db.getName()).isEqualTo("Índice de Precios de Consumo");
    }

    @Test
    public void testConverterToFlow() {
        IneDialectDriver.Table table = new IneDialectDriver.Table(50902, "IPC Nacional");
        Flow flow = toFlow(table, "IPC");
        assertThat(flow.getRef()).isEqualTo(FlowRef.of("INE", "50902", "1.0"));
        assertThat(flow.getStructureRef()).isEqualTo(StructureRef.of("INE", "DS_50902", "1.0"));
        assertThat(flow.getName()).isEqualTo("IPC Nacional");
    }

    @Test
    public void testConverterToLangCode() {
        assertThat(toLangCode(Languages.parse("en"))).isEqualTo("EN");
        assertThat(toLangCode(Languages.parse("es"))).isEqualTo("ES");
        assertThat(toLangCode(Languages.ANY)).isEqualTo("EN");
    }

    @Test
    public void testConverterToStructureAndDataSet() throws IOException {
        TextParser<IneDialectDriver.SeriesEntry[]> parser =
                TextParser.onParsingReader(IneDialectDriver.SeriesEntry::parseAll);

        IneDialectDriver.SeriesEntry[] series = parser.parseResource(
                IneDialectDriverTest.class, "ine-series.json", StandardCharsets.UTF_8);

        Structure structure = toStructure(series, "50902");
        assertThat(structure.getRef()).isEqualTo(StructureRef.of("INE", "DS_50902", "1.0"));
        assertThat(structure.getDimensions()).isNotEmpty();

        FlowRef flowRef = FlowRef.of("INE", "50902", "1.0");
        DataSet dataSet = buildDataSet(flowRef, series);
        assertThat(dataSet.getRef()).isEqualTo(flowRef);
        assertThat(dataSet.getData()).isNotEmpty();
        dataSet.getData().forEach(s ->
                assertThat(s.getKey().size()).isEqualTo(structure.getDimensions().size())
        );
    }

    @ParameterizedTest
    @CsvFileSource(resources = "IneDialectDriverTest.csv", useHeadersInDisplayName = true)
    @Tag("webQueries")
    public void testBuiltinSources(
            String source, String database, String flow, String key,
            int minFlowCount, int dimCount, int minSeriesCount, int minObsCount,
            @SuppressWarnings("unused") String details) throws IOException {

        IneDialectDriver driver = new IneDialectDriver();
        WebSource webSource = driver.getDefaultSources()
                .stream()
                .filter(item -> item.getId().equals(source))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find source '" + source + "'"));

        DatabaseRef databaseRef = DatabaseRef.parse(database);
        FlowRef flowRef = FlowRef.parse(flow);
        Key queryKey = Key.parse(key);

        try (Connection connection = driver.connect(webSource, Languages.ANY, context)) {
            assertThat(connection.getDatabases())
                    .hasSizeGreaterThanOrEqualTo(1);

            assertThat(connection.getFlows(databaseRef))
                    .hasSizeGreaterThanOrEqualTo(minFlowCount);

            MetaSet meta = connection.getMeta(databaseRef, flowRef);
            assertThat(flowRef.containsRef(meta.getFlow())).isTrue();
            assertThat(meta.getStructure().getDimensions()).hasSize(dimCount);

            DataSet dataSet = connection.getData(databaseRef, flowRef,
                    Query.builder().key(queryKey).build());
            assertThat(dataSet.getData()).hasSizeGreaterThanOrEqualTo(minSeriesCount);
            assertThat(dataSet.getData().stream()
                    .mapToInt(s -> s.getObs().size())
                    .sum()).isGreaterThanOrEqualTo(minObsCount);
        }
    }

    private final WebContext context = WebContext
            .builder()
            .caching(MemCachingSupport.builder().id("local").build())
            .networking(new RiNetworking())
            .onEvent(source -> DriverAssert.eventOf(source, System.out::println))
            .build();
}
