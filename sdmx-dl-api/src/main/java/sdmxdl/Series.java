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
package sdmxdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class Series {

    @lombok.NonNull
    Key key;

    @lombok.NonNull
    @lombok.Singular("meta")
    Map<String, String> meta;

    /**
     * Non-null list of observations sorted chronologically.
     */
    @lombok.NonNull
    @lombok.Singular("obs")
    SortedSet<Obs> obs;

    @lombok.Getter(lazy = true)
    List<Obs> obsList = new ArrayList<>(getObs());

    public static final class Builder {
    }
}
