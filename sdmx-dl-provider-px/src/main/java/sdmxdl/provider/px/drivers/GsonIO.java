package sdmxdl.provider.px.drivers;

import com.google.gson.*;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.io.text.TextFormatter;
import nbbrd.io.text.TextParser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

@MightBePromoted
@lombok.experimental.UtilityClass
class GsonIO {

    @lombok.Builder
    public static final class GsonParser<T> implements TextParser<T> {

        public static <X> @NonNull Builder<X> builder(@NonNull Class<X> type) {
            return new Builder<X>().type(type);
        }

        @lombok.NonNull
        private final Class<T> type;

        @lombok.Singular
        private final Map<Class<?>, Object> typeAdapters;

        @lombok.Getter(lazy = true)
        private final Gson gson = initGson();

        private Gson initGson() {
            GsonBuilder result = new GsonBuilder();
            typeAdapters.forEach(result::registerTypeAdapter);
            return result.create();
        }

        @Override
        public @NonNull T parseReader(@NonNull Reader resource) throws IOException {
            try {
                return getGson().fromJson(resource, type);
            } catch (JsonIOException ex) {
                throw unwrapIOException(ex);
            } catch (JsonSyntaxException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public @NonNull T parseStream(@NonNull InputStream resource, @NonNull Charset encoding) throws IOException {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, encoding))) {
                return parseReader(reader);
            }
        }

        public static final class Builder<T> {

            public <X> @NonNull Builder<T> deserializer(@NonNull Class<X> type, @NonNull JsonDeserializer<X> deserializer) {
                return typeAdapter(type, deserializer);
            }
        }
    }

    @lombok.Builder
    public static final class GsonFormatter<T> implements TextFormatter<T> {

        public static <X> @NonNull Builder<X> builder(@NonNull Class<X> type) {
            return new Builder<X>().type(type);
        }

        @NonNull
        private final Class<T> type;

        @lombok.Singular
        private final Map<Class<?>, Object> typeAdapters;

        @lombok.Getter(lazy = true)
        private final Gson gson = initGson();

        private Gson initGson() {
            GsonBuilder result = new GsonBuilder();
            typeAdapters.forEach(result::registerTypeAdapter);
            return result.create();
        }

        @Override
        public void formatWriter(@NonNull T value, @NonNull Writer resource) throws IOException {
            try {
                getGson().toJson(value, resource);
            } catch (JsonIOException ex) {
                throw unwrapIOException(ex);
            } catch (JsonSyntaxException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public void formatStream(@NonNull T value, @NonNull OutputStream resource, @NonNull Charset encoding) throws IOException {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(resource, encoding))) {
                formatWriter(value, writer);
            }
        }

        public static final class Builder<T> {

            public <X> @NonNull Builder<T> serializer(@NonNull Class<X> type, @NonNull JsonSerializer<X> serializer) {
                return typeAdapter(type, serializer);
            }
        }
    }

    private static IOException unwrapIOException(JsonIOException ex) {
        Throwable cause = ex.getCause();
        return ((cause instanceof IOException) ? (IOException) cause : new IOException(cause));
    }
}
