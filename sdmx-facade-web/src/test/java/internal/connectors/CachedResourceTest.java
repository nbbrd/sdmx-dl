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
package internal.connectors;

import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.util.TypedId;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import java.io.IOException;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class CachedResourceTest {

    @Test
    public void testLoadDataFlowsById() throws IOException {
        CountingClient resource = new CountingClient();
        ConcurrentHashMap cache = new ConcurrentHashMap();
        FakeClock clock = new FakeClock();

        CachedResource target = new CachedResource(resource, "", cache, clock, 100);

        target.loadDataFlowsById();
        assertThat(resource.count).isEqualTo(1);
        assertThat(cache).containsOnlyKeys(flowsId);

        target.loadDataFlowsById();
        assertThat(resource.count).isEqualTo(1);
        assertThat(cache).containsOnlyKeys(flowsId);

        clock.plus(100);
        target.loadDataFlowsById();
        assertThat(resource.count).isEqualTo(2);
        assertThat(cache).containsOnlyKeys(flowsId);

        cache.clear();
        target.loadDataFlowsById();
        assertThat(resource.count).isEqualTo(3);
        assertThat(cache).containsOnlyKeys(flowsId);
    }

    @Test
    public void testLoadDataflow() throws IOException {
        CountingClient resource = new CountingClient();
        ConcurrentHashMap cache = new ConcurrentHashMap();
        FakeClock clock = new FakeClock();

        CachedResource target = new CachedResource(resource, "", cache, clock, 100);

        assertThatThrownBy(() -> target.loadDataflow(null)).isInstanceOf(NullPointerException.class);

        target.loadDataflow(sample);
        assertThat(resource.count).isEqualTo(1);
        assertThat(cache).containsOnlyKeys(flowId);

        target.loadDataflow(sample);
        assertThat(resource.count).isEqualTo(1);
        assertThat(cache).containsOnlyKeys(flowId);

        clock.plus(100);
        target.loadDataflow(sample);
        assertThat(resource.count).isEqualTo(2);
        assertThat(cache).containsOnlyKeys(flowId);

        cache.clear();
        target.loadDataflow(sample);
        assertThat(resource.count).isEqualTo(3);
        assertThat(cache).containsOnlyKeys(flowId);

        cache.clear();
        target.loadDataFlowsById();
        target.loadDataflow(sample);
        assertThat(resource.count).isEqualTo(4);
        assertThat(cache).containsOnlyKeys(flowsId);
    }

    @Test
    public void testLoadDataStructure() throws IOException {
        CountingClient resource = new CountingClient();
        ConcurrentHashMap cache = new ConcurrentHashMap();
        FakeClock clock = new FakeClock();

        CachedResource target = new CachedResource(resource, "", cache, clock, 100);

        assertThatThrownBy(() -> target.loadDataflow(null)).isInstanceOf(NullPointerException.class);

        target.loadDataStructure(sample);
        assertThat(resource.count).isEqualTo(2);
        assertThat(cache).containsOnlyKeys(flowId, structId);

        target.loadDataStructure(sample);
        assertThat(resource.count).isEqualTo(2);
        assertThat(cache).containsOnlyKeys(flowId, structId);

        clock.plus(100);
        target.loadDataStructure(sample);
        assertThat(resource.count).isEqualTo(4);
        assertThat(cache).containsOnlyKeys(flowId, structId);

        cache.clear();
        target.loadDataStructure(sample);
        assertThat(resource.count).isEqualTo(6);
        assertThat(cache).containsOnlyKeys(flowId, structId);
    }

    @Test
    public void testLoadData() throws IOException {
        CountingClient resource = new CountingClient();
        ConcurrentHashMap cache = new ConcurrentHashMap();
        FakeClock clock = new FakeClock();

        CachedResource target = new CachedResource(resource, "", cache, clock, 100);

        assertThatThrownBy(() -> target.loadDataflow(null)).isInstanceOf(NullPointerException.class);

        target.loadData(sample, Key.ALL, true);
        assertThat(resource.count).isEqualTo(3);
        assertThat(cache).containsOnlyKeys(flowId, structId, keysId);

        target.loadData(sample, Key.ALL, true);
        assertThat(resource.count).isEqualTo(3);
        assertThat(cache).containsOnlyKeys(flowId, structId, keysId);

        clock.plus(100);
        target.loadData(sample, Key.ALL, true);
        assertThat(resource.count).isEqualTo(6);
        assertThat(cache).containsOnlyKeys(flowId, structId, keysId);

        cache.clear();
        target.loadData(sample, Key.ALL, true);
        assertThat(resource.count).isEqualTo(9);
        assertThat(cache).containsOnlyKeys(flowId, structId, keysId);
    }

    private final DataflowRef sample = DataflowRef.parse("sample");
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

    private static final class CountingClient implements GenericSDMXClient {

        int count = 0;

        @Override
        public Map<String, Dataflow> getDataflows() throws SdmxException {
            count++;
            return Collections.singletonMap("sample", new Dataflow());
        }

        @Override
        public Dataflow getDataflow(String string, String string1, String string2) throws SdmxException {
            count++;
            return new Dataflow();
        }

        @Override
        public DataFlowStructure getDataFlowStructure(DSDIdentifier dsdi, boolean bln) throws SdmxException {
            count++;
            return new DataFlowStructure();
        }

        @Override
        public Map<String, String> getCodes(String string, String string1, String string2) throws SdmxException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<PortableTimeSeries> getTimeSeries(Dataflow dtflw, DataFlowStructure dfs, String string, String string1, String string2, boolean bln, String string3, boolean bln1) throws SdmxException {
            count++;
            return Collections.emptyList();
        }

        @Override
        public boolean needsCredentials() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setCredentials(String string, String string1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public URL getEndpoint() throws SdmxException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setEndpoint(URL url) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String buildDataURL(Dataflow dtflw, String string, String string1, String string2, boolean bln, String string3, boolean bln1) throws SdmxException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
