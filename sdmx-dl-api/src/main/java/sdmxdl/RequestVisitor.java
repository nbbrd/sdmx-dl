package sdmxdl;

import lombok.NonNull;

/**
 * Visitor over the concrete types of {@link Request}.
 * <p>
 * Evolution policy: new request types are added as default methods that throw an
 * {@link UnsupportedOperationException}, so that adding a request type does not break existing
 * implementations.
 *
 * @param <T> the type of the result; implementations decide whether it may be null
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
