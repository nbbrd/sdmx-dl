package internal.sdmxdl.desktop;

import j2html.tags.DomContent;
import j2html.tags.Text;
import sdmxdl.Dimension;
import sdmxdl.desktop.DataSetRef;
import sdmxdl.desktop.FlowStruct;

import static j2html.TagCreator.*;

public class DataSetRefFormats {

    public static String toText(DataSetRef ref, FlowStruct fs) {
        return fs != null
                ? getDimension(ref, fs).getCodelist().getCodes().get(ref.getKey().get(ref.getDimensionIndex()))
                : getKeyText(ref);
    }

    public static String toTooltipText(DataSetRef ref, FlowStruct fs) {
        return html(
                table(
                        tr(th("Key:").withStyle("text-align:right"), td(getKeyText(ref))),
                        tr(th("Dimension:").withStyle("text-align:right"), td(htmlDimension(ref, fs)))
                )
        ).render();
    }

    private static Dimension getDimension(DataSetRef ref, FlowStruct fs) {
        return fs.getDataStructure().getDimensionList().get(ref.getDimensionIndex());
    }

    private static String getKeyText(DataSetRef ref) {
        return ref.getKey().toString();
    }

    private static DomContent htmlDimension(DataSetRef ref, FlowStruct fs) {
        return fs != null
                ? htmlDimension(getDimension(ref, fs))
                : htmlDimension(ref);
    }

    private static DomContent htmlDimension(Dimension dimension) {
        return each(text(dimension.getId()), br(), text(dimension.getLabel()));
    }

    private static Text htmlDimension(DataSetRef ref) {
        return text(String.valueOf(ref.getDimensionIndex()));
    }
}
