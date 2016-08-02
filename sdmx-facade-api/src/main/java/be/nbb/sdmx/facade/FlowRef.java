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

import java.util.Objects;
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
public final class FlowRef {

    public static final String ALL_AGENCIES = "all";
    public static final String LATEST_VERSION = "latest";

    private final String agencyId;
    private final String flowId;
    private final String version;

    private FlowRef(@Nonnull String agencyId, @Nonnull String flowId, @Nonnull String version) {
        this.agencyId = agencyId;
        this.flowId = flowId;
        this.version = version;
    }

    @Nonnull
    public String getAgencyId() {
        return agencyId;
    }

    @Nonnull
    public String getFlowId() {
        return flowId;
    }

    @Nonnull
    public String getVersion() {
        return version;
    }

    public boolean contains(@Nonnull FlowRef that) {
        return (this.agencyId.equals(ALL_AGENCIES) || this.agencyId.equals(that.agencyId))
                && (this.flowId.equals(that.flowId))
                && (this.version.equals(LATEST_VERSION) || this.version.equals(that.version));
    }

    @Override
    public String toString() {
        return agencyId + "," + flowId + "," + version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(agencyId, flowId, version);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof FlowRef && equals((FlowRef) obj));
    }

    private boolean equals(FlowRef that) {
        return this.agencyId.equals(that.agencyId)
                && this.flowId.equals(that.flowId)
                && this.version.equals(that.version);
    }

    @Nonnull
    public static FlowRef parse(@Nonnull String input) throws IllegalArgumentException {
        String[] items = input.split(",", -1);
        switch (items.length) {
            case 3:
                return new FlowRef(emptyToDefault(items[0], ALL_AGENCIES), items[1], emptyToDefault(items[2], LATEST_VERSION));
            case 2:
                return new FlowRef(emptyToDefault(items[0], ALL_AGENCIES), items[1], LATEST_VERSION);
            case 1:
                return new FlowRef(ALL_AGENCIES, items[0], LATEST_VERSION);
            default:
                throw new IllegalArgumentException(input);
        }
    }

    @Nonnull
    public static FlowRef of(@Nullable String agencyId, @Nonnull String flowId, @Nullable String version) throws IllegalArgumentException {
        if (flowId.contains(",")) {
            throw new IllegalArgumentException(flowId);
        }
        return new FlowRef(nullOrEmptyToDefault(agencyId, ALL_AGENCIES), flowId, nullOrEmptyToDefault(version, LATEST_VERSION));
    }

    @Nonnull
    private static String emptyToDefault(@Nonnull String input, @Nonnull String defaultValue) {
        return input.isEmpty() ? defaultValue : input;
    }

    @Nonnull
    private static String nullOrEmptyToDefault(@Nullable String input, @Nonnull String defaultValue) {
        return input == null || input.isEmpty() ? defaultValue : input;
    }
}
