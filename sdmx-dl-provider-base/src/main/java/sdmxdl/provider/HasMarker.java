package sdmxdl.provider;

import lombok.NonNull;
import sdmxdl.DataRepository;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebSource;

public interface HasMarker {

    @NonNull Marker getMarker();

    static @NonNull Marker of(@NonNull SdmxWebSource s) {
        return Marker.parse(s.getDriver() + ":" + s.getId());
    }

    static @NonNull Marker of(@NonNull SdmxFileSource s) {
        return Marker.parse(s.getData().getPath());
    }

    static @NonNull Marker of(@NonNull DataRepository repository) {
        return Marker.parse(repository.getName());
    }
}
