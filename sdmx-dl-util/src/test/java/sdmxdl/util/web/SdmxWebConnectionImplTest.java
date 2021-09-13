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
package sdmxdl.util.web;

import _test.sdmxdl.util.XRepoWebClient;
import org.junit.Test;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.samples.RepoSamples;
import sdmxdl.tck.SdmxConnectionAssert;

/**
 * @author Philippe Charles
 */
public class SdmxWebConnectionImplTest {

    @Test
    public void testCompliance() {
        SdmxRepository repo = RepoSamples.REPO;
        SdmxConnectionAssert.assertCompliance(
                () -> SdmxWebConnectionImpl.of(XRepoWebClient.of(repo), ""),
                SdmxConnectionAssert.Sample
                        .builder()
                        .validFlow(RepoSamples.GOOD_FLOW_REF)
                        .invalidFlow(RepoSamples.BAD_FLOW_REF)
                        .validKey(RepoSamples.K1)
                        .invalidKey(RepoSamples.INVALID_KEY)
                        .build()
        );
    }
}
