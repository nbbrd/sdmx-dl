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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Abstract identifier of a resource.
 *
 * @author Philippe Charles
 */
//@SealedClass
public abstract class ResourceRef<T extends ResourceRef<T>> {

    public static final String ALL_AGENCIES = "all";
    public static final String LATEST_VERSION = "latest";

    @NonNull
    public abstract String getAgency();

    @NonNull
    public abstract String getId();

    @NonNull
    public abstract String getVersion();

    public boolean containsRef(@NonNull Resource<T> that) {
        return contains(that.getRef());
    }

    public boolean contains(@NonNull T that) {
        return (this.getAgency().equals(ALL_AGENCIES) || this.getAgency().equals(that.getAgency()))
                && (this.getId().equals(that.getId()))
                && (this.getVersion().equals(LATEST_VERSION) || this.getVersion().equals(that.getVersion()));
    }

    public boolean equalsRef(@NonNull Resource<T> that) {
        return equals(that.getRef());
    }

    @NonNull
    protected static String toString(ResourceRef<?> ref) {
        return ref.getAgency() + "," + ref.getId() + "," + ref.getVersion();
    }

    @NonNull
    protected static <T extends ResourceRef<T>> T parse(@NonNull String input, @NonNull Factory<T> factory) throws IllegalArgumentException {
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
    protected static <T extends ResourceRef<T>> T of(@Nullable String agencyId, @NonNull String id, @Nullable String version, @NonNull Factory<T> factory) throws IllegalArgumentException {
        if (id.contains(",")) {
            throw new IllegalArgumentException(id);
        }
        return factory.create(nullOrEmptyToDefault(agencyId, ALL_AGENCIES), id, nullOrEmptyToDefault(version, LATEST_VERSION));
    }

    protected interface Factory<T extends ResourceRef<T>> {

        @NonNull
        T create(@NonNull String agencyId, @NonNull String id, @NonNull String version);
    }

    @NonNull
    private static String emptyToDefault(@NonNull String input, @NonNull String defaultValue) {
        return input.isEmpty() ? defaultValue : input;
    }

    @NonNull
    private static String nullOrEmptyToDefault(@Nullable String input, @NonNull String defaultValue) {
        return input == null || input.isEmpty() ? defaultValue : input;
    }
}
