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
package internal.sdmxdl.file;

import sdmxdl.file.SdmxFileSet;
import sdmxdl.xml.stream.StaxUtil;
import java.io.File;
import java.io.IOException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SdmxFileUtil {

    @NonNull
    public String asFlowLabel(@NonNull SdmxFileSet files) {
        return files.getData().getName().replace(".xml", "");
    }

    @NonNull
    @SuppressWarnings("null")
    public String toXml(@NonNull SdmxFileSet files) {
        try {
            return FORMATTER.formatToString(files);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final XMLOutputFactory OUTPUT = XMLOutputFactory.newInstance();

    private final Xml.Formatter<SdmxFileSet> FORMATTER = Stax.StreamFormatter
            .<SdmxFileSet>builder()
            .factory(() -> OUTPUT)
            .handler(SdmxFileUtil::toXml)
            .build();

    private void toXml(SdmxFileSet files, XMLStreamWriter xml) throws XMLStreamException {
        xml.writeEmptyElement(ROOT_TAG);

        xml.writeAttribute(DATA_ATTR, files.getData().toString());

        File structure = files.getStructure();
        if (isValidFile(structure)) {
            xml.writeAttribute(STRUCT_ATTR, files.getStructure().toString());
        }

        String dialect = files.getDialect();
        if (!isNullOrEmpty(dialect)) {
            xml.writeAttribute(DIALECT_ATTR, dialect);
        }

        xml.writeEndDocument();
    }

    @NonNull
    public static SdmxFileSet fromXml(@NonNull String input) throws IllegalArgumentException {
        try {
            return PARSER.parseChars(input);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Cannot parse SdmxFile", ex);
        }
    }

    private final Xml.Parser<SdmxFileSet> PARSER = Stax.StreamParser
            .<SdmxFileSet>builder()
            .factory(StaxUtil::getInputFactoryWithoutNamespace)
            .value(SdmxFileUtil::fromXml)
            .build();

    private static SdmxFileSet fromXml(XMLStreamReader xml) throws XMLStreamException {
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

        return SdmxFileSet.builder()
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
