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
package be.nbb.sdmx.facade.xml.stream;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class XMLStreamUtilTest {

    @Test
    public void testIsTagMatch() {
        assertThat(XMLStreamUtil.isTagMatch("", "")).isTrue();
        assertThat(XMLStreamUtil.isTagMatch("HelloWorld", "HelloWorld")).isTrue();
        assertThat(XMLStreamUtil.isTagMatch("HelloWorld", "helloWorld")).isFalse();
        assertThat(XMLStreamUtil.isTagMatch("helloWorld", "HelloWorld")).isFalse();
        assertThat(XMLStreamUtil.isTagMatch("HelloWorld", "")).isFalse();
        assertThat(XMLStreamUtil.isTagMatch("", "HelloWorld")).isFalse();

        assertThat(XMLStreamUtil.isTagMatch("xxx:HelloWorld", "HelloWorld")).isTrue();
        assertThat(XMLStreamUtil.isTagMatch("xxx:HelloWorld", "helloWorld")).isFalse();
        assertThat(XMLStreamUtil.isTagMatch("xxx:HelloWorld", "Hello")).isFalse();
        assertThat(XMLStreamUtil.isTagMatch("xxx:HelloWorld", "World")).isFalse();
        assertThat(XMLStreamUtil.isTagMatch("xxx:HelloWorld", "")).isFalse();

        assertThat(XMLStreamUtil.isTagMatch("HelloWorld", "xxx:HelloWorld")).isFalse();
        assertThat(XMLStreamUtil.isTagMatch("helloWorld", "xxx:HelloWorld")).isFalse();
        assertThat(XMLStreamUtil.isTagMatch("Hello", "xxx:HelloWorld")).isFalse();
        assertThat(XMLStreamUtil.isTagMatch("World", "xxx:HelloWorld")).isFalse();
        assertThat(XMLStreamUtil.isTagMatch("", "xxx:HelloWorld")).isFalse();
    }

}
