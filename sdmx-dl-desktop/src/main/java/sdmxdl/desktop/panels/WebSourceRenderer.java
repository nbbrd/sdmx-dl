package sdmxdl.desktop.panels;

import internal.sdmxdl.desktop.util.AccentColors;
import internal.sdmxdl.desktop.util.BrowseCommand;
import internal.sdmxdl.desktop.util.ButtonBuilder;
import internal.sdmxdl.desktop.util.JDocument;
import j2html.tags.DomContent;
import sdmxdl.Confidentiality;
import sdmxdl.Languages;
import sdmxdl.desktop.MainComponent;
import sdmxdl.desktop.Sdmxdl;
import sdmxdl.web.WebSource;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Optional;

import static internal.sdmxdl.desktop.util.Html4Swing.labelTag;
import static j2html.TagCreator.*;
import static org.kordamp.ikonli.materialdesign.MaterialDesign.MDI_WEB;

public enum WebSourceRenderer implements Renderer<WebSource> {

    INSTANCE;

    @Override
    public String toHeaderText(WebSource value, Runnable onUpdate) {
        return value.getId();
    }

    @Override
    public String toHeaderTooltip(WebSource value, Runnable onUpdate) {
        return toText(value, onUpdate);
    }

    @Override
    public String toText(WebSource value, Runnable onUpdate) {
        return html(dom(value, Sdmxdl.INSTANCE.getLanguages())).render();
    }

    @Override
    public Icon toIcon(WebSource value, Runnable onUpdate) {
        return Sdmxdl.INSTANCE.getIconSupport().getIcon(value.getId(), 16, onUpdate);
    }

    @Override
    public JDocument<WebSource> toView(MainComponent main, WebSource value) {
        JDocument<WebSource> result = new JDocument<>();
        result.addComponent("Settings", new WebSourcePanel(), WebSourcePanel::setModel);
        result.addToolBarItem(new ButtonBuilder()
                .action(BrowseCommand.ofURL(this::getWebsite)
                        .toAction(result)
                        .withWeakPropertyChangeListener(result, JDocument.MODEL_PROPERTY))
                .ikon(MDI_WEB)
                .toolTipText("Open web site")
                .build());
        result.setModel(value);
        return result;
    }

    private URL getWebsite(JDocument<WebSource> o) {
        return Optional.ofNullable(o.getModel()).map(WebSource::getWebsite).orElse(null);
    }

    private static DomContent dom(WebSource value, Languages languages) {
        return join(
                labelTag(value.getId(), getColor(value.getConfidentiality())),
                text(" " + value.getName(languages))
        );
    }

    public static Color getColor(Confidentiality confidentiality) {
        switch (confidentiality) {
            case PUBLIC:
//                return UIManager.getColor(UIConstants.TREE_ICON_LEAF_COLOR);
                return AccentColors.LIGHT_GREEN;
            case UNRESTRICTED:
                return AccentColors.LIGHT_BLUE;
            case RESTRICTED:
                return AccentColors.LIGHT_YELLOW;
            case CONFIDENTIAL:
                return AccentColors.LIGHT_ORANGE;
            case SECRET:
                return AccentColors.LIGHT_RED;
            default:
                throw new RuntimeException();
        }
    }
}
