package sdmxdl;

import lombok.NonNull;
import nbbrd.design.RepresentableAs;

import java.net.URI;

@lombok.Value
@lombok.Builder
public class DatabaseRequest {

    @NonNull
    @lombok.Builder.Default
    DatabaseRef database = DatabaseRef.NO_DATABASE;

    @NonNull
    @lombok.Builder.Default
    Languages languages = Languages.ANY;

    public static @NonNull Builder builderOf(@NonNull SourceRequest request) {
        return builder()
                .languages(request.getLanguages());
    }

    public static final class Builder {

        public Builder databaseOf(@NonNull String database) {
            return database(DatabaseRef.parse(database));
        }

        public Builder languagesOf(@NonNull String languages) {
            return languages(Languages.parse(languages));
        }
    }
}
