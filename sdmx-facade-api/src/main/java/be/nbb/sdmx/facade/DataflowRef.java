/*
 * Copyright 2015 National Bank of Belgium
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

import internal.util.ResourceRefs;
import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Identifier of a data flow used in a data (or meta data) query.
 * <p>
 * The syntax is agency id, artefact id, version, separated by a “,”. For
 * example: AGENCY_ID,FLOW_ID,VERSION
 * <p>
 * In case the string only contains one out of these 3 elements, it is
 * considered to be the flow id, i.e. ALL,FLOW_ID,LATEST
 * <p>
 * In case the string only contains two out of these 3 elements, they are
 * considered to be the agency id and the flow id, i.e. AGENCY_ID,FLOW_ID,LATEST
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataflowRef implements ResourceRef {

    @lombok.NonNull
    private String agency;

    @lombok.NonNull
    private String id;

    @lombok.NonNull
    private String version;

    public boolean containsRef(@NonNull Dataflow that) {
        return contains(that.getRef());
    }

    public boolean contains(@NonNull DataflowRef that) {
        return (this.agency.equals(ALL_AGENCIES) || this.agency.equals(that.agency))
                && (this.id.equals(that.id))
                && (this.version.equals(LATEST_VERSION) || this.version.equals(that.version));
    }

    @Override
    public String toString() {
        return ResourceRefs.toString(this);
    }

    @NonNull
    public static DataflowRef parse(@NonNull String input) throws IllegalArgumentException {
        return ResourceRefs.parse(input, DataflowRef::new);
    }

    @NonNull
    public static DataflowRef of(@Nullable String agency, @NonNull String id, @Nullable String version) throws IllegalArgumentException {
        return ResourceRefs.of(agency, id, version, DataflowRef::new);
    }
}
