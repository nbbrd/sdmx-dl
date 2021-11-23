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

import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataStructure;
import sdmxdl.Frequency;
import sdmxdl.Key;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.ObsParser;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.util.parser.DefaultObsParser;
import sdmxdl.util.parser.FreqFactory;
import sdmxdl.util.parser.StandardReportingFormat;
import sdmxdl.util.parser.TimeFormatParser;

import java.util.function.UnaryOperator;

import static sdmxdl.Frequency.*;

/**
 * https://www.insee.fr/fr/information/2862759
 *
 * @author Philippe Charles
 */
@ServiceProvider(SdmxDialect.class)
public final class InseeDialect implements SdmxDialect {

    @Override
    public String getName() {
        return "INSEE2017";
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public @NonNull ObsFactory getObsFactory() {
        return InseeDialect::getObsParser;
    }

    private static ObsParser getObsParser(DataStructure dsd) {
        return DefaultObsParser
                .builder()
                .freqFactory(getFreqFactory(dsd))
                .periodFactory(InseeDialect::getPeriodParser)
                .build();
    }

    static FreqFactory getFreqFactory(DataStructure dsd) {
        return FreqFactory.sdmx21(dsd).toBuilder().parser(InseeDialect::parseInseeFreq).build();
    }

    static TimeFormatParser getPeriodParser(Key.Builder key, UnaryOperator<String> attributes) {
        return EXTENDED_PARSER;
    }

    private static Frequency parseInseeFreq(CharSequence code) {
        if (code != null && code.length() == 1) {
            switch (code.charAt(0)) {
                case 'A':
                    return ANNUAL;
                case 'S':
                    return HALF_YEARLY;
                case 'T':
                    return QUARTERLY;
                case 'M':
                    return MONTHLY;
                case 'B':
                    // Two-monthly
                    // FIXME: define new freq?
                    return MONTHLY;
            }
        }
        return Frequency.UNDEFINED;
    }

    private static final StandardReportingFormat TWO_MONTH = StandardReportingFormat
            .builder()
            .indicator('B')
            .durationOf("P2M")
            .limitPerYear(6)
            .build();

    private static final TimeFormatParser EXTENDED_PARSER =
            TimeFormatParser.onObservationalTimePeriod()
                    .orElse(TimeFormatParser.onStandardReporting(TWO_MONTH));
}
