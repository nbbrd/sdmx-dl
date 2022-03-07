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
package internal.sdmxdl.ri.ext;

import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataStructure;
import sdmxdl.Series;
import sdmxdl.ext.SeriesMeta;
import sdmxdl.ext.spi.Dialect;
import sdmxdl.util.ext.SeriesMetaFactory;

import java.time.temporal.TemporalAmount;
import java.util.function.Function;

/**
 * @author Philippe Charles
 */
@ServiceProvider(Dialect.class)
public final class EcbDialect implements Dialect {

    @Override
    public String getName() {
        return "ECB2020";
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public @NonNull Function<Series, SeriesMeta> getMetaFactory(DataStructure dsd) {
        return getFreqFactory(dsd)::get;
    }

    private static SeriesMetaFactory getFreqFactory(DataStructure dsd) {
        return SeriesMetaFactory
                .sdmx21(dsd)
                .toBuilder()
                .byDimension(SeriesMetaFactory.getFrequencyCodeIdIndex(dsd), EcbDialect::parseFreq)
                .build();
    }

    private static TemporalAmount parseFreq(CharSequence code) {
        if (code != null && code.length() == 1) {
            switch (code.charAt(0)) {
                case 'A':
                    return SeriesMetaFactory.ANNUAL;
                case 'S':
                case 'H':
                    return SeriesMetaFactory.HALF_YEARLY;
                case 'Q':
                    return SeriesMetaFactory.QUARTERLY;
                case 'M':
                    return SeriesMetaFactory.MONTHLY;
                case 'W':
                    return SeriesMetaFactory.WEEKLY;
                case 'D':
                    return SeriesMetaFactory.DAILY;
                case 'B':
                    return SeriesMetaFactory.DAILY_BUSINESS;
                case 'N':
                    return SeriesMetaFactory.MINUTELY;
                default:
                    return null;
            }
        }
        return SeriesMetaFactory.UNDEFINED;
    }
}
