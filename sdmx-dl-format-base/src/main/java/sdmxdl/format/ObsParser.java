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
package sdmxdl.format;

import lombok.NonNull;
import nbbrd.design.NotThreadSafe;
import nbbrd.io.text.Parser;
import org.jspecify.annotations.Nullable;
import sdmxdl.format.time.ObservationalTimePeriod;
import sdmxdl.format.time.TimeFormats;

import static sdmxdl.format.time.TimeFormats.IGNORE_ERROR;

/**
 * @author Philippe Charles
 */
@NotThreadSafe
@lombok.RequiredArgsConstructor
public final class ObsParser {

    public static @NonNull ObsParser newDefault() {
        return new ObsParser(TimeFormats.getObservationalTimePeriod(IGNORE_ERROR), Parser.onDouble());
    }

    private final Parser<ObservationalTimePeriod> timeParser;
    private final Parser<Double> valueParser;
    private String period = null;
    private String value = null;

    @NonNull
    public ObsParser clear() {
        this.period = null;
        this.value = null;
        return this;
    }

    @NonNull
    public ObsParser period(@Nullable String period) {
        this.period = period;
        return this;
    }

    @NonNull
    public ObsParser value(@Nullable String value) {
        this.value = value;
        return this;
    }

    @Nullable
    public ObservationalTimePeriod parsePeriod() {
        return timeParser.parse(this.period);
    }

    @Nullable
    public Double parseValue() {
        return valueParser.parse(value);
    }
}
