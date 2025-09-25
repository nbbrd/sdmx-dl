package sdmxdl;

import lombok.NonNull;

@lombok.Value
@lombok.Builder
public class KeyRequest {

    @NonNull
    @lombok.Builder.Default
    DatabaseRef database = DatabaseRef.NO_DATABASE;

    @NonNull
    FlowRef flow;

    @NonNull
    @lombok.Builder.Default
    Key key = Key.ALL;

    @NonNull
    @lombok.Builder.Default
    Detail detail = Detail.FULL;

    @NonNull
    @lombok.Builder.Default
    Languages languages = Languages.ANY;

    public @NonNull Query toQuery() {
        return Query.builder().key(getKey()).detail(getDetail()).build();
    }

    public static @NonNull Builder builderOf(@NonNull FlowRequest request) {
        return builder()
                .database(request.getDatabase())
                .flow(request.getFlow())
                .languages(request.getLanguages());
    }

    public static final class Builder {

        public Builder databaseOf(@NonNull String database) {
            return database(DatabaseRef.parse(database));
        }

        public Builder flowOf(@NonNull String flow) {
            return flow(FlowRef.parse(flow));
        }

        public Builder keyOf(@NonNull String key) {
            return key(Key.parse(key));
        }

        public Builder detailOf(@NonNull String detail) {
            return detail(Detail.valueOf(detail));
        }

        public Builder languagesOf(@NonNull String languages) {
            return languages(Languages.parse(languages));
        }
    }
}
