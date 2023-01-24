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
package sdmxdl.provider.ext;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Parser;

import java.time.temporal.TemporalAmount;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class TimeUnitParsers {

    @NonNull
    public Parser<TemporalAmount> onFreqCodeList() {
        return TimeUnitParsers::parseFreqCode;
    }

    @NonNull
    public Parser<TemporalAmount> onTimeFormatCodeList() {
        return TimeUnitParsers::parseTimeFormatCode;
    }

    // http://sdmx.org/wp-content/uploads/CL_FREQ_v2.0_update_April_2015.doc
    @VisibleForTesting
    TemporalAmount parseFreqCode(CharSequence code) {
        if (code == null) {
            return null;
        }
        switch (code.length()) {
            case 0:
                return null;
            case 1:
                return parseStandardFreqCode(code.charAt(0));
            default:
                TemporalAmount base = parseStandardFreqCode(code.charAt(0));
                return isMultiplier(code.toString().substring(1)) ? base : null;
        }
    }

    private TemporalAmount parseStandardFreqCode(char code) {
        switch (code) {
            case 'A':
                return SeriesMetaFactory.ANNUAL;
            case 'S':
                return SeriesMetaFactory.HALF_YEARLY;
            case 'Q':
                return SeriesMetaFactory.QUARTERLY;
            case 'M':
                return SeriesMetaFactory.MONTHLY;
            case 'W':
                return SeriesMetaFactory.WEEKLY;
            case 'D':
                return SeriesMetaFactory.DAILY;
            case 'H':
                return SeriesMetaFactory.HOURLY;
            case 'B':
                return SeriesMetaFactory.DAILY_BUSINESS;
            case 'N':
                return SeriesMetaFactory.MINUTELY;
            default:
                return null;
        }
    }

    private boolean isMultiplier(String input) {
        try {
            return Integer.parseInt(input) > 1;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    // http://sdmx.org/wp-content/uploads/CL_TIME_FORMAT_1.0_2009.doc
    @VisibleForTesting
    TemporalAmount parseTimeFormatCode(CharSequence code) {
        if (code == null) {
            return null;
        }
        switch (code.toString()) {
            case "P1Y":
                return SeriesMetaFactory.ANNUAL;
            case "P6M":
                return SeriesMetaFactory.HALF_YEARLY;
            case "P3M":
                return SeriesMetaFactory.QUARTERLY;
            case "P1M":
                return SeriesMetaFactory.MONTHLY;
            case "P7D":
                return SeriesMetaFactory.WEEKLY;
            case "P1D":
                return SeriesMetaFactory.DAILY;
            case "PT1M":
                return SeriesMetaFactory.MINUTELY;
            default:
                return null;
        }
    }
}
