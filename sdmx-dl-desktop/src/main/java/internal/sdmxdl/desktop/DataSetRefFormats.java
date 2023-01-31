package internal.sdmxdl.desktop;

import sdmxdl.Dimension;
import sdmxdl.desktop.DataSetRef;
import sdmxdl.desktop.FlowStruct;

public class DataSetRefFormats {

    public static String toText(DataSetRef ref, FlowStruct fs) {
        return fs != null
                ? getDimension(ref, fs).getCodelist().getCodes().get(ref.getKey().get(ref.getDimensionIndex()))
                : ref.getKey().toString();
    }

    private static Dimension getDimension(DataSetRef ref, FlowStruct fs) {
        return fs.getDataStructure().getDimensionList().get(ref.getDimensionIndex());
    }

    public static String toTooltipText(DataSetRef ref, FlowStruct fs) {
        return "<html>" +
                "<table>" +
                "<tr><th align=right>Key:</th><td>" + ref.getKey() + "</td></tr>" +
                "<tr><th align=right>Dimension:</th><td>" + (fs != null ? toTooltipText(getDimension(ref, fs)) : ref.getDimensionIndex()) + "</td></tr>" +
                "</table>";
    }

    private static String toTooltipText(Dimension dimension) {
        return dimension.getId() + "<br>" + dimension.getLabel();
    }
}
