package sdmxdl.provider.ri.drivers;

import lombok.NonNull;
import nbbrd.io.http.ext.ThrowingStatusException;
import sdmxdl.provider.web.RestErrorMapping;

public class Sdmx21RestErrors implements RiRestErrors {

    public static final Sdmx21RestErrors DEFAULT = new Sdmx21RestErrors();

    @Override
    public @NonNull RestErrorMapping getFlowsError(@NonNull ThrowingStatusException ex) {
        return RestErrorMapping.getByHttpCode(ex.getResponseCode());
    }

    @Override
    public @NonNull RestErrorMapping getFlowError(@NonNull ThrowingStatusException ex) {
        return RestErrorMapping.getByHttpCode(ex.getResponseCode());
    }

    @Override
    public @NonNull RestErrorMapping getStructureError(@NonNull ThrowingStatusException ex) {
        return RestErrorMapping.getByHttpCode(ex.getResponseCode());
    }

    @Override
    public @NonNull RestErrorMapping getDataError(@NonNull ThrowingStatusException ex) {
        return RestErrorMapping.getByHttpCode(ex.getResponseCode());
    }

    @Override
    public @NonNull RestErrorMapping getCodelistError(@NonNull ThrowingStatusException ex) {
        return RestErrorMapping.getByHttpCode(ex.getResponseCode());
    }
}
