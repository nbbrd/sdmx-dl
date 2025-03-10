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

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class SdmxFileSourceTest {

    @Test
    public void testAsDataflowRef() {
        assertThat(FileSource.builder().data(data).structure(structure).build().asDataflowRef().toString())
                .isEqualTo("all,data&struct,latest");

        assertThat(FileSource.builder().data(data).structure(Paths.get("").toFile()).build().asDataflowRef().toString())
                .isEqualTo("all,data,latest");

        assertThat(FileSource.builder().data(data).build().asDataflowRef().toString())
                .isEqualTo("all,data,latest");
    }

    private final File data = Paths.get("a.xml").toFile();
    private final File structure = Paths.get("b.xml").toFile();
}
