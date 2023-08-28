package sdmxdl.provider.px.drivers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nbbrd.design.MightBePromoted;

import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@MightBePromoted
@lombok.experimental.UtilityClass
class GsonUtil {

    public static String getAsString(JsonObject obj, String memberName) {
        if (obj.has(memberName)) return obj.get(memberName).getAsString();
        throw new NoSuchElementException(memberName);
    }

    public static Stream<JsonElement> asStream(JsonArray array) {
        return StreamSupport.stream(array.spliterator(), false);
    }
}
