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
package be.nbb.sdmx.facade.tck;

import internal.io.ConsumerWithIO;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.SdmxConnection;
import java.io.IOException;
import java.util.concurrent.Callable;
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
        }

        try (SdmxConnection conn = supplier.call()) {
            conn.close();
        } catch (Exception ex) {
            s.fail("Subsequent calls to #close must not raise exception", ex);
        }

        assertState(s, supplier, o -> o.getData(ref, Key.ALL, false), "getData(DataFlowRef, Key, boolean)");
        assertState(s, supplier, o -> o.getDataStructure(ref), "getDataStructure(DataFlowRef)");
        assertState(s, supplier, o -> o.getDataflow(ref), "getDataflow(DataFlowRef)");
        assertState(s, supplier, SdmxConnection::getDataflows, "getDataflows()");
    }

    @SuppressWarnings("null")
    private static void assertNonnull(SoftAssertions s, SdmxConnection conn, DataflowRef ref) {
        s.assertThatThrownBy(() -> conn.getData(null, Key.ALL, false))
                .as("Expecting 'getData(DataFlowRef, Key, boolean)' to raise NPE when called with null flowRef")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getData(ref, null, false))
                .as("Expecting 'getData(DataFlowRef, Key, boolean)' to raise NPE when called with null key")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getDataStructure(null))
                .as("Expecting 'getDataStructure(DataFlowRef)' to raise NPE when called with null flowRef")
                .isInstanceOf(NullPointerException.class);

        s.assertThatThrownBy(() -> conn.getDataflow(null))
                .as("Expecting 'getDataflow(DataFlowRef)' to raise NPE when called with null flowRef")
                .isInstanceOf(NullPointerException.class);
    }

    private static void assertState(SoftAssertions s, Callable<SdmxConnection> supplier, ConsumerWithIO<SdmxConnection> consumer, String expression) throws Exception {
        try (SdmxConnection conn = supplier.call()) {
            conn.close();
            s.assertThatThrownBy(() -> consumer.accept(conn))
                    .as("Expecting '%s' to raise IOException when called after close", expression)
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("closed");
        }
    }
}
