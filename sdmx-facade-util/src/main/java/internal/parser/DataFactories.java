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
package internal.parser;

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.parser.DataFactory;
import be.nbb.sdmx.facade.parser.FreqParser;
import be.nbb.sdmx.facade.parser.FreqUtil;
import be.nbb.sdmx.facade.util.SafeParser;
import java.time.LocalDateTime;

/**
 *
 * @author Philippe Charles
 */
public enum DataFactories implements DataFactory {

    SDMX20 {
        @Override
        public FreqParser getFreqParser(DataStructure dsd) {
            return FreqParser.sdmx20();
        }

        @Override
        public SafeParser<LocalDateTime> getPeriodParser(Frequency freq) {
            return FreqUtil.onStandardFreq(freq);
        }

        @Override
        public SafeParser<Double> getValueParser() {
            return SafeParser.onStandardDouble();
        }
    },
    SDMX21 {
        @Override
        public FreqParser getFreqParser(DataStructure dsd) {
            return FreqParser.sdmx21(dsd);
        }

        @Override
        public SafeParser<LocalDateTime> getPeriodParser(Frequency freq) {
            return FreqUtil.onStandardFreq(freq);
        }

        @Override
        public SafeParser<Double> getValueParser() {
            return SafeParser.onStandardDouble();
        }
    }
}
