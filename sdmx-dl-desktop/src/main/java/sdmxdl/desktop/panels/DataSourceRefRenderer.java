package sdmxdl.desktop.panels;

import j2html.tags.DomContent;
import sdmxdl.Confidentiality;
import sdmxdl.Languages;
import sdmxdl.Options;
import sdmxdl.desktop.DataSourceRef;
import sdmxdl.desktop.FlowStruct;
import sdmxdl.desktop.Sdmxdl;
import sdmxdl.web.WebSource;

import javax.swing.*;

import static internal.sdmxdl.desktop.util.Html4Swing.labelTag;
import static j2html.TagCreator.*;

public enum DataSourceRefRenderer implements Renderer<DataSourceRef> {

    INSTANCE;

    @Override
    public String toText(DataSourceRef value, Runnable onUpdate) {
        FlowStruct flowStruct = Sdmxdl.INSTANCE.getFlowStructSupport().getOrNull(value, onUpdate);
        String text = flowStruct != null ? flowStruct.getFlow().getName() : value.getFlow();

        WebSource webSource = Sdmxdl.lookupWebSource(value);
        return html(small(webSource != null ? id(webSource, value.toOptions()) : id(value.getSource(), value.toOptions())), text(" "), text(text)).render();
    }

    @Override
    public Icon toIcon(DataSourceRef value, Runnable onUpdate) {
//        return Sdmxdl.INSTANCE.getIconSupport().getIcon(value, 16, onUpdate);
        return null;
    }

    @Override
    public String toTooltip(DataSourceRef value, Runnable onUpdate) {
        FlowStruct flowStruct = Sdmxdl.INSTANCE.getFlowStructSupport().getOrNull(value, onUpdate);
        return html(htmlSource(value), br(), htmlFlow(value, flowStruct), br(), htmlDimensions(value)).render();
    }

    private static DomContent htmlSource(DataSourceRef ref) {
        WebSource webSource = Sdmxdl.lookupWebSource(ref);
        return webSource != null
                ? idAndName(webSource, ref.toOptions())
                : id(ref.getSource(), ref.toOptions());
    }

    private static DomContent id(String value, Options options) {
        return labelTag(value
                        + (options.getCatalogId() != null && !options.getCatalogId().isEmpty() ? "/" + options.getCatalogId() : "")
                        + (!options.getLanguages().equals(Languages.ANY) ? " (" + options.getLanguages() + ")" : ""),
                WebSourceRenderer.getColor(Confidentiality.SECRET)
        );
    }

    private static DomContent idAndName(WebSource value, Options options) {
        return join(id(value, options), text(" "), name(value, options));
    }

    private static DomContent id(WebSource value, Options options) {
        return labelTag(value.getId()
                        + (options.getCatalogId() != null && !options.getCatalogId().isEmpty() ? "/" + options.getCatalogId() : "")
                        + (!options.getLanguages().equals(Languages.ANY) ? " (" + options.getLanguages() + ")" : ""),
                WebSourceRenderer.getColor(value.getConfidentiality())
        );
    }

    private static DomContent name(WebSource value, Options options) {
        return text(value.getName(options.getLanguages()));
    }

    private static DomContent htmlFlow(DataSourceRef ref, FlowStruct fs) {
        return fs != null
                ? join(labelTag(fs.getFlow().getRef().toString(), UIManager.getColor("Tree.icon.leafColor")), text(" "), text(fs.getFlow().getName()))
                : labelTag(ref.getFlow(), UIManager.getColor("Tree.icon.leafColor"));
    }

    private static DomContent htmlDimensions(DataSourceRef ref) {
        return each(ref.getDimensions(), dimension -> each(text(dimension), br()));
    }
}
