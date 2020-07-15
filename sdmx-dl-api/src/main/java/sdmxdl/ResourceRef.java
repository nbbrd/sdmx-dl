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


/**
 * Abstract identifier of a resource.
 *
 * @author Philippe Charles
 */
public interface ResourceRef<T extends ResourceRef<T>> {

    String ALL_AGENCIES = "all";
    String LATEST_VERSION = "latest";

    @NonNull
    String getAgency();

    @NonNull
    String getId();

    @NonNull
    String getVersion();

    default boolean containsRef(@NonNull Resource<T> that) {
        return contains(that.getRef());
    }

    default boolean contains(@NonNull T that) {
        return (this.getAgency().equals(ALL_AGENCIES) || this.getAgency().equals(that.getAgency()))
                && (this.getId().equals(that.getId()))
                && (this.getVersion().equals(LATEST_VERSION) || this.getVersion().equals(that.getVersion()));
    }

    default boolean equalsRef(@NonNull Resource<T> that) {
        return equals(that.getRef());
    }
}
