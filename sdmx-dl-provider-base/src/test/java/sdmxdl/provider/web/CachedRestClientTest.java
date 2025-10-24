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

import _test.sdmxdl.util.CachingAssert;
import _test.sdmxdl.util.XCountingRestClient;
import _test.sdmxdl.util.XRepoRestClient;
import nbbrd.io.function.IOConsumer;
import nbbrd.io.function.IOFunction;
import org.assertj.core.api.HamcrestCondition;
import org.junit.jupiter.api.Test;
import sdmxdl.*;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.TypedId;
import sdmxdl.web.WebSource;
import tests.sdmxdl.api.RepoSamples;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static _test.sdmxdl.util.CachingAssert.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static sdmxdl.Languages.ANY;
import static sdmxdl.provider.web.CachedRestClient.getBase;
import static tests.sdmxdl.api.KeyAssert.keys;
import static tests.sdmxdl.api.RepoSamples.*;

/**
 * @author Philippe Charles
 */
public class CachedRestClientTest {

    private final URI base = URI.create("cache:rest");
    private final Duration ttl = Duration.ofMillis(100);

    private final String flowsId = TypedId.resolveURI(base, "flows").toString();
    private final String flowId = TypedId.resolveURI(base, "flow", FLOW_REF.toString()).toString();
    private final String structId = TypedId.resolveURI(base, "struct", STRUCT_REF.toString()).toString();
    private final String seriesKeysOnlyId = TypedId.resolveURI(base, "seriesKeysOnly", FLOW_REF.toString()).toString();
    private final String noDataId = TypedId.resolveURI(base, "noData", FLOW_REF.toString()).toString();

    private CachedRestClient getClient(CachingAssert.Context ctx) {
        RestClient original = XRepoRestClient.of(RepoSamples.REPO);
        RestClient counting = XCountingRestClient.of(original, ctx.getCount());
        return new CachedRestClient(counting, ctx.newCache(), base, ttl);
    }

    @FunctionalInterface
    private interface Method<T> extends IOFunction<CachedRestClient, T> {
    }

    @Test
    public void testGetFlows() throws IOException {
        Method<List<Flow>> x = CachedRestClient::getFlows;

        checkCacheHit(this::getClient, x, new HamcrestCondition<>(hasItem(FLOW)), flowsId, ttl);
    }

    @Test
    public void testGetStructure() throws IOException {
        Method<Structure> x = client -> client.getStructure(STRUCT_REF);

        checkCacheHit(this::getClient, x, new HamcrestCondition<>(equalTo(STRUCT)), structId, ttl);
    }

    @Test
    public void testGetData() throws IOException {
        for (Key key : keys("all", "M.BE.INDUSTRY", ".BE.INDUSTRY", "A.BE.INDUSTRY")) {
            for (Detail filter : Detail.values()) {
                DataRef ref = DataRef.of(FLOW_REF, Query.builder().key(key).detail(filter).build());

                Method<Collection<Series>> x = client -> {
                    try (Stream<Series> cursor = client.getData(ref, STRUCT)) {
                        return cursor.collect(toList());
                    }
                };

                HamcrestCondition<Collection<Series>> validator = new HamcrestCondition<>(equalTo(DATA_SET.getDataStream(ref.getQuery()).collect(toList())));

                if (filter.isIgnoreData()) {
                    if (filter.isIgnoreMeta()) {
                        checkCacheHit(this::getClient, x, validator, seriesKeysOnlyId, ttl);
                    } else {
                        checkCacheHit(this::getClient, x, validator, noDataId, ttl);
                    }
                } else {
                    checkCacheMiss(this::getClient, x, validator, noDataId, ttl);
                    checkCacheMiss(this::getClient, x, validator, seriesKeysOnlyId, ttl);
                }
            }
        }
    }

    @Test
    public void testNarrowerRequest() throws IOException {
        Context ctx = new Context();
        CachedRestClient client = getClient(ctx);

        for (Detail filter : new Detail[]{Detail.SERIES_KEYS_ONLY, Detail.NO_DATA}) {
            IOConsumer<Key> method = key -> client.getData(DataRef.of(FLOW_REF, Query.builder().key(key).detail(filter).build()), STRUCT).close();

            ctx.reset();
            method.acceptWithIO(Key.ALL);
            method.acceptWithIO(Key.parse("M.BE.INDUSTRY"));
            assertThat(ctx.getCount()).hasValue(1);

            ctx.reset();
            method.acceptWithIO(Key.parse("M.BE.INDUSTRY"));
            method.acceptWithIO(Key.ALL);
            assertThat(ctx.getCount()).hasValue(2);

            ctx.reset();
            method.acceptWithIO(Key.parse("M.BE.INDUSTRY"));
            method.acceptWithIO(Key.parse("M.BE.INDUSTRY"));
            assertThat(ctx.getCount()).hasValue(1);

            ctx.reset();
            method.acceptWithIO(Key.parse("M..INDUSTRY"));
            method.acceptWithIO(Key.parse("M.BE.INDUSTRY"));
            assertThat(ctx.getCount()).hasValue(1);

            ctx.reset();
            method.acceptWithIO(Key.parse("M.BE.INDUSTRY"));
            method.acceptWithIO(Key.parse("M..INDUSTRY"));
            assertThat(ctx.getCount()).hasValue(2);

            ctx.reset();
            method.acceptWithIO(Key.parse("A.BE.INDUSTRY"));
            method.acceptWithIO(Key.parse("M.BE.INDUSTRY"));
            assertThat(ctx.getCount()).hasValue(2);

            ctx.reset();
            method.acceptWithIO(Key.parse("A..INDUSTRY"));
            method.acceptWithIO(Key.parse("M.BE.INDUSTRY"));
            assertThat(ctx.getCount()).hasValue(2);

            ctx.reset();
            method.acceptWithIO(Key.parse("A..INDUSTRY"));
            method.acceptWithIO(Key.parse("M..INDUSTRY"));
            assertThat(ctx.getCount()).hasValue(2);
        }
    }

    @Test
    public void testGetBase() {
        WebSource s1 = WebSource
                .builder()
                .id("id1")
                .driver("driver1")
                .endpointOf("http://localhost/rest")
                .build();

        assertThat(getBase(s1, ANY))
                .hasToString("cache:rest/" + TypedId.getUniqueID(s1) + "/*")
                .isEqualTo(getBase(s1, ANY))
                .isNotEqualTo(getBase(s1, Languages.parse("fr")))
                .isNotEqualTo(getBase(s1.toBuilder().id("id2").build(), ANY))
                .isNotEqualTo(getBase(s1.toBuilder().driver("driver2").build(), ANY))
                .isNotEqualTo(getBase(s1.toBuilder().endpointOf("http://localhost/stuff").build(), ANY))
                .isNotEqualTo(getBase(s1.toBuilder().endpointOf("http://nbb.be/rest").build(), ANY))
                .isNotEqualTo(getBase(s1.toBuilder().property("k", "v").build(), ANY))
        ;
    }
}
