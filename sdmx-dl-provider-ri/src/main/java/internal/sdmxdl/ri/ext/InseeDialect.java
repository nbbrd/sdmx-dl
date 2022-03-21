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

import lombok.NonNull;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataStructure;
import sdmxdl.Series;
import sdmxdl.ext.SeriesMeta;
import sdmxdl.ext.spi.Dialect;
import sdmxdl.util.ext.SeriesMetaFactory;

import java.time.temporal.TemporalAmount;
import java.util.function.Function;

/**
 * https://www.insee.fr/fr/information/2862759
 *
 * @author Philippe Charles
 */
@ServiceProvider(Dialect.class)
public final class InseeDialect implements Dialect {

    @Override
    public String getName() {
        return "INSEE2017";
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public @NonNull Function<Series, SeriesMeta> getMetaFactory(DataStructure dsd) {
        return getFreqFactory(dsd)::get;
    }

    static SeriesMetaFactory getFreqFactory(DataStructure dsd) {
        return SeriesMetaFactory
                .sdmx21(dsd)
                .toBuilder()
                .byDimension(SeriesMetaFactory.getFrequencyCodeIdIndex(dsd), InseeDialect::parseInseeFreq)
                .build();
    }

    private static TemporalAmount parseInseeFreq(CharSequence code) {
        if (code != null && code.length() == 1) {
            switch (code.charAt(0)) {
                case 'A':
                    return SeriesMetaFactory.ANNUAL;
                case 'S':
                    return SeriesMetaFactory.HALF_YEARLY;
                case 'T':
                    return SeriesMetaFactory.QUARTERLY;
                case 'M':
                    return SeriesMetaFactory.MONTHLY;
                case 'B':
                    // Two-monthly
                    // FIXME: define new freq?
                    return SeriesMetaFactory.MONTHLY;
            }
        }
        return SeriesMetaFactory.UNDEFINED;
    }
}
