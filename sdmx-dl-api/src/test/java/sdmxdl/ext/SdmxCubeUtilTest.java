package sdmxdl.ext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sdmxdl.Connection;
import sdmxdl.Feature;
import sdmxdl.Key;
import sdmxdl.Series;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.Driver;
import tests.sdmxdl.web.spi.MockedDriver;
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.Key.ALL;
import static sdmxdl.Languages.ANY;
import static tests.sdmxdl.api.RepoSamples.*;

@SuppressWarnings("ConstantConditions")
public class SdmxCubeUtilTest {

    private static final Key M__ = Key.parse("M..");

    private static final Key M_BE_ = Key.parse("M.BE.");
    private static final Key M_BE_INDUSTRY = Key.parse("M.BE.INDUSTRY");
    private static final Key M_BE_XXX = Key.parse("M.BE.XXX");

    private static final Key M_FR_ = Key.parse("M.FR.");
    private static final Key M_FR_INDUSTRY = Key.parse("M.FR.INDUSTRY");
    private static final Key M_FR_XXX = Key.parse("M.FR.XXX");

    private static final Series FAKE_S4 = Series
            .builder()
            .key(M_FR_XXX)
            .build();

    private List<Driver> getDrivers() {
        return Stream.of(EnumSet.noneOf(Feature.class), EnumSet.allOf(Feature.class))
                .map(features -> MockedDriver.builder().repo(REPO, features).build())
                .collect(Collectors.toList());
    }

    @Test
    public void testGetAllSeries() throws IOException {
        for (Driver driver : getDrivers()) {
            SdmxWebSource source = driver.getDefaultSources().iterator().next();

            try (Connection c = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getAllSeries(null, FLOW_REF, ALL));
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getAllSeries(c, null, ALL));
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getAllSeries(c, FLOW_REF, null));

                assertThatIllegalArgumentException().isThrownBy(() -> SdmxCubeUtil.getAllSeries(c, FLOW_REF, M_BE_INDUSTRY)).withMessageContaining("node");
                assertThatIllegalArgumentException().isThrownBy(() -> SdmxCubeUtil.getAllSeries(c, FLOW_REF, M_FR_XXX)).withMessageContaining("node");

                if (c.getSupportedFeatures().contains(Feature.DATA_QUERY_DETAIL)) {
                    assertThat(listOf(() -> SdmxCubeUtil.getAllSeries(c, FLOW_REF, ALL))).containsExactly(noData(S1), noData(S2), noData(S3));
                    assertThat(listOf(() -> SdmxCubeUtil.getAllSeries(c, FLOW_REF, M__))).containsExactly(noData(S1), noData(S2), noData(S3));
                    assertThat(listOf(() -> SdmxCubeUtil.getAllSeries(c, FLOW_REF, M_BE_))).containsExactly(noData(S1), noData(S2));
                } else {
                    assertThat(listOf(() -> SdmxCubeUtil.getAllSeries(c, FLOW_REF, ALL))).containsExactly(noMetaNoData(S1), noMetaNoData(S2), noMetaNoData(S3), FAKE_S4);
                    assertThat(listOf(() -> SdmxCubeUtil.getAllSeries(c, FLOW_REF, M__))).containsExactly(noMetaNoData(S1), noMetaNoData(S2), noMetaNoData(S3), FAKE_S4);
                    assertThat(listOf(() -> SdmxCubeUtil.getAllSeries(c, FLOW_REF, M_BE_))).containsExactly(noMetaNoData(S1), noMetaNoData(S2));
                }
            }
        }
    }

    @Test
    public void testGetAllSeriesWithData() throws IOException {
        for (Driver driver : getDrivers()) {
            SdmxWebSource source = driver.getDefaultSources().iterator().next();

            try (Connection c = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getAllSeriesWithData(null, FLOW_REF, ALL));
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getAllSeriesWithData(c, null, ALL));
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getAllSeriesWithData(c, FLOW_REF, null));

                assertThatIllegalArgumentException().isThrownBy(() -> SdmxCubeUtil.getAllSeries(c, FLOW_REF, M_BE_INDUSTRY)).withMessageContaining("node");
                assertThatIllegalArgumentException().isThrownBy(() -> SdmxCubeUtil.getAllSeries(c, FLOW_REF, M_FR_XXX)).withMessageContaining("node");

                if (c.getSupportedFeatures().contains(Feature.DATA_QUERY_DETAIL)) {
                    assertThat(listOf(() -> SdmxCubeUtil.getAllSeriesWithData(c, FLOW_REF, ALL))).containsExactly(S1, S2, S3);
                    assertThat(listOf(() -> SdmxCubeUtil.getAllSeriesWithData(c, FLOW_REF, M__))).containsExactly(S1, S2, S3);
                    assertThat(listOf(() -> SdmxCubeUtil.getAllSeriesWithData(c, FLOW_REF, M_BE_))).containsExactly(S1, S2);
                } else {
                    assertThat(listOf(() -> SdmxCubeUtil.getAllSeriesWithData(c, FLOW_REF, ALL))).containsExactly(S1, S2, S3, FAKE_S4);
                    assertThat(listOf(() -> SdmxCubeUtil.getAllSeriesWithData(c, FLOW_REF, M__))).containsExactly(S1, S2, S3, FAKE_S4);
                    assertThat(listOf(() -> SdmxCubeUtil.getAllSeriesWithData(c, FLOW_REF, M_BE_))).containsExactly(S1, S2);
                }
            }
        }
    }

    @Test
    public void testGetSeries() throws IOException {
        for (Driver driver : getDrivers()) {
            SdmxWebSource source = driver.getDefaultSources().iterator().next();

            try (Connection c = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getSeries(null, FLOW_REF, K1));
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getSeries(c, null, K1));
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getSeries(c, FLOW_REF, null));

                assertThatIllegalArgumentException().isThrownBy(() -> SdmxCubeUtil.getSeries(c, FLOW_REF, ALL)).withMessageContaining("leaf");
                assertThatIllegalArgumentException().isThrownBy(() -> SdmxCubeUtil.getSeries(c, FLOW_REF, M__)).withMessageContaining("leaf");
                assertThatIllegalArgumentException().isThrownBy(() -> SdmxCubeUtil.getSeries(c, FLOW_REF, M_BE_)).withMessageContaining("leaf");

                Assertions.assertThat(SdmxCubeUtil.getSeries(c, FLOW_REF, M_BE_INDUSTRY)).contains(noData(S1));
                Assertions.assertThat(SdmxCubeUtil.getSeries(c, FLOW_REF, M_FR_XXX)).isEmpty();
            }
        }
    }

    @Test
    public void testGetSeriesWithData() throws IOException {
        for (Driver driver : getDrivers()) {
            SdmxWebSource source = driver.getDefaultSources().iterator().next();

            try (Connection c = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getSeriesWithData(null, FLOW_REF, K1));
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getSeriesWithData(c, null, K1));
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getSeriesWithData(c, FLOW_REF, null));

                assertThatIllegalArgumentException().isThrownBy(() -> SdmxCubeUtil.getSeriesWithData(c, FLOW_REF, ALL)).withMessageContaining("leaf");
                assertThatIllegalArgumentException().isThrownBy(() -> SdmxCubeUtil.getSeriesWithData(c, FLOW_REF, M__)).withMessageContaining("leaf");
                assertThatIllegalArgumentException().isThrownBy(() -> SdmxCubeUtil.getSeriesWithData(c, FLOW_REF, M_BE_)).withMessageContaining("leaf");

                Assertions.assertThat(SdmxCubeUtil.getSeriesWithData(c, FLOW_REF, M_BE_INDUSTRY)).contains(S1);
                Assertions.assertThat(SdmxCubeUtil.getSeriesWithData(c, FLOW_REF, M_FR_XXX)).isEmpty();
            }
        }
    }

    @Test
    public void testGetChildren() throws IOException {
        for (Driver driver : getDrivers()) {
            SdmxWebSource source = driver.getDefaultSources().iterator().next();

            try (Connection c = driver.connect(source, ANY, DriverAssert.noOpWebContext())) {
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getChildren(null, FLOW_REF, ALL, 0));
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getChildren(c, null, ALL, 0));
                assertThatNullPointerException().isThrownBy(() -> SdmxCubeUtil.getChildren(c, FLOW_REF, null, 0));

                assertThatIllegalArgumentException().isThrownBy(() -> SdmxCubeUtil.getChildren(c, FLOW_REF, ALL, -1)).withMessageContaining("dimensionIndex");
                assertThatIllegalArgumentException().isThrownBy(() -> SdmxCubeUtil.getChildren(c, FLOW_REF, M_BE_INDUSTRY, 0)).withMessageContaining("node");
                assertThatIllegalArgumentException().isThrownBy(() -> SdmxCubeUtil.getChildren(c, FLOW_REF, M_BE_, 0)).withMessageContaining("dimensionIndex");

                Assertions.assertThat(SdmxCubeUtil.getChildren(c, FLOW_REF, ALL, 0)).containsExactly("M");
                Assertions.assertThat(SdmxCubeUtil.getChildren(c, FLOW_REF, ALL, 1)).containsExactly("BE", "FR");
                Assertions.assertThat(SdmxCubeUtil.getChildren(c, FLOW_REF, ALL, 2)).containsExactly("INDUSTRY", "XXX");

                Assertions.assertThat(SdmxCubeUtil.getChildren(c, FLOW_REF, M__, 1)).containsExactly("BE", "FR");
                Assertions.assertThat(SdmxCubeUtil.getChildren(c, FLOW_REF, M__, 2)).containsExactly("INDUSTRY", "XXX");

                if (c.getSupportedFeatures().contains(Feature.DATA_QUERY_DETAIL)) {
                    Assertions.assertThat(SdmxCubeUtil.getChildren(c, FLOW_REF, M_BE_, 2)).containsExactly("INDUSTRY", "XXX");
                    Assertions.assertThat(SdmxCubeUtil.getChildren(c, FLOW_REF, M_FR_, 2)).containsExactly("INDUSTRY");
                } else {
                    Assertions.assertThat(SdmxCubeUtil.getChildren(c, FLOW_REF, M_BE_, 2)).containsExactly("INDUSTRY", "XXX");
                    Assertions.assertThat(SdmxCubeUtil.getChildren(c, FLOW_REF, M_FR_, 2)).containsExactly("INDUSTRY", "XXX");
                }
            }
        }
    }

    @Test
    public void testGetDimensionById() {
        Assertions.assertThat(SdmxCubeUtil.getDimensionById(STRUCT, "")).isEmpty();
        Assertions.assertThat(SdmxCubeUtil.getDimensionById(STRUCT, "FREQ")).hasValue(DIM1);
        Assertions.assertThat(SdmxCubeUtil.getDimensionById(STRUCT, "REGION")).hasValue(DIM2);
        Assertions.assertThat(SdmxCubeUtil.getDimensionById(STRUCT, "SECTOR")).hasValue(DIM3);
    }

    @Test
    public void testGetDimensionIndexById() {
        Assertions.assertThat(SdmxCubeUtil.getDimensionIndexById(STRUCT, "")).isEmpty();
        Assertions.assertThat(SdmxCubeUtil.getDimensionIndexById(STRUCT, "FREQ")).hasValue(0);
        Assertions.assertThat(SdmxCubeUtil.getDimensionIndexById(STRUCT, "REGION")).hasValue(1);
        Assertions.assertThat(SdmxCubeUtil.getDimensionIndexById(STRUCT, "SECTOR")).hasValue(2);
    }

    private static List<Series> listOf(SeriesStreamSupplier supplier) throws IOException {
        try (Stream<Series> stream = supplier.getWithIO()) {
            return stream.collect(Collectors.toList());
        }
    }

    private static Series noData(Series series) {
        return series.toBuilder().clearObs().build();
    }

    private static Series noMetaNoData(Series series) {
        return series.toBuilder().clearMeta().clearObs().build();
    }

    @FunctionalInterface
    private interface SeriesStreamSupplier {
        Stream<Series> getWithIO() throws IOException;
    }
}
