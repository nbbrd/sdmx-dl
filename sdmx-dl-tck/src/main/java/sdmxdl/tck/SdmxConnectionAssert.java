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

import internal.sdmxdl.tck.TckUtil;
import nbbrd.io.function.IOConsumer;
import nbbrd.io.function.IOSupplier;
import org.assertj.core.api.SoftAssertions;
import sdmxdl.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SdmxConnectionAssert {

    @lombok.Builder
    public static class Sample {
        DataflowRef valid;
        DataflowRef invalid;
    }

    public void assertCompliance(IOSupplier<SdmxConnection> supplier, Sample sample) {
        TckUtil.run(s -> assertCompliance(s, supplier, sample));
    }

    public void assertCompliance(SoftAssertions s, IOSupplier<SdmxConnection> supplier, Sample sample) {
        try (SdmxConnection conn = supplier.getWithIO()) {
            assertNonnull(s, conn, sample.valid);
            DataCursorAssert.assertCompliance(s, () -> conn.getDataCursor(sample.valid, Key.ALL, DataFilter.ALL));
            s.assertThat(conn.getData(sample.valid, Key.ALL, DataFilter.ALL)).containsExactlyElementsOf(cursorToSeries(sample.valid, Key.ALL, DataFilter.ALL, conn));
            s.assertThat(conn.getDataStream(sample.valid, Key.ALL, DataFilter.ALL)).containsExactlyElementsOf(cursorToSeries(sample.valid, Key.ALL, DataFilter.ALL, conn));
            s.assertThat(conn.getFlows()).isNotEmpty().filteredOn(sample.valid::containsRef).isNotEmpty();
            s.assertThat(conn.getFlow(sample.valid)).isNotNull();
            s.assertThat(conn.getStructure(sample.valid)).isNotNull();
        } catch (Exception ex) {
            s.fail("Not expected to raise exception", ex);
        }

        try (SdmxConnection conn = supplier.getWithIO()) {
            conn.close();
        } catch (Exception ex) {
            s.fail("Subsequent calls to #close must not raise exception", ex);
        }

        assertState(s, supplier, o -> o.getData(sample.valid, Key.ALL, DataFilter.ALL), "getData(DataflowRef, Key, DataFilter)");
        assertState(s, supplier, o -> o.getDataStream(sample.valid, Key.ALL, DataFilter.ALL), "getDataStream(DataflowRef, Key, DataFilter)");
        assertState(s, supplier, o -> o.getDataCursor(sample.valid, Key.ALL, DataFilter.ALL), "getDataCursor(DataflowRef, Key, DataFilter)");
        assertState(s, supplier, o -> o.getStructure(sample.valid), "getStructure(DataflowRef)");
        assertState(s, supplier, o -> o.getFlow(sample.valid), "getFlow(DataflowRef)");
        assertState(s, supplier, SdmxConnection::getFlows, "getFlows()");
    }

    @SuppressWarnings("null")
    private void assertNonnull(SoftAssertions s, SdmxConnection conn, DataflowRef ref) {
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

    private void assertState(SoftAssertions s, IOSupplier<SdmxConnection> supplier, IOConsumer<SdmxConnection> consumer, String expression) {
        try (SdmxConnection conn = supplier.getWithIO()) {
            conn.close();
            s.assertThatThrownBy(() -> consumer.acceptWithIO(conn))
                    .as("Expecting '%s' to raise IOException when called after close", expression)
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("closed");
        } catch (Exception ex) {
            s.fail("Not expected to raise exception", ex);
        }
    }

    private List<Series> cursorToSeries(DataflowRef ref, Key key, DataFilter filter, SdmxConnection conn) throws IOException {
        List<Series> result = new ArrayList<>();
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
