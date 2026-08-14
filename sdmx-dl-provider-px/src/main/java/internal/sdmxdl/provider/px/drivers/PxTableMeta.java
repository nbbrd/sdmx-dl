package internal.sdmxdl.provider.px.drivers;

import com.google.gson.*;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.TextParser;
import sdmxdl.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@VisibleForTesting
@lombok.Value
public class PxTableMeta {

    String title;
    List<PxTableVariable> variables;

    public Structure toStructure(StructureRef ref) throws IOException {
        PxTableVariable timeVariable = getTimeVariable();
        return Structure
                .builder()
                .ref(ref)
                .timeDimensionId(timeVariable.getCode())
                .primaryMeasureId(DEFAULT_PRIMARY_MEASURE)
                .name(title)
                .dimensions(toDimensionList(timeVariable))
                .attribute(UNIT_MEASURE_ATTRIBUTE)
                .build();
    }

    public PxTableVariable getTimeVariable() throws IOException {
        {
            PxTableVariable main = variables.stream().filter(PxTableVariable::isTime).findFirst().orElse(null);
            if (main != null) return main;
        }
        {
            PxTableVariable fallback = variables.stream().filter(PxTableVariable::hasTimeValues).findFirst().orElse(null);
            if (fallback != null) return fallback;
        }
        throw new IOException("Time variable not found");
    }

    List<Dimension> toDimensionList(PxTableVariable timeVariable) {
        return variables.stream()
                .filter(item -> !timeVariable.equals(item))
                .map(item -> item.toDimension())
                .collect(Collectors.toList());
    }

    static final String DEFAULT_PRIMARY_MEASURE = "OBS_VALUE";

    static final Attribute UNIT_MEASURE_ATTRIBUTE = Attribute
            .builder()
            .id("UNIT_MEASURE")
            .name("Unit measure")
            .relationship(AttributeRelationship.SERIES)
            .build();

    public static final TextParser<PxTableMeta> JSON_PARSER = GsonIO.GsonParser
            .builder(PxTableMeta.class)
            .deserializer(PxTableMeta.class, PxTableMeta::deserialize)
            .deserializer(PxTableVariable.class, PxTableVariable::deserialize)
            .build();

    @MightBeGenerated
    private static PxTableMeta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject x = json.getAsJsonObject();
        JsonArray y = x.getAsJsonArray("variables");
        return new PxTableMeta(
                x.get("title").getAsString(),
                GsonUtil.asStream(y).map(o -> context.<PxTableVariable>deserialize(o, PxTableVariable.class)).collect(toList())
        );
    }
}
