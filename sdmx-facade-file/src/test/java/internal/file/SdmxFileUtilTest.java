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
package internal.file;

import be.nbb.sdmx.facade.file.SdmxFileSet;
import java.io.File;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SdmxFileUtilTest {

    @Test
    public void testAsDataflowRef() {
        assertThat(SdmxFileUtil.asDataflowRef(SdmxFileSet.of(data, structure)).toString())
                .isEqualTo("all,data&struct,latest");

        assertThat(SdmxFileUtil.asDataflowRef(SdmxFileSet.of(data, new File(""))).toString())
                .isEqualTo("all,data,latest");

        assertThat(SdmxFileUtil.asDataflowRef(SdmxFileSet.of(data, null)).toString())
                .isEqualTo("all,data,latest");
    }

    @Test
    public void testToXml() {
        assertThat(SdmxFileUtil.toXml(SdmxFileSet.of(data, structure)))
                .isEqualTo("<file data=\"a.xml\" structure=\"b.xml\"/>");

        assertThat(SdmxFileUtil.toXml(SdmxFileSet.of(data, null)))
                .isEqualTo("<file data=\"a.xml\"/>");
    }

    @Test
    @SuppressWarnings("null")
    public void testFromXml() {
        assertThatThrownBy(() -> SdmxFileUtil.fromXml(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SdmxFileUtil.fromXml("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SdmxFileUtil.fromXml("<file />")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SdmxFileUtil.fromXml("<file data=\"\" />")).isInstanceOf(IllegalArgumentException.class);

        assertThat(SdmxFileUtil.fromXml("<file data=\"a.xml\" />"))
                .hasFieldOrPropertyWithValue("data", data)
                .hasFieldOrPropertyWithValue("structure", null);

        assertThat(SdmxFileUtil.fromXml("<file data=\"a.xml\" structure=\"\" />"))
                .hasFieldOrPropertyWithValue("data", data)
                .hasFieldOrPropertyWithValue("structure", null);

        assertThat(SdmxFileUtil.fromXml("<file data=\"a.xml\" structure=\"b.xml\" />"))
                .hasFieldOrPropertyWithValue("data", data)
                .hasFieldOrPropertyWithValue("structure", structure);
    }

    private final File data = new File("a.xml");
    private final File structure = new File("b.xml");
}
