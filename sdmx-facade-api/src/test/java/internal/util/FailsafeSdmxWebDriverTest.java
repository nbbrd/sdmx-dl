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

import _test.TestDriver;
import _test.CustomException;
import _test.FailsafeHandler;
import be.nbb.sdmx.facade.web.spi.SdmxWebContext;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public class FailsafeSdmxWebDriverTest {

    @Test
    public void testGetName() {
        failsafe.reset();
        assertThat(validDriver.getName()).isEqualTo("valid");
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(failingDriver.getName()).isEqualTo(TestDriver.FAILING.getClass().getName());
        failsafe.assertUnexpectedError("unexpected exception", CustomException.class);

        failsafe.reset();
        assertThat(nullDriver.getName()).isEqualTo(TestDriver.NULL.getClass().getName());
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetRank() {
        failsafe.reset();
        assertThat(validDriver.getRank()).isEqualTo(SdmxWebDriver.NATIVE_RANK);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(failingDriver.getRank()).isEqualTo(SdmxWebDriver.UNKNOWN);
        failsafe.assertUnexpectedError("unexpected exception", CustomException.class);
    }

    @Test
    public void testConnect() throws IOException {
        failsafe.reset();
        assertThatNullPointerException()
                .isThrownBy(() -> validDriver.connect(null, context));
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatNullPointerException()
                .isThrownBy(() -> validDriver.connect(TestDriver.SOURCE, null));
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(validDriver.connect(TestDriver.SOURCE, context))
                .isNotNull()
                .isInstanceOf(FailsafeSdmxWebConnection.class);
        failsafe.assertEmpty();
    }

    @Test
    public void testGetDefaultSources() {
        failsafe.reset();
        assertThat(validDriver.getDefaultSources()).containsExactly(TestDriver.SOURCE);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(failingDriver.getDefaultSources()).isEmpty();
        failsafe.assertUnexpectedError("unexpected exception", CustomException.class);

        failsafe.reset();
        assertThat(nullDriver.getDefaultSources()).isEmpty();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetSupportedProperties() {
        failsafe.reset();
        assertThat(validDriver.getSupportedProperties()).containsExactly("hello");
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(failingDriver.getSupportedProperties()).isEmpty();
        failsafe.assertUnexpectedError("unexpected exception", CustomException.class);

        failsafe.reset();
        assertThat(nullDriver.getSupportedProperties()).isEmpty();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    private final FailsafeHandler failsafe = new FailsafeHandler();

    private final FailsafeSdmxWebDriver validDriver = new FailsafeSdmxWebDriver(TestDriver.VALID, failsafe, failsafe);
    private final FailsafeSdmxWebDriver failingDriver = new FailsafeSdmxWebDriver(TestDriver.FAILING, failsafe, failsafe);
    private final FailsafeSdmxWebDriver nullDriver = new FailsafeSdmxWebDriver(TestDriver.NULL, failsafe, failsafe);

    private final SdmxWebContext context = SdmxWebContext.builder().build();
}
