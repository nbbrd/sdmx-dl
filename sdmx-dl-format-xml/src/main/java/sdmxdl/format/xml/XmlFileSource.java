package sdmxdl.format.xml;

import internal.sdmxdl.format.xml.ImmutableXMLInputFactory;
import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import sdmxdl.file.FileSource;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;

@lombok.experimental.UtilityClass
public class XmlFileSource {

    public Xml.Parser<FileSource> getParser() {
        return PARSER;
    }

    public Xml.Formatter<FileSource> getFormatter() {
        return FORMATTER;
    }

    private static final XMLOutputFactory OUTPUT = XMLOutputFactory.newInstance();

    private final Xml.Formatter<FileSource> FORMATTER = Stax.StreamFormatter
            .<FileSource>builder()
            .factory(() -> OUTPUT)
            .handler2(XmlFileSource::formatXml)
            .build();

    private void formatXml(FileSource source, XMLStreamWriter xml, Charset encoding) throws XMLStreamException {
        xml.writeStartDocument(encoding.name(), "1.0");
        xml.writeEmptyElement(ROOT_TAG);

        xml.writeAttribute(DATA_ATTR, source.getData().toString());

        File structure = source.getStructure();
        if (isValidFile(structure)) {
            xml.writeAttribute(STRUCT_ATTR, source.getStructure().toString());
        }

        xml.writeEndDocument();
    }

    private final Xml.Parser<FileSource> PARSER = Stax.StreamParser
            .<FileSource>builder()
            .factory(ImmutableXMLInputFactory::getInputFactoryWithoutNamespace)
            .value(XmlFileSource::parseXml)
            .build();

    private static FileSource parseXml(XMLStreamReader xml) throws XMLStreamException {
        String data = null;
        String structure = null;

        while (xml.hasNext()) {
            if (xml.next() == XMLStreamReader.START_ELEMENT && xml.getLocalName().equals(ROOT_TAG)) {
                data = xml.getAttributeValue(null, DATA_ATTR);
                structure = xml.getAttributeValue(null, STRUCT_ATTR);
            }
        }

        if (isNullOrEmpty(data)) {
            throw new XMLStreamException("Missing data attribute");
        }

        return FileSource.builder()
                .data(Paths.get(data).toFile())
                .structure(!isNullOrEmpty(structure) ? Paths.get(structure).toFile() : null)
                .build();
    }

    public boolean isValidFile(File file) {
        return file != null && !file.toString().isEmpty();
    }

    private boolean isNullOrEmpty(String o) {
        return o == null || o.isEmpty();
    }

    private static final String ROOT_TAG = "file";
    private static final String DATA_ATTR = "data";
    private static final String STRUCT_ATTR = "structure";
}
