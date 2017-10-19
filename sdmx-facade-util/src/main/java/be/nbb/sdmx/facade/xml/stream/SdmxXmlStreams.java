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

import be.nbb.sdmx.facade.util.DataFactory;
import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.LanguagePriorityList;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import be.nbb.sdmx.facade.xml.stream.XMLStreamUtil.ReaderFunc;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SdmxXmlStreams {

    @Nonnull
    public XMLStream<DataCursor> compactData20(@Nonnull DataStructure dsd) throws IOException {
        return compactData20(dsd, DataFactory.sdmx20());
    }

    @Nonnull
    public XMLStream<DataCursor> compactData20(@Nonnull DataStructure dsd, @Nonnull DataFactory df) throws IOException {
        return o -> new XMLStreamCompactDataCursor(o, df.getKeyBuilder(dsd), df.getObsParser(dsd), df.getFreqParser(dsd), "", "");
    }

    @Nonnull
    public XMLStream<DataCursor> compactData21(@Nonnull DataStructure dsd) throws IOException {
        return compactData21(dsd, DataFactory.sdmx21());
    }

    @Nonnull
    public XMLStream<DataCursor> compactData21(@Nonnull DataStructure dsd, @Nonnull DataFactory df) throws IOException {
        return o -> new XMLStreamCompactDataCursor(o, df.getKeyBuilder(dsd), df.getObsParser(dsd), df.getFreqParser(dsd), dsd.getTimeDimensionId(), dsd.getPrimaryMeasureId());
    }

    @Nonnull
    public XMLStream<DataCursor> genericData20(@Nonnull DataStructure dsd) throws IOException {
        return genericData20(dsd, DataFactory.sdmx20());
    }

    @Nonnull
    public XMLStream<DataCursor> genericData20(@Nonnull DataStructure dsd, @Nonnull DataFactory df) throws IOException {
        return o -> XMLStreamGenericDataCursor.sdmx20(o, df.getKeyBuilder(dsd), df.getObsParser(dsd), df.getFreqParser(dsd));
    }

    @Nonnull
    public XMLStream<DataCursor> genericData21(@Nonnull DataStructure dsd) throws IOException {
        return genericData21(dsd, DataFactory.sdmx21());
    }

    @Nonnull
    public XMLStream<DataCursor> genericData21(@Nonnull DataStructure dsd, @Nonnull DataFactory df) throws IOException {
        return o -> XMLStreamGenericDataCursor.sdmx21(o, df.getKeyBuilder(dsd), df.getObsParser(dsd), df.getFreqParser(dsd));
    }

    @Nonnull
    public XMLStream<List<DataStructure>> struct20(@Nonnull LanguagePriorityList langs) throws IOException {
        return o -> struct(o, new XMLStreamStructure20(langs)::parse);
    }

    @Nonnull
    public XMLStream<List<DataStructure>> struct21(@Nonnull LanguagePriorityList langs) throws IOException {
        return o -> struct(o, new XMLStreamStructure21(langs)::parse);
    }

    @Nonnull
    private List<DataStructure> struct(@Nonnull XMLStreamReader reader, @Nonnull ReaderFunc<List<DataStructure>> func) throws IOException {
        try {
            return XMLStreamUtil.with(reader, func);
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }
}
