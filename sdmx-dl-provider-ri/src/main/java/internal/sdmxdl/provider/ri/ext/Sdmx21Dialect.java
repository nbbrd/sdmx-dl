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
package internal.sdmxdl.provider.ri.ext;

import lombok.NonNull;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataStructure;
import sdmxdl.Series;
import sdmxdl.ext.SeriesMeta;
import sdmxdl.ext.spi.Dialect;
import sdmxdl.provider.ext.SeriesMetaFactory;

import java.util.function.Function;

/**
 * @author Philippe Charles
 */
@ServiceProvider(Dialect.class)
public final class Sdmx21Dialect implements Dialect {

    @Override
    public String getName() {
        return SDMX21_DIALECT;
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public @NonNull Function<Series, SeriesMeta> getMetaFactory(DataStructure dsd) {
        return SeriesMetaFactory.sdmx21(dsd)::get;
    }
}
