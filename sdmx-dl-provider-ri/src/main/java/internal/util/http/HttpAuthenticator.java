package internal.util.http;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.PasswordAuthentication;
import java.net.URL;

public interface HttpAuthenticator {

    @Nullable PasswordAuthentication getPasswordAuthentication(@NonNull URL url);

    void invalidate(@NonNull URL url);

    static @NonNull HttpAuthenticator noOp() {
        return HttpImpl.Authenticators.NONE;
    }
}
