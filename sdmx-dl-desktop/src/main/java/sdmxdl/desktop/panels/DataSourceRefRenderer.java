package sdmxdl.desktop.panels;

import j2html.tags.DomContent;
import sdmxdl.CatalogRef;
import sdmxdl.Confidentiality;
import sdmxdl.Languages;
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
        return html(small(webSource != null ? id(webSource, value.getCatalog(), value.getLanguages()) : id(value.getSource(), value.getCatalog(), value.getLanguages())), text(" "), text(text)).render();
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
                ? idAndName(webSource, ref.getCatalog(), ref.getLanguages())
                : id(ref.getSource(), ref.getCatalog(), ref.getLanguages());
    }

    private static DomContent id(String value, CatalogRef catalogId, Languages languages) {
        return labelTag(value
                        + (!catalogId.equals(CatalogRef.NO_CATALOG) ? "/" + catalogId : "")
                        + (!languages.equals(Languages.ANY) ? " (" + languages + ")" : ""),
                WebSourceRenderer.getColor(Confidentiality.SECRET)
        );
    }

    private static DomContent idAndName(WebSource value, CatalogRef catalogId, Languages languages) {
        return join(id(value, catalogId, languages), text(" "), name(value, catalogId, languages));
    }

    private static DomContent id(WebSource value, CatalogRef catalogId, Languages languages) {
        return labelTag(value.getId()
                        + (!catalogId.equals(CatalogRef.NO_CATALOG) ? "/" + catalogId : "")
                        + (!languages.equals(Languages.ANY) ? " (" + languages + ")" : ""),
                WebSourceRenderer.getColor(value.getConfidentiality())
        );
    }

    private static DomContent name(WebSource value, CatalogRef catalogId, Languages languages) {
        return text(value.getName(languages));
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
