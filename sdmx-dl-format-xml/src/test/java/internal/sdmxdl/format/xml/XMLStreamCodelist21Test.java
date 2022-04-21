package internal.sdmxdl.format.xml;

import nbbrd.io.xml.Xml;
import org.junit.jupiter.api.Test;
import sdmxdl.Codelist;
import sdmxdl.CodelistRef;
import sdmxdl.LanguagePriorityList;
import sdmxdl.format.xml.SdmxXmlStreams;
import tests.sdmxdl.format.xml.SdmxXmlSources;

import javax.xml.stream.XMLStreamException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

public class XMLStreamCodelist21Test {

    @Test
    public void test() throws Exception {
        Xml.Parser<List<Codelist>> parser = SdmxXmlStreams.codelist21(LanguagePriorityList.ANY);

        assertThat(parser.parseReader(SdmxXmlSources.ECB_DATA_STRUCTURE::openReader))
                .hasSize(9)
                .element(0)
                .satisfies(codelist -> {
                    assertThat(codelist.getRef())
                            .isEqualTo(CodelistRef.of("ECB", "CL_AME_AGG_METHOD", "1.0"));
                    assertThat(codelist.getCodes())
                            .hasSize(5)
                            .containsEntry("0", "Standard aggregation");
                });

        assertThatIOException()
                .isThrownBy(() -> parser.parseReader(SdmxXmlSources.NBB_DATA_STRUCTURE::openReader))
                .withCauseInstanceOf(XMLStreamException.class)
                .withMessageContaining("Invalid namespace");
    }
}
