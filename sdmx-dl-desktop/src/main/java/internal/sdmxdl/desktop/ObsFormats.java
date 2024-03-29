package internal.sdmxdl.desktop;

import j2html.tags.DomContent;
import nbbrd.io.text.Formatter;
import sdmxdl.Attribute;
import sdmxdl.AttributeRelationship;
import sdmxdl.Obs;
import sdmxdl.Structure;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

public final class ObsFormats {

    public static Formatter<Obs> getChartTooltipFormatter(Structure dsd) {
        Map<String, Attribute> attributes = dsd.getAttributes().stream()
                .filter(attribute -> attribute.getRelationship().equals(AttributeRelationship.OBSERVATION))
                .collect(Collectors.toMap(Attribute::getId, Function.identity()));
        return obs -> getChartToolTipText(obs, attributes);
    }

    private static String getChartToolTipText(Obs obs, Map<String, Attribute> attributes) {
        return obs.getPeriod().toShortString() + ": " + obs.getValue();
    }

    public static Formatter<Obs> getHtmlTooltipFormatter(Structure dsd) {
        Map<String, Attribute> attributes = dsd.getAttributes().stream()
                .filter(attribute -> attribute.getRelationship().equals(AttributeRelationship.OBSERVATION))
                .collect(Collectors.toMap(Attribute::getId, Function.identity()));
        return obs -> getToolTipText(obs, attributes);
    }

    private static String getToolTipText(Obs obs, Map<String, Attribute> attributes) {
        return html(
                table(
                        tr(th("Period:").withStyle("text-align:right"), td(text(obs.getPeriod().toShortString()))),
                        tr(th("Value:").withStyle("text-align:right"), td(text(String.valueOf(obs.getValue())))),
                        tr(th("Meta:").withStyle("text-align:right"), td(metaToHtml(obs.getMeta(), attributes)))
                )
        ).render();
    }

    private static DomContent metaToHtml(Map<String, String> meta, Map<String, Attribute> attributes) {
        return table(each(meta.entrySet(), i -> metaToHtml(i, attributes))).withStyle("border-style: solid; border-width: 1px;");
    }

    private static DomContent metaToHtml(Map.Entry<String, String> entry, Map<String, Attribute> attributes) {
        Attribute attribute = attributes.get(entry.getKey());
        return attribute != null
                ? tr(
                td(attribute.getName()).withStyle("text-align=right"),
                td(attribute.isCoded() ? attribute.getCodelist().getCodes().get(entry.getValue()) : entry.getValue())
        )
                : tr(td(entry.getKey()).withStyle("text-align=right"), td(entry.getValue()));
    }
}
