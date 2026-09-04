/*
 * Copyright 2017 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package sdmxdl.provider.web;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.Detail.DATA_ONLY;
import static sdmxdl.Detail.FULL;
import static sdmxdl.Feature.*;
import static sdmxdl.provider.web.RestConnection.deriveQuery;
import static tests.sdmxdl.api.RepoSamples.STRUCT;

import _test.sdmxdl.util.XRepoRestClient;
import java.time.LocalDateTime;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import sdmxdl.*;
import tests.sdmxdl.api.ConnectionAssert;
import tests.sdmxdl.api.RepoSamples;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("ConstantConditions")
public class RestConnectionTest {

    @Test
    public void testCompliance() {
        DataRepository repo = RepoSamples.REPO;
        ConnectionAssert.assertCompliance(
                () -> RestConnection.of(XRepoRestClient.of(repo)),
                ConnectionAssert.Sample.builder()
                        .validFlow(RepoSamples.FLOW_REF)
                        .invalidFlow(RepoSamples.BAD_FLOW_REF)
                        .validKey(RepoSamples.K1)
                        .invalidKey(RepoSamples.INVALID_KEY)
                        .build());
    }

    @Test
    public void deriveQueryPreservesAllParametersWhenAllFeaturesSupported() {
        Query query = Query.builder()
                .key(Key.of("M", "BE", ""))
                .detail(DATA_ONLY)
                .startPeriod(LocalDateTime.of(2010, 1, 1, 0, 0))
                .endPeriod(LocalDateTime.of(2020, 12, 31, 23, 59))
                .firstNObservations(100)
                .lastNObservations(50)
                .build();

        Query result = deriveQuery(query, EnumSet.allOf(Feature.class), STRUCT);

        assertThat(result)
                .returns(Key.of("M", "BE", ""), Query::getKey)
                .returns(DATA_ONLY, Query::getDetail)
                .returns(query.getStartPeriod(), Query::getStartPeriod)
                .returns(query.getEndPeriod(), Query::getEndPeriod)
                .returns(100, Query::getFirstNObservations)
                .returns(50, Query::getLastNObservations);
    }

    @Test
    public void deriveQueryDefaultsAllParametersWhenNoFeaturesSupported() {
        Query query = Query.builder()
                .key(Key.ALL)
                .detail(DATA_ONLY)
                .startPeriod(LocalDateTime.of(2010, 1, 1, 0, 0))
                .endPeriod(LocalDateTime.of(2020, 12, 31, 23, 59))
                .firstNObservations(100)
                .lastNObservations(50)
                .build();

        Query result = deriveQuery(query, emptySet(), STRUCT);

        assertThat(result)
                .returns(Key.of("", "", "INDUSTRY+XXX"), Query::getKey)
                .returns(FULL, Query::getDetail)
                .returns(null, Query::getStartPeriod)
                .returns(null, Query::getEndPeriod)
                .returns(null, Query::getFirstNObservations)
                .returns(null, Query::getLastNObservations);
    }

    @Test
    public void deriveQueryHandlesKeyConversion() {
        assertThat(deriveQuery(Query.builder().key(Key.ALL).build(), emptySet(), STRUCT)
                        .getKey())
                .isNotEqualTo(Key.ALL)
                .isEqualTo(Key.of("", "", "INDUSTRY+XXX"));

        assertThat(deriveQuery(Query.builder().key(Key.ALL).build(), EnumSet.of(DATA_QUERY_ALL_KEYWORD), STRUCT)
                        .getKey())
                .isEqualTo(Key.ALL);

        assertThat(deriveQuery(Query.builder().key(Key.of("M", "BE", "")).build(), emptySet(), STRUCT)
                        .getKey())
                .isEqualTo(Key.of("M", "BE", ""));
    }

    @Test
    public void deriveQueryHandlesDetailLevel() {
        assertThat(deriveQuery(
                                Query.builder().key(Key.ALL).detail(DATA_ONLY).build(),
                                EnumSet.of(DATA_QUERY_DETAIL),
                                STRUCT)
                        .getDetail())
                .isEqualTo(DATA_ONLY);

        assertThat(deriveQuery(Query.builder().key(Key.ALL).detail(DATA_ONLY).build(), emptySet(), STRUCT)
                        .getDetail())
                .isEqualTo(FULL);

        for (Detail detail : Detail.values()) {
            Query queryWithDetail = Query.builder().key(Key.ALL).detail(detail).build();
            assertThat(deriveQuery(queryWithDetail, EnumSet.of(DATA_QUERY_DETAIL), STRUCT)
                            .getDetail())
                    .isEqualTo(detail);
            assertThat(deriveQuery(queryWithDetail, emptySet(), STRUCT).getDetail())
                    .isEqualTo(FULL);
        }
    }

    @Test
    public void deriveQueryHandlesTimeRangeFilters() {
        LocalDateTime startPeriod = LocalDateTime.of(2010, 1, 1, 0, 0);
        LocalDateTime endPeriod = LocalDateTime.of(2020, 12, 31, 23, 59);

        assertThat(deriveQuery(
                                Query.builder()
                                        .key(Key.ALL)
                                        .startPeriod(startPeriod)
                                        .build(),
                                EnumSet.of(DATA_QUERY_TIME_RANGE),
                                STRUCT)
                        .getStartPeriod())
                .isEqualTo(startPeriod);

        assertThat(deriveQuery(
                                Query.builder()
                                        .key(Key.ALL)
                                        .startPeriod(startPeriod)
                                        .build(),
                                emptySet(),
                                STRUCT)
                        .getStartPeriod())
                .isNull();

        assertThat(deriveQuery(
                                Query.builder()
                                        .key(Key.ALL)
                                        .endPeriod(endPeriod)
                                        .build(),
                                EnumSet.of(DATA_QUERY_TIME_RANGE),
                                STRUCT)
                        .getEndPeriod())
                .isEqualTo(endPeriod);

        assertThat(deriveQuery(Query.builder().key(Key.ALL).endPeriod(endPeriod).build(), emptySet(), STRUCT)
                        .getEndPeriod())
                .isNull();

        assertThat(deriveQuery(
                        Query.builder()
                                .key(Key.ALL)
                                .startPeriod(null)
                                .endPeriod(null)
                                .build(),
                        EnumSet.of(DATA_QUERY_TIME_RANGE),
                        STRUCT))
                .returns(null, Query::getStartPeriod)
                .returns(null, Query::getEndPeriod);
    }

    @Test
    public void deriveQueryHandlesObservationCountFilters() {
        assertThat(deriveQuery(
                                Query.builder()
                                        .key(Key.ALL)
                                        .firstNObservations(100)
                                        .build(),
                                EnumSet.of(DATA_QUERY_OBS_COUNT),
                                STRUCT)
                        .getFirstNObservations())
                .isEqualTo(100);

        assertThat(deriveQuery(
                                Query.builder()
                                        .key(Key.ALL)
                                        .firstNObservations(100)
                                        .build(),
                                emptySet(),
                                STRUCT)
                        .getFirstNObservations())
                .isNull();

        assertThat(deriveQuery(
                                Query.builder()
                                        .key(Key.ALL)
                                        .lastNObservations(50)
                                        .build(),
                                EnumSet.of(DATA_QUERY_OBS_COUNT),
                                STRUCT)
                        .getLastNObservations())
                .isEqualTo(50);

        assertThat(deriveQuery(
                                Query.builder()
                                        .key(Key.ALL)
                                        .lastNObservations(50)
                                        .build(),
                                emptySet(),
                                STRUCT)
                        .getLastNObservations())
                .isNull();

        assertThat(deriveQuery(
                        Query.builder()
                                .key(Key.ALL)
                                .firstNObservations(null)
                                .lastNObservations(null)
                                .build(),
                        EnumSet.of(DATA_QUERY_OBS_COUNT),
                        STRUCT))
                .returns(null, Query::getFirstNObservations)
                .returns(null, Query::getLastNObservations);
    }

    @Test
    public void deriveQueryHandlesSelectiveFeatureSupport() {
        LocalDateTime startPeriod = LocalDateTime.of(2010, 1, 1, 0, 0);
        Query query = Query.builder()
                .key(Key.ALL)
                .detail(DATA_ONLY)
                .startPeriod(startPeriod)
                .endPeriod(LocalDateTime.of(2020, 12, 31, 23, 59))
                .firstNObservations(100)
                .build();

        Query result = deriveQuery(query, EnumSet.of(DATA_QUERY_DETAIL, DATA_QUERY_TIME_RANGE), STRUCT);

        assertThat(result)
                .returns(DATA_ONLY, Query::getDetail)
                .returns(startPeriod, Query::getStartPeriod)
                .returns(query.getEndPeriod(), Query::getEndPeriod)
                .returns(null, Query::getFirstNObservations);
    }

    @Test
    public void deriveQueryReturnsNewInstanceAndHandlesStructure() {
        Query query = Query.builder().key(Key.ALL).detail(DATA_ONLY).build();
        Query result = deriveQuery(query, emptySet(), STRUCT);

        assertThat(result).isNotSameAs(query);

        assertThat(result.getKey()).returns("..INDUSTRY+XXX", Key::toString);
    }

    @Test
    public void deriveQueryExpandsLastDimensionWhenTrailingWildcards() {
        Query result = deriveQuery(Query.builder().key(Key.of("M", "", "")).build(), emptySet(), STRUCT);

        assertThat(result.getKey()).isEqualTo(Key.of("M", "", "INDUSTRY+XXX"));
    }

    @Test
    public void deriveQueryKeepsLastDimensionWildcardWhenNotTrailingWildcardPair() {
        Query result = deriveQuery(Query.builder().key(Key.of("M", "BE", "")).build(), emptySet(), STRUCT);

        assertThat(result.getKey()).isEqualTo(Key.of("M", "BE", ""));
    }

    @Test
    public void deriveQueryKeepsWildcardPairForTwoDimensionsOnly() {
        Query result = deriveQuery(Query.builder().key(Key.of("", "")).build(), emptySet(), STRUCT);

        assertThat(result.getKey()).isEqualTo(Key.of("", ""));
    }

    @Test
    public void deriveQueryExpandsShortestTrailingWildcardDimension() {
        Dimension lastDimensionWithMoreCodes = RepoSamples.DIM3.toBuilder()
                .codelist(RepoSamples.CL3.toBuilder()
                        .code("YYY", "Yet another sector")
                        .build())
                .build();

        Structure structureWithShorterPreviousExpansion = Structure.builder()
                .ref(STRUCT.getRef())
                .dimension(RepoSamples.DIM1)
                .dimension(RepoSamples.DIM2)
                .dimension(lastDimensionWithMoreCodes)
                .attribute(RepoSamples.NOT_CODED_ATTRIBUTE)
                .attribute(RepoSamples.CODED_ATTRIBUTE)
                .timeDimensionId(STRUCT.getTimeDimensionId())
                .primaryMeasureId(STRUCT.getPrimaryMeasureId())
                .name(STRUCT.getName())
                .build();

        Query result = deriveQuery(
                Query.builder().key(Key.of("M", "", "")).build(), emptySet(), structureWithShorterPreviousExpansion);

        assertThat(result.getKey()).isEqualTo(Key.of("M", "BE+FR", ""));
    }

    @Test
    public void deriveQueryExpandsCodedTrailingWildcardWhenOtherTrailingDimensionHasNoCodes() {
        Dimension uncodedLastDimension =
                Dimension.builder().id("UNCLAST").name("Uncoded last").build();

        Structure structureWithUncodedLastDimension = Structure.builder()
                .ref(STRUCT.getRef())
                .dimension(RepoSamples.DIM1)
                .dimension(RepoSamples.DIM2)
                .dimension(uncodedLastDimension)
                .attribute(RepoSamples.NOT_CODED_ATTRIBUTE)
                .attribute(RepoSamples.CODED_ATTRIBUTE)
                .timeDimensionId(STRUCT.getTimeDimensionId())
                .primaryMeasureId(STRUCT.getPrimaryMeasureId())
                .name(STRUCT.getName())
                .build();

        Query result = deriveQuery(
                Query.builder().key(Key.of("M", "", "")).build(), emptySet(), structureWithUncodedLastDimension);

        assertThat(result.getKey()).isEqualTo(Key.of("M", "BE+FR", ""));
    }

    @Test
    public void deriveQueryKeepsTrailingWildcardsWhenBothTrailingDimensionsHaveNoCodes() {
        Dimension uncodedPreviousDimension =
                Dimension.builder().id("UNCPREV").name("Uncoded previous").build();
        Dimension uncodedLastDimension =
                Dimension.builder().id("UNCLAST").name("Uncoded last").build();

        Structure structureWithUncodedTrailingDimensions = Structure.builder()
                .ref(STRUCT.getRef())
                .dimension(RepoSamples.DIM1)
                .dimension(uncodedPreviousDimension)
                .dimension(uncodedLastDimension)
                .attribute(RepoSamples.NOT_CODED_ATTRIBUTE)
                .attribute(RepoSamples.CODED_ATTRIBUTE)
                .timeDimensionId(STRUCT.getTimeDimensionId())
                .primaryMeasureId(STRUCT.getPrimaryMeasureId())
                .name(STRUCT.getName())
                .build();

        Query result = deriveQuery(
                Query.builder().key(Key.of("M", "", "")).build(), emptySet(), structureWithUncodedTrailingDimensions);

        assertThat(result.getKey()).isEqualTo(Key.of("M", "", ""));
    }
}
