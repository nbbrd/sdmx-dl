package sdmxdl.provider.web;

import java.net.Proxy;
import java.net.URL;

@lombok.experimental.UtilityClass
public class WebEvents {

    public static String onQuery(URL url, Proxy proxy) {
        String result = "Querying " + url;
        if (!proxy.equals(Proxy.NO_PROXY)) {
            result += " with proxy '" + proxy + "'";
        }
        return result;
    }

    public static String onRedirection(URL oldUrl, URL newUrl) {
        return "Redirecting to " + newUrl;
    }
}
