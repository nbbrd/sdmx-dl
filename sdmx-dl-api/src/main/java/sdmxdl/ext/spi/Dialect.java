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
package sdmxdl.ext.spi;

import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import sdmxdl.DataStructure;
import sdmxdl.Series;
import sdmxdl.ext.SeriesMeta;

import java.util.function.Function;

/**
 * @author Philippe Charles
 */
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        loaderName = "internal.util.DialectLoader"
)
@ThreadSafe
public interface Dialect {

    @NonNull
    String getName();

    @NonNull
    String getDescription();

    @NonNull Function<Series, SeriesMeta> getMetaFactory(@NonNull DataStructure dsd);

    String SDMX20_DIALECT = "SDMX20";
    String SDMX21_DIALECT = "SDMX21";
}
