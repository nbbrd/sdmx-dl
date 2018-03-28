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

import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.util.UnexpectedIOException;
import ioutil.IO;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import _test.samples.FacadeResource;
import static _test.samples.FacadeResource.ECB_FLOW_REF;
import static _test.samples.FacadeResource.ECB_STRUCT_REF;
import _test.client.FailingWebClient;
import _test.client.NoOpWebClient;
import _test.client.NullWebClient;
import _test.client.RepoWebClient;

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
        DataQuery all = DataQuery.of(Key.ALL, false);
        DataStructure struct = DataStructure.builder().ref(ECB_STRUCT_REF).label("hello").build();

        assertOperation(o -> FailsafeWebClient.of(o).getData(ECB_FLOW_REF, all, struct));
    }

    @Test
    public void testIsSeriesKeysOnlySupported() {
        IO.Consumer<SdmxWebClient> op = o -> FailsafeWebClient.of(o).isSeriesKeysOnlySupported();

        assertThatThrownBy(() -> op.acceptWithIO(NoOpWebClient.INSTANCE))
                .isInstanceOf(IOException.class);

        assertThatThrownBy(() -> op.acceptWithIO(FailingWebClient.INSTANCE))
                .isInstanceOf(UnexpectedIOException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);

        assertThatCode(() -> op.acceptWithIO(RepoWebClient.of(FacadeResource.ecb())))
                .doesNotThrowAnyException();
    }

    @Test
    public void testPeekStructureRef() {
        IO.Consumer<SdmxWebClient> op = o -> FailsafeWebClient.of(o).peekStructureRef(ECB_FLOW_REF);

        assertThatThrownBy(() -> op.acceptWithIO(NoOpWebClient.INSTANCE))
                .isInstanceOf(IOException.class);

        assertThatThrownBy(() -> op.acceptWithIO(FailingWebClient.INSTANCE))
                .isInstanceOf(UnexpectedIOException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);

        assertThatCode(() -> op.acceptWithIO(RepoWebClient.of(FacadeResource.ecb())))
                .doesNotThrowAnyException();
    }

    private static void assertOperation(IO.Consumer<SdmxWebClient> op) {
        assertThatThrownBy(() -> op.acceptWithIO(NoOpWebClient.INSTANCE))
                .isInstanceOf(IOException.class);

        assertThatThrownBy(() -> op.acceptWithIO(FailingWebClient.INSTANCE))
                .isInstanceOf(UnexpectedIOException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> op.acceptWithIO(NullWebClient.INSTANCE))
                .isInstanceOf(UnexpectedIOException.class)
                .hasCauseInstanceOf(NullPointerException.class);

        assertThatCode(() -> op.acceptWithIO(RepoWebClient.of(FacadeResource.ecb())))
                .doesNotThrowAnyException();
    }
}
