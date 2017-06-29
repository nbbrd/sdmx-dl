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
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.util.ObsParser;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import be.nbb.sdmx.facade.util.FreqParser;
import be.nbb.sdmx.facade.xml.stream.XMLStreamUtil.ReaderFunc;
import java.util.function.Function;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SdmxXmlStreams {

    @Nonnull
    public XMLStream<DataCursor> compactData20(@Nonnull DataStructure dsd) throws IOException {
        return compactData20(dsd, DataParser::sdmx20);
    }

    @Nonnull
    public XMLStream<DataCursor> compactData20(@Nonnull DataStructure dsd, @Nonnull Function<DataStructure, DataParser> customizer) throws IOException {
        return o -> compactData20(o, customizer.apply(dsd));
    }

    @Nonnull
    private DataCursor compactData20(@Nonnull XMLStreamReader r, @Nonnull DataParser p) throws IOException {
        return new XMLStreamCompactDataCursor(r, p.getKeyBuilder(), p.getObsParser(), p.getFreqParser(), p.getTimeDimensionId(), p.getPrimaryMeasureId());
    }

    @Nonnull
    public XMLStream<DataCursor> compactData21(@Nonnull DataStructure dsd) throws IOException {
        return compactData21(dsd, DataParser::sdmx21);
    }

    @Nonnull
    public XMLStream<DataCursor> compactData21(@Nonnull DataStructure dsd, @Nonnull Function<DataStructure, DataParser> customizer) throws IOException {
        return o -> compactData21(o, customizer.apply(dsd));
    }

    @Nonnull
    private DataCursor compactData21(@Nonnull XMLStreamReader r, @Nonnull DataParser p) throws IOException {
        return new XMLStreamCompactDataCursor(r, p.getKeyBuilder(), p.getObsParser(), p.getFreqParser(), p.getTimeDimensionId(), p.getPrimaryMeasureId());
    }

    @Nonnull
    public XMLStream<DataCursor> genericData20(@Nonnull DataStructure dsd) throws IOException {
        return genericData20(dsd, DataParser::sdmx20);
    }

    @Nonnull
    public XMLStream<DataCursor> genericData20(@Nonnull DataStructure dsd, @Nonnull Function<DataStructure, DataParser> customizer) throws IOException {
        return o -> genericData20(o, customizer.apply(dsd));
    }

    @Nonnull
    private DataCursor genericData20(@Nonnull XMLStreamReader r, @Nonnull DataParser p) throws IOException {
        return new XMLStreamGenericDataCursor(r, p.getKeyBuilder(), p.getObsParser(), p.getFreqParser(), GenericDataParser.sdmx20());
    }

    @Nonnull
    public XMLStream<DataCursor> genericData21(@Nonnull DataStructure dsd) throws IOException {
        return genericData21(dsd, DataParser::sdmx21);
    }

    @Nonnull
    public XMLStream<DataCursor> genericData21(@Nonnull DataStructure dsd, @Nonnull Function<DataStructure, DataParser> customizer) throws IOException {
        return o -> genericData21(o, customizer.apply(dsd));
    }

    @Nonnull
    private DataCursor genericData21(@Nonnull XMLStreamReader r, @Nonnull DataParser p) throws IOException {
        return new XMLStreamGenericDataCursor(r, p.getKeyBuilder(), p.getObsParser(), p.getFreqParser(), GenericDataParser.sdmx21());
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

    @lombok.Value
    public static class DataParser {

        @lombok.NonNull
        Key.Builder keyBuilder;

        @lombok.NonNull
        FreqParser freqParser;

        @lombok.NonNull
        ObsParser obsParser;

        @lombok.NonNull
        String timeDimensionId;

        @lombok.NonNull
        String primaryMeasureId;

        @Nonnull
        public static DataParser sdmx20(@Nonnull DataStructure o) {
            return new DataParser(Key.builder(o), FreqParser.sdmx20(), ObsParser.standard(), "", "");
        }

        @Nonnull
        public static DataParser sdmx21(@Nonnull DataStructure o) {
            return new DataParser(Key.builder(o), FreqParser.sdmx21(o), ObsParser.standard(), o.getTimeDimensionId(), o.getPrimaryMeasureId());
        }
    }
}
