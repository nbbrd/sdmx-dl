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
package sdmxdl.file;

import java.io.File;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SdmxFileSetTest {

    @Test
    public void testAsDataflowRef() {
        assertThat(SdmxFileSet.builder().data(data).structure(structure).build().asDataflowRef().toString())
                .isEqualTo("all,data&struct,latest");

        assertThat(SdmxFileSet.builder().data(data).structure(new File("")).build().asDataflowRef().toString())
                .isEqualTo("all,data,latest");

        assertThat(SdmxFileSet.builder().data(data).build().asDataflowRef().toString())
                .isEqualTo("all,data,latest");
    }

    private final File data = new File("a.xml");
    private final File structure = new File("b.xml");
}
