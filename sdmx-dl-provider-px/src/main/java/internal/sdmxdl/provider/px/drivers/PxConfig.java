package internal.sdmxdl.provider.px.drivers;

import com.google.gson.*;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.TextFormatter;
import nbbrd.io.text.TextParser;

import java.lang.reflect.Type;

@VisibleForTesting
@lombok.Value
public class PxConfig {

    int maxValues;
    int maxCells;
    int maxCalls;
    int timeWindow;

    public static final TextParser<PxConfig> JSON_PARSER = GsonIO.GsonParser
            .builder(PxConfig.class)
            .deserializer(PxConfig.class, PxConfig::deserialize)
            .build();

    @MightBeGenerated
    private static PxConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject x = json.getAsJsonObject();
        return new PxConfig(
                GsonUtil.getAsInt(x, "maxValues", 0),
                GsonUtil.getAsInt(x, "maxCells", 0),
                GsonUtil.getAsInt(x, "maxCalls", 0),
                GsonUtil.getAsInt(x, "timeWindow", 0)
        );
    }

    public static final TextFormatter<PxConfig> JSON_FORMATTER = GsonIO.GsonFormatter
            .builder(PxConfig.class)
            .serializer(PxConfig.class, PxConfig::serialize)
            .build();

    @MightBeGenerated
    private static JsonElement serialize(PxConfig src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.addProperty("maxValues", src.maxValues);
        result.addProperty("maxCells", src.maxCells);
        result.addProperty("maxCalls", src.maxCalls);
        result.addProperty("timeWindow", src.timeWindow);
        return result;
    }
}
