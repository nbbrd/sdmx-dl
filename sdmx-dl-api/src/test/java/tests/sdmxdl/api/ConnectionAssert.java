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

import static sdmxdl.DatabaseRef.NO_DATABASE;
import static tests.sdmxdl.api.SdmxConditions.*;
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
        FlowRef validFlow;

        @lombok.NonNull
        FlowRef invalidFlow;

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

        assertState(s, supplier, o -> o.getData(NO_DATABASE, sample.validFlow, Query.ALL), "getData(DataflowRef, DataQuery)");
        assertState(s, supplier, o -> o.getDataStream(NO_DATABASE, sample.validFlow, Query.ALL), "getDataStream(DataflowRef, DataQuery)");
        assertState(s, supplier, o -> o.getStructure(NO_DATABASE, sample.validFlow), "getStructure(DataflowRef)");
        assertState(s, supplier, o -> o.getFlow(NO_DATABASE, sample.validFlow), "getFlow(DataflowRef)");
        assertState(s, supplier, o -> o.getFlows(NO_DATABASE), "getFlows()");
        assertState(s, supplier, o -> o.getAvailableDimensionCodes(NO_DATABASE, sample.validFlow, Key.ALL, 0), "getAvailableDimensionValues(DataflowRef,Key,int)");
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
        for (Detail filter : Detail.values()) {
            checkValidKey(s, sample, conn, filter);
            checkInvalidKey(s, sample, conn, filter);
        }

        s.assertThat(conn.getFlows(NO_DATABASE))
                .are(validDataflow())
                .anyMatch(sample.validFlow::containsRef);

        s.assertThat(conn.getFlow(NO_DATABASE, sample.validFlow))
                .is(validDataflow());

        Structure dsd = conn.getStructure(NO_DATABASE, sample.validFlow);
        s.assertThat(dsd).has(validName());
        s.assertThat(dsd.getAttributes()).are(validAttribute());
        s.assertThat(dsd.getDimensions()).are(validDimension());
    }

    private void checkInvalidKey(SoftAssertions s, Sample sample, Connection conn, Detail filter) {
        Query invalidQuery = Query.builder().key(sample.invalidKey).detail(filter).build();

        s.assertThatThrownBy(() -> conn.getData(NO_DATABASE, sample.validFlow, invalidQuery))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContainingAll("Expecting key", sample.invalidKey.toString());

        s.assertThatThrownBy(() -> conn.getDataStream(NO_DATABASE, sample.validFlow, invalidQuery))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContainingAll("Expecting key", sample.invalidKey.toString());
    }

    private void checkValidKey(SoftAssertions s, Sample sample, Connection conn, Detail filter) throws IOException {
        Query validQuery = Query.builder().key(sample.validKey).detail(filter).build();

        s.assertThat(conn.getDataStream(NO_DATABASE, sample.validFlow, validQuery))
                .containsExactlyElementsOf(conn.getData(NO_DATABASE, sample.validFlow, validQuery).getData());
    }

    private void checkInvalidFlow(SoftAssertions s, Sample sample, Connection conn) {
        for (Detail filter : Detail.values()) {
            Query validQuery = Query.builder().key(sample.validKey).detail(filter).build();

            s.assertThatThrownBy(() -> conn.getData(NO_DATABASE, sample.invalidFlow, validQuery))
                    .isInstanceOf(IOException.class);

            s.assertThatThrownBy(() -> conn.getDataStream(NO_DATABASE, sample.invalidFlow, validQuery))
                    .isInstanceOf(IOException.class);

            s.assertThatThrownBy(() -> conn.getFlow(NO_DATABASE, sample.invalidFlow))
                    .isInstanceOf(IOException.class);

            s.assertThatThrownBy(() -> conn.getStructure(NO_DATABASE, sample.invalidFlow))
                    .isInstanceOf(IOException.class);

            s.assertThatThrownBy(() -> conn.getAvailableDimensionCodes(NO_DATABASE, sample.invalidFlow, sample.validKey, 0))
                    .isInstanceOf(IOException.class);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void assertNonnull(SoftAssertions s, Connection conn, FlowRef ref) {
        s.assertThatThrownBy(() -> conn.getData(NO_DATABASE, null, Query.ALL))
                .as(nullDescriptionOf("getData(DataflowRef, DataQuery)", "flowRef"))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getData(NO_DATABASE, ref, null))
                .as(nullDescriptionOf("getData(DataflowRef, DataQuery)", "query"))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getDataStream(NO_DATABASE, null, Query.ALL))
                .as(nullDescriptionOf("getDataStream(DataflowRef, DataQuery)", "flowRef"))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getDataStream(NO_DATABASE, ref, null))
                .as(nullDescriptionOf("getDataStream(DataflowRef, DataQuery)", "query"))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getStructure(NO_DATABASE, null))
                .as(nullDescriptionOf("getStructure(DataflowRef)", "flowRef"))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getFlow(NO_DATABASE, null))
                .as(nullDescriptionOf("getFlow(DataflowRef)", "flowRef"))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getAvailableDimensionCodes(NO_DATABASE, null, Key.ALL, 0))
                .as(nullDescriptionOf("getAvailableDimensionValues(DataflowRef,Key,int)", "flowRef"))
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getAvailableDimensionCodes(NO_DATABASE, ref, null, 0))
                .as(nullDescriptionOf("getAvailableDimensionValues(DataflowRef,Key,int)", "key"))
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
