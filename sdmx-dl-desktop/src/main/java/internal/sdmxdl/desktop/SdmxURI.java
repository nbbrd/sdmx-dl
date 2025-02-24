package internal.sdmxdl.desktop;

import sdmxdl.DatabaseRef;
import sdmxdl.FlowRef;
import sdmxdl.Key;

public final class SdmxURI {

    private SdmxURI() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String dataSourceURI(String source, FlowRef flow, DatabaseRef database) {
        return "sdmx-dl:/" + source + "/" + flow + (!database.equals(DatabaseRef.NO_DATABASE) ? "?d=" + database : "");
    }

    public static String dataSetURI(String source, FlowRef flow, Key key, DatabaseRef database) {
        return "sdmx-dl:/" + source + "/" + flow + "/" + key + (!database.equals(DatabaseRef.NO_DATABASE) ? "?d=" + database : "");
    }
}
