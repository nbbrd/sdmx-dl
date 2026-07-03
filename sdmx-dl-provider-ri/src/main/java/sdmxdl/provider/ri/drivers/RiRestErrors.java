package sdmxdl.provider.ri.drivers;

import lombok.NonNull;
import nbbrd.io.http.ext.ThrowingStatusException;
import sdmxdl.provider.web.RestErrorMapping;

public interface RiRestErrors {

    @NonNull RestErrorMapping getFlowsError(@NonNull ThrowingStatusException ex);

    @NonNull RestErrorMapping getFlowError(@NonNull ThrowingStatusException ex);

    @NonNull RestErrorMapping getStructureError(@NonNull ThrowingStatusException ex);

    @NonNull RestErrorMapping getDataError(@NonNull ThrowingStatusException ex);

    @NonNull RestErrorMapping getCodelistError(@NonNull ThrowingStatusException ex);
}
