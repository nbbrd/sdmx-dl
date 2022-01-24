package internal.util.http;

import nbbrd.design.StaticFactoryMethod;

import java.net.Proxy;
import java.net.URL;

@lombok.experimental.UtilityClass
public class HttpConstants {

    public static final String HTTP_ACCEPT_HEADER = "Accept";

    public static final String HTTP_ACCEPT_LANGUAGE_HEADER = "Accept-Language";

    public static final String HTTP_ACCEPT_ENCODING_HEADER = "Accept-Encoding";

    public static final String HTTP_LOCATION_HEADER = "Location";

    public static final String HTTP_AUTHORIZATION_HEADER = "Authorization";

    public static final String HTTP_AUTHENTICATE_HEADER = "WWW-Authenticate";

    public static final String HTTP_USER_AGENT_HEADER = "User-Agent";

    public static final String HTTP_CONTENT_TYPE_HEADER = "Content-Type";

    public static final String HTTP_CONTENT_ENCODING_HEADER = "Content-Encoding";

    // https://en.wikipedia.org/wiki/Downgrade_attack
    public static boolean isDowngradingProtocolOnRedirect(URL oldUrl, URL newUrl) {
        return isHttpsProtocol(oldUrl) && !isHttpsProtocol(newUrl);
    }

    public static boolean isHttpsProtocol(URL oldUrl) {
        return "https".equalsIgnoreCase(oldUrl.getProtocol());
    }

    public static boolean isHttpProtocol(URL oldUrl) {
        return "http".equalsIgnoreCase(oldUrl.getProtocol());
    }

    public static boolean hasProxy(Proxy proxy) {
        return !proxy.equals(Proxy.NO_PROXY);
    }

    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
    public enum ResponseType {

        INFORMATIONAL, SUCCESSFUL, REDIRECTION, CLIENT_ERROR, SERVER_ERROR, UNKNOWN;

        @StaticFactoryMethod
        public static ResponseType ofResponseCode(int code) {
            switch (code / 100) {
                case 1:
                    return INFORMATIONAL;
                case 2:
                    return SUCCESSFUL;
                case 3:
                    return REDIRECTION;
                case 4:
                    return CLIENT_ERROR;
                case 5:
                    return SERVER_ERROR;
                default:
                    return UNKNOWN;
            }
        }
    }
}
