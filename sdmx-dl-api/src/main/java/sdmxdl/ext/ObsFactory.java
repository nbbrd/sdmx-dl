package sdmxdl.ext;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataStructure;

public interface ObsFactory {

    @NonNull
    ObsParser getParser(@NonNull DataStructure dsd);
}
