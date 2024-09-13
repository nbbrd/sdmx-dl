package internal.sdmxdl.desktop;

import internal.sdmxdl.desktop.util.Html4Swing;
import j2html.tags.DomContent;
import sdmxdl.desktop.DataSourceRef;
import sdmxdl.desktop.FlowStruct;
import sdmxdl.desktop.Renderer;
import sdmxdl.desktop.Sdmxdl;
import sdmxdl.web.WebSource;

import static j2html.TagCreator.*;

public class DataSourceRefFormats {

    public static String toText(DataSourceRef ref, FlowStruct fs) {
        return fs != null ? fs.getFlow().getName() : ref.getFlow().toString();
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

    private static DomContent htmlSource(DataSourceRef ref) {
        WebSource webSource = Sdmxdl.lookupWebSource(ref);
        return webSource != null
                ? join(Html4Swing.labelTag(webSource.getId(), Renderer.getColor(webSource.getConfidentiality())), text(" / " + ref.toOptions()))
                : text(ref.getSource() + " / " + ref.toOptions());
    }

    private static DomContent htmlFlow(DataSourceRef ref, FlowStruct fs) {
        return fs != null
                ? each(text(fs.getFlow().getRef().toString()), br(), text(fs.getFlow().getName()))
                : text(ref.getFlow().toString());
    }

    private static DomContent htmlDimensions(DataSourceRef ref) {
        return each(ref.getDimensions(), dimension -> each(text(dimension), br()));
    }
}
