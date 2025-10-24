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
package internal.sdmxdl.format.xml;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Philippe Charles
 */
final class AttributesBuilder {

    private final Map<String, String> data = new HashMap<>();

    @NonNull
    AttributesBuilder clear() {
        data.clear();
        return this;
    }

    @NonNull
    AttributesBuilder put(@Nullable String key, @Nullable String value) {
        if (key != null && value != null) {
            data.put(key, value);
        }
        return this;
    }

    @Nullable
    String getAttribute(@NonNull String key) {
        return data.get(key);
    }

    @NonNull
    Map<String, String> build() {
        switch (data.size()) {
            case 0:
                return Collections.emptyMap();
            case 1:
                Map.Entry<String, String> single = data.entrySet().iterator().next();
                return Collections.singletonMap(single.getKey(), single.getValue());
            default:
                return Collections.unmodifiableMap(new HashMap<>(data));
        }
    }
}
