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
package internal.web;

import _test.client.XFailingWebClient;
import _test.client.XRepoWebClient;
import _test.samples.FacadeResource;
import static _test.samples.FacadeResource.ECB_FLOW_REF;
import static _test.samples.FacadeResource.ECB_STRUCT_REF;
import be.nbb.sdmx.facade.DataFilter;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Key;
import ioutil.IO;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class FailsafeWebClientTest {

    @Test
    public void testGetFlows() {
        assertOperation(o -> FailsafeWebClient.of(o).getFlows());
    }

    @Test
    public void testGetFlow() {
        assertOperation(o -> FailsafeWebClient.of(o).getFlow(ECB_FLOW_REF));
    }

    @Test
    public void testGetStructure() {
        assertOperation(o -> FailsafeWebClient.of(o).getStructure(ECB_STRUCT_REF));
    }

    @Test
    public void testGetData() {
        DataRequest request = new DataRequest(ECB_FLOW_REF, Key.ALL, DataFilter.ALL);
        DataStructure struct = DataStructure.builder().ref(ECB_STRUCT_REF).label("hello").build();

        assertOperation(o -> FailsafeWebClient.of(o).getData(request, struct));
    }

    @Test
    public void testIsSeriesKeysOnlySupported() {
        IO.Consumer<SdmxWebClient> op = o -> FailsafeWebClient.of(o).isSeriesKeysOnlySupported();

        assertThatThrownBy(() -> op.acceptWithIO(XFailingWebClient.EXPECTED))
                .isInstanceOf(IOException.class);

        assertThatThrownBy(() -> op.acceptWithIO(XFailingWebClient.UNEXPECTED))
                .isInstanceOf(IOException.class)
                .hasCauseInstanceOf(RuntimeException.class);

        assertThatCode(() -> op.acceptWithIO(XRepoWebClient.of(FacadeResource.ecb())))
                .doesNotThrowAnyException();
    }

    @Test
    public void testPeekStructureRef() {
        IO.Consumer<SdmxWebClient> op = o -> FailsafeWebClient.of(o).peekStructureRef(ECB_FLOW_REF);

        assertThatThrownBy(() -> op.acceptWithIO(XFailingWebClient.EXPECTED))
                .isInstanceOf(IOException.class);

        assertThatThrownBy(() -> op.acceptWithIO(XFailingWebClient.UNEXPECTED))
                .isInstanceOf(IOException.class)
                .hasCauseInstanceOf(RuntimeException.class);

        assertThatCode(() -> op.acceptWithIO(XRepoWebClient.of(FacadeResource.ecb())))
                .doesNotThrowAnyException();
    }

    private static void assertOperation(IO.Consumer<SdmxWebClient> op) {
        assertThatThrownBy(() -> op.acceptWithIO(XFailingWebClient.EXPECTED))
                .isInstanceOf(IOException.class);

        assertThatThrownBy(() -> op.acceptWithIO(XFailingWebClient.UNEXPECTED))
                .isInstanceOf(IOException.class)
                .hasCauseInstanceOf(RuntimeException.class);

        assertThatThrownBy(() -> op.acceptWithIO(XFailingWebClient.NULL))
                .isInstanceOf(IOException.class)
                .hasCauseInstanceOf(NullPointerException.class);

        assertThatCode(() -> op.acceptWithIO(XRepoWebClient.of(FacadeResource.ecb())))
                .doesNotThrowAnyException();
    }
}
