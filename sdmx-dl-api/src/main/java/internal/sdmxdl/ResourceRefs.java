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
package internal.sdmxdl;

import sdmxdl.ResourceRef;
import static sdmxdl.ResourceRef.ALL_AGENCIES;
import static sdmxdl.ResourceRef.LATEST_VERSION;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class ResourceRefs {

    @NonNull
    private static String emptyToDefault(@NonNull String input, @NonNull String defaultValue) {
        return input.isEmpty() ? defaultValue : input;
    }

    @NonNull
    private static String nullOrEmptyToDefault(@Nullable String input, @NonNull String defaultValue) {
        return input == null || input.isEmpty() ? defaultValue : input;
    }

    @NonNull
    public static String toString(ResourceRef ref) {
        return ref.getAgency() + "," + ref.getId() + "," + ref.getVersion();
    }

    @NonNull
    public static <T extends ResourceRef> T parse(@NonNull String input, @NonNull RefFactory<T> factory) throws IllegalArgumentException {
        String[] items = input.split(",", -1);
        switch (items.length) {
            case 3:
                return factory.create(emptyToDefault(items[0], ALL_AGENCIES), items[1], emptyToDefault(items[2], LATEST_VERSION));
            case 2:
                return factory.create(emptyToDefault(items[0], ALL_AGENCIES), items[1], LATEST_VERSION);
            case 1:
                return factory.create(ALL_AGENCIES, items[0], LATEST_VERSION);
            default:
                throw new IllegalArgumentException(input);
        }
    }

    @NonNull
    public static <T extends ResourceRef> T of(@Nullable String agencyId, @NonNull String id, @Nullable String version, @NonNull RefFactory<T> factory) throws IllegalArgumentException {
        if (id.contains(",")) {
            throw new IllegalArgumentException(id);
        }
        return factory.create(nullOrEmptyToDefault(agencyId, ALL_AGENCIES), id, nullOrEmptyToDefault(version, LATEST_VERSION));
    }

    public interface RefFactory<T extends ResourceRef> {

        @NonNull
        T create(@NonNull String agencyId, @NonNull String id, @NonNull String version);
    }
}
