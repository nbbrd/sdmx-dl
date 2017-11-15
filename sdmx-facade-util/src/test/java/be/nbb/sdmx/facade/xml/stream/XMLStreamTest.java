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

import be.nbb.sdmx.facade.samples.SdmxSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class XMLStreamTest {

    @Test
    @SuppressWarnings("null")
    public void testValidParseStream() throws IOException {
        XMLStream<String> p = (o, c) -> {
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
                .isInstanceOf(IOException.class)
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
        XMLStream<String> p = (o, c) -> {
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
                .isInstanceOf(IOException.class)
                .hasNoSuppressedExceptions()
                .hasCauseInstanceOf(XMLStreamException.class)
                .hasRootCauseInstanceOf(ReadError.class);

        assertThatThrownBy(() -> p.parseStream(xif, CloseErrorStream::new, UTF_8))
                .isInstanceOf(ParseError.class)
                .hasSuppressedException(new CloseError())
                .hasNoCause();
    }

    private final XMLInputFactory xif = SdmxSource.XIF;

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
