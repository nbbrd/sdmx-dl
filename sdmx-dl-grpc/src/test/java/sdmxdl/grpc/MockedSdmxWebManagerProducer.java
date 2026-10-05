package sdmxdl.grpc;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.stream.IntStream;
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import tests.sdmxdl.web.spi.MockedDriver;
import tests.sdmxdl.web.spi.MockedRegistry;

/**
 * Replaces {@link SdmxWebManagerProducer} in tests with an in-memory manager, so that tests do not depend on
 * live web sources.
 */
@Alternative
@Priority(1)
@Singleton
public class MockedSdmxWebManagerProducer {

    private static final String DRIVER_ID = "MOCKED_DRIVER";

    @Produces
    @Singleton
    public SdmxWebManager sdmxWebManager() {
        DataRepository ecb = ecbRepository();
        DataRepository estat = DataRepository.builder()
                .name("ESTAT")
                .flow(flow("NAMA_10_GDP", "GDP and main components", "ESTAT"))
                .flow(flow("UNE_RT_M", "Unemployment by sex and age - monthly data", "ESTAT"))
                .build();
        DataRepository bbk = DataRepository.builder()
                .name("BBK")
                .flow(flow("BBEX3", "Exchange rate statistics", "BBK"))
                .build();
        return SdmxWebManager.builder()
                .driver(MockedDriver.builder()
                        .id(DRIVER_ID)
                        .available(true)
                        .repo(ecb, EnumSet.allOf(Feature.class))
                        .repo(estat, EnumSet.allOf(Feature.class))
                        .repo(bbk, EnumSet.allOf(Feature.class))
                        .build())
                .registry(MockedRegistry.builder()
                        .source(source("ECB", "European Central Bank", "https://data.ecb.europa.eu"))
                        .source(source("ESTAT", "Eurostat", "https://ec.europa.eu/eurostat"))
                        .source(source("BBK", "Deutsche Bundesbank", "https://www.bundesbank.de"))
                        .build())
                .build();
    }

    private static WebSource source(String id, String name, String website) {
        return WebSource.builder()
                .id(id)
                .name("en", name)
                .driver(DRIVER_ID)
                .endpointOf(id)
                .websiteOf(website)
                .confidentiality(Confidentiality.PUBLIC)
                .build();
    }

    private static Flow flow(String id, String name, String agency) {
        return Flow.builder()
                .ref(FlowRef.of(agency, id, "1.0"))
                .structureRef(StructureRef.of(agency, id, "1.0"))
                .name(name)
                .description(name)
                .build();
    }

    private static DataRepository ecbRepository() {
        StructureRef structureRef = StructureRef.of("ECB", "ECB_EXR1", "1.0");

        Codelist currencies = Codelist.builder()
                .ref(CodelistRef.of("ECB", "CL_CURRENCY", "1.0"))
                .code("AUD", "Australian dollar")
                .code("CAD", "Canadian dollar")
                .code("CHF", "Swiss franc")
                .code("CNY", "Chinese yuan renminbi")
                .code("DKK", "Danish krone")
                .code("EUR", "Euro")
                .code("GBP", "UK pound sterling")
                .code("JPY", "Japanese yen")
                .code("NOK", "Norwegian krone")
                .code("SEK", "Swedish krona")
                .code("USD", "US dollar")
                .code("ZAR", "South African rand")
                .build();

        Structure structure = Structure.builder()
                .ref(structureRef)
                .name("Exchange Rates")
                .dimension(Dimension.builder()
                        .id("FREQ")
                        .name("Frequency")
                        .index(0)
                        .codelist(Codelist.builder()
                                .ref(CodelistRef.of("ECB", "CL_FREQ", "1.0"))
                                .code("A", "Annual")
                                .code("M", "Monthly")
                                .build())
                        .build())
                .dimension(Dimension.builder()
                        .id("CURRENCY")
                        .name("Currency")
                        .index(1)
                        .codelist(currencies)
                        .build())
                .dimension(Dimension.builder()
                        .id("CURRENCY_DENOM")
                        .name("Currency denominator")
                        .index(2)
                        .codelist(currencies)
                        .build())
                .dimension(Dimension.builder()
                        .id("EXR_TYPE")
                        .name("Exchange rate type")
                        .index(3)
                        .codelist(Codelist.builder()
                                .ref(CodelistRef.of("ECB", "CL_EXR_TYPE", "1.0"))
                                .code("SP00", "Spot")
                                .build())
                        .build())
                .dimension(Dimension.builder()
                        .id("EXR_SUFFIX")
                        .name("Series variation - EXR context")
                        .index(4)
                        .codelist(Codelist.builder()
                                .ref(CodelistRef.of("ECB", "CL_EXR_SUFFIX", "1.0"))
                                .code("A", "Average")
                                .code("E", "End-of-period")
                                .build())
                        .build())
                .attribute(Attribute.builder()
                        .id("TITLE")
                        .name("Series title")
                        .relationship(AttributeRelationship.SERIES)
                        .build())
                .attribute(Attribute.builder()
                        .id("OBS_STATUS")
                        .name("Observation status")
                        .relationship(AttributeRelationship.OBSERVATION)
                        .codelist(Codelist.builder()
                                .ref(CodelistRef.of("ECB", "CL_OBS_STATUS", "1.0"))
                                .code("A", "Normal value")
                                .build())
                        .build())
                .timeDimensionId("TIME_PERIOD")
                .primaryMeasureId("OBS_VALUE")
                .build();

        Flow exr = Flow.builder()
                .ref(FlowRef.of("ECB", "EXR", "1.0"))
                .structureRef(structureRef)
                .name("Exchange Rates")
                .description("ECB reference exchange rates")
                .build();

        return DataRepository.builder()
                .name("ECB")
                .structure(structure)
                .flow(exr)
                .flow(flow("BSI", "Balance Sheet Items", "ECB"))
                .flow(flow("ICP", "Harmonised Index of Consumer Prices", "ECB"))
                .flow(flow("MIR", "MFI Interest Rate Statistics", "ECB"))
                .dataSet(DataSet.builder()
                        .ref(exr.getRef())
                        .series(monthlySeries("M.CHF.EUR.SP00.A", "Swiss franc/Euro", 1.6))
                        .series(monthlySeries("M.USD.EUR.SP00.A", "US dollar/Euro", 1.1))
                        .build())
                .build();
    }

    private static Series monthlySeries(String key, String title, double base) {
        Series.Builder result = Series.builder().key(Key.parse(key)).meta("TITLE", title);
        LocalDate start = LocalDate.of(1999, 1, 1);
        IntStream.range(0, 12 * 26)
                .mapToObj(i -> Obs.builder()
                        .period(TimeInterval.of(start.plusMonths(i).atStartOfDay(), Duration.parse("P1M")))
                        .value(base + (i % 12) / 100.0)
                        .build())
                .forEach(result::obs);
        return result.build();
    }
}
