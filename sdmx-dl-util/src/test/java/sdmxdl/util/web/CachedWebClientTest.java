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

import _test.sdmxdl.util.client.XCallStackWebClient;
import _test.sdmxdl.util.client.XRepoWebClient;
import org.junit.Test;
import sdmxdl.DataFilter;
import sdmxdl.Key;
import sdmxdl.ext.SdmxCache;
import sdmxdl.samples.RepoSamples;
import sdmxdl.util.TypedId;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * @author Philippe Charles
 */
public class CachedWebClientTest {

    @Test
    public void testGetFlows() throws IOException {
        AtomicInteger count = new AtomicInteger();
        ConcurrentHashMap cache = new ConcurrentHashMap();
        FakeClock clock = new FakeClock();

        CachedWebClient target = new CachedWebClient(getClient(count), SdmxCache.of(cache, clock), Duration.ofMillis(100), "");

        target.getFlows();
        assertThat(count).hasValue(1);
        assertThat(cache).containsOnlyKeys(flowsId);

        target.getFlows();
        assertThat(count).hasValue(1);
        assertThat(cache).containsOnlyKeys(flowsId);

        clock.plus(100);
        target.getFlows();
        assertThat(count).hasValue(2);
        assertThat(cache).containsOnlyKeys(flowsId);

        cache.clear();
        target.getFlows();
        assertThat(count).hasValue(3);
        assertThat(cache).containsOnlyKeys(flowsId);
    }

    @Test
    public void testGetFlow() throws IOException {
        AtomicInteger count = new AtomicInteger();
        ConcurrentHashMap cache = new ConcurrentHashMap();
        FakeClock clock = new FakeClock();

        CachedWebClient target = new CachedWebClient(getClient(count), SdmxCache.of(cache, clock), Duration.ofMillis(100), "");

        assertThatNullPointerException().isThrownBy(() -> target.getFlow(null));

        target.getFlow(RepoSamples.GOOD_FLOW_REF);
        assertThat(count).hasValue(1);
        assertThat(cache).containsOnlyKeys(flowId);

        target.getFlow(RepoSamples.GOOD_FLOW_REF);
        assertThat(count).hasValue(1);
        assertThat(cache).containsOnlyKeys(flowId);

        clock.plus(100);
        target.getFlow(RepoSamples.GOOD_FLOW_REF);
        assertThat(count).hasValue(2);
        assertThat(cache).containsOnlyKeys(flowId);

        cache.clear();
        target.getFlow(RepoSamples.GOOD_FLOW_REF);
        assertThat(count).hasValue(3);
        assertThat(cache).containsOnlyKeys(flowId);

        cache.clear();
        target.getFlows();
        target.getFlow(RepoSamples.GOOD_FLOW_REF);
        assertThat(count).hasValue(4);
        assertThat(cache).containsOnlyKeys(flowsId);
    }

    @Test
    public void testGetStructure() throws IOException {
        AtomicInteger count = new AtomicInteger();
        ConcurrentHashMap cache = new ConcurrentHashMap();
        FakeClock clock = new FakeClock();

        CachedWebClient target = new CachedWebClient(getClient(count), SdmxCache.of(cache, clock), Duration.ofMillis(100), "");

        assertThatNullPointerException().isThrownBy(() -> target.getStructure(null));

        target.getStructure(RepoSamples.GOOD_STRUCT_REF);
        assertThat(count).hasValue(1);
        assertThat(cache).containsOnlyKeys(structId);

        target.getStructure(RepoSamples.GOOD_STRUCT_REF);
        assertThat(count).hasValue(1);
        assertThat(cache).containsOnlyKeys(structId);

        clock.plus(100);
        target.getStructure(RepoSamples.GOOD_STRUCT_REF);
        assertThat(count).hasValue(2);
        assertThat(cache).containsOnlyKeys(structId);

        cache.clear();
        target.getStructure(RepoSamples.GOOD_STRUCT_REF);
        assertThat(count).hasValue(3);
        assertThat(cache).containsOnlyKeys(structId);
    }

    @Test
    public void testLoadData() throws IOException {
        AtomicInteger count = new AtomicInteger();
        ConcurrentHashMap cache = new ConcurrentHashMap();
        FakeClock clock = new FakeClock();

        CachedWebClient target = new CachedWebClient(getClient(count), SdmxCache.of(cache, clock), Duration.ofMillis(100), "");

        assertThatNullPointerException().isThrownBy(() -> target.getData(null, null));

        DataRequest request = new DataRequest(RepoSamples.GOOD_FLOW_REF, Key.ALL, filter);

        target.getData(request, null);
        assertThat(count).hasValue(1);
        assertThat(cache).containsOnlyKeys(keysId);

        target.getData(request, null);
        assertThat(count).hasValue(1);
        assertThat(cache).containsOnlyKeys(keysId);

        clock.plus(100);
        target.getData(request, null);
        assertThat(count).hasValue(2);
        assertThat(cache).containsOnlyKeys(keysId);

        cache.clear();
        target.getData(request, null);
        assertThat(count).hasValue(3);
        assertThat(cache).containsOnlyKeys(keysId);
    }

    private final DataFilter filter = DataFilter.SERIES_KEYS_ONLY;
    private final String flowsId = TypedId.of("flows://", Function.identity(), Function.identity()).getContent();
    private final String flowId = TypedId.of("flow://", Function.identity(), Function.identity()).with(RepoSamples.GOOD_FLOW_REF).getContent();
    private final String structId = TypedId.of("struct://", Function.identity(), Function.identity()).with(RepoSamples.GOOD_STRUCT_REF).getContent();
    private final String keysId = TypedId.of("keys://", Function.identity(), Function.identity()).with(RepoSamples.GOOD_FLOW_REF).getContent();

    private static final class FakeClock extends Clock {

        private Instant current = Instant.now();

        void plus(long durationInMillis) {
            current = current.plus(100, ChronoUnit.MILLIS);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.systemDefault();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return current;
        }
    }

    private static SdmxWebClient getClient(AtomicInteger count) throws IOException {
        SdmxWebClient original = XRepoWebClient.of(RepoSamples.REPO);
        return XCallStackWebClient.of(original, count);
    }
}
