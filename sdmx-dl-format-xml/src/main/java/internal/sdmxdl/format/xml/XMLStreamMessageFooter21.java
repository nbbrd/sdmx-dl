package internal.sdmxdl.format.xml;

import lombok.NonNull;
import nbbrd.io.text.Parser;
import sdmxdl.Languages;
import sdmxdl.format.MessageFooter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public final class XMLStreamMessageFooter21 {

    private static final String HEADER_TAG = "Header";
    private static final String FOOTER_TAG = "Footer";
    private static final String MESSAGE_TAG = "Message";
    private static final String TEXT_TAG = "Text";

    private static final String CODE_ATTR = "code";
    private static final String SEVERITY_ATTR = "severity";
    private static final String LANG_ATTR = "lang";

    private final TextBuilder label;

    public XMLStreamMessageFooter21(Languages langs) {
        this.label = new TextBuilder(langs);
    }

    public @NonNull MessageFooter parse(@NonNull XMLStreamReader reader) throws XMLStreamException {
        if (XMLStreamUtil.isNotNamespaceAware(reader)) {
            throw new XMLStreamException("Cannot parse structure");
        }

        MessageFooter.Builder result = MessageFooter.builder();
        while (XMLStreamUtil.nextTags(reader, "")) {
            switch (reader.getLocalName()) {
                case HEADER_TAG:
                    parseHeader(reader);
                    break;
                case FOOTER_TAG:
                    while (XMLStreamUtil.nextTag(reader, FOOTER_TAG, MESSAGE_TAG)) {
                        parseMessage(reader, result);
                    }
                    break;
            }
        }
        return result.build();
    }

    private void parseHeader(XMLStreamReader reader) throws XMLStreamException {
        String ns = reader.getNamespaceURI();
        XMLStreamUtil.check(Sdmxml.MESSAGE_V21.is(ns), reader, "Invalid namespace '%s'", ns);
    }

    private void parseMessage(XMLStreamReader reader, MessageFooter.Builder result) throws XMLStreamException {
        String code = reader.getAttributeValue(null, CODE_ATTR);
        XMLStreamUtil.check(code != null, reader, "Missing code");

        result.code(Parser.onInteger().parseValue(code).orElseThrow(() -> new XMLStreamException("Cannot parse fotter code")));

        result.severity(reader.getAttributeValue(null, SEVERITY_ATTR));

        while (XMLStreamUtil.nextTag(reader, MESSAGE_TAG, TEXT_TAG)) {
            parseText(reader, result);
        }
    }

    private void parseText(XMLStreamReader reader, MessageFooter.Builder result) throws XMLStreamException {
        label.clear();
        String lang = reader.getAttributeValue(null, LANG_ATTR);
        if (lang != null) {
            label.put(lang, reader.getElementText());
        }
        result.text(label.build());
    }
}
