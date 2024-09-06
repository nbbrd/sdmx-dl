package internal.sdmxdl.desktop;

import com.formdev.flatlaf.ui.FlatUIUtils;
import internal.sdmxdl.desktop.util.Colors;
import j2html.attributes.Attr;

import java.awt.*;

import static j2html.TagCreator.*;

public class PropertyFormats {

    public static String toText(String property) {
        int last = property.lastIndexOf(".");
        return last != -1
                ? html
                (
                        span(property.substring(0, last + 1)).attr(Attr.COLOR, getDisabledColor()),
                        text(property.substring(last + 1))
                ).render()
                : property;
    }

    private static String getDisabledColor() {
        return Colors.getHexString(FlatUIUtils.getUIColor("Label.disabledForeground", FlatUIUtils.getUIColor("Label.disabledText", Color.GRAY)));
    }
}
