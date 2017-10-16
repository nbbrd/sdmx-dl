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
package internal.util;

import be.nbb.sdmx.facade.ResourceRef;
import static be.nbb.sdmx.facade.ResourceRef.ALL_AGENCIES;
import static be.nbb.sdmx.facade.ResourceRef.LATEST_VERSION;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class ResourceRefs {

    @Nonnull
    private static String emptyToDefault(@Nonnull String input, @Nonnull String defaultValue) {
        return input.isEmpty() ? defaultValue : input;
    }

    @Nonnull
    private static String nullOrEmptyToDefault(@Nullable String input, @Nonnull String defaultValue) {
        return input == null || input.isEmpty() ? defaultValue : input;
    }

    @Nonnull
    public static String toString(ResourceRef ref) {
        return ref.getAgencyId() + "," + ref.getId() + "," + ref.getVersion();
    }

    @Nonnull
    public static <T extends ResourceRef> T parse(@Nonnull String input, @Nonnull RefFactory<T> factory) throws IllegalArgumentException {
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

    @Nonnull
    public static <T extends ResourceRef> T of(@Nullable String agencyId, @Nonnull String id, @Nullable String version, @Nonnull RefFactory<T> factory) throws IllegalArgumentException {
        if (id.contains(",")) {
            throw new IllegalArgumentException(id);
        }
        return factory.create(nullOrEmptyToDefault(agencyId, ALL_AGENCIES), id, nullOrEmptyToDefault(version, LATEST_VERSION));
    }

    public interface RefFactory<T extends ResourceRef> {

        @Nonnull
        T create(@Nonnull String agencyId, @Nonnull String id, @Nonnull String version);
    }
}
