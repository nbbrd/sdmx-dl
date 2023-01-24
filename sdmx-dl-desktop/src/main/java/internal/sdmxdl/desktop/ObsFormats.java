package internal.sdmxdl.desktop;

import nbbrd.io.text.Formatter;
import sdmxdl.Attribute;
import sdmxdl.AttributeRelationship;
import sdmxdl.DataStructure;
import sdmxdl.Obs;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ObsFormats {

    public static Formatter<Obs> getHtmlTooltipFormatter(DataStructure dsd) {
        Map<String, Attribute> attributes = dsd.getAttributes().stream()
                .filter(attribute -> attribute.getRelationship().equals(AttributeRelationship.OBSERVATION))
                .collect(Collectors.toMap(Attribute::getId, Function.identity()));
        return obs -> getToolTipText(obs, attributes);
    }

    private static String getToolTipText(Obs obs, Map<String, Attribute> attributes) {
        return "<html>" +
                "<table>" +
                "<tr><th align=right>Period:</th><td>" + obs.getPeriod() + "</td></tr>" +
                "<tr><th align=right>Value:</th><td>" + obs.getValue() + "</td></tr>" +
                "<tr><th align=right>Meta:</th><td>" + metaToHtml(obs.getMeta(), attributes) + "</td></tr>" +
                "</table>";
    }

    private static String metaToHtml(Map<String, String> meta, Map<String, Attribute> attributes) {
        return "<table style=\"border-style: solid; border-width: 1px;\">" +
                meta.entrySet().stream()
                        .map(entry -> metaToHtml(entry, attributes))
                        .collect(Collectors.joining()) +
                "</table>";
    }

    private static String metaToHtml(Map.Entry<String, String> entry, Map<String, Attribute> attributes) {
        Attribute attribute = attributes.get(entry.getKey());
        return attribute != null
                ? "<tr><td align=right>" + attribute.getLabel() + ":</td><td>" + (attribute.isCoded() ? attribute.getCodelist().getCodes().get(entry.getValue()) : entry.getValue()) + "</td></tr>"
                : "<tr><td align=right>" + entry.getKey() + ":</td><td>" + entry.getValue() + "</td></tr>";
    }
}
