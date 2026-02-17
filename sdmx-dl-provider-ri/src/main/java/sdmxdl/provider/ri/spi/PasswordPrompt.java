package sdmxdl.provider.ri.spi;

import internal.util.credentials.NoOpPasswordPrompt;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.ThreadSafe;
import nbbrd.service.*;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.PasswordAuthentication;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        fallback = NoOpPasswordPrompt.class,
        loaderName = "internal.{{canonicalName}}Loader"
)
@ThreadSafe
public interface PasswordPrompt {

    @ServiceId(pattern = ServiceId.SCREAMING_SNAKE_CASE)
    @NonNull
    String getPromptId();

    @ServiceFilter
    boolean isPromptAvailable();

    @ServiceSorter(reverse = true)
    int getPromptRank();

    @Nullable PasswordAuthentication promptCredentials(@NonNull String caption, @NonNull String message) throws IOException;

    @StaticFactoryMethod
    static @NonNull PasswordPrompt noOp() {
        return NoOpPasswordPrompt.INSTANCE;
    }

    int UNKNOWN_PROMPT_RANK = -1;
}
