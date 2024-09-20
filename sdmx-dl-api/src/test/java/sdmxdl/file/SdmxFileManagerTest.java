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
import sdmxdl.Languages;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * @author Philippe Charles
 */
public class SdmxFileManagerTest {

//    @Test
//    public void testCompliance() throws IOException {
//        File compact21 = temp.newFile();
//        SdmxSource.OTHER_COMPACT21.copyTo(compact21);
//
//        SdmxFileSource source = SdmxFileSource.builder().data(compact21).build();
//
//        ConnectionSupplierAssert.assertCompliance(SdmxFileManager.ofServiceLoader(), XmlFileSet.toXml(files), "ko");
//    }

    @Test
    @SuppressWarnings({"null", "ConstantConditions"})
    public void test() {
        SdmxFileManager m = SdmxFileManager.ofServiceLoader();
        assertThatNullPointerException().isThrownBy(() -> m.getConnection(null, Languages.ANY));
    }

    private final FileSource source = FileSource.builder().data(new File("hello")).build();
}
