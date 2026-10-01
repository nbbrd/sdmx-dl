package sdmxdl;

import lombok.NonNull;

@lombok.Value
@lombok.Builder
public class MetaRequest implements Request {

    @NonNull @lombok.Builder.Default
    DatabaseRef database = DatabaseRef.NO_DATABASE;

    @NonNull FlowRef flow;

    @NonNull @lombok.Builder.Default
    Languages languages = Languages.ANY;

    @Override
    public <T> T accept(@NonNull RequestVisitor<T> visitor) {
        return visitor.visitMeta(this);
    }

    public static final class Builder {

        public Builder databaseOf(@NonNull String database) {
            return database(DatabaseRef.parse(database));
        }

        public Builder flowOf(@NonNull String flow) {
            return flow(FlowRef.parse(flow));
        }

        public Builder languagesOf(@NonNull String languages) {
            return languages(Languages.parse(languages));
        }
    }
}
