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
package internal.web;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.util.NoOpCursor;
import be.nbb.sdmx.facade.util.TypedId;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class CachedRestClientTest {

    @Test
    public void testGetFlows() throws IOException {
        CountingClient resource = new CountingClient();
        ConcurrentHashMap cache = new ConcurrentHashMap();
        FakeClock clock = new FakeClock();

        CachedRestClient target = new CachedRestClient(resource, "", cache, clock, 100);

        target.getFlows();
        assertThat(resource.count).isEqualTo(1);
        assertThat(cache).containsOnlyKeys(flowsId);

        target.getFlows();
        assertThat(resource.count).isEqualTo(1);
        assertThat(cache).containsOnlyKeys(flowsId);

        clock.plus(100);
        target.getFlows();
        assertThat(resource.count).isEqualTo(2);
        assertThat(cache).containsOnlyKeys(flowsId);

        cache.clear();
        target.getFlows();
        assertThat(resource.count).isEqualTo(3);
        assertThat(cache).containsOnlyKeys(flowsId);
    }

    @Test
    public void testGetFlow() throws IOException {
        CountingClient resource = new CountingClient();
        ConcurrentHashMap cache = new ConcurrentHashMap();
        FakeClock clock = new FakeClock();

        CachedRestClient target = new CachedRestClient(resource, "", cache, clock, 100);

        assertThatNullPointerException().isThrownBy(() -> target.getFlow(null));

        target.getFlow(FLOW_REF);
        assertThat(resource.count).isEqualTo(1);
        assertThat(cache).containsOnlyKeys(flowId);

        target.getFlow(FLOW_REF);
        assertThat(resource.count).isEqualTo(1);
        assertThat(cache).containsOnlyKeys(flowId);

        clock.plus(100);
        target.getFlow(FLOW_REF);
        assertThat(resource.count).isEqualTo(2);
        assertThat(cache).containsOnlyKeys(flowId);

        cache.clear();
        target.getFlow(FLOW_REF);
        assertThat(resource.count).isEqualTo(3);
        assertThat(cache).containsOnlyKeys(flowId);

        cache.clear();
        target.getFlows();
        target.getFlow(FLOW_REF);
        assertThat(resource.count).isEqualTo(4);
        assertThat(cache).containsOnlyKeys(flowsId);
    }

    @Test
    public void testGetStructure() throws IOException {
        CountingClient resource = new CountingClient();
        ConcurrentHashMap cache = new ConcurrentHashMap();
        FakeClock clock = new FakeClock();

        CachedRestClient target = new CachedRestClient(resource, "", cache, clock, 100);

        assertThatNullPointerException().isThrownBy(() -> target.getStructure(null));

        target.getStructure(STRUCT_REF);
        assertThat(resource.count).isEqualTo(1);
        assertThat(cache).containsOnlyKeys(structId);

        target.getStructure(STRUCT_REF);
        assertThat(resource.count).isEqualTo(1);
        assertThat(cache).containsOnlyKeys(structId);

        clock.plus(100);
        target.getStructure(STRUCT_REF);
        assertThat(resource.count).isEqualTo(2);
        assertThat(cache).containsOnlyKeys(structId);

        cache.clear();
        target.getStructure(STRUCT_REF);
        assertThat(resource.count).isEqualTo(3);
        assertThat(cache).containsOnlyKeys(structId);
    }

    @Test
    public void testLoadData() throws IOException {
        CountingClient resource = new CountingClient();
        ConcurrentHashMap cache = new ConcurrentHashMap();
        FakeClock clock = new FakeClock();

        CachedRestClient target = new CachedRestClient(resource, "", cache, clock, 100);

        assertThatNullPointerException().isThrownBy(() -> target.getData(null, null, query));

        target.getData(FLOW_REF, null, query);
        assertThat(resource.count).isEqualTo(1);
        assertThat(cache).containsOnlyKeys(keysId);

        target.getData(FLOW_REF, null, query);
        assertThat(resource.count).isEqualTo(1);
        assertThat(cache).containsOnlyKeys(keysId);

        clock.plus(100);
        target.getData(FLOW_REF, null, query);
        assertThat(resource.count).isEqualTo(2);
        assertThat(cache).containsOnlyKeys(keysId);

        cache.clear();
        target.getData(FLOW_REF, null, query);
        assertThat(resource.count).isEqualTo(3);
        assertThat(cache).containsOnlyKeys(keysId);
    }

    private static final DataflowRef FLOW_REF = DataflowRef.parse("sample");
    private static final DataStructureRef STRUCT_REF = DataStructureRef.parse("sample");
    private static final DataStructure STRUCT = DataStructure.builder().ref(STRUCT_REF).label("").build();
    private final DataQuery query = DataQuery.of(Key.ALL, true);
    private final TypedId<?> flowsId = TypedId.of("flows://");
    private final TypedId<?> flowId = TypedId.of("flow://all,sample,latest");
    private final TypedId<?> structId = TypedId.of("struct://all,sample,latest");
    private final TypedId<?> keysId = TypedId.of("keys://all,sample,latest");

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

    private static final class CountingClient implements RestClient {

        int count = 0;

        @Override
        public List<Dataflow> getFlows() throws IOException {
            count++;
            return Collections.singletonList(Dataflow.of(FLOW_REF, STRUCT_REF, "sample"));
        }

        @Override
        public Dataflow getFlow(DataflowRef ref) throws IOException {
            count++;
            return Dataflow.of(FLOW_REF, STRUCT_REF, "sample");
        }

        @Override
        public DataStructure getStructure(DataStructureRef ref) throws IOException {
            count++;
            return STRUCT;
        }

        @Override
        public DataCursor getData(DataflowRef flowRef, DataStructure dsd, DataQuery query) throws IOException {
            count++;
            return NoOpCursor.noOp();
        }

        @Override
        public boolean isSeriesKeysOnlySupported() {
            return true;
        }

        @Override
        public DataStructureRef peekStructureRef(DataflowRef flowRef) {
            return null;
        }
    }
}
