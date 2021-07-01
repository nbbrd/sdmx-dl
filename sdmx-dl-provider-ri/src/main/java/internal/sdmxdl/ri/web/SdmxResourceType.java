package internal.sdmxdl.ri.web;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.Optional;

public enum SdmxResourceType {

    DATA, DATAFLOW, DATASTRUCTURE;

    public <T> @NonNull Optional<T> applyOn(@NonNull Map<SdmxResourceType, T> map) {
        return Optional.ofNullable(map.get(this));
    }
}
