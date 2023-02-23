package sdmxdl.provider;

import lombok.NonNull;
import sdmxdl.DataRepository;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebSource;

@lombok.Value(staticConstructor = "of")
public class Marker implements CharSequence {

    @lombok.experimental.Delegate(types = CharSequence.class)
    @NonNull String content;

    @Override
    public String toString() {
        return content;
    }

    public static @NonNull Marker of(@NonNull SdmxWebSource s) {
        return new Marker(s.getDriver() + ":" + s.getId());
    }

    public static @NonNull Marker of(@NonNull SdmxFileSource s) {
        return new Marker(s.getData().getPath());
    }

    public static @NonNull Marker of(@NonNull DataRepository repository) {
        return new Marker(repository.getName());
    }
}
