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
package be.nbb.sdmx.facade.file;

import be.nbb.sdmx.facade.DataflowRef;
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
@lombok.Value
public class SdmxFile {

    @lombok.NonNull
    File data;

    File structure;

    @Nonnull
    public DataflowRef getDataflowRef() {
        return DataflowRef.parse("file");
    }

    @Override
    public String toString() {
        try {
            StringWriter stream = new StringWriter();
            XMLStreamWriter writer = OUTPUT.createXMLStreamWriter(stream);
            writer.writeStartElement("sdmxFile");
            writer.writeAttribute("data", data.toString());
            if (structure != null) {
                writer.writeAttribute("structure", structure.toString());
            }
            writer.writeEndElement();
            writer.close();
            return stream.toString();
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Nonnull
    public static SdmxFile parse(@Nonnull String input) throws IllegalArgumentException {
        try {
            XMLStreamReader reader = INPUT.createXMLStreamReader(new StringReader(input));
            String data = null;
            String structure = null;
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamReader.START_ELEMENT && reader.getLocalName().equals("sdmxFile")) {
                    data = reader.getAttributeValue(null, "data");
                    structure = reader.getAttributeValue(null, "structure");
                }
            }
            reader.close();
            return new SdmxFile(data != null ? new File(data) : null, structure != null ? new File(structure) : null);
        } catch (XMLStreamException ex) {
            throw new IllegalArgumentException("Cannot parse SdmxFile", ex);
        }
    }

    private static final XMLOutputFactory OUTPUT = XMLOutputFactory.newInstance();
    private static final XMLInputFactory INPUT = XMLInputFactory.newInstance();
}
