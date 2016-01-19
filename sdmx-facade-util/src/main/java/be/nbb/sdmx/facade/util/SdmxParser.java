/*
 * Copyright 2015 National Bank of Belgium
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
package be.nbb.sdmx.facade.util;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dimension;
import be.nbb.sdmx.facade.Key;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Philippe Charles
 */
public abstract class SdmxParser {

    @Nonnull
    abstract public DataCursor genericData20(@Nonnull InputStreamReader stream, @Nonnull DataStructure dsd) throws IOException;

    @Nonnull
    abstract public DataCursor genericData21(@Nonnull InputStreamReader stream, @Nonnull DataStructure dsd) throws IOException;

    @Nonnull
    abstract public DataCursor compactData21(@Nonnull InputStreamReader stream, @Nonnull DataStructure dsd) throws IOException;

    public static SdmxParser getDefault() {
        return DefaultParser.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class DefaultParser extends SdmxParser {

        private static final DefaultParser INSTANCE = new DefaultParser();

        private final XMLInputFactory factory;

        private DefaultParser() {
            this.factory = XMLInputFactory.newInstance();
        }

        @Override
        public DataCursor genericData20(InputStreamReader stream, DataStructure dsd) throws IOException {
            try {
                return new XMLStreamGenericDataCursor20(factory.createXMLStreamReader(stream), Key.builder(dsd));
            } catch (XMLStreamException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public DataCursor genericData21(InputStreamReader stream, DataStructure dsd) throws IOException {
            try {
                return new XMLStreamGenericDataCursor21(factory.createXMLStreamReader(stream), Key.builder(dsd), getFrequencyCodeIdIndex(dsd));
            } catch (XMLStreamException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public DataCursor compactData21(InputStreamReader stream, DataStructure dsd) throws IOException {
            try {
                return new XMLStreamCompactDataCursor21(factory.createXMLStreamReader(stream), Key.builder(dsd), getFrequencyCodeIdIndex(dsd), dsd.getTimeDimensionId(), dsd.getPrimaryMeasureId());
            } catch (XMLStreamException ex) {
                throw new IOException(ex);
            }
        }

        private static int getFrequencyCodeIdIndex(DataStructure dfs) {
            Dimension dimension = tryFindFreq(dfs.getDimensions());
            return dimension != null ? (dimension.getPosition() - 1) : XMLStreamGenericDataCursor21.NO_FREQUENCY_CODE_ID_INDEX;
        }

        private static Dimension tryFindFreq(Set<Dimension> list) {
            for (Dimension o : list) {
                switch (o.getId()) {
                    case "FREQ":
                    case "FREQUENCY":
                        return o;
                }
            }
            return null;
        }
    }
    //</editor-fold>
}
