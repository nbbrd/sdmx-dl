package sdmxdl.provider.ri.authenticators;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import sdmxdl.HasExpiration;

import java.net.PasswordAuthentication;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@lombok.Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Credentials implements HasExpiration {

    @NonNull
    PasswordAuthentication credentials;

    @NonNull
    Instant expirationTime;

    public boolean isEmpty() {
        return credentials.getUserName().isEmpty() && credentials.getPassword().length == 0;
    }

    @StaticFactoryMethod
    public static @NonNull Credentials of(@NonNull PasswordAuthentication credentials, @NonNull Clock clock, @NonNull Duration ttl) {
        return new Credentials(credentials, clock.instant().plus(ttl));
    }

    @StaticFactoryMethod
    public static @NonNull Credentials empty(@NonNull Clock clock, @NonNull Duration ttl) {
        return new Credentials(EMPTY, clock.instant().plus(ttl));
    }

    private static final PasswordAuthentication EMPTY = new PasswordAuthentication("", new char[0]);
}
