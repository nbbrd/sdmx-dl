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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class StaxTest {

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

    @Test
    @SuppressWarnings("null")
    public void testValidParseStream() throws IOException {
        Stax.Parser<String> p = (o, c) -> {
            c.close();
            return "";
        };

        assertThatThrownBy(() -> p.parseStream(null, EmptyStream::new, UTF_8))
                .isInstanceOf(NullPointerException.class)
                .hasNoSuppressedExceptions()
                .hasNoCause();

        assertThatThrownBy(() -> p.parseStream(xif, null, UTF_8))
                .isInstanceOf(NullPointerException.class)
                .hasNoSuppressedExceptions()
                .hasNoCause();

        assertThatThrownBy(() -> p.parseStream(xif, OpenErrorStream::new, UTF_8))
                .isInstanceOf(OpenError.class)
                .hasNoSuppressedExceptions()
                .hasNoCause();

        assertThatThrownBy(() -> p.parseStream(xif, ReadErrorStream::new, UTF_8))
                .isInstanceOf(Stax.XMLStreamIOException.class)
                .hasNoSuppressedExceptions()
                .hasCauseInstanceOf(XMLStreamException.class)
                .hasRootCauseInstanceOf(ReadError.class);

        assertThatThrownBy(() -> p.parseStream(xif, CloseErrorStream::new, UTF_8))
                .isInstanceOf(CloseError.class)
                .hasNoSuppressedExceptions()
                .hasNoCause();
    }

    @Test
    @SuppressWarnings("null")
    public void testInvalidParseStream() throws IOException {
        Stax.Parser<String> p = (o, c) -> {
            try (Closeable x = c) {
                throw new ParseError();
            }
        };

        assertThatThrownBy(() -> p.parseStream(null, EmptyStream::new, UTF_8))
                .isInstanceOf(NullPointerException.class)
                .hasNoSuppressedExceptions()
                .hasNoCause();

        assertThatThrownBy(() -> p.parseStream(xif, null, UTF_8))
                .isInstanceOf(NullPointerException.class)
                .hasNoSuppressedExceptions()
                .hasNoCause();

        assertThatThrownBy(() -> p.parseStream(xif, OpenErrorStream::new, UTF_8))
                .isInstanceOf(OpenError.class)
                .hasNoSuppressedExceptions()
                .hasNoCause();

        assertThatThrownBy(() -> p.parseStream(xif, ReadErrorStream::new, UTF_8))
                .isInstanceOf(Stax.XMLStreamIOException.class)
                .hasNoSuppressedExceptions()
                .hasCauseInstanceOf(XMLStreamException.class)
                .hasRootCauseInstanceOf(ReadError.class);

        assertThatThrownBy(() -> p.parseStream(xif, CloseErrorStream::new, UTF_8))
                .isInstanceOf(ParseError.class)
                .hasSuppressedException(new CloseError())
                .hasNoCause();
    }

    private final XMLInputFactory xif = Stax.getInputFactory();

    private static class EmptyStream extends InputStream {

        @Override
        public int read() throws IOException {
            return -1;
        }
    }

    private static final class OpenErrorStream extends EmptyStream {

        public OpenErrorStream() throws IOException {
            throw new OpenError();
        }
    }

    private static final class ReadErrorStream extends EmptyStream {

        @Override
        public int read() throws IOException {
            throw new ReadError();
        }
    }

    private static final class CloseErrorStream extends EmptyStream {

        @Override
        public void close() throws IOException {
            throw new CloseError();
        }
    }

    private static final class ParseError extends IOException {
    }

    private static final class OpenError extends IOException {
    }

    private static final class ReadError extends IOException {
    }

    private static final class CloseError extends IOException {
    }
}
