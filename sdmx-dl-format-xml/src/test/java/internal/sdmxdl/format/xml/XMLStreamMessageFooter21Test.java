package internal.sdmxdl.format.xml;

import nbbrd.io.xml.Xml;
import org.junit.jupiter.api.Test;
import sdmxdl.Languages;
import sdmxdl.format.MessageFooter;
import sdmxdl.format.xml.SdmxXmlStreams;
import tests.sdmxdl.format.xml.SdmxXmlSources;

import javax.xml.stream.XMLStreamException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

public class XMLStreamMessageFooter21Test {

    @Test
    public void test() throws Exception {
        Xml.Parser<MessageFooter> parser = SdmxXmlStreams.messageFooter21(Languages.ANY);

        assertThat(parser.parseResource(XMLStreamMessageFooter21Test.class, "MessageFooter.xml"))
                .satisfies(msg -> {
                    assertThat(msg.getCode()).isEqualTo(413);
                    assertThat(msg.getSeverity()).isEqualTo("Infomation");
                    assertThat(msg.getTexts())
                            .hasSize(2)
                            .contains("Response too large due to client request");
                });

        assertThatIOException()
                .isThrownBy(() -> parser.parseReader(SdmxXmlSources.NBB_DATA_STRUCTURE::openReader))
                .withCauseInstanceOf(XMLStreamException.class)
                .withMessageContaining("Invalid namespace");
    }
}
