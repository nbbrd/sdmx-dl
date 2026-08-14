package internal.sdmxdl.provider.px.drivers;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nbbrd.design.MightBePromoted;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@MightBePromoted
@lombok.experimental.UtilityClass
public class GsonUtil {

    public static String getAsString(JsonObject obj, String memberName) {
        if (obj.has(memberName)) return obj.get(memberName).getAsString();
        throw new NoSuchElementException(memberName);
    }

    public static int getAsInt(JsonObject obj, String memberName, int defaultValue) {
        return obj.has(memberName) && !obj.get(memberName).isJsonNull()
                ? obj.get(memberName).getAsInt()
                : defaultValue;
    }

    public static String[] getAsStringArray(JsonObject obj, String memberName) {
        if (!obj.has(memberName)) return null;
        return asStream(obj.getAsJsonArray(memberName))
                .map(JsonElement::getAsString)
                .toArray(String[]::new);
    }

    public static List<String> getAsStringList(JsonObject obj, String memberName) {
        if (!obj.has(memberName) || obj.get(memberName).isJsonNull()) return Collections.emptyList();
        return asStream(obj.getAsJsonArray(memberName))
                .map(JsonElement::getAsString)
                .collect(toList());
    }

    public static <V> Map<String, V> getAsMap(JsonObject obj, String memberName, JsonDeserializationContext context, Class<V> valueType) {
        if (obj.has(memberName)) return obj.get(memberName)
                .getAsJsonObject().entrySet().stream()
                .collect(toMap(
                        e -> e.getKey(),
                        e -> context.deserialize(e.getValue(), valueType),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
        throw new NoSuchElementException(memberName);
    }


    public static Stream<JsonElement> asStream(JsonArray array) {
        return StreamSupport.stream(array.spliterator(), false);
    }
}
