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
package sdmxdl.format.xml;

import org.assertj.core.util.xml.XmlStringPrettyFormatter;
import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;
import sdmxdl.web.WebSources;
import tests.sdmxdl.api.RepoSamples;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

/**
 * @author Philippe Charles
 */
public class XmlWebSourcesFormatTest {

    private final List<WebSource> sample = Arrays.asList(RepoSamples.BASIC_SOURCE, RepoSamples.FULL_SOURCE);

    private final String stringSample
            = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<sources>\n"
            + "    <source>\n"
            + "        <name>ECB</name>\n"
            + "        <driver>ri:sdmx21</driver>\n"
            + "        <endpoint>https://data-api.ecb.europa.eu/service</endpoint>\n"
            + "    </source>\n"
            + "    <source>\n"
            + "        <name>ECB</name>\n"
            + "        <description lang=\"en\">European Central Bank</description>\n"
            + "        <driver>ri:sdmx21</driver>\n"
            + "        <confidentiality>SECRET</confidentiality>\n"
            + "        <endpoint>https://data-api.ecb.europa.eu/service</endpoint>\n"
            + "        <property key=\"detailSupported\" value=\"true\"/>\n"
            + "        <alias>XYZ</alias>\n"
            + "        <website>https://sdw.ecb.europa.eu</website>\n"
            + "        <monitor>ABC:xyz</monitor>\n"
            + "        <monitorWebsite>https://someaddress</monitorWebsite>\n"
            + "    </source>\n"
            + "</sources>\n";

    @Test
    public void testParser() throws IOException {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(stringSample.getBytes(UTF_8))) {
            assertThat(XmlWebSourcesFormat.INSTANCE.parseStream(stream))
                    .extracting(WebSources::getSources, list(WebSource.class))
                    .containsExactlyElementsOf(sample);
        }
    }

    @Test
    public void testFormatter() throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            XmlWebSourcesFormat.INSTANCE.formatStream(WebSources.builder().sources(sample).build(), stream);
            assertThat(XmlStringPrettyFormatter.xmlPrettyFormat(new String(stream.toByteArray(), UTF_8)))
                    .isEqualToIgnoringNewLines(stringSample);
        }
    }
}
