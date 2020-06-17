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
package sdmxdl.tck;

import sdmxdl.DataCursor;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.DataFilter;
import sdmxdl.Obs;
import sdmxdl.SdmxConnection;
import sdmxdl.Series;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import nbbrd.io.function.IOConsumer;
import org.assertj.core.api.SoftAssertions;

/**
 *
 * @author Philippe Charles
 */
public final class ConnectionAssert {

    public static void assertCompliance(Callable<SdmxConnection> supplier, DataflowRef ref) {
        SoftAssertions s = new SoftAssertions();
        try {
            assertCompliance(s, supplier, ref);
        } catch (Exception ex) {
            s.fail("Unexpected exception", ex);
        }
        s.assertAll();
    }

    public static void assertCompliance(SoftAssertions s, Callable<SdmxConnection> supplier, DataflowRef ref) throws Exception {
        try (SdmxConnection conn = supplier.call()) {
            assertNonnull(s, conn, ref);
            DataCursorAssert.assertCompliance(s, () -> conn.getDataCursor(ref, Key.ALL, DataFilter.ALL));
            s.assertThat(conn.getData(ref, Key.ALL, DataFilter.ALL)).containsExactlyElementsOf(cursorToSeries(ref, Key.ALL, DataFilter.ALL, conn));
            s.assertThat(conn.getDataStream(ref, Key.ALL, DataFilter.ALL)).containsExactlyElementsOf(cursorToSeries(ref, Key.ALL, DataFilter.ALL, conn));
            s.assertThat(conn.getFlows()).isNotEmpty().filteredOn(ref::containsRef).isNotEmpty();
            s.assertThat(conn.getFlow(ref)).isNotNull();
            s.assertThat(conn.getStructure(ref)).isNotNull();
        }

        try (SdmxConnection conn = supplier.call()) {
            conn.close();
        } catch (Exception ex) {
            s.fail("Subsequent calls to #close must not raise exception", ex);
        }

        assertState(s, supplier, o -> o.getData(ref, Key.ALL, DataFilter.ALL), "getData(DataflowRef, Key, DataFilter)");
        assertState(s, supplier, o -> o.getDataStream(ref, Key.ALL, DataFilter.ALL), "getDataStream(DataflowRef, Key, DataFilter)");
        assertState(s, supplier, o -> o.getDataCursor(ref, Key.ALL, DataFilter.ALL), "getDataCursor(DataflowRef, Key, DataFilter)");
        assertState(s, supplier, o -> o.getStructure(ref), "getStructure(DataflowRef)");
        assertState(s, supplier, o -> o.getFlow(ref), "getFlow(DataflowRef)");
        assertState(s, supplier, SdmxConnection::getFlows, "getFlows()");
    }

    @SuppressWarnings("null")
    private static void assertNonnull(SoftAssertions s, SdmxConnection conn, DataflowRef ref) {
        s.assertThatThrownBy(() -> conn.getDataCursor(null, Key.ALL, DataFilter.ALL))
                .as("Expecting 'getDataCursor(DataflowRef, Key, DataFilter)' to raise NPE when called with null flowRef")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getData(null, Key.ALL, DataFilter.ALL))
                .as("Expecting 'getData(DataflowRef, Key, DataFilter)' to raise NPE when called with null flowRef")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getData(ref, null, DataFilter.ALL))
                .as("Expecting 'getData(DataflowRef, Key, DataFilter)' to raise NPE when called with null key")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getData(ref, Key.ALL, null))
                .as("Expecting 'getData(DataflowRef, Key, DataFilter)' to raise NPE when called with null query")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getDataStream(null, Key.ALL, DataFilter.ALL))
                .as("Expecting 'getDataStream(DataflowRef, Key, DataFilter)' to raise NPE when called with null flowRef")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getDataStream(ref, null, DataFilter.ALL))
                .as("Expecting 'getDataStream(DataflowRef, Key, DataFilter)' to raise NPE when called with null key")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getDataStream(ref, Key.ALL, null))
                .as("Expecting 'getDataStream(DataflowRef, Key, DataFilter)' to raise NPE when called with null query")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getDataCursor(null, Key.ALL, DataFilter.ALL))
                .as("Expecting 'getDataCursor(DataflowRef, Key, DataFilter)' to raise NPE when called with null flowRef")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getDataCursor(ref, null, DataFilter.ALL))
                .as("Expecting 'getDataCursor(DataflowRef, Key, DataFilter)' to raise NPE when called with null key")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getDataCursor(ref, Key.ALL, null))
                .as("Expecting 'getDataCursor(DataflowRef, Key, DataFilter)' to raise NPE when called with null query")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getStructure(null))
                .as("Expecting 'getStructure(DataflowRef)' to raise NPE when called with null flowRef")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getFlow(null))
                .as("Expecting 'getFlow(DataflowRef)' to raise NPE when called with null flowRef")
                .isInstanceOf(NullPointerException.class);
    }

    private static void assertState(SoftAssertions s, Callable<SdmxConnection> supplier, IOConsumer<SdmxConnection> consumer, String expression) throws Exception {
        try (SdmxConnection conn = supplier.call()) {
            conn.close();
            s.assertThatThrownBy(() -> consumer.acceptWithIO(conn))
                    .as("Expecting '%s' to raise IOException when called after close", expression)
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("closed");
        }
    }

    private static List<Series> cursorToSeries(DataflowRef ref, Key key, DataFilter filter, SdmxConnection conn) throws IOException {
        List<Series> result = new ArrayList();
        try (DataCursor c = conn.getDataCursor(ref, key, filter)) {
            while (c.nextSeries()) {
                Series.Builder series = Series.builder();
                series.key(c.getSeriesKey());
                series.freq(c.getSeriesFrequency());
                series.meta(c.getSeriesAttributes());
                while (c.nextObs()) {
                    series.obs(Obs.of(c.getObsPeriod(), c.getObsValue()));
                }
                result.add(series.build());
            }
        }
        return result;
    }
}
