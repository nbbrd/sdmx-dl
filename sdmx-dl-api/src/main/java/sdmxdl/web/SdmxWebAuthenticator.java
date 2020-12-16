package sdmxdl.web;

import internal.sdmxdl.web.NoOpWebAuthenticator;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.PasswordAuthentication;

@ThreadSafe
public interface SdmxWebAuthenticator {

    @Nullable
    PasswordAuthentication getPasswordAuthentication(@NonNull SdmxWebSource source);

    void invalidate(@NonNull SdmxWebSource source);

    @StaticFactoryMethod
    @NonNull
    static SdmxWebAuthenticator noOp() {
        return NoOpWebAuthenticator.INSTANCE;
    }
}
