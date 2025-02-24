package sdmxdl.desktop.panels;

import com.formdev.flatlaf.util.ColorFunctions;
import internal.Colors;
import j2html.tags.DomContent;
import j2html.tags.specialized.KbdTag;
import sdmxdl.DatabaseRef;
import sdmxdl.Flow;
import sdmxdl.FlowRef;
import sdmxdl.Languages;
import sdmxdl.desktop.DataSourceRef;
import sdmxdl.desktop.FlowStruct;
import sdmxdl.desktop.Sdmxdl;
import sdmxdl.web.WebSource;

import javax.swing.*;
import java.awt.*;

import static internal.sdmxdl.desktop.util.Html4Swing.labelTag;
import static j2html.TagCreator.*;

public enum DataSourceRefRenderer implements Renderer<DataSourceRef> {

    INSTANCE;

    @Override
    public String toText(DataSourceRef value, Runnable onUpdate) {
        return html(small(sourceId(value)), text(" "), flowName(value, onUpdate)).render();
    }

    @Override
    public Icon toIcon(DataSourceRef value, Runnable onUpdate) {
//        return Sdmxdl.INSTANCE.getIconSupport().getIcon(value, 16, onUpdate);
        return null;
    }

    @Override
    public String toTooltip(DataSourceRef value, Runnable onUpdate) {
        return html(sourceIdAndName(value), br(), flowRefAndName(value, onUpdate), br(), htmlDimensions(value)).render();
    }

    private static DomContent sourceId(DataSourceRef ref) {
        WebSource s = ref.toWebSource(Sdmxdl.INSTANCE.getSdmxManager());
        if (s != null) {
            DomContent sourceId = sourceId(s, ref.getDatabase(), ref.getLanguages());
            return ref.isDebug()
                    ? each(debugPrefix(getPrefixColor(getColor(s))), sourceId)
                    : sourceId;
        } else {
            DomContent sourceId = sourceId(ref.getSource(), ref.getDatabase(), ref.getLanguages());
            return ref.isDebug()
                    ? each(debugPrefix(getFallbackColor()), sourceId)
                    : sourceId;
        }
    }

    private static DomContent sourceIdAndName(DataSourceRef ref) {
        WebSource s = ref.toWebSource(Sdmxdl.INSTANCE.getSdmxManager());
        if (s != null) {
            DomContent sourceIdAndName = sourceIdAndName(s, ref.getDatabase(), ref.getLanguages());
            return ref.isDebug()
                    ? each(debugPrefix(getPrefixColor(getColor(s))), sourceIdAndName)
                    : sourceIdAndName;
        } else {
            DomContent sourceId = sourceId(ref.getSource(), ref.getDatabase(), ref.getLanguages());
            return ref.isDebug()
                    ? each(debugPrefix(getFallbackColor()), sourceId)
                    : sourceId;
        }
    }

    private static DomContent sourceId(String value, DatabaseRef database, Languages languages) {
        return labelTag(value
                        + (!database.equals(DatabaseRef.NO_DATABASE) ? "/" + database : "")
                        + (!languages.equals(Languages.ANY) ? " (" + languages + ")" : ""),
                getFallbackColor()
        );
    }

    private static DomContent sourceIdAndName(WebSource value, DatabaseRef database, Languages languages) {
        return join(sourceId(value, database, languages), name(value, database, languages));
    }

    private static DomContent sourceId(WebSource value, DatabaseRef database, Languages languages) {
        return labelTag(value.getId()
                        + (!database.equals(DatabaseRef.NO_DATABASE) ? "/" + database : "")
                        + (!languages.equals(Languages.ANY) ? " (" + languages + ")" : ""),
                getColor(value)
        );
    }

    private static DomContent name(WebSource value, DatabaseRef database, Languages languages) {
        return text(value.getName(languages));
    }

    private DomContent flowName(DataSourceRef value, Runnable onUpdate) {
        FlowStruct flowStruct = Sdmxdl.INSTANCE.getFlowStructSupport().getOrNull(value, onUpdate);
        return text(flowStruct != null ? flowStruct.getFlow().getName() : value.getFlow());
    }

    private static DomContent flowRefAndName(DataSourceRef ref, Runnable onUpdate) {
        FlowStruct fs = Sdmxdl.INSTANCE.getFlowStructSupport().getOrNull(ref, onUpdate);
        WebSource s = ref.toWebSource(Sdmxdl.INSTANCE.getSdmxManager());
        Color color = s != null ? getColor(s) : getFallbackColor();
        return fs != null ? flowRefAndName(fs.getFlow(), color) : flowRef(ref, color);
    }

    private static DomContent flowRefAndName(Flow flow, Color color) {
        return join(flowRef(flow.getRef(), color), text(flow.getName()));
    }

    private static DomContent flowRef(DataSourceRef ref, Color color) {
        return flowRef(ref.toFlowRef(), color);
    }

    private static DomContent flowRef(FlowRef ref, Color color) {
        Color secondary = getFallbackColor();
        return each(labelTag(ref.getAgency(), secondary), labelTag(ref.getId(), color), labelTag(ref.getVersion(), secondary));
    }

    private static DomContent htmlDimensions(DataSourceRef ref) {
        return each(ref.getDimensions(), dimension -> each(text(dimension), br()));
    }

    private static Color getColor(WebSource s) {
        return WebSourceRenderer.getColor(s.getConfidentiality());
    }

    private static Color getFallbackColor() {
        return UIManager.getColor("Tree.icon.leafColor");
    }

    private static KbdTag debugPrefix(Color color) {
        return labelTag("⏵", color);
    }

    private static Color getPrefixColor(Color color) {
        return Colors.isDark(color)
                ? ColorFunctions.lighten(color, .25f)
                : ColorFunctions.darken(color, .25f);
    }
}
