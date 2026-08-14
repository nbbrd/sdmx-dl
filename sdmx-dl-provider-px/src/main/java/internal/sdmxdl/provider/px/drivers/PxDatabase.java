package internal.sdmxdl.provider.px.drivers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.TextParser;
import sdmxdl.DatabaseRef;

import java.lang.reflect.Type;

@VisibleForTesting
@lombok.Value
public class PxDatabase {

    String dbId;
    String text;

    public sdmxdl.Database toDatabase() {
        return new sdmxdl.Database(DatabaseRef.parse(dbId), text);
    }

    public static final TextParser<PxDatabase[]> JSON_PARSER = GsonIO.GsonParser
            .builder(PxDatabase[].class)
            .deserializer(PxDatabase.class, PxDatabase::deserialize)
            .build();

    @MightBeGenerated
    private static PxDatabase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        return new PxDatabase(
                GsonUtil.getAsString(obj, "dbid"),
                GsonUtil.getAsString(obj, "text")
        );
    }
}
