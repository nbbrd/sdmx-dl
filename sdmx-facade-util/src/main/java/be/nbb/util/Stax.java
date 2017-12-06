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
package be.nbb.util;

import ioutil.IO;
import ioutil.Xml;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Stax {

    @FunctionalInterface
    public interface Supplier<T> {

        T getWithStream() throws XMLStreamException;
    }

    @FunctionalInterface
    public interface Function<T, R> {

        R applyWithStream(T t) throws XMLStreamException;
    }

    @FunctionalInterface
    public interface Parser<T> {

        @Nonnull
        T parse(@Nonnull XMLStreamReader reader, @Nonnull Closeable onClose) throws IOException;

        @Nonnull
        default IO.Function<File, T> onFile(@Nonnull XMLInputFactory xf, @Nonnull Charset cs) {
            return source -> parseStream(this, xf, () -> new FileInputStream(source), cs);
        }

        @Nonnull
        default IO.Function<Path, T> onPath(@Nonnull XMLInputFactory xf, @Nonnull Charset cs) {
            return source -> {
                Optional<File> file = IO.getFile(source);
                return file.isPresent()
                        ? parseStream(this, xf, () -> new FileInputStream(file.get()), cs)
                        : parseReader(this, xf, () -> Files.newBufferedReader(source, cs));
            };
        }

        @Nonnull
        default IO.Function<IO.Supplier<? extends InputStream>, T> onInputStream(@Nonnull XMLInputFactory xf, @Nonnull Charset cs) {
            return source -> parseStream(this, xf, source, cs);
        }

        @Nonnull
        default IO.Function<IO.Supplier<? extends Reader>, T> onReader(@Nonnull XMLInputFactory xf) {
            return source -> parseReader(this, xf, source);
        }

        @Nonnull
        static <R> Parser<R> of(@Nonnull Function<XMLStreamReader, R> func) {
            return (reader, onClose) -> {
                try (Closeable c = onClose) {
                    return func.applyWithStream(reader);
                } catch (XMLStreamException ex) {
                    throw new XMLStreamIOException(ex);
                }
            };
        }
    }

    @Nonnull
    private <T> T parse(Parser<T> parser, @Nonnull Supplier<? extends XMLStreamReader> supplier, @Nonnull Closeable onClose) throws IOException {
        try {
            XMLStreamReader xml = supplier.getWithStream();
            return parser.parse(xml, () -> closeBoth(xml, onClose));
        } catch (XMLStreamException ex) {
            ensureClosed(ex, onClose);
            throw new XMLStreamIOException("Failed to create XMLStreamReader", ex);
        }
    }

    @Nonnull
    <T> T parseStream(Parser<T> parser, @Nonnull XMLInputFactory xf, @Nonnull IO.Supplier<? extends InputStream> source, @Nonnull Charset cs) throws IOException {
        InputStream stream = source.getWithIO();
        return parse(parser, () -> xf.createXMLStreamReader(stream, cs.name()), stream);
    }

    @Nonnull
    <T> T parseReader(Parser<T> parser, @Nonnull XMLInputFactory xf, @Nonnull IO.Supplier<? extends Reader> source) throws IOException {
        Reader reader = source.getWithIO();
        return parse(parser, () -> xf.createXMLStreamReader(reader), reader);
    }

    public static final class XMLStreamIOException extends IOException {

        public XMLStreamIOException(XMLStreamException ex) {
            super(ex);
        }

        public XMLStreamIOException(String message, XMLStreamException ex) {
            super(message, ex);
        }
    }

    public void closeBoth(XMLStreamReader reader, Closeable onClose) throws IOException {
        try {
            reader.close();
        } catch (XMLStreamException ex) {
            ensureClosed(ex, onClose);
            throw new Stax.XMLStreamIOException("Failed to close xml stream reader", ex);
        }
        onClose.close();
    }

    void ensureClosed(XMLStreamException ex, Closeable onClose) throws IOException {
        try {
            onClose.close();
        } catch (IOException suppressed) {
            ex.addSuppressed(suppressed);
        }
    }

    @Nonnull
    public XMLInputFactory getInputFactory() {
        return ImmutableInputFactory.DEFAULT;
    }

    @Nonnull
    public XMLInputFactory getInputFactoryWithoutNamespace() {
        return ImmutableInputFactory.WITHOUT_NAMESPACE;
    }

    public boolean isNotNamespaceAware(@Nonnull XMLStreamReader f) {
        return !(Boolean) f.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE);
    }

    private static final class ImmutableInputFactory extends XMLInputFactory {

        static final XMLInputFactory DEFAULT = new ImmutableInputFactory(true);
        static final XMLInputFactory WITHOUT_NAMESPACE = new ImmutableInputFactory(false);

        private final XMLInputFactory delegate;

        private ImmutableInputFactory(boolean namespaceAware) {
            this.delegate = XMLInputFactory.newFactory();
            if (!namespaceAware && delegate.isPropertySupported(XMLInputFactory.IS_NAMESPACE_AWARE)) {
                delegate.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
            }
            Xml.StAX.preventXXE(delegate);
        }

        @Override
        public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
            return delegate.createXMLStreamReader(reader);
        }

        @Override
        public XMLStreamReader createXMLStreamReader(Source source) throws XMLStreamException {
            return delegate.createXMLStreamReader(source);
        }

        @Override
        public XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
            return delegate.createXMLStreamReader(stream);
        }

        @Override
        public XMLStreamReader createXMLStreamReader(InputStream stream, String encoding) throws XMLStreamException {
            return delegate.createXMLStreamReader(stream, encoding);
        }

        @Override
        public XMLStreamReader createXMLStreamReader(String systemId, InputStream stream) throws XMLStreamException {
            return delegate.createXMLStreamReader(systemId, stream);
        }

        @Override
        public XMLStreamReader createXMLStreamReader(String systemId, Reader reader) throws XMLStreamException {
            return delegate.createXMLStreamReader(systemId, reader);
        }

        @Override
        public XMLEventReader createXMLEventReader(Reader reader) throws XMLStreamException {
            return delegate.createXMLEventReader(reader);
        }

        @Override
        public XMLEventReader createXMLEventReader(String systemId, Reader reader) throws XMLStreamException {
            return delegate.createXMLEventReader(systemId, reader);
        }

        @Override
        public XMLEventReader createXMLEventReader(XMLStreamReader reader) throws XMLStreamException {
            return delegate.createXMLEventReader(reader);
        }

        @Override
        public XMLEventReader createXMLEventReader(Source source) throws XMLStreamException {
            return delegate.createXMLEventReader(source);
        }

        @Override
        public XMLEventReader createXMLEventReader(InputStream stream) throws XMLStreamException {
            return delegate.createXMLEventReader(stream);
        }

        @Override
        public XMLEventReader createXMLEventReader(InputStream stream, String encoding) throws XMLStreamException {
            return delegate.createXMLEventReader(stream, encoding);
        }

        @Override
        public XMLEventReader createXMLEventReader(String systemId, InputStream stream) throws XMLStreamException {
            return delegate.createXMLEventReader(systemId, stream);
        }

        @Override
        public XMLStreamReader createFilteredReader(XMLStreamReader reader, StreamFilter filter) throws XMLStreamException {
            return delegate.createFilteredReader(reader, filter);
        }

        @Override
        public XMLEventReader createFilteredReader(XMLEventReader reader, EventFilter filter) throws XMLStreamException {
            return delegate.createFilteredReader(reader, filter);
        }

        @Override
        public XMLResolver getXMLResolver() {
            return delegate.getXMLResolver();
        }

        @Override
        public void setXMLResolver(XMLResolver resolver) {
            throw new UnsupportedOperationException();
        }

        @Override
        public XMLReporter getXMLReporter() {
            return delegate.getXMLReporter();
        }

        @Override
        public void setXMLReporter(XMLReporter reporter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setProperty(String name, Object value) throws IllegalArgumentException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getProperty(String name) throws IllegalArgumentException {
            return delegate.getProperty(name);
        }

        @Override
        public boolean isPropertySupported(String name) {
            return delegate.isPropertySupported(name);
        }

        @Override
        public void setEventAllocator(XMLEventAllocator allocator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public XMLEventAllocator getEventAllocator() {
            return delegate.getEventAllocator();
        }
    }
}
