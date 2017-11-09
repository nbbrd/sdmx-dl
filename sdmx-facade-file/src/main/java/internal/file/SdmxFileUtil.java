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

import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.file.SdmxFileSet;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLInputFactory;
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
    public DataflowRef asDataflowRef(@Nonnull SdmxFileSet files) {
        return DataflowRef.parse("data" + (files.getStructure() != null ? "&struct" : ""));
    }

    @Nonnull
    public String toXml(@Nonnull SdmxFileSet files) {
        StringWriter result = new StringWriter();
        try {
            XMLStreamWriter xml = OUTPUT.createXMLStreamWriter(result);
            xml.writeEmptyElement(ROOT_TAG);
            xml.writeAttribute(DATA_ATTR, files.getData().toString());
            File structure = files.getStructure();
            if (structure != null) {
                xml.writeAttribute(STRUCT_ATTR, structure.toString());
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
        try {
            XMLStreamReader xml = INPUT.createXMLStreamReader(new StringReader(input));
            while (xml.hasNext()) {
                if (xml.next() == XMLStreamReader.START_ELEMENT && xml.getLocalName().equals(ROOT_TAG)) {
                    data = xml.getAttributeValue(null, DATA_ATTR);
                    structure = xml.getAttributeValue(null, STRUCT_ATTR);
                }
            }
            xml.close();
        } catch (XMLStreamException ex) {
            throw new IllegalArgumentException("Cannot parse SdmxFile", ex);
        }
        if (data == null) {
            throw new IllegalArgumentException("Cannot parse SdmxFile from '" + input + "'");
        }
        return SdmxFileSet.of(new File(data), structure != null ? new File(structure) : null);
    }

    private static final String ROOT_TAG = "file";
    private static final String DATA_ATTR = "data";
    private static final String STRUCT_ATTR = "structure";
    private static final XMLOutputFactory OUTPUT = XMLOutputFactory.newInstance();
    private static final XMLInputFactory INPUT = XMLInputFactory.newInstance();
}
