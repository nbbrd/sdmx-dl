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

import be.nbb.sdmx.facade.repo.SdmxRepository;
import be.nbb.sdmx.facade.tck.ConnectionAssert;
import java.io.IOException;
import org.junit.Test;
import _test.samples.FacadeResource;
import _test.client.RepoWebClient;

/**
 *
 * @author Philippe Charles
 */
public class WebConnectionTest {

    @Test
    public void testCompliance() throws IOException {
        SdmxRepository repo = FacadeResource.ecb();
        ConnectionAssert.assertCompliance(() -> WebConnection.of(RepoWebClient.of(repo)), FacadeResource.ECB_FLOW_REF);
    }
}
