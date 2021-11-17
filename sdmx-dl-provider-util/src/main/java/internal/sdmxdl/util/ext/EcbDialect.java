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
package internal.sdmxdl.util.ext;

import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataStructure;
import sdmxdl.Frequency;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.ObsParser;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.util.parser.DefaultObsParser;
import sdmxdl.util.parser.FreqFactory;
import sdmxdl.util.parser.TimeFormatParsers;

import java.util.Objects;

import static sdmxdl.Frequency.*;
import static sdmxdl.util.parser.TimeFormatParsers.FIRST_DAY_OF_YEAR;

/**
 * @author Philippe Charles
 */
@ServiceProvider(SdmxDialect.class)
public final class EcbDialect implements SdmxDialect {

    @Override
    public String getName() {
        return "ECB2020";
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public @NonNull ObsFactory getObsFactory() {
        return EcbDialect::getObsParser;
    }

    private static ObsParser getObsParser(DataStructure dsd) {
        Objects.requireNonNull(dsd);
        return new DefaultObsParser(
                getFreqFactory(dsd),
                freq -> TimeFormatParsers.getObservationalTimePeriod(FIRST_DAY_OF_YEAR),
                Parser.onDouble()
        );
    }

    private static FreqFactory getFreqFactory(DataStructure dsd) {
        return FreqFactory.sdmx21(dsd).toBuilder().parser(EcbDialect::parseFreq).build();
    }

    private static Frequency parseFreq(CharSequence code) {
        if (code != null && code.length() == 1) {
            switch (code.charAt(0)) {
                case 'A':
                    return ANNUAL;
                case 'S':
                case 'H':
                    return HALF_YEARLY;
                case 'Q':
                    return QUARTERLY;
                case 'M':
                    return MONTHLY;
                case 'W':
                    return WEEKLY;
                case 'D':
                    return DAILY;
                case 'B':
                    return DAILY_BUSINESS;
                case 'N':
                    return MINUTELY;
                default:
                    return null;
            }
        }
        return Frequency.UNDEFINED;
    }
}
