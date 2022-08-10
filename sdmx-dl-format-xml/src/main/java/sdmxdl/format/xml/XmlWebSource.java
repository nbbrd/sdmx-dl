/*
 * Copyright 2018 National Bank of Belgium
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
package sdmxdl.format.xml;

import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import sdmxdl.web.SdmxWebSource;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("SwitchStatementWithTooFewBranches")
@lombok.experimental.UtilityClass
public final class XmlWebSource {

    public static Xml.Parser<List<SdmxWebSource>> getParser() {
        return PARSER;
    }

    public static Xml.Formatter<List<SdmxWebSource>> getFormatter() {
        return FORMATTER;
    }

    private static final Xml.Parser<List<SdmxWebSource>> PARSER = Stax.StreamParser.valueOf(XmlWebSource::parseXml);
    private static final Xml.Formatter<List<SdmxWebSource>> FORMATTER = Stax.StreamFormatter.of(XmlWebSource::formatXml);

    private static final String SOURCES_TAG = "sources";
    private static final String SOURCE_TAG = "source";
    private static final String NAME_TAG = "name";
    private static final String DESCRIPTION_TAG = "description";
    private static final String DRIVER_TAG = "driver";
    private static final String DIALECT_TAG = "dialect";
    private static final String ENDPOINT_TAG = "endpoint";
    private static final String PROPERTY_TAG = "property";
    private static final String ALIAS_TAG = "alias";
    private static final String WEBSITE_TAG = "website";
    private static final String LANG_ATTR = "lang";
    private static final String KEY_ATTR = "key";
    private static final String VALUE_ATTR = "value";
    private static final String MONITOR_TAG = "monitor";
    private static final String MONITOR_WEBSITE_TAG = "monitorWebsite";

    private static List<SdmxWebSource> parseXml(XMLStreamReader reader) throws XMLStreamException {
        List<SdmxWebSource> result = new ArrayList<>();
        SdmxWebSource.Builder item = SdmxWebSource.builder();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case SOURCE_TAG:
                            item = SdmxWebSource.builder();
                            break;
                        case NAME_TAG:
                            item.name(reader.getElementText());
                            break;
                        case DESCRIPTION_TAG:
                            String lang = reader.getAttributeValue(null, LANG_ATTR);
                            item.description(lang != null ? lang : SdmxWebSource.ROOT_LANGUAGE, reader.getElementText());
                            break;
                        case DRIVER_TAG:
                            item.driver(reader.getElementText());
                            break;
                        case DIALECT_TAG:
                            item.dialect(reader.getElementText());
                            break;
                        case ENDPOINT_TAG:
                            item.endpointOf(reader.getElementText());
                            break;
                        case PROPERTY_TAG:
                            item.property(reader.getAttributeValue(null, KEY_ATTR), reader.getAttributeValue(null, VALUE_ATTR));
                            break;
                        case ALIAS_TAG:
                            item.alias(reader.getElementText());
                            break;
                        case WEBSITE_TAG:
                            item.websiteOf(reader.getElementText());
                            break;
                        case MONITOR_TAG:
                            item.monitorOf(reader.getElementText());
                            break;
                        case MONITOR_WEBSITE_TAG:
                            item.monitorWebsiteOf(reader.getElementText());
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case SOURCE_TAG:
                            result.add(item.build());
                            break;
                    }
                    break;
            }
        }
        return result;
    }

    private static void formatXml(List<SdmxWebSource> list, XMLStreamWriter writer, Charset encoding) throws XMLStreamException {
        writer.writeStartDocument(encoding.name(), "1.0");
        writer.writeStartElement(SOURCES_TAG);
        for (SdmxWebSource source : list) {
            writer.writeStartElement(SOURCE_TAG);
            writeTextElement(writer, NAME_TAG, source.getName());
            for (Map.Entry<String, String> description : source.getDescriptions().entrySet()) {
                writeDescription(writer, description);
            }
            writeTextElement(writer, DRIVER_TAG, source.getDriver());
            writeTextElement(writer, DIALECT_TAG, source.getDialect());
            writeTextElement(writer, ENDPOINT_TAG, source.getEndpoint().toString());
            for (Map.Entry<String, String> property : source.getProperties().entrySet()) {
                writeProperty(writer, property);
            }
            for (String alias : source.getAliases()) {
                writeTextElement(writer, ALIAS_TAG, alias);
            }
            writeTextElement(writer, WEBSITE_TAG, source.getWebsite());
            writeTextElement(writer, MONITOR_TAG, source.getMonitor());
            writeTextElement(writer, MONITOR_WEBSITE_TAG, source.getMonitorWebsite());
            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private static void writeDescription(XMLStreamWriter writer, Map.Entry<String, String> description) throws XMLStreamException {
        writer.writeStartElement(DESCRIPTION_TAG);
        String lang = description.getKey();
        if (!lang.isEmpty()) {
            writer.writeAttribute(LANG_ATTR, lang);
        }
        writer.writeCharacters(description.getValue());
        writer.writeEndElement();
    }

    private static void writeProperty(XMLStreamWriter writer, Map.Entry<String, String> property) throws XMLStreamException {
        writer.writeStartElement(PROPERTY_TAG);
        writer.writeAttribute(KEY_ATTR, property.getKey());
        writer.writeAttribute(VALUE_ATTR, property.getValue());
        writer.writeEndElement();
    }

    private static void writeTextElement(XMLStreamWriter writer, String name, Object value) throws XMLStreamException {
        if (value != null) {
            writer.writeStartElement(name);
            writer.writeCharacters(value.toString());
            writer.writeEndElement();
        }
    }
}
