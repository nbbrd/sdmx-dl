package sdmxdl.provider.web;

import sdmxdl.StructureRef;
import sdmxdl.provider.DataRef;

import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

@lombok.experimental.UtilityClass
public class WebEvents {

    public static String onQuery(String method, URI uri, Proxy proxy) {
        return onQuery(method, uri.toString(), proxy);
    }

    public static String onQuery(URL url, Proxy proxy) {
        return onQuery("GET", url.toString(), proxy);
    }

    private static String onQuery(String method, String query, Proxy proxy) {
        String result = "HTTP " + method + " " + query;
        if (!proxy.equals(Proxy.NO_PROXY)) {
            result += " with proxy '" + proxy + "'";
        }
        return result;
    }

    public static String onRedirection(URL oldUrl, URL newUrl) {
        return "Redirecting to " + newUrl;
    }

    public static String onDriverSelection(String driverId) {
        return "Using driver '" + driverId + "'";
    }

    public static String onFlowsQuery() {
        return "Querying flows";
    }

    public static String onStructureQuery(StructureRef ref) {
        return "Querying structure for '" + ref + "'";
    }

    public static String onDataQuery(DataRef ref) {
        return "Querying data with key '" + ref.getQuery().getKey() + "' detail=" + ref.getQuery().getDetail();
    }

    public static String onDataReceived(long seriesCount, long obsCount, long elapsedMs) {
        return String.format(Locale.ROOT, "Received %d series, %d observations (%dms)", seriesCount, obsCount, elapsedMs);
    }
}
