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
package tests.sdmxdl.api;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.*;

import java.io.IOException;

import static tests.sdmxdl.api.TckUtil.nullDescriptionOf;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class ConnectionAssert {

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

    @FunctionalInterface
    public interface ConnectionSupplier {
        Connection getWithIO() throws IOException;
    }

    @FunctionalInterface
    public interface ConnectionConsumer {
        void acceptWithIO(Connection connection) throws IOException;
    }

    public void assertCompliance(ConnectionSupplier supplier, Sample sample) {
        TckUtil.run(s -> assertCompliance(s, supplier, sample));
    }

    public void assertCompliance(SoftAssertions s, ConnectionSupplier supplier, Sample sample) {
        try (Connection conn = supplier.getWithIO()) {
            checkValidFlow(s, sample, conn);
            checkInvalidFlow(s, sample, conn);
        } catch (IOException ex) {
            s.fail("Not expected to raise exception", ex);
        }

        checkRedundantClose(s, supplier);

        assertState(s, supplier, o -> o.getData(sample.validFlow, DataQuery.ALL), "getData(DataflowRef, DataQuery)");
        assertState(s, supplier, o -> o.getDataStream(sample.validFlow, DataQuery.ALL), "getDataStream(DataflowRef, DataQuery)");
        assertState(s, supplier, o -> o.getStructure(sample.validFlow), "getStructure(DataflowRef)");
        assertState(s, supplier, o -> o.getFlow(sample.validFlow), "getFlow(DataflowRef)");
        assertState(s, supplier, Connection::getFlows, "getFlows()");
    }

    @SuppressWarnings("RedundantExplicitClose")
    private void checkRedundantClose(SoftAssertions s, ConnectionSupplier supplier) {
        try (Connection conn = supplier.getWithIO()) {
            conn.close();
        } catch (IOException ex) {
            s.fail("Subsequent calls to #close must not raise exception", ex);
        }
    }

    private void checkValidFlow(SoftAssertions s, Sample sample, Connection conn) throws IOException {
        assertNonnull(s, conn, sample.validFlow);
        for (DataDetail filter : DataDetail.values()) {
            checkValidKey(s, sample, conn, filter);
            checkInvalidKey(s, sample, conn, filter);
        }
        s.assertThat(conn.getFlows()).isNotEmpty().filteredOn(sample.validFlow::containsRef).isNotEmpty();
        s.assertThat(conn.getFlow(sample.validFlow)).isNotNull();
        s.assertThat(conn.getStructure(sample.validFlow)).isNotNull();
    }

    private void checkInvalidKey(SoftAssertions s, Sample sample, Connection conn, DataDetail filter) {
        DataQuery invalidQuery = DataQuery.builder().key(sample.invalidKey).detail(filter).build();

        s.assertThatThrownBy(() -> conn.getData(sample.validFlow, invalidQuery))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContainingAll("Expecting key", sample.invalidKey.toString());

        s.assertThatThrownBy(() -> conn.getDataStream(sample.validFlow, invalidQuery))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContainingAll("Expecting key", sample.invalidKey.toString());
    }

    private void checkValidKey(SoftAssertions s, Sample sample, Connection conn, DataDetail filter) throws IOException {
        DataQuery validQuery = DataQuery.builder().key(sample.validKey).detail(filter).build();

        s.assertThat(conn.getDataStream(sample.validFlow, validQuery))
                .containsExactlyElementsOf(conn.getData(sample.validFlow, validQuery).getData());
    }

    private void checkInvalidFlow(SoftAssertions s, Sample sample, Connection conn) {
        for (DataDetail filter : DataDetail.values()) {
            DataQuery validQuery = DataQuery.builder().key(sample.validKey).detail(filter).build();

            s.assertThatThrownBy(() -> conn.getData(sample.invalidFlow, validQuery))
                    .isInstanceOf(IOException.class);

            s.assertThatThrownBy(() -> conn.getDataStream(sample.invalidFlow, validQuery))
                    .isInstanceOf(IOException.class);

            s.assertThatThrownBy(() -> conn.getFlow(sample.invalidFlow))
                    .isInstanceOf(IOException.class);

            s.assertThatThrownBy(() -> conn.getStructure(sample.invalidFlow))
                    .isInstanceOf(IOException.class);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void assertNonnull(SoftAssertions s, Connection conn, DataflowRef ref) {
        s.assertThatThrownBy(() -> conn.getData(null, DataQuery.ALL))
                .as(nullDescriptionOf("getData(DataflowRef, DataQuery)", "flowRef"))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getData(ref, null))
                .as(nullDescriptionOf("getData(DataflowRef, DataQuery)", "query"))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getDataStream(null, DataQuery.ALL))
                .as(nullDescriptionOf("getDataStream(DataflowRef, DataQuery)", "flowRef"))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getDataStream(ref, null))
                .as(nullDescriptionOf("getDataStream(DataflowRef, DataQuery)", "query"))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getStructure(null))
                .as(nullDescriptionOf("getStructure(DataflowRef)", "flowRef"))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getFlow(null))
                .as(nullDescriptionOf("getFlow(DataflowRef)", "flowRef"))
                .isInstanceOf(NullPointerException.class);
    }

    private void assertState(SoftAssertions s, ConnectionSupplier supplier, ConnectionConsumer consumer, String expression) {
        try (Connection conn = supplier.getWithIO()) {
            conn.close();
            s.assertThatThrownBy(() -> consumer.acceptWithIO(conn))
                    .as("Expecting '%s' to raise IOException when called after close", expression)
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("closed");
        } catch (IOException ex) {
            s.fail("Not expected to raise exception", ex);
        }
    }
}
