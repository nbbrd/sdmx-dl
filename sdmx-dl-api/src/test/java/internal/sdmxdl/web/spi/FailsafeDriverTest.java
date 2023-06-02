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
import _test.sdmxdl.TestDriver;
import org.junit.jupiter.api.Test;
import sdmxdl.web.spi.WebDriver;
import tests.sdmxdl.web.MockedDriver;
import tests.sdmxdl.web.WebDriverAssert;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

/**
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public class FailsafeDriverTest {

    @Test
    public void testCompliance() {
        WebDriverAssert.assertCompliance(
                FailsafeDriver.wrap(MockedDriver.builder().build())
        );
    }

    @Test
    public void testGetName() {
        failsafe.reset();
        assertThat(valid.getId()).isEqualTo("valid");
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(failing.getId()).isEqualTo(TestDriver.FAILING.getClass().getName());
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        assertThat(nul.getId()).isEqualTo(TestDriver.NULL.getClass().getName());
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetRank() {
        failsafe.reset();
        assertThat(valid.getRank()).isEqualTo(WebDriver.NATIVE_RANK);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(failing.getRank()).isEqualTo(WebDriver.UNKNOWN);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);
    }

    @Test
    public void testIsAvailable() {
        failsafe.reset();
        assertThat(valid.isAvailable()).isTrue();
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(failing.isAvailable()).isFalse();
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);
    }

    @Test
    public void testConnect() throws IOException {
        failsafe.reset();
        assertThat(valid.connect(TestDriver.SOURCE, WebDriverAssert.noOpWebContext()))
                .isNotNull()
                .isInstanceOf(FailsafeConnection.class);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failing.connect(TestDriver.SOURCE, WebDriverAssert.noOpWebContext()))
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> nul.connect(TestDriver.SOURCE, WebDriverAssert.noOpWebContext()))
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetDefaultSources() {
        failsafe.reset();
        assertThat(valid.getDefaultSources()).containsExactly(TestDriver.SOURCE);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(failing.getDefaultSources()).isEmpty();
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        assertThat(nul.getDefaultSources()).isEmpty();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetSupportedProperties() {
        failsafe.reset();
        assertThat(valid.getSupportedProperties()).containsExactly("hello");
        failsafe.assertEmpty();

        failsafe.reset();
        assertThat(failing.getSupportedProperties()).isEmpty();
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        assertThat(nul.getSupportedProperties()).isEmpty();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    private final FailsafeHandler failsafe = new FailsafeHandler();

    private final FailsafeDriver valid = new FailsafeDriver(TestDriver.VALID, failsafe, failsafe);
    private final FailsafeDriver failing = new FailsafeDriver(TestDriver.FAILING, failsafe, failsafe);
    private final FailsafeDriver nul = new FailsafeDriver(TestDriver.NULL, failsafe, failsafe);
}
