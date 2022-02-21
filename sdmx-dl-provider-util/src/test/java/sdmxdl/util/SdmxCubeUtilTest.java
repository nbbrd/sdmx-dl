package sdmxdl.util;

import nbbrd.io.function.IOSupplier;
import org.junit.jupiter.api.Test;
import sdmxdl.*;
import sdmxdl.repo.SdmxRepository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.*;
import static sdmxdl.Key.ALL;
import static sdmxdl.samples.RepoSamples.*;
import static sdmxdl.util.SdmxCubeUtil.*;

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
            .freq(Frequency.UNDEFINED)
            .build();

    private static final SdmxRepository WITH_DETAIL = REPO;
    private static final SdmxRepository WITHOUT_DETAIL = REPO.toBuilder().clearSupportedFeatures().build();

    @Test
    public void testGetAllSeries() throws IOException {
        for (SdmxRepository repo : asList(WITH_DETAIL, WITHOUT_DETAIL)) {
            try (SdmxConnection c = repo.asConnection()) {
                assertThatNullPointerException().isThrownBy(() -> getAllSeries(null, FLOW_REF, ALL));
                assertThatNullPointerException().isThrownBy(() -> getAllSeries(c, null, ALL));
                assertThatNullPointerException().isThrownBy(() -> getAllSeries(c, FLOW_REF, null));

                assertThatIllegalArgumentException().isThrownBy(() -> getAllSeries(c, FLOW_REF, M_BE_INDUSTRY)).withMessageContaining("node");
                assertThatIllegalArgumentException().isThrownBy(() -> getAllSeries(c, FLOW_REF, M_FR_XXX)).withMessageContaining("node");

                if (repo.getSupportedFeatures().contains(Feature.DATA_QUERY_DETAIL)) {
                    assertThat(listOf(() -> getAllSeries(c, FLOW_REF, ALL))).containsExactly(noData(S1), noData(S2), noData(S3));
                    assertThat(listOf(() -> getAllSeries(c, FLOW_REF, M__))).containsExactly(noData(S1), noData(S2), noData(S3));
                    assertThat(listOf(() -> getAllSeries(c, FLOW_REF, M_BE_))).containsExactly(noData(S1), noData(S2));
                } else {
                    assertThat(listOf(() -> getAllSeries(c, FLOW_REF, ALL))).containsExactly(noMetaNoData(S1), noMetaNoData(S2), noMetaNoData(S3), FAKE_S4);
                    assertThat(listOf(() -> getAllSeries(c, FLOW_REF, M__))).containsExactly(noMetaNoData(S1), noMetaNoData(S2), noMetaNoData(S3), FAKE_S4);
                    assertThat(listOf(() -> getAllSeries(c, FLOW_REF, M_BE_))).containsExactly(noMetaNoData(S1), noMetaNoData(S2));
                }
            }
        }
    }

    @Test
    public void testGetAllSeriesWithData() throws IOException {
        for (SdmxRepository repo : asList(WITH_DETAIL, WITHOUT_DETAIL)) {
            try (SdmxConnection c = repo.asConnection()) {
                assertThatNullPointerException().isThrownBy(() -> getAllSeriesWithData(null, FLOW_REF, ALL));
                assertThatNullPointerException().isThrownBy(() -> getAllSeriesWithData(c, null, ALL));
                assertThatNullPointerException().isThrownBy(() -> getAllSeriesWithData(c, FLOW_REF, null));

                assertThatIllegalArgumentException().isThrownBy(() -> getAllSeries(c, FLOW_REF, M_BE_INDUSTRY)).withMessageContaining("node");
                assertThatIllegalArgumentException().isThrownBy(() -> getAllSeries(c, FLOW_REF, M_FR_XXX)).withMessageContaining("node");

                if (repo.getSupportedFeatures().contains(Feature.DATA_QUERY_DETAIL)) {
                    assertThat(listOf(() -> getAllSeriesWithData(c, FLOW_REF, ALL))).containsExactly(S1, S2, S3);
                    assertThat(listOf(() -> getAllSeriesWithData(c, FLOW_REF, M__))).containsExactly(S1, S2, S3);
                    assertThat(listOf(() -> getAllSeriesWithData(c, FLOW_REF, M_BE_))).containsExactly(S1, S2);
                } else {
                    assertThat(listOf(() -> getAllSeriesWithData(c, FLOW_REF, ALL))).containsExactly(S1, S2, S3, FAKE_S4);
                    assertThat(listOf(() -> getAllSeriesWithData(c, FLOW_REF, M__))).containsExactly(S1, S2, S3, FAKE_S4);
                    assertThat(listOf(() -> getAllSeriesWithData(c, FLOW_REF, M_BE_))).containsExactly(S1, S2);
                }
            }
        }
    }

    @Test
    public void testGetSeries() throws IOException {
        for (SdmxRepository repo : asList(WITH_DETAIL, WITHOUT_DETAIL)) {
            try (SdmxConnection conn = repo.asConnection()) {
                assertThatNullPointerException().isThrownBy(() -> getSeries(null, FLOW_REF, K1));
                assertThatNullPointerException().isThrownBy(() -> getSeries(conn, null, K1));
                assertThatNullPointerException().isThrownBy(() -> getSeries(conn, FLOW_REF, null));

                assertThatIllegalArgumentException().isThrownBy(() -> getSeries(conn, FLOW_REF, ALL)).withMessageContaining("leaf");
                assertThatIllegalArgumentException().isThrownBy(() -> getSeries(conn, FLOW_REF, M__)).withMessageContaining("leaf");
                assertThatIllegalArgumentException().isThrownBy(() -> getSeries(conn, FLOW_REF, M_BE_)).withMessageContaining("leaf");

                assertThat(getSeries(conn, FLOW_REF, M_BE_INDUSTRY)).contains(noData(S1));
                assertThat(getSeries(conn, FLOW_REF, M_FR_XXX)).isEmpty();
            }
        }
    }

    @Test
    public void testGetSeriesWithData() throws IOException {
        for (SdmxRepository repo : asList(WITH_DETAIL, WITHOUT_DETAIL)) {
            try (SdmxConnection conn = repo.asConnection()) {
                assertThatNullPointerException().isThrownBy(() -> getSeriesWithData(null, FLOW_REF, K1));
                assertThatNullPointerException().isThrownBy(() -> getSeriesWithData(conn, null, K1));
                assertThatNullPointerException().isThrownBy(() -> getSeriesWithData(conn, FLOW_REF, null));

                assertThatIllegalArgumentException().isThrownBy(() -> getSeriesWithData(conn, FLOW_REF, ALL)).withMessageContaining("leaf");
                assertThatIllegalArgumentException().isThrownBy(() -> getSeriesWithData(conn, FLOW_REF, M__)).withMessageContaining("leaf");
                assertThatIllegalArgumentException().isThrownBy(() -> getSeriesWithData(conn, FLOW_REF, M_BE_)).withMessageContaining("leaf");

                assertThat(getSeriesWithData(conn, FLOW_REF, M_BE_INDUSTRY)).contains(S1);
                assertThat(getSeriesWithData(conn, FLOW_REF, M_FR_XXX)).isEmpty();
            }
        }
    }

    @Test
    public void testGetChildren() throws IOException {
        for (SdmxRepository repo : asList(WITH_DETAIL, WITHOUT_DETAIL)) {
            try (SdmxConnection conn = repo.asConnection()) {
                assertThatNullPointerException().isThrownBy(() -> getChildren(null, FLOW_REF, ALL, 0));
                assertThatNullPointerException().isThrownBy(() -> getChildren(conn, null, ALL, 0));
                assertThatNullPointerException().isThrownBy(() -> getChildren(conn, FLOW_REF, null, 0));

                assertThatIllegalArgumentException().isThrownBy(() -> getChildren(conn, FLOW_REF, ALL, -1)).withMessageContaining("dimensionIndex");
                assertThatIllegalArgumentException().isThrownBy(() -> getChildren(conn, FLOW_REF, M_BE_INDUSTRY, 0)).withMessageContaining("node");
                assertThatIllegalArgumentException().isThrownBy(() -> getChildren(conn, FLOW_REF, M_BE_, 0)).withMessageContaining("dimensionIndex");

                assertThat(getChildren(conn, FLOW_REF, ALL, 0)).containsExactly("M");
                assertThat(getChildren(conn, FLOW_REF, ALL, 1)).containsExactly("BE", "FR");
                assertThat(getChildren(conn, FLOW_REF, ALL, 2)).containsExactly("INDUSTRY", "XXX");

                assertThat(getChildren(conn, FLOW_REF, M__, 1)).containsExactly("BE", "FR");
                assertThat(getChildren(conn, FLOW_REF, M__, 2)).containsExactly("INDUSTRY", "XXX");

                if (repo.getSupportedFeatures().contains(Feature.DATA_QUERY_DETAIL)) {
                    assertThat(getChildren(conn, FLOW_REF, M_BE_, 2)).containsExactly("INDUSTRY", "XXX");
                    assertThat(getChildren(conn, FLOW_REF, M_FR_, 2)).containsExactly("INDUSTRY");
                } else {
                    assertThat(getChildren(conn, FLOW_REF, M_BE_, 2)).containsExactly("INDUSTRY", "XXX");
                    assertThat(getChildren(conn, FLOW_REF, M_FR_, 2)).containsExactly("INDUSTRY", "XXX");
                }
            }
        }
    }

    @Test
    public void testGetDimensionById() {
        assertThat(getDimensionById(STRUCT, "")).isEmpty();
        assertThat(getDimensionById(STRUCT, "FREQ")).hasValue(DIM1);
        assertThat(getDimensionById(STRUCT, "REGION")).hasValue(DIM2);
        assertThat(getDimensionById(STRUCT, "SECTOR")).hasValue(DIM3);
    }

    @Test
    public void testGetDimensionIndexById() {
        assertThat(getDimensionIndexById(STRUCT, "")).isEmpty();
        assertThat(getDimensionIndexById(STRUCT, "FREQ")).hasValue(0);
        assertThat(getDimensionIndexById(STRUCT, "REGION")).hasValue(1);
        assertThat(getDimensionIndexById(STRUCT, "SECTOR")).hasValue(2);
    }

    private static List<Series> listOf(IOSupplier<Stream<Series>> supplier) throws IOException {
        try (Stream<Series> stream = supplier.getWithIO()) {
            return stream.collect(Collectors.toList());
        }
    }

    private static Series noData(Series series) {
        return series.toBuilder().freq(Frequency.UNDEFINED).clearObs().build();
    }

    private static Series noMetaNoData(Series series) {
        return series.toBuilder().freq(Frequency.UNDEFINED).clearMeta().clearObs().build();
    }
}
