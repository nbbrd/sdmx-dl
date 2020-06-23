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
package sdmxdl.xml;

import org.junit.Test;
import sdmxdl.file.SdmxFileSource;

import java.io.File;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
public class XmlFileSourceTest {

    @Test
    public void testToXml() {
        assertThat(XmlFileSource.toXml(SdmxFileSource.builder().data(data).structure(structure).dialect("hello").build()))
                .isEqualTo("<file data=\"a.xml\" structure=\"b.xml\" dialect=\"hello\"/>");

        assertThat(XmlFileSource.toXml(SdmxFileSource.builder().data(data).structure(structure).build()))
                .isEqualTo("<file data=\"a.xml\" structure=\"b.xml\"/>");

        assertThat(XmlFileSource.toXml(SdmxFileSource.builder().data(data).build()))
                .isEqualTo("<file data=\"a.xml\"/>");
    }

    @Test
    @SuppressWarnings("null")
    public void testFromXml() {
        assertThatNullPointerException().isThrownBy(() -> XmlFileSource.fromXml(null));
        assertThatIllegalArgumentException().isThrownBy(() -> XmlFileSource.fromXml(""));
        assertThatIllegalArgumentException().isThrownBy(() -> XmlFileSource.fromXml("<file />"));
        assertThatIllegalArgumentException().isThrownBy(() -> XmlFileSource.fromXml("<file data=\"\" />"));

        assertThat(XmlFileSource.fromXml("<file data=\"a.xml\" />"))
                .hasFieldOrPropertyWithValue("data", data)
                .hasFieldOrPropertyWithValue("structure", null)
                .hasFieldOrPropertyWithValue("dialect", null);

        assertThat(XmlFileSource.fromXml("<file data=\"a.xml\" structure=\"\" />"))
                .hasFieldOrPropertyWithValue("data", data)
                .hasFieldOrPropertyWithValue("structure", null)
                .hasFieldOrPropertyWithValue("dialect", null);

        assertThat(XmlFileSource.fromXml("<file data=\"a.xml\" structure=\"b.xml\" />"))
                .hasFieldOrPropertyWithValue("data", data)
                .hasFieldOrPropertyWithValue("structure", structure)
                .hasFieldOrPropertyWithValue("dialect", null);

        assertThat(XmlFileSource.fromXml("<file data=\"a.xml\" structure=\"b.xml\" dialect=\"hello\" />"))
                .hasFieldOrPropertyWithValue("data", data)
                .hasFieldOrPropertyWithValue("structure", structure)
                .hasFieldOrPropertyWithValue("dialect", dialect);
    }

    private final File data = new File("a.xml");
    private final File structure = new File("b.xml");
    private final String dialect = "hello";
}
