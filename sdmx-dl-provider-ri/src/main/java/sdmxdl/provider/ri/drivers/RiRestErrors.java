package sdmxdl.provider.ri.drivers;

import internal.util.http.HttpResponseException;
import lombok.NonNull;
import sdmxdl.provider.web.RestErrorMapping;

public interface RiRestErrors {

    @NonNull RestErrorMapping getFlowsError(@NonNull HttpResponseException ex);

    @NonNull RestErrorMapping getFlowError(@NonNull HttpResponseException ex);

    @NonNull RestErrorMapping getStructureError(@NonNull HttpResponseException ex);

    @NonNull RestErrorMapping getDataError(@NonNull HttpResponseException ex);

    @NonNull RestErrorMapping getCodelistError(@NonNull HttpResponseException ex);
}
