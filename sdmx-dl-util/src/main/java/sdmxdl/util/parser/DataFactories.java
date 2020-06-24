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
package sdmxdl.util.parser;

import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataStructure;
import sdmxdl.Frequency;
import sdmxdl.Key;
import sdmxdl.ext.ObsFactory;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * @author Philippe Charles
 */
public enum DataFactories implements ObsFactory {

    SDMX20 {
        @Override
        public @NonNull DefaultObsParser getParser(@NonNull DataStructure dsd) {
            Objects.requireNonNull(dsd);
            return new DefaultObsParser(getFreqParser(dsd), this::getPeriodParser, this.getValueParser());
        }

        public BiFunction<Key.Builder, UnaryOperator<String>, Frequency> getFreqParser(DataStructure dsd) {
            return Freqs.sdmx20();
        }

        public Parser<LocalDateTime> getPeriodParser(Frequency freq) {
            return Freqs.onStandardFreq(freq);
        }

        public Parser<Double> getValueParser() {
            return Parser.onDouble();
        }
    },
    SDMX21 {
        @Override
        public @NonNull DefaultObsParser getParser(@NonNull DataStructure dsd) {
            Objects.requireNonNull(dsd);
            return new DefaultObsParser(getFreqParser(dsd), this::getPeriodParser, this.getValueParser());
        }

        public BiFunction<Key.Builder, UnaryOperator<String>, Frequency> getFreqParser(DataStructure dsd) {
            return Freqs.sdmx21(dsd);
        }

        public Parser<LocalDateTime> getPeriodParser(Frequency freq) {
            return Freqs.onStandardFreq(freq);
        }

        public Parser<Double> getValueParser() {
            return Parser.onDouble();
        }
    }
}
