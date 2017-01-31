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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

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
@Immutable
public final class DataflowRef extends ResourceRef {

    public boolean contains(@Nonnull DataflowRef that) {
        return super.contains(that);
    }

    private DataflowRef(String agencyId, String flowId, String version) {
        super(agencyId, flowId, version);
    }

    @Nonnull
    public static DataflowRef parse(@Nonnull String input) throws IllegalArgumentException {
        return ResourceRef.parse(input, Factory.INSTANCE);
    }

    @Nonnull
    public static DataflowRef of(@Nullable String agencyId, @Nonnull String flowId, @Nullable String version) throws IllegalArgumentException {
        return ResourceRef.of(agencyId, flowId, version, Factory.INSTANCE);
    }

    private enum Factory implements RefFactory<DataflowRef> {

        INSTANCE;

        @Override
        public DataflowRef create(String agencyId, String id, String version) {
            return new DataflowRef(agencyId, id, version);
        }
    }
}
