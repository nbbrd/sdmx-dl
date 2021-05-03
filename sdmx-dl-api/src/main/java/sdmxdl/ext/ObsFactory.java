package sdmxdl.ext;

import nbbrd.design.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataStructure;

@ThreadSafe
public interface ObsFactory {

    @NonNull
    ObsParser getObsParser(@NonNull DataStructure dsd);
}
