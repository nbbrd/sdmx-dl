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

import static sdmxdl.tck.DataFilterAssert.filters;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SdmxConnectionAssert {

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    public static class Sample {

        @lombok.NonNull
        DataflowRef validFlow;

        @lombok.NonNull
        DataflowRef invalidFlow;

        @lombok.NonNull
        Key validKey;

        @lombok.NonNull
        Key invalidKey;
    }

    public void assertCompliance(IOSupplier<SdmxConnection> supplier, Sample sample) {
        TckUtil.run(s -> assertCompliance(s, supplier, sample));
    }

    public void assertCompliance(SoftAssertions s, IOSupplier<SdmxConnection> supplier, Sample sample) {
        try (SdmxConnection conn = supplier.getWithIO()) {
            checkValidFlow(s, sample, conn);
            checkInvalidFlow(s, sample, conn);
        } catch (Exception ex) {
            s.fail("Not expected to raise exception", ex);
        }

        try (SdmxConnection conn = supplier.getWithIO()) {
            //noinspection RedundantExplicitClose
            conn.close();
        } catch (Exception ex) {
            s.fail("Subsequent calls to #close must not raise exception", ex);
        }

        DataRef validFlow = DataRef.of(sample.validFlow, Key.ALL, DataFilter.FULL);
        assertState(s, supplier, o -> o.getData(validFlow), "getData(DataflowRef, Key, DataFilter)");
        assertState(s, supplier, o -> o.getDataStream(validFlow), "getDataStream(DataflowRef, Key, DataFilter)");
        assertState(s, supplier, o -> o.getDataCursor(validFlow), "getDataCursor(DataflowRef, Key, DataFilter)");
        assertState(s, supplier, o -> o.getStructure(sample.validFlow), "getStructure(DataflowRef)");
        assertState(s, supplier, o -> o.getFlow(sample.validFlow), "getFlow(DataflowRef)");
        assertState(s, supplier, SdmxConnection::getFlows, "getFlows()");
    }

    private void checkValidFlow(SoftAssertions s, Sample sample, SdmxConnection conn) throws Exception {
        assertNonnull(s, conn, sample.validFlow);
        for (DataFilter filter : filters(DataFilter.Detail.values())) {
            checkValidKey(s, sample, conn, filter);
            checkInvalidKey(s, sample, conn, filter);
        }
        s.assertThat(conn.getFlows()).isNotEmpty().filteredOn(sample.validFlow::containsRef).isNotEmpty();
        s.assertThat(conn.getFlow(sample.validFlow)).isNotNull();
        s.assertThat(conn.getStructure(sample.validFlow)).isNotNull();
    }

    private void checkInvalidKey(SoftAssertions s, Sample sample, SdmxConnection conn, DataFilter filter) {
        DataRef invalidKey = DataRef.of(sample.validFlow, sample.invalidKey, filter);
        s.assertThatThrownBy(() -> conn.getData(invalidKey))
                .isInstanceOf(IOException.class)
                .hasMessageContainingAll("Invalid key", sample.invalidKey.toString());
        s.assertThatThrownBy(() -> conn.getDataCursor(invalidKey))
                .isInstanceOf(IOException.class)
                .hasMessageContainingAll("Invalid key", sample.invalidKey.toString());
        s.assertThatThrownBy(() -> conn.getDataStream(invalidKey))
                .isInstanceOf(IOException.class)
                .hasMessageContainingAll("Invalid key", sample.invalidKey.toString());
    }

    private void checkValidKey(SoftAssertions s, Sample sample, SdmxConnection conn, DataFilter filter) throws Exception {
        DataRef validKey = DataRef.of(sample.validFlow, sample.validKey, filter);
        DataCursorAssert.assertCompliance(s, () -> conn.getDataCursor(validKey), sample.validKey, filter);
        List<Series> data = cursorToSeries(validKey, conn);
        s.assertThat(conn.getData(validKey)).containsExactlyElementsOf(data);
        s.assertThat(conn.getDataStream(validKey)).containsExactlyElementsOf(data);
    }

    private void checkInvalidFlow(SoftAssertions s, Sample sample, SdmxConnection conn) {
        for (DataFilter filter : filters(DataFilter.Detail.values())) {
            DataRef invalidFlow = DataRef.of(sample.invalidFlow, sample.validKey, filter);
            s.assertThatThrownBy(() -> conn.getDataStream(invalidFlow))
                    .isInstanceOf(IOException.class);
            s.assertThatThrownBy(() -> conn.getFlow(sample.invalidFlow))
                    .isInstanceOf(IOException.class);
            s.assertThatThrownBy(() -> conn.getData(invalidFlow))
                    .isInstanceOf(IOException.class);
            s.assertThatThrownBy(() -> conn.getDataCursor(invalidFlow))
                    .isInstanceOf(IOException.class);
            s.assertThatThrownBy(() -> conn.getStructure(sample.invalidFlow))
                    .isInstanceOf(IOException.class);
        }
    }

    @SuppressWarnings("null")
    private void assertNonnull(SoftAssertions s, SdmxConnection conn, DataflowRef ref) {
        s.assertThatThrownBy(() -> conn.getDataCursor(null))
                .as("Expecting 'getDataCursor(DataRef)' to raise NPE when called with null dataRef")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getData(null))
                .as("Expecting 'getData(DataRef)' to raise NPE when called with null dataRef")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getDataStream(null))
                .as("Expecting 'getDataStream(DataRef)' to raise NPE when called with null dataRef")
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

    private List<Series> cursorToSeries(DataRef dataRef, SdmxConnection conn) throws IOException {
        DataFilter.Detail detail = dataRef.getFilter().getDetail();
        List<Series> result = new ArrayList<>();
        try (DataCursor c = conn.getDataCursor(dataRef)) {
            Series.Builder series = Series.builder();
            while (c.nextSeries()) {
                series.clearMeta()
                        .clearObs()
                        .key(c.getSeriesKey())
                        .freq(c.getSeriesFrequency());
                if (detail.isMetaRequested()) {
                    series.meta(c.getSeriesAttributes());
                }
                if (detail.isDataRequested()) {
                    Obs.Builder obs = Obs.builder();
                    while (c.nextObs()) {
                        series.obs(obs
                                .clearMeta()
                                .period(c.getObsPeriod())
                                .value(c.getObsValue())
                                .meta(c.getObsAttributes())
                                .build()
                        );
                    }
                }
                result.add(series.build());
            }
        }
        return result;
    }
}
