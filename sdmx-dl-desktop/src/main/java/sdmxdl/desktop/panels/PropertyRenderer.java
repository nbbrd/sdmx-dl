package sdmxdl.desktop.panels;

import com.formdev.flatlaf.ui.FlatUIUtils;
import internal.sdmxdl.desktop.util.Colors;
import j2html.attributes.Attr;

import java.awt.*;

import static j2html.TagCreator.*;

public enum PropertyRenderer implements Renderer<String> {

    INSTANCE;

    @Override
    public String toHeaderText(String value, Runnable onUpdate) {
        return value;
    }

    @Override
    public String toText(String value, Runnable onUpdate) {
        int last = value.lastIndexOf(".");
        return last != -1
                ? html
                (
                        span(value.substring(0, last + 1)).attr(Attr.COLOR, getDisabledColor()),
                        text(value.substring(last + 1))
                ).render()
                : value;
    }

    private static String getDisabledColor() {
        return Colors.getHexString(FlatUIUtils.getUIColor("Label.disabledForeground", FlatUIUtils.getUIColor("Label.disabledText", Color.GRAY)));
    }
}
