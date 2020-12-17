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
package sdmxdl.xml.stream;

import nbbrd.io.Resource;
import nbbrd.io.WrappedIOException;
import nbbrd.io.xml.Stax;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.xml.stream.*;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class StaxUtil {

    public void closeBoth(XMLStreamReader reader, Closeable onClose) throws IOException {
        try {
            reader.close();
        } catch (XMLStreamException ex) {
            Resource.ensureClosed(ex, onClose);
            throw WrappedIOException.wrap(ex);
        }
        onClose.close();
    }

    @NonNull
    public XMLInputFactory getInputFactory() {
        return ImmutableInputFactory.DEFAULT;
    }

    @NonNull
    public XMLInputFactory getInputFactoryWithoutNamespace() {
        return ImmutableInputFactory.WITHOUT_NAMESPACE;
    }

    public boolean isNotNamespaceAware(@NonNull XMLStreamReader f) {
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
            Stax.preventXXE(delegate);
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
