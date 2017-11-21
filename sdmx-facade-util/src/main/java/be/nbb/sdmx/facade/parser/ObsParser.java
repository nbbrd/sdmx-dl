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
package be.nbb.sdmx.facade.parser;

import be.nbb.sdmx.facade.util.Chars;
import be.nbb.sdmx.facade.Frequency;
import java.time.LocalDateTime;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
public final class ObsParser {

    @Nonnull
    public static ObsParser standard() {
        return new ObsParser(Freqs::onStandardFreq, Chars.Parser.onStandardDouble());
    }

    private final Function<Frequency, Chars.Parser<LocalDateTime>> toPeriodParser;
    private final Chars.Parser<Double> valueParser;
    private Chars.Parser<LocalDateTime> periodParser;
    private Frequency freq;
    private String period;
    private String value;

    public ObsParser(Function<Frequency, Chars.Parser<LocalDateTime>> toPeriodParser, Chars.Parser<Double> valueParser) {
        this.toPeriodParser = toPeriodParser;
        this.valueParser = valueParser;
        this.periodParser = toPeriodParser.apply(Frequency.UNDEFINED);
        this.freq = Frequency.UNDEFINED;
        this.period = null;
        this.value = null;
    }

    @Nonnull
    public Frequency getFrequency() {
        return freq;
    }

    @Nullable
    public String getPeriod() {
        return period;
    }

    @Nullable
    public String getValue() {
        return value;
    }

    @Nonnull
    public ObsParser clear() {
        this.period = null;
        this.value = null;
        return this;
    }

    @Nonnull
    public ObsParser frequency(@Nonnull Frequency freq) {
        if (this.freq != freq) {
            this.freq = freq;
            this.periodParser = toPeriodParser.apply(freq);
        }
        return this;
    }

    @Nonnull
    public ObsParser period(@Nullable String period) {
        this.period = period;
        return this;
    }

    @Nonnull
    public ObsParser value(@Nullable String value) {
        this.value = value;
        return this;
    }

    @Nullable
    public LocalDateTime parsePeriod() {
        return period != null ? periodParser.parse(period) : null;
    }

    @Nullable
    public Double parseValue() {
        return value != null ? valueParser.parse(value) : null;
    }
}
