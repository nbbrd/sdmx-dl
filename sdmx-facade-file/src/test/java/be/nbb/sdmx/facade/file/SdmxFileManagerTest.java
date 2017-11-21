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
package be.nbb.sdmx.facade.file;

import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.samples.SdmxSource;
import be.nbb.sdmx.facade.tck.ConnectionSupplierAssert;
import internal.file.SdmxFileUtil;
import java.io.File;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public class SdmxFileManagerTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testCompliance() throws IOException {
        File compact21 = temp.newFile();
        SdmxSource.OTHER_COMPACT21.copyTo(compact21);

        SdmxFileSet files = SdmxFileSet.builder().data(compact21).build();

        ConnectionSupplierAssert.assertCompliance(SdmxFileManager.ofServiceLoader(), SdmxFileUtil.toXml(files), "ko");
    }

    @Test
    @SuppressWarnings("null")
    public void test() {
        SdmxFileManager m = SdmxFileManager.ofServiceLoader();
        assertThatThrownBy(() -> m.getConnection((String) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> m.getConnection((String) null, LanguagePriorityList.ANY)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> m.getConnection(SdmxFileUtil.toXml(files), null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> m.getConnection((SdmxFileSet) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> m.getConnection(files, null)).isInstanceOf(NullPointerException.class);
    }

    private final SdmxFileSet files = SdmxFileSet.builder().data(new File("hello")).build();
}
