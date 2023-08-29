package sdmxdl.provider.ri.drivers;

import internal.util.http.HttpResponseException;
import lombok.NonNull;
import sdmxdl.provider.web.RestErrorMapping;

public class Sdmx21RestErrors implements RiRestErrors {

    public static final Sdmx21RestErrors DEFAULT = new Sdmx21RestErrors();

    @Override
    public @NonNull RestErrorMapping getFlowsError(@NonNull HttpResponseException ex) {
        return RestErrorMapping.getByHttpCode(ex.getResponseCode());
    }

    @Override
    public @NonNull RestErrorMapping getFlowError(@NonNull HttpResponseException ex) {
        return RestErrorMapping.getByHttpCode(ex.getResponseCode());
    }

    @Override
    public @NonNull RestErrorMapping getStructureError(@NonNull HttpResponseException ex) {
        return RestErrorMapping.getByHttpCode(ex.getResponseCode());
    }

    @Override
    public @NonNull RestErrorMapping getDataError(@NonNull HttpResponseException ex) {
        return RestErrorMapping.getByHttpCode(ex.getResponseCode());
    }

    @Override
    public @NonNull RestErrorMapping getCodelistError(@NonNull HttpResponseException ex) {
        return RestErrorMapping.getByHttpCode(ex.getResponseCode());
    }
}
