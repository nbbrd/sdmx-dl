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
package internal.file;

import be.nbb.sdmx.facade.file.SdmxFileSet;
import be.nbb.util.Stax;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SdmxFileUtil {

    @Nonnull
    public String asFlowLabel(@Nonnull SdmxFileSet files) {
        return files.getData().getName().replace(".xml", "");
    }

    @Nonnull
    @SuppressWarnings("null")
    public String toXml(@Nonnull SdmxFileSet files) {
        StringWriter result = new StringWriter();
        try {
            XMLStreamWriter xml = OUTPUT.createXMLStreamWriter(result);
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
            xml.close();
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
        return result.toString();
    }

    @Nonnull
    public static SdmxFileSet fromXml(@Nonnull String input) throws IllegalArgumentException {
        String data = null;
        String structure = null;
        String dialect = null;
        try {
            XMLStreamReader xml = Stax.getInputFactoryWithoutNamespace().createXMLStreamReader(new StringReader(input));
            while (xml.hasNext()) {
                if (xml.next() == XMLStreamReader.START_ELEMENT && xml.getLocalName().equals(ROOT_TAG)) {
                    data = xml.getAttributeValue(null, DATA_ATTR);
                    structure = xml.getAttributeValue(null, STRUCT_ATTR);
                    dialect = xml.getAttributeValue(null, DIALECT_ATTR);
                }
            }
            xml.close();
        } catch (XMLStreamException ex) {
            throw new IllegalArgumentException("Cannot parse SdmxFile", ex);
        }
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Cannot parse SdmxFile from '" + input + "'");
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
    private static final XMLOutputFactory OUTPUT = XMLOutputFactory.newInstance();
}
