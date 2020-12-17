package sdmxdl.xml;

import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.xml.stream.StaxUtil;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.nio.charset.Charset;

@lombok.experimental.UtilityClass
public class XmlFileSource {

    public Xml.Parser<SdmxFileSource> getParser() {
        return PARSER;
    }

    public Xml.Formatter<SdmxFileSource> getFormatter() {
        return FORMATTER;
    }

    private static final XMLOutputFactory OUTPUT = XMLOutputFactory.newInstance();

    private final Xml.Formatter<SdmxFileSource> FORMATTER = Stax.StreamFormatter
            .<SdmxFileSource>builder()
            .factory(() -> OUTPUT)
            .handler2(XmlFileSource::format)
            .build();

    private void format(SdmxFileSource source, XMLStreamWriter xml, Charset encoding) throws XMLStreamException {
        xml.writeStartDocument(encoding.name(), "1.0");
        xml.writeEmptyElement(ROOT_TAG);

        xml.writeAttribute(DATA_ATTR, source.getData().toString());

        File structure = source.getStructure();
        if (isValidFile(structure)) {
            xml.writeAttribute(STRUCT_ATTR, source.getStructure().toString());
        }

        String dialect = source.getDialect();
        if (!isNullOrEmpty(dialect)) {
            xml.writeAttribute(DIALECT_ATTR, dialect);
        }

        xml.writeEndDocument();
    }

    private final Xml.Parser<SdmxFileSource> PARSER = Stax.StreamParser
            .<SdmxFileSource>builder()
            .factory(StaxUtil::getInputFactoryWithoutNamespace)
            .value(XmlFileSource::parse)
            .build();

    private static SdmxFileSource parse(XMLStreamReader xml) throws XMLStreamException {
        String data = null;
        String structure = null;
        String dialect = null;

        while (xml.hasNext()) {
            if (xml.next() == XMLStreamReader.START_ELEMENT && xml.getLocalName().equals(ROOT_TAG)) {
                data = xml.getAttributeValue(null, DATA_ATTR);
                structure = xml.getAttributeValue(null, STRUCT_ATTR);
                dialect = xml.getAttributeValue(null, DIALECT_ATTR);
            }
        }

        if (isNullOrEmpty(data)) {
            throw new XMLStreamException("Missing data attribute");
        }

        return SdmxFileSource.builder()
                .data(new File(data))
                .structure(!isNullOrEmpty(structure) ? new File(structure) : null)
                .dialect(dialect)
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
    private static final String DIALECT_ATTR = "dialect";
}
