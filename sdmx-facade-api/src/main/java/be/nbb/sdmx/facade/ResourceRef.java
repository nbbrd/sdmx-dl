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
package be.nbb.sdmx.facade;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Abstract identifier of a resource.
 *
 * @author Philippe Charles
 */
public abstract class ResourceRef {

    public static final String ALL_AGENCIES = "all";
    public static final String LATEST_VERSION = "latest";

    private final String agencyId;
    private final String id;
    private final String version;

    protected ResourceRef(@Nonnull String agencyId, @Nonnull String id, @Nonnull String version) {
        this.agencyId = agencyId;
        this.id = id;
        this.version = version;
    }

    @Nonnull
    public String getAgencyId() {
        return agencyId;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public String getVersion() {
        return version;
    }

    protected boolean contains(@Nonnull ResourceRef that) {
        return (this.agencyId.equals(ALL_AGENCIES) || this.agencyId.equals(that.agencyId))
                && (this.id.equals(that.id))
                && (this.version.equals(LATEST_VERSION) || this.version.equals(that.version));
    }

    @Override
    public String toString() {
        return agencyId + "," + id + "," + version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(agencyId, id, version);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ResourceRef && equals((ResourceRef) obj));
    }

    private boolean equals(ResourceRef that) {
        return this.agencyId.equals(that.agencyId)
                && this.id.equals(that.id)
                && this.version.equals(that.version);
    }

    @Nonnull
    private static String emptyToDefault(@Nonnull String input, @Nonnull String defaultValue) {
        return input.isEmpty() ? defaultValue : input;
    }

    @Nonnull
    private static String nullOrEmptyToDefault(@Nullable String input, @Nonnull String defaultValue) {
        return input == null || input.isEmpty() ? defaultValue : input;
    }

    @Nonnull
    protected static <T extends ResourceRef> T parse(@Nonnull String input, @Nonnull RefFactory<T> factory) throws IllegalArgumentException {
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
    protected static <T extends ResourceRef> T of(@Nullable String agencyId, @Nonnull String id, @Nullable String version, @Nonnull RefFactory<T> factory) throws IllegalArgumentException {
        if (id.contains(",")) {
            throw new IllegalArgumentException(id);
        }
        return factory.create(nullOrEmptyToDefault(agencyId, ALL_AGENCIES), id, nullOrEmptyToDefault(version, LATEST_VERSION));
    }

    protected interface RefFactory<T extends ResourceRef> {

        @Nonnull
        T create(@Nonnull String agencyId, @Nonnull String id, @Nonnull String version);
    }
}
