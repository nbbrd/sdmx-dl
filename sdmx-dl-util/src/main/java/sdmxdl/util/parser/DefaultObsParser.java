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
package sdmxdl.util.parser;

import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.Frequency;
import sdmxdl.Key;
import sdmxdl.ext.ObsParser;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * @author Philippe Charles
 */
public final class DefaultObsParser implements ObsParser {

    private final BiFunction<Key.Builder, UnaryOperator<String>, Frequency> freqParser;
    private final Function<Frequency, Parser<LocalDateTime>> toPeriodParser;
    private final Parser<Double> valueParser;
    private Parser<LocalDateTime> periodParser;
    private Frequency freq;
    private String period;
    private String value;

    public DefaultObsParser(
            BiFunction<Key.Builder, UnaryOperator<String>, Frequency> freqParser,
            Function<Frequency, Parser<LocalDateTime>> toPeriodParser,
            Parser<Double> valueParser) {
        this.freqParser = freqParser;
        this.toPeriodParser = toPeriodParser;
        this.valueParser = valueParser;
        this.periodParser = Parser.onNull();
        this.freq = Frequency.UNDEFINED;
        this.period = null;
        this.value = null;
    }

    @Override
    @NonNull
    public Frequency getFrequency() {
        return freq;
    }

    @Override
    @Nullable
    public String getPeriod() {
        return period;
    }

    @Override
    @Nullable
    public String getValue() {
        return value;
    }

    @Override
    @NonNull
    public ObsParser clear() {
        this.period = null;
        this.value = null;
        return this;
    }

    @Override
    @NonNull
    public ObsParser frequency(Key.@NonNull Builder key, @NonNull UnaryOperator<String> attributes) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(attributes);
        Frequency freq = freqParser.apply(key, attributes);
        if (this.freq != freq) {
            this.freq = freq;
            this.periodParser = toPeriodParser.apply(freq);
            if (this.periodParser == null) {
                this.periodParser = Parser.onNull();
            }
        }
        return this;
    }

    @Override
    @NonNull
    public ObsParser period(@Nullable String period) {
        this.period = period;
        return this;
    }

    @Override
    @NonNull
    public ObsParser value(@Nullable String value) {
        this.value = value;
        return this;
    }

    @Override
    @Nullable
    public LocalDateTime parsePeriod() {
        return periodParser.parse(period);
    }

    @Override
    @Nullable
    public Double parseValue() {
        return valueParser.parse(value);
    }
}
