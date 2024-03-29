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
package sdmxdl.provider.web;

import _test.sdmxdl.util.XRepoRestClient;
import org.junit.jupiter.api.Test;
import sdmxdl.DataRepository;
import sdmxdl.provider.Validator;
import tests.sdmxdl.api.ConnectionAssert;
import tests.sdmxdl.api.RepoSamples;

/**
 * @author Philippe Charles
 */
public class SdmxRestConnectionTest {

    @Test
    public void testCompliance() {
        DataRepository repo = RepoSamples.REPO;
        ConnectionAssert.assertCompliance(
                () -> RestConnection.of(XRepoRestClient.of(repo), Validator.noOp(), false),
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
