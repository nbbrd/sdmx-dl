/*
 * Copyright 2020 National Bank of Belgium
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
package sdmxdl.testing;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.CatalogRef;
import sdmxdl.FlowRef;
import sdmxdl.Languages;
import sdmxdl.Query;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class WebRequest {

    String digest;

    @lombok.NonNull
    String source;

    @lombok.NonNull
    Languages languages;

    @lombok.Builder.Default
    @NonNull
    CatalogRef catalog = CatalogRef.NO_CATALOG;

    @lombok.NonNull
    FlowRef flowRef;

    @lombok.NonNull
    Query query;

    @lombok.NonNull
    IntRange flowCount;

    @lombok.NonNull
    IntRange dimensionCount;

    @lombok.NonNull
    IntRange seriesCount;

    @lombok.NonNull
    IntRange obsCount;
}
