package sdmxdl.provider;

import lombok.NonNull;
import sdmxdl.DataRepository;
import sdmxdl.file.FileSource;
import sdmxdl.web.WebSource;

public interface HasMarker {

    @NonNull Marker getMarker();

    static @NonNull Marker of(@NonNull WebSource s) {
        return Marker.parse(s.getDriver() + ":" + s.getId());
    }

    static @NonNull Marker of(@NonNull FileSource s) {
        return Marker.parse(s.getData().getPath());
    }

    static @NonNull Marker of(@NonNull DataRepository repository) {
        return Marker.parse(repository.getName());
    }
}
