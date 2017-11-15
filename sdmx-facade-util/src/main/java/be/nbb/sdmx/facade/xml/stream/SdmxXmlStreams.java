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
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.LanguagePriorityList;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;

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
        return (o, onClose) -> new XMLStreamCompactDataCursor(o, onClose, Key.builder(dsd), df.getObsParser(dsd), df.getFreqParser(dsd), "", "");
    }

    @Nonnull
    public XMLStream<DataCursor> compactData21(@Nonnull DataStructure dsd) throws IOException {
        return compactData21(dsd, DataFactory.sdmx21());
    }

    @Nonnull
    public XMLStream<DataCursor> compactData21(@Nonnull DataStructure dsd, @Nonnull DataFactory df) throws IOException {
        return (o, onClose) -> new XMLStreamCompactDataCursor(o, onClose, Key.builder(dsd), df.getObsParser(dsd), df.getFreqParser(dsd), dsd.getTimeDimensionId(), dsd.getPrimaryMeasureId());
    }

    @Nonnull
    public XMLStream<DataCursor> genericData20(@Nonnull DataStructure dsd) throws IOException {
        return genericData20(dsd, DataFactory.sdmx20());
    }

    @Nonnull
    public XMLStream<DataCursor> genericData20(@Nonnull DataStructure dsd, @Nonnull DataFactory df) throws IOException {
        return (o, onClose) -> XMLStreamGenericDataCursor.sdmx20(o, onClose, Key.builder(dsd), df.getObsParser(dsd), df.getFreqParser(dsd));
    }

    @Nonnull
    public XMLStream<DataCursor> genericData21(@Nonnull DataStructure dsd) throws IOException {
        return genericData21(dsd, DataFactory.sdmx21());
    }

    @Nonnull
    public XMLStream<DataCursor> genericData21(@Nonnull DataStructure dsd, @Nonnull DataFactory df) throws IOException {
        return (o, onClose) -> XMLStreamGenericDataCursor.sdmx21(o, onClose, Key.builder(dsd), df.getObsParser(dsd), df.getFreqParser(dsd));
    }

    @Nonnull
    public XMLStream<List<DataStructure>> struct20(@Nonnull LanguagePriorityList langs) throws IOException {
        return XMLStream.of(new XMLStreamStructure20(langs)::parse);
    }

    @Nonnull
    public XMLStream<List<DataStructure>> struct21(@Nonnull LanguagePriorityList langs) throws IOException {
        return XMLStream.of(new XMLStreamStructure21(langs)::parse);
    }
}
