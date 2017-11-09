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

import internal.file.SdmxFileUtil;
import java.io.File;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SdmxFileSetTest {

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThatThrownBy(() -> SdmxFileSet.of(null, null)).isInstanceOf(NullPointerException.class);

        assertThat(SdmxFileSet.of(data, null))
                .hasFieldOrPropertyWithValue("data", data)
                .hasFieldOrPropertyWithValue("structure", null);

        assertThat(SdmxFileSet.of(data, structure))
                .hasFieldOrPropertyWithValue("data", data)
                .hasFieldOrPropertyWithValue("structure", structure);
    }

    @Test
    public void testToString() {
        assertThat(SdmxFileUtil.toXml(SdmxFileSet.of(data, structure)))
                .isEqualTo("<file data=\"a.xml\" structure=\"b.xml\"/>");

        assertThat(SdmxFileUtil.toXml(SdmxFileSet.of(data, null)))
                .isEqualTo("<file data=\"a.xml\"/>");
    }

    @Test
    @SuppressWarnings("null")
    public void testParse() {
        assertThatThrownBy(() -> SdmxFileUtil.fromXml(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SdmxFileUtil.fromXml("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SdmxFileUtil.fromXml("<file />")).isInstanceOf(IllegalArgumentException.class);

        assertThat(SdmxFileUtil.fromXml("<file data=\"\" />"))
                .hasFieldOrPropertyWithValue("data", new File(""))
                .hasFieldOrPropertyWithValue("structure", null);

        assertThat(SdmxFileUtil.fromXml("<file data=\"a.xml\" />"))
                .hasFieldOrPropertyWithValue("data", data)
                .hasFieldOrPropertyWithValue("structure", null);

        assertThat(SdmxFileUtil.fromXml("<file data=\"a.xml\" structure=\"b.xml\" />"))
                .hasFieldOrPropertyWithValue("data", data)
                .hasFieldOrPropertyWithValue("structure", structure);
    }

    private final File data = new File("a.xml");
    private final File structure = new File("b.xml");
}
