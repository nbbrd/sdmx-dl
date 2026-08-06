package internal.sdmxdl.provider.px.drivers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Parser;
import sdmxdl.Codelist;
import sdmxdl.CodelistRef;
import sdmxdl.Dimension;
import sdmxdl.format.time.ObservationalTimePeriod;
import sdmxdl.format.time.TimeFormats;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import static sdmxdl.format.time.TimeFormats.IGNORE_ERROR;

@VisibleForTesting
@lombok.Value
public class PxTableVariable {

    String code;
    String text;
    List<String> values;
    List<String> valueTexts;
    boolean time;

    public Dimension toDimension() {
        return Dimension
                .builder()
                .id(code)
                .name(text)
                .codelist(Codelist
                        .builder()
                        .ref(CodelistRef.parse(code))
                        .codes(CollectionUtil.zip(values, valueTexts))
                        .build())
                .build();
    }

    boolean hasTimeValues() {
        return getValueTexts().stream().map(TIME_PERIOD_PARSER::parse).allMatch(Objects::nonNull);
    }

    public static final Parser<ObservationalTimePeriod> TIME_PERIOD_PARSER = TimeFormats
            .getObservationalTimePeriod(IGNORE_ERROR)
            .orElse(TimeFormats.onParser(YearRange::isParsable, YearRange::parse, IGNORE_ERROR));

    @MightBeGenerated
    static PxTableVariable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject x = json.getAsJsonObject();
        return new PxTableVariable(
                GsonUtil.getAsString(x, "code"),
                GsonUtil.getAsString(x, "text"),
                GsonUtil.getAsStringList(x, "values"),
                GsonUtil.getAsStringList(x, "valueTexts"),
                x.has("time") && x.get("time").getAsBoolean()
        );
    }
}
