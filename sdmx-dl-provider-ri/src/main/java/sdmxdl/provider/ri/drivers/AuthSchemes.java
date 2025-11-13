package sdmxdl.provider.ri.drivers;

public final class AuthSchemes {

    private AuthSchemes() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String BASIC_AUTH_SCHEME = "BASIC";

    public static final String MSAL_AUTH_SCHEME = "MSAL";
}
