/*
 * Copyright 2015 National Bank of Belgium
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
package sdmxdl.util.file;

import _test.sdmxdl.util.XRepoFileClient;
import org.junit.jupiter.api.Test;
import tests.sdmxdl.api.RepoSamples;
import tests.sdmxdl.api.ConnectionAssert;

import java.io.IOException;

/**
 * @author Philippe Charles
 */
public class SdmxFileConnectionImplTest {

    @Test
    public void testCompliance() throws IOException {
        ConnectionAssert.assertCompliance(
                () -> new FileConnectionImpl(new XRepoFileClient(RepoSamples.REPO), RepoSamples.FLOW),
                ConnectionAssert.Sample
                        .builder()
                        .validFlow(RepoSamples.FLOW_REF)
                        .invalidFlow(RepoSamples.BAD_FLOW_REF)
                        .validKey(RepoSamples.K1)
                        .invalidKey(RepoSamples.INVALID_KEY)
                        .build()
        );
    }
}
