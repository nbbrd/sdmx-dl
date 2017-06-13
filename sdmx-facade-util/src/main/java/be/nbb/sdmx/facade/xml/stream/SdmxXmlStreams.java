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
package be.nbb.sdmx.facade.xml.stream;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Key;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SdmxXmlStreams {

    @Nonnull
    public DataCursor compactData20(@Nonnull XMLInputFactory factory, @Nonnull Reader stream, @Nonnull DataStructure dsd) throws IOException {
        try {
            return compactData20(factory.createXMLStreamReader(stream), dsd);
        } catch (XMLStreamException ex) {
            throw new IOException("While creating reader", ex);
        }
    }

    private DataCursor compactData20(XMLStreamReader reader, DataStructure dsd) {
        TimeFormatParser parser = TimeFormatParser.sdmx20();
        return new XMLStreamCompactDataCursor(reader, Key.builder(dsd), parser, dsd.getTimeDimensionId(), dsd.getPrimaryMeasureId());
    }

    @Nonnull
    public DataCursor compactData21(@Nonnull XMLInputFactory factory, @Nonnull Reader stream, @Nonnull DataStructure dsd) throws IOException {
        try {
            return compactData21(factory.createXMLStreamReader(stream), dsd);
        } catch (XMLStreamException ex) {
            throw new IOException("While creating reader", ex);
        }
    }

    private DataCursor compactData21(XMLStreamReader reader, DataStructure dsd) {
        TimeFormatParser parser = TimeFormatParser.sdmx21(TimeFormatParser.getFrequencyCodeIdIndex(dsd));
        return new XMLStreamCompactDataCursor(reader, Key.builder(dsd), parser, dsd.getTimeDimensionId(), dsd.getPrimaryMeasureId());
    }

    @Nonnull
    public DataCursor genericData20(@Nonnull XMLInputFactory factory, @Nonnull Reader stream, @Nonnull DataStructure dsd) throws IOException {
        try {
            return genericData20(factory.createXMLStreamReader(stream), dsd);
        } catch (XMLStreamException ex) {
            throw new IOException("While creating reader", ex);
        }
    }

    private DataCursor genericData20(XMLStreamReader reader, DataStructure dsd) {
        TimeFormatParser tfParser = TimeFormatParser.sdmx20();
        GenericDataParser genericParser = GenericDataParser.sdmx20();
        return new XMLStreamGenericDataCursor(reader, Key.builder(dsd), tfParser, genericParser);
    }

    @Nonnull
    public DataCursor genericData21(@Nonnull XMLInputFactory factory, @Nonnull Reader stream, @Nonnull DataStructure dsd) throws IOException {
        try {
            return genericData21(factory.createXMLStreamReader(stream), dsd);
        } catch (XMLStreamException ex) {
            throw new IOException("While creating reader", ex);
        }
    }

    private DataCursor genericData21(XMLStreamReader reader, DataStructure dsd) {
        TimeFormatParser tfParser = TimeFormatParser.sdmx21(TimeFormatParser.getFrequencyCodeIdIndex(dsd));
        GenericDataParser genericParser = GenericDataParser.sdmx21();
        return new XMLStreamGenericDataCursor(reader, Key.builder(dsd), tfParser, genericParser);
    }

    @Nonnull
    public List<DataStructure> struct20(@Nonnull XMLInputFactory factory, @Nonnull Reader stream, @Nonnull String preferredLang) throws IOException {
        try {
            XMLStreamReader reader = factory.createXMLStreamReader(stream);
            try {
                return new XMLStreamStructure20(preferredLang).parse(reader);
            } finally {
                reader.close();
            }
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    @Nonnull
    public List<DataStructure> struct21(@Nonnull XMLInputFactory factory, @Nonnull Reader stream, @Nonnull String preferredLang) throws IOException {
        try {
            XMLStreamReader reader = factory.createXMLStreamReader(stream);
            try {
                return new XMLStreamStructure21(preferredLang).parse(reader);
            } finally {
                reader.close();
            }
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }
}
