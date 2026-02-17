package sdmxdl.web;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import sdmxdl.HasExpiration;

import java.net.PasswordAuthentication;
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
    public static @NonNull Credentials of(@NonNull PasswordAuthentication credentials, @NonNull Instant expirationTime) {
        return new Credentials(credentials, expirationTime);
    }

    @StaticFactoryMethod
    public static @NonNull Credentials empty(@NonNull Instant expirationTime) {
        return new Credentials(EMPTY, expirationTime);
    }

    private static final PasswordAuthentication EMPTY = new PasswordAuthentication("", new char[0]);
}
