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
import be.nbb.sdmx.facade.parser.Freqs;
import java.time.LocalDateTime;
import nbbrd.io.text.Parser;

/**
 *
 * @author Philippe Charles
 */
public enum DataFactories implements DataFactory {

    SDMX20 {
        @Override
        public Freqs.Parser getFreqParser(DataStructure dsd) {
            return Freqs.Parser.sdmx20();
        }

        @Override
        public Parser<LocalDateTime> getPeriodParser(Frequency freq) {
            return Freqs.onStandardFreq(freq);
        }

        @Override
        public Parser<Double> getValueParser() {
            return Parser.onDouble();
        }
    },
    SDMX21 {
        @Override
        public Freqs.Parser getFreqParser(DataStructure dsd) {
            return Freqs.Parser.sdmx21(dsd);
        }

        @Override
        public Parser<LocalDateTime> getPeriodParser(Frequency freq) {
            return Freqs.onStandardFreq(freq);
        }

        @Override
        public Parser<Double> getValueParser() {
            return Parser.onDouble();
        }
    }
}
