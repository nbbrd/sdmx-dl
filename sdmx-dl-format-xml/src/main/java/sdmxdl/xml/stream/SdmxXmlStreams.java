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
package sdmxdl.xml.stream;


import lombok.NonNull;
import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import sdmxdl.*;
import sdmxdl.ext.MessageFooter;
import sdmxdl.ext.ObsParser;
import sdmxdl.xml.DataCursor;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SdmxXmlStreams {

    public Xml.@NonNull Parser<DataCursor> compactData20(@NonNull DataStructure dsd, @NonNull Supplier<ObsParser> df) {
        return Stax.StreamParser.<DataCursor>builder()
                .factory(StaxUtil::getInputFactoryWithoutNamespace)
                .handler((o, onClose) -> new XMLStreamCompactDataCursor(o, onClose, Key.builder(dsd), df.get(), dsd.getTimeDimensionId(), dsd.getPrimaryMeasureId()))
                .build();
    }

    public Xml.@NonNull Parser<DataCursor> compactData21(@NonNull DataStructure dsd, @NonNull Supplier<ObsParser> df) {
        return Stax.StreamParser.<DataCursor>builder()
                .factory(StaxUtil::getInputFactoryWithoutNamespace)
                .handler((o, onClose) -> new XMLStreamCompactDataCursor(o, onClose, Key.builder(dsd), df.get(), dsd.getTimeDimensionId(), dsd.getPrimaryMeasureId()))
                .build();
    }

    public Xml.@NonNull Parser<DataCursor> genericData20(@NonNull DataStructure dsd, @NonNull Supplier<ObsParser> df) {
        return Stax.StreamParser.<DataCursor>builder()
                .factory(StaxUtil::getInputFactoryWithoutNamespace)
                .handler((o, onClose) -> XMLStreamGenericDataCursor.sdmx20(o, onClose, Key.builder(dsd), df.get()))
                .build();
    }

    public Xml.@NonNull Parser<DataCursor> genericData21(@NonNull DataStructure dsd, @NonNull Supplier<ObsParser> df) {
        return Stax.StreamParser.<DataCursor>builder()
                .factory(StaxUtil::getInputFactoryWithoutNamespace)
                .handler((o, onClose) -> XMLStreamGenericDataCursor.sdmx21(o, onClose, Key.builder(dsd), df.get()))
                .build();
    }

    public Xml.@NonNull Parser<List<DataStructure>> struct20(@NonNull LanguagePriorityList langs) {
        return Stax.StreamParser.<List<DataStructure>>builder()
                .factory(StaxUtil::getInputFactory)
                .handler(Stax.FlowHandler.of(new XMLStreamStructure20(langs)::parse))
                .build();
    }

    public Xml.@NonNull Parser<List<DataStructure>> struct21(@NonNull LanguagePriorityList langs) {
        return Stax.StreamParser.<List<DataStructure>>builder()
                .factory(StaxUtil::getInputFactory)
                .handler(Stax.FlowHandler.of(new XMLStreamStructure21(langs)::parse))
                .build();
    }

    public Xml.@NonNull Parser<List<Dataflow>> flow21(@NonNull LanguagePriorityList langs) {
        return Stax.StreamParser.<List<Dataflow>>builder()
                .factory(StaxUtil::getInputFactory)
                .handler(Stax.FlowHandler.of(new XMLStreamFlow21(langs)::parse))
                .build();
    }

    public Xml.@NonNull Parser<List<Codelist>> codelist21(@NonNull LanguagePriorityList langs) {
        return Stax.StreamParser.<List<Codelist>>builder()
                .factory(StaxUtil::getInputFactory)
                .handler(Stax.FlowHandler.of(new XMLStreamCodelist21(langs)::parse))
                .build();
    }

    public Xml.@NonNull Parser<MessageFooter> messageFooter21(@NonNull LanguagePriorityList langs) {
        return Stax.StreamParser.<MessageFooter>builder()
                .factory(StaxUtil::getInputFactory)
                .handler(Stax.FlowHandler.of(new XMLStreamMessageFooter21(langs)::parse))
                .build();
    }
}
