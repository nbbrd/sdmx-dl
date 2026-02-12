package internal.util.credentials;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.provider.ri.spi.PasswordPrompt;

import java.net.PasswordAuthentication;

public enum NoOpPasswordPrompt implements PasswordPrompt {

    INSTANCE;

    @Override
    public @NonNull String getPromptId() {
        return "NO_OP";
    }

    @Override
    public boolean isPromptAvailable() {
        return true;
    }

    @Override
    public int getPromptRank() {
        return UNKNOWN_PROMPT_RANK;
    }

    @Override
    public @Nullable PasswordAuthentication promptCredentials(@NonNull String caption, @NonNull String message) {
        return null;
    }
}
