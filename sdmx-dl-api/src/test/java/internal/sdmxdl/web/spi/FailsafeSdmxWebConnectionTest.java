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
package internal.sdmxdl.web.spi;

import _test.sdmxdl.CustomException;
import _test.sdmxdl.FailsafeHandler;
import _test.sdmxdl.TestConnection;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sdmxdl.DataRef;

import java.io.IOException;

/**
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public class FailsafeSdmxWebConnectionTest {

    @Test
    public void testPing() throws IOException {
        failsafe.reset();
        Assertions.assertThat(validDriver.ping()).isEqualTo(TestConnection.PING);
        failsafe.assertEmpty();

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(failingDriver::ping)
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(nullDriver::ping)
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetDriver() throws IOException {
        failsafe.reset();
        Assertions.assertThat(validDriver.getDriver()).isEqualTo(TestConnection.DRIVER);
        failsafe.assertEmpty();

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(failingDriver::getDriver)
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(nullDriver::getDriver)
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetFlows() throws IOException {
        failsafe.reset();
        Assertions.assertThat(validDriver.getFlows()).isEqualTo(TestConnection.FLOWS);
        failsafe.assertEmpty();

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(failingDriver::getFlows)
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(nullDriver::getFlows)
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetFlow() throws IOException {
        failsafe.reset();
        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getFlow(null));
        failsafe.assertEmpty();

        failsafe.reset();
        Assertions.assertThat(validDriver.getFlow(TestConnection.FLOW_REF)).isEqualTo(TestConnection.FLOW);
        failsafe.assertEmpty();

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(() -> failingDriver.getFlow(TestConnection.FLOW_REF))
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(() -> nullDriver.getFlow(TestConnection.FLOW_REF))
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetStructure() throws IOException {
        failsafe.reset();
        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getStructure(null));
        failsafe.assertEmpty();

        failsafe.reset();
        Assertions.assertThat(validDriver.getStructure(TestConnection.FLOW_REF)).isEqualTo(TestConnection.STRUCT);
        failsafe.assertEmpty();

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(() -> failingDriver.getStructure(TestConnection.FLOW_REF))
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(() -> nullDriver.getStructure(TestConnection.FLOW_REF))
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetData() throws IOException {
        failsafe.reset();
        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getData(null));
        failsafe.assertEmpty();

        failsafe.reset();
        Assertions.assertThat(validDriver.getData(dataRef)).isEqualTo(TestConnection.DATA);
        failsafe.assertEmpty();

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(() -> failingDriver.getData(dataRef))
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(() -> nullDriver.getData(dataRef))
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetDataStream() throws IOException {
        failsafe.reset();
        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getDataStream(null));
        failsafe.assertEmpty();

        failsafe.reset();
        Assertions.assertThat(validDriver.getDataStream(dataRef))
                .containsExactlyElementsOf(TestConnection.DATA);
        failsafe.assertEmpty();

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(() -> failingDriver.getDataStream(dataRef))
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(() -> nullDriver.getDataStream(dataRef))
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testIsDetailSupported() throws IOException {
        failsafe.reset();
        Assertions.assertThat(validDriver.isDetailSupported()).isEqualTo(true);
        failsafe.assertEmpty();

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(failingDriver::isDetailSupported)
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);
    }

    @Test
    public void testClose() {
        failsafe.reset();
        Assertions.assertThatCode(validDriver::close).doesNotThrowAnyException();
        failsafe.assertEmpty();

        failsafe.reset();
        Assertions.assertThatIOException()
                .isThrownBy(failingDriver::close)
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);
    }

    private final FailsafeHandler failsafe = new FailsafeHandler();

    private final FailsafeSdmxWebConnection validDriver = new FailsafeSdmxWebConnection(TestConnection.VALID, failsafe, failsafe);
    private final FailsafeSdmxWebConnection failingDriver = new FailsafeSdmxWebConnection(TestConnection.FAILING, failsafe, failsafe);
    private final FailsafeSdmxWebConnection nullDriver = new FailsafeSdmxWebConnection(TestConnection.NULL, failsafe, failsafe);

    private final DataRef dataRef = DataRef.of(TestConnection.FLOW_REF, TestConnection.KEY, TestConnection.FILTER);
}
