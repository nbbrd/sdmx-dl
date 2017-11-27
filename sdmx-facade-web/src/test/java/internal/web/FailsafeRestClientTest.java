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
import be.nbb.sdmx.facade.util.IO;
import be.nbb.sdmx.facade.util.UnexpectedIOException;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import test.FacadeResource;
import static test.FacadeResource.ECB_FLOW_REF;
import static test.FacadeResource.ECB_STRUCT_REF;
import test.client.FailingRestClient;
import test.client.NoOpRestClient;
import test.client.NullRestClient;
import test.client.RepoRestClient;

/**
 *
 * @author Philippe Charles
 */
public class FailsafeRestClientTest {

    @Test
    public void testGetFlows() {
        assertOperation(o -> FailsafeRestClient.of(o).getFlows());
    }

    @Test
    public void testGetFlow() {
        assertOperation(o -> FailsafeRestClient.of(o).getFlow(ECB_FLOW_REF));
    }

    @Test
    public void testGetStructure() {
        assertOperation(o -> FailsafeRestClient.of(o).getStructure(ECB_STRUCT_REF));
    }

    @Test
    public void testGetData() {
        DataQuery all = DataQuery.of(Key.ALL, false);
        DataStructure struct = DataStructure.builder().ref(ECB_STRUCT_REF).label("hello").build();

        assertOperation(o -> FailsafeRestClient.of(o).getData(ECB_FLOW_REF, struct, all));
    }

    @Test
    public void testIsSeriesKeysOnlySupported() {
        IO.Consumer<RestClient> op = o -> FailsafeRestClient.of(o).isSeriesKeysOnlySupported();

        assertThatThrownBy(() -> op.acceptWithIO(NoOpRestClient.INSTANCE))
                .isInstanceOf(IOException.class);

        assertThatThrownBy(() -> op.acceptWithIO(FailingRestClient.INSTANCE))
                .isInstanceOf(UnexpectedIOException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);

        assertThatCode(() -> op.acceptWithIO(RepoRestClient.of(FacadeResource.ecb())))
                .doesNotThrowAnyException();
    }

    @Test
    public void testPeekStructureRef() {
        IO.Consumer<RestClient> op = o -> FailsafeRestClient.of(o).peekStructureRef(ECB_FLOW_REF);

        assertThatThrownBy(() -> op.acceptWithIO(NoOpRestClient.INSTANCE))
                .isInstanceOf(IOException.class);

        assertThatThrownBy(() -> op.acceptWithIO(FailingRestClient.INSTANCE))
                .isInstanceOf(UnexpectedIOException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);

        assertThatCode(() -> op.acceptWithIO(RepoRestClient.of(FacadeResource.ecb())))
                .doesNotThrowAnyException();
    }

    private static void assertOperation(IO.Consumer<RestClient> op) {
        assertThatThrownBy(() -> op.acceptWithIO(NoOpRestClient.INSTANCE))
                .isInstanceOf(IOException.class);

        assertThatThrownBy(() -> op.acceptWithIO(FailingRestClient.INSTANCE))
                .isInstanceOf(UnexpectedIOException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> op.acceptWithIO(NullRestClient.INSTANCE))
                .isInstanceOf(UnexpectedIOException.class)
                .hasCauseInstanceOf(NullPointerException.class);

        assertThatCode(() -> op.acceptWithIO(RepoRestClient.of(FacadeResource.ecb())))
                .doesNotThrowAnyException();
    }
}
