package sdmxdl.web.spi;

import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceFilter;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.net.PasswordAuthentication;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        loaderName = "internal.util.WebAuthenticatorLoader"
)
@ThreadSafe
public interface WebAuthenticator {

    @ServiceFilter
    boolean isAvailable();

    @Nullable
    PasswordAuthentication getPasswordAuthentication(@NonNull SdmxWebSource source) throws IOException;

    void invalidate(@NonNull SdmxWebSource source) throws IOException;
}
