package sdmxdl.xml.stream;

import nbbrd.io.xml.Xml;
import org.junit.jupiter.api.Test;
import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.MessageFooter;
import sdmxdl.samples.SdmxSource;

import javax.xml.stream.XMLStreamException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

public class XMLStreamMessageFooter21Test {

    @Test
    public void test() throws Exception {
        Xml.Parser<MessageFooter> parser = SdmxXmlStreams.messageFooter21(LanguagePriorityList.ANY);

        assertThat(parser.parseResource(XMLStreamMessageFooter21Test.class, "MessageFooter.xml"))
                .satisfies(msg -> {
                    assertThat(msg.getCode()).isEqualTo(413);
                    assertThat(msg.getSeverity()).isEqualTo("Infomation");
                    assertThat(msg.getTexts())
                            .hasSize(2)
                            .contains("Response too large due to client request");
                });

        assertThatIOException()
                .isThrownBy(() -> parser.parseReader(SdmxSource.NBB_DATA_STRUCTURE::openReader))
                .withCauseInstanceOf(XMLStreamException.class)
                .withMessageContaining("Invalid namespace");
    }
}