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
package internal.util;

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.util.DataFactory;
import be.nbb.sdmx.facade.util.FreqParser;
import be.nbb.sdmx.facade.util.ObsParser;

/**
 *
 * @author Philippe Charles
 */
public enum StandardDataFactory implements DataFactory {

    SDMX20 {
        @Override
        public Key.Builder getKeyBuilder(DataStructure o) {
            return Key.builder(o);
        }

        @Override
        public FreqParser getFreqParser(DataStructure o) {
            return FreqParser.sdmx20();
        }

        @Override
        public ObsParser getObsParser(DataStructure o) {
            return ObsParser.standard();
        }
    }, SDMX21 {
        @Override
        public Key.Builder getKeyBuilder(DataStructure o) {
            return Key.builder(o);
        }

        @Override
        public FreqParser getFreqParser(DataStructure o) {
            return FreqParser.sdmx21(o);
        }

        @Override
        public ObsParser getObsParser(DataStructure o) {
            return ObsParser.standard();
        }
    }
}
