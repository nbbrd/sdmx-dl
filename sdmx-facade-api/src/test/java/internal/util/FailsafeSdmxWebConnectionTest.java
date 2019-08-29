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
package internal.util;

import _test.CustomException;
import _test.FailsafeHandler;
import static _test.TestConnection.*;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public class FailsafeSdmxWebConnectionTest {

    @Test
    public void testPing() throws IOException {
        failsafe.reset();
        assertThat(validDriver.ping()).isEqualTo(PING);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failingDriver.ping())
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected exception", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> nullDriver.ping())
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetDriver() throws IOException {
        failsafe.reset();
        assertThat(validDriver.getDriver()).isEqualTo(DRIVER);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failingDriver.getDriver())
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected exception", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> nullDriver.getDriver())
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetFlows() throws IOException {
        failsafe.reset();
        assertThat(validDriver.getFlows()).isEqualTo(FLOWS);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failingDriver.getFlows())
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected exception", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> nullDriver.getFlows())
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetFlow() throws IOException {
        failsafe.reset();
        assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getFlow(null));
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(validDriver.getFlow(FLOW_REF)).isEqualTo(FLOW);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failingDriver.getFlow(FLOW_REF))
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected exception", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> nullDriver.getFlow(FLOW_REF))
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetStructure() throws IOException {
        failsafe.reset();
        assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getStructure(null));
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(validDriver.getStructure(FLOW_REF)).isEqualTo(STRUCT);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failingDriver.getStructure(FLOW_REF))
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected exception", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> nullDriver.getStructure(FLOW_REF))
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetData() throws IOException {
        failsafe.reset();
        assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getData(null, KEY, FILTER));
        assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getData(FLOW_REF, null, FILTER));
        assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getData(FLOW_REF, KEY, null));
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(validDriver.getData(FLOW_REF, KEY, FILTER)).isEqualTo(DATA);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failingDriver.getData(FLOW_REF, KEY, FILTER))
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected exception", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> nullDriver.getData(FLOW_REF, KEY, FILTER))
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetDataStream() throws IOException {
        failsafe.reset();
        assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getDataStream(null, KEY, FILTER));
        assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getDataStream(FLOW_REF, null, FILTER));
        assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getDataStream(FLOW_REF, KEY, null));
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(validDriver.getDataStream(FLOW_REF, KEY, FILTER)).isEqualTo(DATA);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failingDriver.getDataStream(FLOW_REF, KEY, FILTER))
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected exception", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> nullDriver.getDataStream(FLOW_REF, KEY, FILTER))
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetDataCursor() throws IOException {
        failsafe.reset();
        assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getDataCursor(null, KEY, FILTER));
        assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getDataCursor(FLOW_REF, null, FILTER));
        assertThatNullPointerException()
                .isThrownBy(() -> validDriver.getDataCursor(FLOW_REF, KEY, null));
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(validDriver.getDataCursor(FLOW_REF, KEY, FILTER)).isNotNull();
        failsafe.assertEmpty();
        
        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failingDriver.getDataCursor(FLOW_REF, KEY, FILTER))
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected exception", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> nullDriver.getDataCursor(FLOW_REF, KEY, FILTER))
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testIsSeriesKeysOnlySupported() throws IOException {
        failsafe.reset();
        assertThat(validDriver.isSeriesKeysOnlySupported()).isEqualTo(true);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failingDriver.isSeriesKeysOnlySupported())
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected exception", CustomException.class);
    }

    @Test
    public void testClose() throws IOException {
        failsafe.reset();
        assertThatCode(() -> validDriver.close()).doesNotThrowAnyException();
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failingDriver.close())
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected exception", CustomException.class);
    }

    private final FailsafeHandler failsafe = new FailsafeHandler();

    private final FailsafeSdmxWebConnection validDriver = new FailsafeSdmxWebConnection(VALID, failsafe, failsafe);
    private final FailsafeSdmxWebConnection failingDriver = new FailsafeSdmxWebConnection(FAILING, failsafe, failsafe);
    private final FailsafeSdmxWebConnection nullDriver = new FailsafeSdmxWebConnection(NULL, failsafe, failsafe);
}
