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
package sdmxdl.util.web;

import _test.sdmxdl.util.CachingAssert;
import _test.sdmxdl.util.XCountingRestClient;
import _test.sdmxdl.util.XRepoRestClient;
import nbbrd.io.function.IOConsumer;
import nbbrd.io.function.IOFunction;
import org.assertj.core.api.HamcrestCondition;
import org.junit.jupiter.api.Test;
import sdmxdl.*;
import sdmxdl.samples.RepoSamples;
import sdmxdl.util.TypedId;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static _test.sdmxdl.util.CachingAssert.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static sdmxdl.samples.RepoSamples.*;
import static sdmxdl.tck.DataFilterAssert.filters;
import static sdmxdl.tck.KeyAssert.keys;

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
        SdmxRestClient original = XRepoRestClient.of(RepoSamples.REPO);
        SdmxRestClient counting = XCountingRestClient.of(original, ctx.getCount());
        return new CachedRestClient(counting, ctx.newCache(), base, ttl);
    }

    @FunctionalInterface
    private interface Method<T> extends IOFunction<CachedRestClient, T> {
    }

    @Test
    public void testGetFlows() throws IOException {
        Method<List<Dataflow>> x = CachedRestClient::getFlows;

        checkCacheHit(this::getClient, x, new HamcrestCondition<>(hasItem(FLOW)), flowsId, ttl);
    }

    @Test
    public void testGetFlow() throws IOException {
        Method<Dataflow> x = client -> client.getFlow(FLOW_REF);

        checkCacheHit(this::getClient, x, new HamcrestCondition<>(equalTo(FLOW)), flowId, ttl);
    }

    @Test
    public void testPeekDataflowFromCache() throws IOException {
        Context ctx = new Context();
        CachedRestClient client = getClient(ctx);

        ctx.reset();
        client.getFlows();
        client.getFlow(FLOW_REF);
        assertThat(ctx.getCount()).hasValue(1);
        assertThat(ctx.getMap()).containsOnlyKeys(flowsId);
    }

    @Test
    public void testGetStructure() throws IOException {
        Method<DataStructure> x = client -> client.getStructure(STRUCT_REF);

        checkCacheHit(this::getClient, x, new HamcrestCondition<>(equalTo(STRUCT)), structId, ttl);
    }

    @Test
    public void testGetData() throws IOException {
        for (Key key : keys("all", "M.BE.INDUSTRY", ".BE.INDUSTRY", "A.BE.INDUSTRY")) {
            for (DataFilter filter : filters(DataFilter.Detail.values())) {
                Method<Collection<Series>> x = client -> {
                    try (Stream<Series> cursor = client.getData(DataRef.of(FLOW_REF, key, filter), STRUCT)) {
                        return cursor.collect(Collectors.toList());
                    }
                };

                HamcrestCondition<Collection<Series>> validator = new HamcrestCondition<>(equalTo(DATA_SET.getData(key, filter)));

                if (filter.getDetail().isDataRequested()) {
                    checkCacheMiss(this::getClient, x, validator, noDataId, ttl);
                    checkCacheMiss(this::getClient, x, validator, seriesKeysOnlyId, ttl);
                } else {
                    if (filter.getDetail().isMetaRequested()) {
                        checkCacheHit(this::getClient, x, validator, noDataId, ttl);
                    } else {
                        checkCacheHit(this::getClient, x, validator, seriesKeysOnlyId, ttl);
                    }
                }
            }
        }
    }

    @Test
    public void testNarrowerRequest() throws IOException {
        Context ctx = new Context();
        CachedRestClient client = getClient(ctx);

        for (DataFilter filter : new DataFilter[]{DataFilter.SERIES_KEYS_ONLY, DataFilter.NO_DATA}) {
            IOConsumer<Key> method = key -> client.getData(DataRef.of(FLOW_REF, key, filter), STRUCT).close();

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
}
