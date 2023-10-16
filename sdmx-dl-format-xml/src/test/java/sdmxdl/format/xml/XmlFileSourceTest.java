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
package sdmxdl.format.xml;

import nbbrd.io.xml.Xml;
import org.junit.jupiter.api.Test;
import sdmxdl.file.FileSource;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
public class XmlFileSourceTest {

    @Test
    public void testFormatter() throws IOException {
        Xml.Formatter<FileSource> x = XmlFileSource.getFormatter();

        assertThat(x.formatToString(FileSource.builder().data(data).structure(structure).build()))
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?><file data=\"a.xml\" structure=\"b.xml\"/>");

        assertThat(x.formatToString(FileSource.builder().data(data).build()))
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?><file data=\"a.xml\"/>");
    }

    @Test
    @SuppressWarnings("null")
    public void testParser() throws IOException {
        Xml.Parser<FileSource> x = XmlFileSource.getParser();

        assertThatNullPointerException().isThrownBy(() -> x.parseChars(null));
        assertThatIOException().isThrownBy(() -> x.parseChars(""));
        assertThatIOException().isThrownBy(() -> x.parseChars("<file />"));
        assertThatIOException().isThrownBy(() -> x.parseChars("<file data=\"\" />"));

        assertThat(x.parseChars("<file data=\"a.xml\" />"))
                .hasFieldOrPropertyWithValue("data", data)
                .hasFieldOrPropertyWithValue("structure", null);

        assertThat(x.parseChars("<file data=\"a.xml\" structure=\"\" />"))
                .hasFieldOrPropertyWithValue("data", data)
                .hasFieldOrPropertyWithValue("structure", null);

        assertThat(x.parseChars("<file data=\"a.xml\" structure=\"b.xml\" />"))
                .hasFieldOrPropertyWithValue("data", data)
                .hasFieldOrPropertyWithValue("structure", structure);
    }

    private final File data = new File("a.xml");
    private final File structure = new File("b.xml");
}
