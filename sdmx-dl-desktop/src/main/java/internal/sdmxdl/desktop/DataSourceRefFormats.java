package internal.sdmxdl.desktop;

import j2html.tags.DomContent;
import j2html.tags.Text;
import sdmxdl.desktop.DataSourceRef;
import sdmxdl.desktop.FlowStruct;

import static j2html.TagCreator.*;

public class DataSourceRefFormats {

    public static String toText(DataSourceRef ref, FlowStruct fs) {
        return fs != null ? fs.getDataflow().getName() : ref.getFlow().toString();
    }

    public static String toTooltipText(DataSourceRef ref, FlowStruct fs) {
        return html(
                table(
                        tr(th("Source:").withStyle("text-align:right"), td(htmlSource(ref))),
                        tr(th("Flow:").withStyle("text-align:right"), td(htmlFlow(ref, fs))),
                        tr(th("Dimensions:").withStyle("text-align:right"), td(htmlDimensions(ref)))
                )
        ).render();
    }

    private static Text htmlSource(DataSourceRef ref) {
        return text(ref.getSource());
    }

    private static DomContent htmlFlow(DataSourceRef ref, FlowStruct fs) {
        return fs != null
                ? each(text(fs.getDataflow().getRef().toString()), br(), text(fs.getDataflow().getName()))
                : text(ref.getFlow().toString());
    }

    private static DomContent htmlDimensions(DataSourceRef ref) {
        return each(ref.getDimensions(), dimension -> each(text(dimension), br()));
    }
}
