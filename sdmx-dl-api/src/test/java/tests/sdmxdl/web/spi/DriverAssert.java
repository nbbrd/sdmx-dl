package tests.sdmxdl.web.spi;

import internal.sdmxdl.web.spi.DriverLoader;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import org.assertj.core.api.Condition;
import sdmxdl.*;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.api.ExtensionPoint;
import tests.sdmxdl.api.TckUtil;

import java.io.IOException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.Languages.ANY;
import static tests.sdmxdl.api.SdmxConditions.*;

@lombok.experimental.UtilityClass
public class DriverAssert {

    public WebContext noOpWebContext() {
        return WebContext.builder().build();
    }

    @MightBeGenerated
    private static final ExtensionPoint<Driver> EXTENSION_POINT = ExtensionPoint
            .<Driver>builder()
            .id(Driver::getDriverId)
            .idPattern(DriverLoader.ID_PATTERN)
            .rank(Driver::getDriverRank)
            .rankLowerBound(Driver.UNKNOWN_DRIVER_RANK)
            .properties(Driver::getDriverProperties)
            .propertiesPrefix(Driver.DRIVER_PROPERTY_PREFIX)
            .build();

    @SuppressWarnings("null")
    public void assertCompliance(@NonNull Driver driver) {
        TckUtil.run(s -> EXTENSION_POINT.assertCompliance(s, driver));

        WebSource validSource = WebSource
                .builder()
                .id("valid")
                .driver(driver.getDriverId())
                .endpointOf("http://localhost")
                .build();

        WebSource invalidSource = validSource.toBuilder().driver("").build();

        WebContext context = DriverAssert.noOpWebContext();

        assertThatNullPointerException().isThrownBy(() -> driver.connect(null, ANY, context));
        assertThatNullPointerException().isThrownBy(() -> driver.connect(validSource, null, context));
        assertThatNullPointerException().isThrownBy(() -> driver.connect(validSource, ANY, null));

        assertThatIllegalArgumentException().isThrownBy(() -> driver.connect(invalidSource, ANY, context));

        assertThat(driver.getDefaultSources()).allSatisfy(o -> checkSource(o, driver));
    }

    private void checkSource(WebSource o, Driver d) {
        assertThat(o.getId()).isNotBlank();
        assertThat(o.getProperties()).isNotNull();
        assertThat(o.getDriver()).isEqualTo(d.getDriverId());
        assertThat(o.getProperties().keySet()).isSubsetOf(d.getDriverProperties());
        assertThat(o.getConfidentiality()).isEqualTo(Confidentiality.PUBLIC);
    }

    public static void assertBuiltinSource(Driver driver, SourceQuery query, WebContext context) throws IOException {
        WebSource webSource = driver.getDefaultSources()
                .stream()
                .filter(item -> item.getId().equals(query.getSource()))
                .findFirst()
                .orElseThrow(RuntimeException::new);

        try (Connection connection = driver.connect(webSource, query.getLanguages(), context)) {
            DatabaseRef database = DatabaseRef.parse(query.getDatabase());
            assertThat(connection.getFlows(database))
                    .describedAs("Flows of %s/%s", webSource.getId(), database)
                    .are(validFlow(query.isNoDescription()))
                    .hasSizeGreaterThanOrEqualTo(query.getMinFlowCount());

            FlowRef flowRef = FlowRef.parse(query.getFlow());
            assertThat(connection.getFlow(database, flowRef))
                    .describedAs("Flow %s/%s/%s", webSource.getId(), database, flowRef)
                    .is(validFlow(query.isNoDescription()))
                    .is(new Condition<>(flowRef::containsRef, "valid flow ref"));

            StructureRef structureRef = connection.getFlow(database, flowRef).getStructureRef();
            assertThat(connection.getStructure(database, flowRef))
                    .is(validStructure())
                    .is(new Condition<>(structureRef::containsRef, "valid structure ref"))
                    .satisfies(structure -> {
                        assertThat(structure.getDimensions())
                                .describedAs("Dimensions of %s/%s/%s", webSource.getId(), database, flowRef)
                                .are(validDimension())
                                .hasSize(query.getDimCount());
                        assertThat(structure.getAttributes())
                                .describedAs("Attributes of %s/%s/%s", webSource.getId(), database, flowRef)
                                .are(validAttribute());
                    });

            Key key = Key.parse(query.getKey());
            assertThat(connection.getData(database, flowRef, Query.builder().key(key).build()))
                    .satisfies(dataSet -> {
                        assertThat(dataSet.getData())
                                .describedAs("Data of %s/%s/%s for key %s", webSource.getId(), database, flowRef, key)
                                .has(uniqueSeriesKeys())
                                .have(uniqueObs())
                                .hasSizeGreaterThanOrEqualTo(query.getMinSeriesCount());
                        assertThat(dataSet.getData().stream().mapToInt(series -> series.getObs().size()).sum())
                                .describedAs("Observations of %s/%s/%s for key %s", webSource.getId(), database, flowRef, key)
                                .isGreaterThanOrEqualTo(query.getMinObsCount());
                    });
        }

    }

    //    @MightBePromoted
    public static <W extends WebSource> EventListener<W> eventOf(Consumer<String> consumer) {
        return (W source, String marker, CharSequence message) -> consumer.accept(source.getId() + " " + marker + " " + message);
    }

    @lombok.Value
    @lombok.Builder
    public static class SourceQuery {
        String source;
        @lombok.Builder.Default
        Languages languages = ANY;
        @lombok.Builder.Default
        String database = "";
        int minFlowCount;
        String flow;
        @lombok.Builder.Default
        boolean noDescription = true;
        String key;
        int dimCount;
        int minSeriesCount;
        int minObsCount;
    }
}
