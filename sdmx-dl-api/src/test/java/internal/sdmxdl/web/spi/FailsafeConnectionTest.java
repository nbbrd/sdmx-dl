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
import org.junit.jupiter.api.Test;
import sdmxdl.DataQuery;
import sdmxdl.Feature;
import sdmxdl.web.SdmxWebSource;
import tests.sdmxdl.api.ConnectionAssert;
import tests.sdmxdl.api.RepoSamples;
import tests.sdmxdl.web.spi.MockedDriver;
import tests.sdmxdl.web.spi.DriverAssert;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static sdmxdl.Languages.ANY;

/**
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public class FailsafeConnectionTest {

    @Test
    public void testCompliance() {
        MockedDriver driver = MockedDriver.builder().repo(RepoSamples.REPO, EnumSet.allOf(Feature.class)).build();
        SdmxWebSource source = driver.getDefaultSources().iterator().next();

        ConnectionAssert.assertCompliance(
                () -> FailsafeConnection.wrap(driver.connect(source, ANY, DriverAssert.noOpWebContext())),
                ConnectionAssert.Sample
                        .builder()
                        .validFlow(RepoSamples.FLOW_REF)
                        .invalidFlow(RepoSamples.BAD_FLOW_REF)
                        .validKey(RepoSamples.K1)
                        .invalidKey(RepoSamples.INVALID_KEY)
                        .build()
        );
    }

    @Test
    public void testTestConnection() {
        failsafe.reset();
        assertThatNoException()
                .isThrownBy(valid::testConnection);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(failing::testConnection)
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);
    }

    @Test
    public void testGetFlows() {
        failsafe.reset();
        assertThatNoException()
                .isThrownBy(valid::getFlows);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(failing::getFlows)
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(nul::getFlows)
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetFlow() {
        failsafe.reset();
        assertThatNoException()
                .isThrownBy(() -> valid.getFlow(RepoSamples.FLOW_REF));
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failing.getFlow(RepoSamples.FLOW_REF))
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> nul.getFlow(RepoSamples.FLOW_REF))
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetStructure() {
        failsafe.reset();
        assertThatNoException()
                .isThrownBy(() -> valid.getStructure(RepoSamples.FLOW_REF));
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failing.getStructure(RepoSamples.FLOW_REF))
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> nul.getStructure(RepoSamples.FLOW_REF))
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetData() {
        failsafe.reset();
        assertThatNoException()
                .isThrownBy(() -> valid.getData(RepoSamples.FLOW_REF, DataQuery.ALL));
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failing.getData(RepoSamples.FLOW_REF, DataQuery.ALL))
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> nul.getData(RepoSamples.FLOW_REF, DataQuery.ALL))
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetDataStream() {
        failsafe.reset();
        assertThatNoException()
                .isThrownBy(() -> valid.getDataStream(RepoSamples.FLOW_REF, DataQuery.ALL));
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> failing.getDataStream(RepoSamples.FLOW_REF, DataQuery.ALL))
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(() -> nul.getDataStream(RepoSamples.FLOW_REF, DataQuery.ALL))
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testGetSupportedFeatures() {
        failsafe.reset();
        assertThatNoException()
                .isThrownBy(valid::getSupportedFeatures);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(failing::getSupportedFeatures)
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(nul::getSupportedFeatures)
                .withNoCause();
        failsafe.assertUnexpectedNull("unexpected null");
    }

    @Test
    public void testClose() {
        failsafe.reset();
        assertThatNoException()
                .isThrownBy(valid::close);
        failsafe.assertEmpty();

        failsafe.reset();
        assertThatIOException()
                .isThrownBy(failing::close)
                .withCauseInstanceOf(CustomException.class);
        failsafe.assertUnexpectedError("unexpected CustomException", CustomException.class);
    }

    private final FailsafeHandler failsafe = new FailsafeHandler();

    private final FailsafeConnection valid = new FailsafeConnection(TestConnection.TEST_VALID, failsafe, failsafe);
    private final FailsafeConnection failing = new FailsafeConnection(TestConnection.TEST_FAILING, failsafe, failsafe);
    private final FailsafeConnection nul = new FailsafeConnection(TestConnection.TEST_NULL, failsafe, failsafe);
}
