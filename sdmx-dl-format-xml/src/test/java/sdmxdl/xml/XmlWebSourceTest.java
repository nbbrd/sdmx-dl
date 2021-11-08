/*
 * Copyright 2018 National Bank of Belgium
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

import org.assertj.core.util.xml.XmlStringPrettyFormatter;
import org.junit.jupiter.api.Test;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class XmlWebSourceTest {

    private final List<SdmxWebSource> sample = Arrays.asList(
            SdmxWebSource
                    .builder()
                    .name("ECB")
                    .description("European Central Bank")
                    .driver("ri:sdmx21")
                    .dialect("SDMX21")
                    .uriOf("https://sdw-wsrest.ecb.europa.eu/service")
                    .property("detailSupported", "true")
                    .alias("XYZ")
                    .websiteOf("https://sdw.ecb.europa.eu")
                    .monitorOf("ABC", "xyz")
                    .build(),
            SdmxWebSource
                    .builder()
                    .name("other")
                    .driver("dummy")
                    .uriOf("http://localhost")
                    .build()
    );

    private final String stringSample
            = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<sources>\n"
            + "    <source>\n"
            + "        <name>ECB</name>\n"
            + "        <description>European Central Bank</description>\n"
            + "        <driver>ri:sdmx21</driver>\n"
            + "        <dialect>SDMX21</dialect>\n"
            + "        <endpoint>https://sdw-wsrest.ecb.europa.eu/service</endpoint>\n"
            + "        <property key=\"detailSupported\" value=\"true\"/>\n"
            + "        <alias>XYZ</alias>\n"
            + "        <website>https://sdw.ecb.europa.eu</website>\n"
            + "        <monitor id=\"xyz\" provider=\"ABC\"/>\n"
            + "    </source>\n"
            + "    <source>\n"
            + "        <name>other</name>\n"
            + "        <driver>dummy</driver>\n"
            + "        <endpoint>http://localhost</endpoint>\n"
            + "    </source>\n"
            + "</sources>\n";

    @Test
    public void testParser() throws IOException {
        assertThat(XmlWebSource.getParser().parseChars(stringSample))
                .containsExactlyElementsOf(sample);
    }

    @Test
    public void testFormatter() throws IOException {
        assertThat(XmlStringPrettyFormatter.xmlPrettyFormat(XmlWebSource.getFormatter().formatToString(sample)))
                .isEqualToIgnoringNewLines(stringSample);
    }
}
