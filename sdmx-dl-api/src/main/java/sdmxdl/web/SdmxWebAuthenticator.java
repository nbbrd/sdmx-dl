package sdmxdl.web;

import internal.sdmxdl.web.NoOpWebAuthenticator;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.PasswordAuthentication;

public interface SdmxWebAuthenticator {

    @Nullable
    PasswordAuthentication getPasswordAuthentication(@NonNull SdmxWebSource source);

    void invalidate(@NonNull SdmxWebSource source);

    @NonNull
    static SdmxWebAuthenticator noOp() {
        return NoOpWebAuthenticator.INSTANCE;
    }
}
