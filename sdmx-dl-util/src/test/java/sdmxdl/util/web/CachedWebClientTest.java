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
import _test.sdmxdl.util.XCountingWebClient;
import _test.sdmxdl.util.XRepoWebClient;
import nbbrd.io.function.IOConsumer;
import nbbrd.io.function.IOFunction;
import org.junit.Test;
import sdmxdl.*;
import sdmxdl.samples.RepoSamples;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import static _test.sdmxdl.util.CachingAssert.*;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.samples.RepoSamples.*;
import static sdmxdl.tck.DataFilterAssert.filters;
import static sdmxdl.tck.KeyAssert.keys;

/**
 * @author Philippe Charles
 */
public class CachedWebClientTest {

    private final String base = "abc/";
    private final Duration ttl = Duration.ofMillis(100);

    private final String flowsId = idOf("flows://", base);
    private final String flowId = idOf("flow://", base, GOOD_FLOW_REF);
    private final String structId = idOf("struct://", base, GOOD_STRUCT_REF);
    private final String seriesKeysOnlyId = idOf("seriesKeysOnly://", base, GOOD_FLOW_REF);
    private final String noDataId = idOf("noData://", base, GOOD_FLOW_REF);

    private CachedWebClient getClient(CachingAssert.Context ctx) {
        SdmxWebClient original = XRepoWebClient.of(RepoSamples.REPO);
        SdmxWebClient counting = XCountingWebClient.of(original, ctx.getCount());
        return new CachedWebClient(counting, ctx.newCache(), base, ttl);
    }

    @FunctionalInterface
    private interface Method<T> extends IOFunction<CachedWebClient, T> {

        default Consumer<CachedWebClient> andThen(IOConsumer<T> consumer) {
            return IOConsumer.unchecked(o -> consumer.acceptWithIO(applyWithIO(o)));
        }

        default Consumer<CachedWebClient> asConsumer() {
            return IOConsumer.unchecked(this::applyWithIO);
        }
    }

    @Test
    public void testGetFlows() throws IOException {
        Method<List<Dataflow>> method = CachedWebClient::getFlows;

        checkCacheHit(this::getClient, method.asConsumer(), flowsId, ttl.toMillis());

        assertThat(method.compose(this::getClient).applyWithIO(new Context()))
                .containsExactly(FLOW);
    }

    @Test
    public void testGetFlow() throws IOException {
        Method<Dataflow> method = client -> client.getFlow(GOOD_FLOW_REF);

        checkCacheHit(this::getClient, method.asConsumer(), flowId, ttl.toMillis());

        assertThat(method.compose(this::getClient).applyWithIO(new Context()))
                .isEqualTo(FLOW);
    }

    @Test
    public void testPeekDataflowFromCache() throws IOException {
        Context ctx = new Context();
        CachedWebClient client = getClient(ctx);

        ctx.reset();
        client.getFlows();
        client.getFlow(GOOD_FLOW_REF);
        assertThat(ctx.getCount()).hasValue(1);
        assertThat(ctx.getMap()).containsOnlyKeys(flowsId);
    }

    @Test
    public void testGetStructure() throws IOException {
        Method<DataStructure> method = client -> client.getStructure(GOOD_STRUCT_REF);

        checkCacheHit(this::getClient, method.asConsumer(), structId, ttl.toMillis());

        assertThat(method.compose(this::getClient).applyWithIO(new Context()))
                .isEqualTo(STRUCT);
    }

    @Test
    public void testGetData() throws IOException {
        for (Key key : keys("all", "M.BE.INDUSTRY", ".BE.INDUSTRY", "A.BE.INDUSTRY")) {
            for (DataFilter filter : filters(DataFilter.Detail.values())) {
                Method<DataCursor> method = client -> client.getData(new DataRequest(GOOD_FLOW_REF, key, filter), STRUCT);

                if (filter.getDetail().isDataRequested()) {
                    checkCacheMiss(this::getClient, method.andThen(Closeable::close), noDataId, ttl.toMillis());
                    checkCacheMiss(this::getClient, method.andThen(Closeable::close), seriesKeysOnlyId, ttl.toMillis());
                } else {
                    if (filter.getDetail().isMetaRequested()) {
                        checkCacheHit(this::getClient, method.andThen(Closeable::close), noDataId, ttl.toMillis());
                    } else {
                        checkCacheHit(this::getClient, method.andThen(Closeable::close), seriesKeysOnlyId, ttl.toMillis());
                    }
                }

                try (DataCursor cursor = method.compose(this::getClient).applyWithIO(new Context())) {
                    assertThat(cursor.toStream())
                            .containsExactlyElementsOf(RepoSamples.DATA_SET.getData(key, filter));
                }
            }
        }
    }

    @Test
    public void testBroaderRequest() throws IOException {
        Context ctx = new Context();
        CachedWebClient client = getClient(ctx);

        for (DataFilter filter : new DataFilter[]{DataFilter.SERIES_KEYS_ONLY, DataFilter.NO_DATA}) {
            IOConsumer<Key> method = key -> client.getData(new DataRequest(GOOD_FLOW_REF, key, filter), STRUCT).close();

            ctx.reset();
            method.acceptWithIO(Key.ALL);
            method.acceptWithIO(Key.parse("M.BE.INDUSTRY"));
            assertThat(ctx.getCount()).hasValue(1);

            ctx.reset();
            method.acceptWithIO(Key.parse("M.BE.INDUSTRY"));
            method.acceptWithIO(Key.ALL);
            assertThat(ctx.getCount()).hasValue(2);
        }
    }
}
