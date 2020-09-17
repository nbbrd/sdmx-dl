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

import org.checkerframework.checker.index.qual.NonNegative;
import sdmxdl.DataflowRef;
import sdmxdl.Key;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public final class WebRequest {

    @lombok.NonNull
    String source;

    @lombok.NonNull
    DataflowRef flow;

    @lombok.NonNull
    Key key;

    @NonNegative
    int minFlowCount;

    @NonNegative
    int dimensionCount;

    @NonNegative
    int minObsCount;
}
