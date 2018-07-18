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

import be.nbb.sdmx.facade.parser.DataFactory;
import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.parser.ObsParser;
import be.nbb.util.StaxUtil;
import ioutil.Stax;
import ioutil.Xml;
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
    public Xml.Parser<DataCursor> compactData20(@Nonnull DataStructure dsd, @Nonnull DataFactory df) throws IOException {
        return Stax.StreamParser.<DataCursor>builder()
                .factory(StaxUtil::getInputFactoryWithoutNamespace)
                .handler((o, onClose) -> new XMLStreamCompactDataCursor(o, onClose, Key.builder(dsd), new ObsParser(df::getPeriodParser, df.getValueParser()), df.getFreqParser(dsd), dsd.getTimeDimensionId(), dsd.getPrimaryMeasureId()))
                .build();
    }

    @Nonnull
    public Xml.Parser<DataCursor> compactData21(@Nonnull DataStructure dsd, @Nonnull DataFactory df) throws IOException {
        return Stax.StreamParser.<DataCursor>builder()
                .factory(StaxUtil::getInputFactoryWithoutNamespace)
                .handler((o, onClose) -> new XMLStreamCompactDataCursor(o, onClose, Key.builder(dsd), new ObsParser(df::getPeriodParser, df.getValueParser()), df.getFreqParser(dsd), dsd.getTimeDimensionId(), dsd.getPrimaryMeasureId()))
                .build();
    }

    @Nonnull
    public Xml.Parser<DataCursor> genericData20(@Nonnull DataStructure dsd, @Nonnull DataFactory df) throws IOException {
        return Stax.StreamParser.<DataCursor>builder()
                .factory(StaxUtil::getInputFactoryWithoutNamespace)
                .handler((o, onClose) -> XMLStreamGenericDataCursor.sdmx20(o, onClose, Key.builder(dsd), new ObsParser(df::getPeriodParser, df.getValueParser()), df.getFreqParser(dsd)))
                .build();
    }

    @Nonnull
    public Xml.Parser<DataCursor> genericData21(@Nonnull DataStructure dsd, @Nonnull DataFactory df) throws IOException {
        return Stax.StreamParser.<DataCursor>builder()
                .factory(StaxUtil::getInputFactoryWithoutNamespace)
                .handler((o, onClose) -> XMLStreamGenericDataCursor.sdmx21(o, onClose, Key.builder(dsd), new ObsParser(df::getPeriodParser, df.getValueParser()), df.getFreqParser(dsd)))
                .build();
    }

    @Nonnull
    public Xml.Parser<List<DataStructure>> struct20(@Nonnull LanguagePriorityList langs) throws IOException {
        return Stax.StreamParser.<List<DataStructure>>builder()
                .factory(StaxUtil::getInputFactory)
                .handler(Stax.FlowHandler.of(new XMLStreamStructure20(langs)::parse))
                .build();
    }

    @Nonnull
    public Xml.Parser<List<DataStructure>> struct21(@Nonnull LanguagePriorityList langs) throws IOException {
        return Stax.StreamParser.<List<DataStructure>>builder()
                .factory(StaxUtil::getInputFactory)
                .handler(Stax.FlowHandler.of(new XMLStreamStructure21(langs)::parse))
                .build();
    }

    @Nonnull
    public Xml.Parser<List<Dataflow>> flow21(@Nonnull LanguagePriorityList langs) throws IOException {
        return Stax.StreamParser.<List<Dataflow>>builder()
                .factory(StaxUtil::getInputFactory)
                .handler(Stax.FlowHandler.of(new XMLStreamFlow21(langs)::parse))
                .build();
    }
}
