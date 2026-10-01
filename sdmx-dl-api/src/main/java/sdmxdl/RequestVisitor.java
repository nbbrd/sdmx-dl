package sdmxdl;

import lombok.NonNull;

/**
 * Visitor over the concrete types of {@link Request}.
 *
 * @param <T> the type of the result
 */
public interface RequestVisitor<T> {

    T visitDatabases(@NonNull DatabasesRequest request);

    T visitFlows(@NonNull FlowsRequest request);

    T visitMeta(@NonNull MetaRequest request);

    T visitData(@NonNull DataRequest request);

    T visitDimensions(@NonNull DimensionsRequest request);

    T visitAttributes(@NonNull AttributesRequest request);

    T visitCodes(@NonNull CodesRequest request);

    T visitAvailability(@NonNull AvailabilityRequest request);
}
