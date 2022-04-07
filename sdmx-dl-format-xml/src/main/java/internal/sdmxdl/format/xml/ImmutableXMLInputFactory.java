package internal.sdmxdl.format.xml;

import lombok.NonNull;
import nbbrd.io.xml.Stax;

import javax.xml.stream.*;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;
import java.io.InputStream;
import java.io.Reader;

public final class ImmutableXMLInputFactory extends XMLInputFactory {

    public static @NonNull XMLInputFactory getDefaultInputFactory() {
        return DEFAULT;
    }

    public static @NonNull XMLInputFactory getInputFactoryWithoutNamespace() {
        return WITHOUT_NAMESPACE;
    }

    private static final ImmutableXMLInputFactory DEFAULT = new ImmutableXMLInputFactory(true);
    private static final ImmutableXMLInputFactory WITHOUT_NAMESPACE = new ImmutableXMLInputFactory(false);

    private final @NonNull XMLInputFactory delegate;

    private ImmutableXMLInputFactory(boolean namespaceAware) {
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
