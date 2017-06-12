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
package be.nbb.sdmx.facade.connectors;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ParsersTest {

    @Test
    public void test() throws Exception {
        assertThat(ConnectorsResource.nbb()).isEqualTo(FacadeResource.nbb());

//        assertThat(ConnectorsResource.ecb()).isEqualTo(FacadeResource.ecb());
//        DataStructure l = ConnectorsResource.ecb().getDataStructures().get(0);
//        DataStructure r = FacadeResource.ecb().getDataStructures().get(0);
//        assertThat(l.getDimensions()).isEqualTo(r.getDimensions());
    }
}
