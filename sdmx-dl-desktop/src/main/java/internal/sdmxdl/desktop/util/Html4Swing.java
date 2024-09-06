package internal.sdmxdl.desktop.util;

import j2html.tags.specialized.HtmlTag;

import static j2html.TagCreator.*;

public final class Html4Swing {

    private Html4Swing() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static HtmlTag nameDescription(String name, String description) {
        return html(b(name), br(), text(description));
    }
}
