package internal.sdmxdl.desktop;

import sdmxdl.desktop.DataSourceRef;
import sdmxdl.desktop.FlowStruct;

public class DataSourceRefFormats {

    public static String toText(DataSourceRef ref, FlowStruct fs) {
        return fs != null ? fs.getDataflow().getName() : ref.getFlow().toString();
    }

    public static String toTooltipText(DataSourceRef ref, FlowStruct fs) {
        return "<html>" +
                "<table>" +
                "<tr><th align=right>Source:</th><td>" + ref.getSource() + "</td></tr>" +
                "<tr><th align=right>Flow:</th><td>" + (fs != null ? (fs.getDataflow().getRef() + "<br>" + fs.getDataflow().getName()) : ref.getFlow()) + "</td></tr>" +
                "<tr><th align=right>Dimensions:</th><td>" + String.join("<br>", ref.getDimensions()) + "</td></tr>" +
                "</table>";
    }
}
