package internal.sdmxdl.desktop;

import j2html.tags.specialized.HtmlTag;

import static j2html.TagCreator.*;

@lombok.experimental.UtilityClass
public class Html4Swing {

    public static HtmlTag nameDescription(String name, String description) {
        return html(b(name), br(), text(description));
    }
}
