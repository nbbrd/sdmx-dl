package internal.sdmxdl.desktop;

import sdmxdl.CatalogRef;
import sdmxdl.FlowRef;
import sdmxdl.Key;

public final class SdmxURI {

    private SdmxURI() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String dataSourceURI(String source, FlowRef flow, CatalogRef catalog) {
        return "sdmx-dl:/" + source + "/" + flow + (!catalog.equals(CatalogRef.NO_CATALOG) ? "?c=" + catalog : "");
    }

    public static String dataSetURI(String source, FlowRef flow, Key key, CatalogRef catalog) {
        return "sdmx-dl:/" + source + "/" + flow + "/" + key + (!catalog.equals(CatalogRef.NO_CATALOG) ? "?c=" + catalog : "");
    }
}
